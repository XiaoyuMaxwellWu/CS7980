package com.eventualconsistency.demo.controller;

import com.eventualconsistency.demo.entity.MysqlTab;
import com.eventualconsistency.demo.vo.ResponseEntry;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @description: some desc
 * @author: Xiaoyu Wu
 * @email: wu.xiaoyu@northeastern.edu
 * @date: 11/9/22 4:18 PM
 */
public abstract class Controller {

  @Autowired
  private MysqlRedisController mysqlRedisController;
  abstract ResponseEntry findByKey(@RequestBody Map<String, Object> requestInfo) throws Exception;
  void updateMySQL(@RequestBody MysqlTab mysqlTab){
    mysqlRedisController.saveInMysql(mysqlTab);
    mysqlRedisController.deleteInRedis(mysqlTab.getCsKey());
  }
 
}
