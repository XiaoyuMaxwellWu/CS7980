package com.eventualconsistency.demo.controller;

import com.eventualconsistency.demo.constants.Constant;
import com.eventualconsistency.demo.constants.Constant.Method;
import com.eventualconsistency.demo.entity.MysqlTab;
import com.eventualconsistency.demo.utils.MultiThread;
import com.eventualconsistency.demo.vo.ResponseEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
  @GetMapping("/responseTime/{method}")
  public void ResponseTime(@PathVariable int method) throws Exception {
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
      results.add(futures.get(i).get());
    }
    Collections.sort(results);
    results.get((int) Math.ceil(99 / 100.0 * results.size()));

  }

  private long calculatePercentile() {
    return 0;
  }

}
