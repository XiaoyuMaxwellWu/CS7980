package com.eventualconsistency.demo.controller;

import com.eventualconsistency.demo.constants.Constant;
import com.eventualconsistency.demo.dao.MysqlRepository;
import com.eventualconsistency.demo.entity.MysqlTab;
import com.eventualconsistency.demo.vo.ResponseEntry;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: some desc
 * @author: Xiaoyu Wu
 * @email: wu.xiaoyu@northeastern.edu
 * @date: 11/13/22 3:27 PM
 */
@RestController
@RequestMapping("/capstone")
@Slf4j
public class BaseLineController extends Controller {
  @Autowired
  private MysqlRepository mysqlRepository;

  @Autowired
  private HashOperations hashOperations;


  // find by key, will first look for Redis, if not found, look for MySQL
  @PostMapping("/findByKey")
  public ResponseEntry findByKey(@RequestBody Map<String, Object> requestInfo)
      throws InterruptedException {
    String key = requestInfo.get("csKey") + "";
    String value;
    Object tryGet = hashOperations.get(Constant.KEY, key);
    if (tryGet == null) {
      log.info("look from Mysql");
      MysqlTab mysqlTab = mysqlRepository.findByCsKey(key);
      if (mysqlTab == null) {
        return null;
      }
      value = mysqlTab.getCsValue();
      Thread.sleep(500);
      hashOperations.put(Constant.KEY, key, value);
      return new ResponseEntry(key, value, false);
    } else {
      value = tryGet.toString();
      log.info("look from Redis");
      return new ResponseEntry(key, value, true);
    }
  }
}
