package com.eventualconsistency.demo.constants;

import com.eventualconsistency.demo.controller.DistributedLockController;
import com.eventualconsistency.demo.controller.MessageQueueController;
import com.eventualconsistency.demo.controller.ZRankController;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @description: some desc
 * @author: Xiaoyu Wu
 * @email: wu.xiaoyu@northeastern.edu
 * @date: 10/8/22 5:26 PM
 */
@Data
public class Constant {

  public static final String KEY = "redis_cache";

  public static final String LOCK = "redis_lock";

  public static final int[] num_threads = new int[]{50, 100, 1000};

  public static final String topic = "reqToMysql";

//  @Autowired
//  private static ZRankController zRankController;
//
//  @Autowired
//  private static MessageQueueController messageQueueController;
//
//  @Autowired
//  private static DistributedLockController distributedLockController;

  public enum Method {
    ZRANK(1, 1),
    MESSAGE_QUEUE(2, 1),
    DISTRIBUTED_LOCK(3, 1);
    int methodCode;
    Object conotroller;

    Method(int methodCode, Object controller) {
      this.methodCode = methodCode;
      this.conotroller = controller;
    }
  }

}
