package com.eventualconsistency.demo.controller;

import com.eventualconsistency.demo.constants.Constant;
import com.eventualconsistency.demo.constants.Constant.Method;
import com.eventualconsistency.demo.dao.MysqlRepository;
import com.eventualconsistency.demo.entity.MysqlTab;
import com.eventualconsistency.demo.utils.Chart;
import com.eventualconsistency.demo.utils.MultiThread;
import com.eventualconsistency.demo.vo.ResponseEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
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

  @Autowired
  private MysqlRepository mysqlRepository;

  @Autowired
  private HashOperations hashOperations;

  private static MultiThread[] instances = new MultiThread[Constant.NUM_THREADS.length];

  //50, 100, 1000, 10,000
  public PerformanceController() {
    for (int i = 0; i < Constant.NUM_THREADS.length; i++) {
      instances[i] = MultiThread.getInstance(Constant.NUM_THREADS[i]);
    }
  }
  //  ZRANK(1, zRankController),
//  MESSAGE_QUEUE(2, messageQueueController),
//  DISTRIBUTED_LOCK(3, distributedLockController),
//  BASELINE(4, mysqlRedisController);

  @GetMapping("/responseTime")
  public void testMultiResponseTime() throws Exception {
    Map<Double, Double>[] dataSet = new Map[]{trt(1),trt(2),trt(3), trt(4)};
    String[] types = new String[]{"Zset Ranking","MessageQueue","Distributed Lock", "Baseline"};
    Chart.drawLineChart("Response Time", "Response Time", "xth percentile",
        "Response Time in milliseconds", dataSet, types);
    Scanner in = new Scanner(System.in);
    in.hasNext();
  }

  // get response time for each method
  private HashMap<Integer, Long> trt(int method) throws Exception {
    Controller controller = Arrays.stream(Method.values())
        .filter(m -> m.getMethodCode() == method).findFirst().orElse(null).getController();
    int whichExecutor = 2;
    Random random = new Random();
    ThreadPoolExecutor poolExecutor = instances[whichExecutor].getPoolExecutor();
    HashMap<String, Object> requestInfo = new HashMap<>();
    requestInfo.put("csKey", "K1");
    mysqlRedisController.clearAll();
    mysqlRedisController.addMysql(new MysqlTab("K1", UUID.randomUUID().toString()));
    ResponseEntry exactEntry = controller.findByKey(requestInfo);
    new Thread(() -> {
      try {
        Thread.sleep(100);
        String uuid = UUID.randomUUID().toString();
        MysqlTab mysqlTab = new MysqlTab("K1", uuid);
        controller.updateMySQL(mysqlTab);
        exactEntry.setCsValue(uuid);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }).start();
    ArrayList<Future<Long>> futures = new ArrayList<>();
    for (int i = 0; i < Constant.NUM_THREADS[whichExecutor]; i++) {
      Future<Long> submit = poolExecutor.submit(() -> {
        Thread.sleep(random.nextInt(1000));
        long startTIme = System.currentTimeMillis();
        controller.findByKey(requestInfo);
        long end = System.currentTimeMillis();
        return (end - startTIme);
      });
      futures.add(submit);
    }
    ArrayList<Long> results = new ArrayList<>();
    for (int i = 0; i < futures.size(); i++) {
      results.add( futures.get(i).get());
    }
    Collections.sort(results);
    HashMap<Integer, Long> map = new HashMap<>();
    // get percentile
    for (int i = 1; i <= 99; i++) {
      map.put(i, results.get((int) Math.ceil(i / 100.0 * results.size()) - 1));
    }
    return map;
  }

  // get cache breakdown ratio

  @GetMapping("/breakdownRatio")
  public void breakdownRatio() throws Exception {
    Map<Double, Double>[] dataSet = new Map[]{breakdownRatioOneMethod(2)};
    String[] types = new String[]{"dreffe","ds","Distributed Lock", "Baseline"};
    Chart.drawLineChart("breakdownRatio", "Response Time", "xth percentile",
        "Response Time in milliseconds", dataSet, types);
    Scanner in = new Scanner(System.in);
    in.hasNext();
  }

  private HashMap<Double, Double> breakdownRatioOneMethod(int method) throws Exception {

    HashMap<Double, Double> map = new HashMap<>();
    map.put((double) method, (double) oneTime(method, 2));
    map.put((double) method, (double) oneTime(method, 3));
    map.put((double) method, (double) oneTime(method, 4));
    map.put((double) method, (double) oneTime(method, 5));
    map.put((double) method, (double) oneTime(method, 6));
    return map;
  }

  private Long oneTime(int method, int whichExecutor) throws Exception {
    Controller controller = Arrays.stream(Method.values())
        .filter(m -> m.getMethodCode() == method).findFirst().orElse(null).getController();

    ThreadPoolExecutor poolExecutor = instances[whichExecutor].getPoolExecutor();
    HashMap<String, Object> requestInfo = new HashMap<>();
    mysqlRedisController.clearAll();
    requestInfo.put("csKey", "K1");
    ResponseEntry exactEntry = controller.findByKey(requestInfo);
    MessageQueueController.mysqlCnt = 0;
    MessageQueueController.redisCnt = 0;
    new Thread(() -> {
      try {
        Thread.sleep(100);
        String uuid = UUID.randomUUID().toString();
        MysqlTab mysqlTab = new MysqlTab("K1", uuid);
        controller.updateMySQL(mysqlTab);
        exactEntry.setCsValue(uuid);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }).start();
    ArrayList<Future<Boolean[]>> results = new ArrayList<>();
    for (int i = 0; i < Constant.NUM_THREADS[whichExecutor]; i++) {
      Future<Boolean[]> submit = poolExecutor.submit(() -> {
        //Thread.sleep(new Random().nextInt(500));
        Boolean[] res = new Boolean[2];
        ResponseEntry entry = controller.findByKey(requestInfo);
        res[0] = entry.getIsReadFromRedis();
        res[1] = entry.getCsValue().equals(exactEntry.getCsValue()) ? true : false;
        return res;
      });
      results.add(submit);
    }
    int mysqlCnt = 0, redisCnt = 0;
    for (Future<Boolean[]> result : results) {
      Boolean[] res = result.get();
      mysqlCnt += res[0] ? 0 : 1;
      redisCnt += res[0] ? 1 : 0;
    }
    if (method == Method.MESSAGE_QUEUE.getMethodCode()) {
      Thread.sleep(1000);
      mysqlCnt = MessageQueueController.mysqlCnt;
      redisCnt = MessageQueueController.redisCnt;
    }
    long l = (long) (100 * (double) mysqlCnt / (mysqlCnt + redisCnt));
    return (long) (double) mysqlCnt / (mysqlCnt + redisCnt) * 100;
  }
  // get cache breakdown ratio
  @GetMapping("/cacheBreakdownRate/{method}")
  public void cacheBreakdownRate(@PathVariable int method) throws Exception{
    Controller controller = Arrays.stream(Method.values())
        .filter(m -> m.getMethodCode() == method).findFirst().orElse(null).getController();
    int whichExecutor = 2;
    Random random = new Random();
    ThreadPoolExecutor poolExecutor = instances[whichExecutor].getPoolExecutor();
    HashMap<String, Object> requestInfo = new HashMap<>();
    requestInfo.put("csKey", "K1");
    ResponseEntry exactEntry = controller.findByKey(requestInfo);

    ArrayList<Future<Boolean[]>> futures = new ArrayList<>();
    new Thread(() -> {
      try {
        Thread.sleep(200);
        String uuid = UUID.randomUUID().toString();
        MysqlTab mysqlTab = new MysqlTab("K1", uuid);
        controller.updateMySQL(mysqlTab);
        exactEntry.setCsValue(uuid);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }).start();
    for (int i = 0; i < Constant.NUM_THREADS[whichExecutor]; i++) {
      Future<Boolean[]> submit = poolExecutor.submit(() -> {
        Thread.sleep(new Random().nextInt(200));
        Boolean[] res = new Boolean[2];
        ResponseEntry entry = controller.findByKey(requestInfo);
        res[0] = entry.getIsReadFromRedis();
        res[1] = entry.getCsValue().equals(exactEntry.getCsValue()) ? true : false;
        return res;
      });
      futures.add(submit);
    }

    double mysqlCnt = 0, redisCnt = 0;
    double rate = 0;
    for (Future<Boolean[]> future : futures) {
      Boolean[] res = future.get();
      mysqlCnt += res[0] ? 0 : 1;
      redisCnt += res[0] ? 1 : 0;
      rate = mysqlCnt / (mysqlCnt + redisCnt) * 100;
    }
    log.info("num of requests to mysql: " + mysqlCnt);
    log.info("num of requests to redis: " + redisCnt);
    log.info("Cache breakdown rate: " + rate);
  }


  @GetMapping("/reachConsistency/{method}")
  public void ReachConsistency(@PathVariable int method) throws Exception {
    String enter = String.valueOf(hashOperations.get(Constant.KEY, "K1"));
    String exit = String.valueOf(mysqlRepository.findByCsKey("K1"));
    long startTIme = System.currentTimeMillis();

    for (long i = 0; i < 1001; i++) {
      while (true) {
        Controller controller = Arrays.stream(Method.values())
            .filter(m -> m.getMethodCode() == method).findFirst().orElse(null).getController();
        int whichExecutor = 1;
        Random random = new Random();
        ThreadPoolExecutor poolExecutor = instances[whichExecutor].getPoolExecutor();
        HashMap<String, Object> requestInfo = new HashMap<>();
        requestInfo.put("csKey", "K1");
        ResponseEntry exactEntry = controller.findByKey(requestInfo);
        new Thread(() -> {
          try {
            Thread.sleep(100);
            String uuid = UUID.randomUUID().toString();
            MysqlTab mysqlTab = new MysqlTab("K1", uuid);
            controller.updateMySQL(mysqlTab);
            exactEntry.setCsValue(uuid);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }).start();
        ArrayList<Future<Long>> futures = new ArrayList<>();

        if (enter.equals(exit)) {
          break;
        }
      }
    }
    long end = System.currentTimeMillis();
    long time = (end - startTIme) / 1000;
    log.info("Average time is" + time);

  }

}
