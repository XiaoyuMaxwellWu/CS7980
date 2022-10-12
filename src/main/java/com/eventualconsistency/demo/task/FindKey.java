package com.eventualconsistency.demo.task;

import com.eventualconsistency.demo.controller.MysqlRedisController;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @description: some desc
 * @author: Xiaoyu Wu
 * @email: wu.xiaoyu@northeastern.edu
 * @date: 10/10/22 10:18 PM
 */
public class FindKey implements Runnable {

  @Autowired
  public MysqlRedisController controller;

  @Override
  public void run() {
    HashMap<String, Object> requestInfo = new HashMap<>();
    requestInfo.put("csKey", "K1");
    try {
      controller.findByKey(requestInfo);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
