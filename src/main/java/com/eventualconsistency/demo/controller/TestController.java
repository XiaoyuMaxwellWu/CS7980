package com.eventualconsistency.demo.controller;


import com.eventualconsistency.demo.dao.MysqlRepository;
import com.eventualconsistency.demo.entity.MysqlTab;
import com.eventualconsistency.demo.entity.RedisEntry;
import com.eventualconsistency.demo.vo.ResponseEntry;
import java.util.List;
import java.util.Map;
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
public class TestController {

  @Autowired
  private MysqlRepository mysqlRepository;

  @Autowired
  private HashOperations hashOperations;
  
  public static final String KEY = "redis_cache";

  // find by key, will first look for Redis, if not found, look for MySQL
  @PostMapping("/findByKey")
  public ResponseEntry findByKey(@RequestBody Map<String, Object> requestInfo) {
    String key = requestInfo.get("csKey") + "";
    String value = (String) hashOperations.get(KEY, key);
    if (value == null) {
      log.info("look from Mysql");
      MysqlTab mysqlTab = mysqlRepository.findByCsKey(key);
      if (mysqlTab == null) {
        return null;
      }
      value = mysqlTab.getCsValue();
      hashOperations.put(KEY, key, value);
    }
    return new ResponseEntry(key, value);
  }


  //clear all data in Mysql and Redis
  @Transactional
  @GetMapping("/clearAll")
  public void clearAll() {
    mysqlRepository.deleteAll();
    Map entries = hashOperations.entries(KEY);
    for (Object o : entries.keySet()) {
      hashOperations.delete(KEY, (String) o);
    }
    log.info("All data has been cleared");
  }

  //add a row to mysql
  @PostMapping("/addMysql")
  public void addMysql(@RequestBody MysqlTab mysqlTab) {
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

  //update a row to mysql
  @PostMapping("/updateMysql")
  public void updateMysql(@RequestBody MysqlTab mysqlTab) {
    mysqlRepository.save(mysqlTab);
    log.info("Saved entry in Mysql: " + mysqlTab);
    hashOperations.delete(KEY, mysqlTab.getCsKey());
    log.info("Deleted entry in Redis: " + mysqlTab);
  }

  //find all data in mysql
  @GetMapping("/mysql")
  public List<MysqlTab> findFromMysql() {
    return mysqlRepository.findAll();
  }


  //add an entry to Redis
  @PostMapping("/addRedis")
  public void addRedis(@RequestBody RedisEntry entry) {
    hashOperations.put(KEY, entry.getCsKey(), entry);
    log.info("Saved entry in Redis: " + entry);
  }

  //delete redis
  @PostMapping("/deleteRedis")
  public void deleteEntryByKey(@RequestBody Map<String, String> requestInfo) {
    String key = requestInfo.get("csKey");
    hashOperations.delete(KEY, key);
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
    return hashOperations.entries(KEY);
  }


}

