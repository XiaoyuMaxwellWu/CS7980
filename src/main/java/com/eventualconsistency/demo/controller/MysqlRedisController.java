package com.eventualconsistency.demo.controller;


import com.eventualconsistency.demo.constants.Constant;
import com.eventualconsistency.demo.dao.MysqlRepository;
import com.eventualconsistency.demo.entity.MysqlTab;
import com.eventualconsistency.demo.entity.RedisEntry;
import com.eventualconsistency.demo.vo.ResponseEntry;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: some desc
 * @author: Xiaoyu Wu
 * @email: wu.xiaoyu@northeastern.edu
 * @date: 10/7/22 2:01 PM
 */
@RestController
@RequestMapping("/capstone")
@Slf4j
public class MysqlRedisController{

  @Autowired
  private MysqlRepository mysqlRepository;

  @Autowired
  private HashOperations hashOperations;


 

  @PostMapping("/findByKeyLongerTime")
  public ResponseEntry findByKeyLongerTime(@RequestBody Map<String, Object> requestInfo)
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
      log.info("value is: " + value);
      Thread.sleep(5000);
      hashOperations.put(Constant.KEY, key, value);
      return new ResponseEntry(key, value, false);
    } else {
      value = tryGet.toString();
      log.info("look from Redis");
      return new ResponseEntry(key, value, true);
    }
  }


  //clear all data in Mysql and Redis
  @Transactional
  @GetMapping("/clearAll")
  public void clearAll() {
    mysqlRepository.deleteAll();
    Map entries = hashOperations.entries(Constant.KEY);
    for (Object o : entries.keySet()) {
      hashOperations.delete(Constant.KEY, (String) o);
    }
    log.info("All data has been cleared");
  }

  //add a row to mysql
  @PostMapping("/addMysql")
  public void addMysql(@RequestBody MysqlTab mysqlTab) {
    List<MysqlTab> all = mysqlRepository.findAll();
    if (all.contains(mysqlTab)) {
      return;
    }
    mysqlRepository.save(mysqlTab);
    log.info("Saved entry in Mysql: " + mysqlTab);
  }

  //delete a row to mysql
  @PostMapping("deleteMysql")
  @Transactional
  public void deleteMysqlByKey(@RequestBody Map<String, Object> requestInfo) {
    String key = requestInfo.get("csKey") + "";
    mysqlRepository.deleteByCsKey(key);
    log.info("Delete a row by key: " + key);
  }

  //update a row in mysql
  @PostMapping("/updateMysql")
  public void updateMysql(@RequestBody MysqlTab mysqlTab) {
    saveInMysql(mysqlTab);
    deleteInRedis(mysqlTab.getCsKey());
  }

  public void saveInMysql(MysqlTab mysqlTab) {
    mysqlRepository.save(mysqlTab);
    log.info("Saved entry in Mysql: " + mysqlTab);
  }

  public void deleteInRedis(String key) {
    Object previous = hashOperations.get(Constant.KEY, key);
    hashOperations.delete(Constant.KEY, key);
    log.info("Deleted entry in Redis: " + previous);
  }


  //find all data in mysql
  @GetMapping("/mysql")
  public List<MysqlTab> findFromMysql() {
    return mysqlRepository.findAll();
  }


  //add an entry to Redis
  @PostMapping("/addRedis")
  public void addRedis(@RequestBody RedisEntry entry) {
    hashOperations.put(Constant.KEY, entry.getCsKey(), entry);
    log.info("Saved entry in Redis: " + entry);
  }

  //delete redis
  @PostMapping("/deleteRedis")
  public void deleteEntryByKey(@RequestBody Map<String, String> requestInfo) {
    String key = requestInfo.get("csKey");
    hashOperations.delete(Constant.KEY, key);
    log.info("Delete an entry by key: " + key);
  }

  //update redis
  @PostMapping("/updateRedis")
  public void updateRedis(@RequestBody RedisEntry entry) {
    addRedis(entry);
  }

  //find all entries
  @GetMapping("/redis")
  public Map findFromRedis() {
    return hashOperations.entries(Constant.KEY);
  }


}

