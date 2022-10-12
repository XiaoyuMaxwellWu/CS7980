package com.eventualconsistency.demo.utils;

import com.eventualconsistency.demo.constants.Constant;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * @description: some desc
 * @author: Xiaoyu Wu
 * @email: wu.xiaoyu@northeastern.edu
 * @date: 10/9/22 4:36 PM
 */
@Slf4j
public class MultiThread {

  private ThreadPoolExecutor poolExecutor;
  private static MultiThread instance;

    public static MultiThread getInstance(int numOfThreads)
  {
    if (instance == null) {
      synchronized (MultiThread.class) {
        if (instance == null)
          instance = new MultiThread(numOfThreads);
      }
    }
    return instance;
  }
 

  public ThreadPoolExecutor getPoolExecutor() {
    return poolExecutor;
  }

  private MultiThread(int k) {
    poolExecutor = new ThreadPoolExecutor(k, k, 0,
        TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>(k * 2),
        new ThreadPoolExecutor.CallerRunsPolicy());
    ShutdownHookRegister();
  }

  public void ShutdownHookRegister() {
    Thread t = new Thread() {
      public void run() {
        shutdown();
      }
    };
    Runtime.getRuntime().addShutdownHook(t);
  }

  private void shutdown() {
    synchronized (log) {
      poolExecutor.shutdown();
      int activeCount = poolExecutor.getActiveCount();
      log.info("-------shutdown---start-------activeCount:" + activeCount);
      while (activeCount != 0) {
        log.info("-------shutdown---ing-------activeCount:" + activeCount + "---" + new Date());
        try {
          Thread.sleep(1000);
          activeCount = poolExecutor.getActiveCount();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      if (activeCount == 0) {
        log.info("-------ALL TASK is OVER----------");
      }
    }
  }
}