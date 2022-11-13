package com.eventualconsistency.demo.controller;

import com.eventualconsistency.demo.constants.Constant;
import com.eventualconsistency.demo.constants.Constant.Method;
import com.eventualconsistency.demo.entity.MysqlTab;
import com.eventualconsistency.demo.utils.MultiThread;
import com.eventualconsistency.demo.vo.ResponseEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: some desc
 * @author: Xiaoyu Wu
 * @email: wu.xiaoyu@northeastern.edu
 * @date: 11/9/22 3:04 PM
 */

@RestController
@RequestMapping("/capstone")
@Slf4j

public class PerformanceController {

  @Autowired
  private MysqlRedisController mysqlRedisController;

  private static MultiThread[] instances = new MultiThread[Constant.num_threads.length];

  //50, 100, 1000, 10,000
  public PerformanceController() {
    for (int i = 0; i < Constant.num_threads.length; i++) {
      instances[i] = MultiThread.getInstance(Constant.num_threads[i]);
    }
  }

  //  ZRANK(1, zRankController),
//  MESSAGE_QUEUE(2, messageQueueController),
//  DISTRIBUTED_LOCK(3, distributedLockController),
//  BASELINE(4, mysqlRedisController);
  @GetMapping("/responseTime/{method}")
  public void ResponseTime(@PathVariable int method) throws Exception {
    Controller controller = Arrays.stream(Method.values())
        .filter(m -> m.getMethodCode() == method).findFirst().orElse(null).getController();
    int whichExecutor = 3;
    Random random = new Random();
    ThreadPoolExecutor poolExecutor = instances[whichExecutor].getPoolExecutor();
    HashMap<String, Object> requestInfo = new HashMap<>();
    requestInfo.put("csKey", "K1");
    controller.findByKey(requestInfo);
    ResponseEntry exactEntry = mysqlRedisController.findByKey(requestInfo);
    ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    ReadLock readLock = reentrantReadWriteLock.readLock();
    WriteLock writeLock = reentrantReadWriteLock.writeLock();
    List<Future<Boolean[]>> results = new ArrayList<>();
    new Thread(() -> {
      try {
        Thread.sleep(100);
        String uuid = UUID.randomUUID().toString();
        MysqlTab mysqlTab = new MysqlTab("K1", uuid);
        mysqlRedisController.saveInMysql(mysqlTab);
        writeLock.lock();
        exactEntry.setCsValue(uuid);
        writeLock.unlock();
        mysqlRedisController.deleteInRedis(mysqlTab.getCsKey());
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }).start();
    for (int i = 0; i < Constant.num_threads[whichExecutor]; i++) {
      Future<Boolean[]> submit = poolExecutor.submit(() -> {
        Thread.sleep(random.nextInt(1000));
        Boolean[] res = new Boolean[2];
        ResponseEntry entry = controller.findByKey(requestInfo);
        return res;
      });
      results.add(submit);
    }
  }


}
