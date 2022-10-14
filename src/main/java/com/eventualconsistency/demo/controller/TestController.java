package com.eventualconsistency.demo.controller;

import com.eventualconsistency.demo.constants.Constant;
import com.eventualconsistency.demo.entity.MysqlTab;
import com.eventualconsistency.demo.utils.MultiThread;
import com.eventualconsistency.demo.vo.ResponseEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: some desc
 * @author: Xiaoyu Wu
 * @email: wu.xiaoyu@northeastern.edu
 * @date: 10/10/22 8:48 PM
 */
@RestController
@RequestMapping("/capstone")
@Slf4j
public class TestController {

  @Autowired
  public MysqlRedisController mysqlRedisController;

  private static MultiThread[] instances = new MultiThread[Constant.num_threads.length];

  //50, 100, 1000
  public TestController() {
    for (int i = 0; i < Constant.num_threads.length; i++) {
      instances[i] = MultiThread.getInstance(Constant.num_threads[i]);
    }
  }

  int a = 3;

  @GetMapping("/lock")
  public void test() throws InterruptedException {
    ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    ReadLock readLock = reentrantReadWriteLock.readLock();
    WriteLock writeLock = reentrantReadWriteLock.writeLock();
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          writeLock.lock();
          a = 5;
          Thread.sleep(20);
          writeLock.unlock();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }).start();
    readLock.lock();
    System.out.println("a is: " + a);
    readLock.unlock();
  }

  @GetMapping("/invalidation")
  public void hotSpotInvalidation() throws InterruptedException, ExecutionException {

    ArrayList<int[]> res = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      res.add(testInvalid());
    }
    for (int i = 0; i < res.size(); i++) {
      int[] store = res.get(i);
      log.info(i+"th "+"time:");
      System.out.println(i+"th "+"time:");
      System.out.println("Num of threads read from Redis: " + store[0]);
      System.out.println("Num of threads read from Mysql: " + store[1]);
      System.out.println(
          "inconsistent read/total read:" + (store[3] - store[2])
              + " / "
              + store[3]);
    }
    
    
  }


  private int[] testInvalid() throws InterruptedException, ExecutionException {
    int whichExecutor = 0;
    ThreadPoolExecutor poolExecutor = instances[whichExecutor].getPoolExecutor();
    HashMap<String, Object> requestInfo = new HashMap<>();
    requestInfo.put("csKey", "K1");
    ResponseEntry exactEntry = mysqlRedisController.findByKey(requestInfo);
    ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    ReadLock readLock = reentrantReadWriteLock.readLock();
    WriteLock writeLock = reentrantReadWriteLock.writeLock();
    List<Future<Boolean[]>> results = new ArrayList<>();
    // execute reading from redis or mysql
    for (int i = 0; i < Constant.num_threads[whichExecutor]; i++) {
      Future<Boolean[]> submit = poolExecutor.submit(() -> {
        Thread.sleep(new Random().nextInt(500));
        Boolean[] res = new Boolean[2];
        ResponseEntry entry = mysqlRedisController.findByKey(requestInfo);
        res[0] = entry.getIsReadFromRedis();
        readLock.lock();
        res[1] = entry.getCsValue().equals(exactEntry.getCsValue()) ? true : false;
        readLock.unlock();
        return res;
      });
      results.add(submit);
    }
    // update mysql, delete in redis
    new Thread(() -> {
      String uuid = UUID.randomUUID().toString();
      MysqlTab mysqlTab = new MysqlTab("K1", uuid);
//      mysqlRedisController.updateMysql(mysqlTab);
      mysqlRedisController.saveInMysql(mysqlTab);
      writeLock.lock();
      exactEntry.setCsValue(uuid);
      writeLock.unlock();
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      mysqlRedisController.deleteInRedis(mysqlTab.getCsKey());
    }).start();

    int mysqlCnt = 0, redisCnt = 0, consistentCnt = 0;
    for (Future<Boolean[]> result : results) {
      Boolean[] res = result.get();
      mysqlCnt += res[0] ? 0 : 1;
      redisCnt += res[0] ? 1 : 0;
      consistentCnt += res[1] ? 1 : 0;
    }
    log.info("Num of threads read from Redis: " + redisCnt);
    log.info("Num of threads read from Mysql: " + mysqlCnt);
    log.info(
        "inconsistent read/total read:" + (Constant.num_threads[whichExecutor] - consistentCnt)
            + " / "
            + Constant.num_threads[whichExecutor]);
    return new int[]{redisCnt, mysqlCnt, (Constant.num_threads[whichExecutor] - consistentCnt),
        Constant.num_threads[whichExecutor]};

  }

  @GetMapping("/inconsistency")
  public void getInconsistency() throws InterruptedException, ExecutionException {
    int whichExecutor = 0;
    ThreadPoolExecutor poolExecutor = instances[whichExecutor].getPoolExecutor();
    HashMap<String, Object> requestInfo = new HashMap<>();
    requestInfo.put("csKey", "K1");
    ResponseEntry exactEntry = mysqlRedisController.findByKey(requestInfo);
    ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    ReadLock readLock = reentrantReadWriteLock.readLock();
    WriteLock writeLock = reentrantReadWriteLock.writeLock();
    List<Future<Boolean[]>> results = new ArrayList<>();
    // execute reading from redis or mysql
    mysqlRedisController.clearAll();
    String uuid2 = UUID.randomUUID().toString();
    MysqlTab mysqlTab2 = new MysqlTab("K1", uuid2);
    mysqlRedisController.addMysql(mysqlTab2);
    Future<Boolean[]> submit = poolExecutor.submit(() -> {
      //Thread.sleep(new Random().nextInt(500));
      Boolean[] res = new Boolean[2];

      ResponseEntry entry = mysqlRedisController.findByKeyLongerTime(requestInfo);
      res[0] = entry.getIsReadFromRedis();
      readLock.lock();
      res[1] = entry.getCsValue().equals(exactEntry.getCsValue()) ? true : false;
      readLock.unlock();
      return res;
    });
    results.add(submit);

    // update mysql, delete in redis
    new Thread(() -> {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      String uuid = UUID.randomUUID().toString();
      MysqlTab mysqlTab = new MysqlTab("K1", uuid);
//      mysqlRedisController.updateMysql(mysqlTab);
      mysqlRedisController.saveInMysql(mysqlTab);
      writeLock.lock();
      exactEntry.setCsValue(uuid);
      writeLock.unlock();
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      //mysqlRedisController.deleteInRedis(mysqlTab.getCsKey());
    }).start();

    int mysqlCnt = 0, redisCnt = 0, consistentCnt = 0;
    for (Future<Boolean[]> result : results) {
      Boolean[] res = result.get();
      mysqlCnt += res[0] ? 0 : 1;
      redisCnt += res[0] ? 1 : 0;
      consistentCnt += res[1] ? 1 : 0;
    }

  }
}
