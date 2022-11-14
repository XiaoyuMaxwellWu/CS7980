package com.eventualconsistency.demo.constants;

import com.eventualconsistency.demo.controller.BaseLineController;
import com.eventualconsistency.demo.controller.Controller;
import com.eventualconsistency.demo.controller.DistributedLockController;
import com.eventualconsistency.demo.controller.MessageQueueController;
import com.eventualconsistency.demo.controller.MysqlRedisController;
import com.eventualconsistency.demo.controller.ZRankController;
import java.util.EnumSet;
import javax.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @description: some desc
 * @author: Xiaoyu Wu
 * @email: wu.xiaoyu@northeastern.edu
 * @date: 10/8/22 5:26 PM
 */
@Data
@Component
public class Constant {

  public static final String KEY = "redis_cache";

  public static final String LOCK = "redis_lock";

  public static final int[] num_threads = new int[]{50, 100, 1000, 10000};

  public static final String topic = "reqToMysql";

  @Autowired
  private ZRankController zRankController;

  @Autowired
  private MessageQueueController messageQueueController;

  @Autowired
  private DistributedLockController distributedLockController;

  @Autowired
  private BaseLineController baseLineController;

  @PostConstruct
  public void init() {
    Controller[] controllers = {zRankController, messageQueueController,
        distributedLockController, baseLineController};
    for (int i = 0; i < EnumSet.allOf(Method.class).size(); i++) {
      Method.values()[i].setController(controllers[i]);
    }
  }

  public enum Method {


    ZRANK(1, null),
    MESSAGE_QUEUE(2, null),
    DISTRIBUTED_LOCK(3, null),
    BASELINE(4, null);
    int methodCode;
    Controller controller;

    Method(int methodCode, Controller controller) {
      this.methodCode = methodCode;
      this.controller = controller;
    }

    public int getMethodCode() {
      return methodCode;
    }

    public Controller getController() {
      return controller;
    }

    public void setController(Controller controller) {
      this.controller = controller;
    }

  }

}
