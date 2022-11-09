package com.eventualconsistency.demo.controller;

import com.eventualconsistency.demo.constants.Constant;
import com.eventualconsistency.demo.constants.Constant.Method;
import com.eventualconsistency.demo.utils.MultiThread;
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
  private DistributedLockController distributedLockController;
  private static MultiThread[] instances = new MultiThread[Constant.num_threads.length];

  //50, 100, 1000
  public PerformanceController() {
    for (int i = 0; i < Constant.num_threads.length; i++) {
      instances[i] = MultiThread.getInstance(Constant.num_threads[i]);
    }
  }

  // EXTEND_CACHE(1),
  // MESSAGE_QUEUE(2),
  // DISTRIBUTED_LOCK(3);
  @GetMapping("/responseTime/{method}")
  public void ResponseTime(@PathVariable int method) {
    
    log.info(method + "");
  }


}
