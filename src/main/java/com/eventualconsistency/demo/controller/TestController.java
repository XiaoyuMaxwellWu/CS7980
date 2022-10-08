package com.eventualconsistency.demo.controller;


import com.eventualconsistency.demo.dao.MysqlRepository;
import com.eventualconsistency.demo.dao.RedisRepository;
import com.eventualconsistency.demo.entity.MysqlTab;
import com.eventualconsistency.demo.entity.RedisEntry;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cache.spi.support.AbstractReadWriteAccess.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
  private RedisTemplate redisTemplate;
  public static final String KEY = "redis_cache";

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
    String key = requestInfo.get("mysqlKey")+"";
    mysqlRepository.deleteByMysqlKey(key);
    log.info("Delete a row by key: " + key);
  }

  //update a row to mysql
  @PostMapping("/updateMysql")
  public void updateMysql(@RequestBody MysqlTab mysqlTab) {
    mysqlRepository.save(mysqlTab);
    log.info("Saved entry in Mysql: " + mysqlTab);
  }

  //find all data in mysql
  @GetMapping("/mysql")
  public List<MysqlTab> findAll() {
    return mysqlRepository.findAll();
  }


  //add an entry to Redis
  @PostMapping("/addRedis")
  public void addRedis(@RequestBody RedisEntry entry) {
    redisTemplate.opsForHash().put(KEY, entry.getKey(), entry);
    log.info("Saved entry in Redis: " + entry);
  }

  //delete redis
  @PostMapping("/deleteRedis")
  public void deleteEntryByKey(@RequestBody Map<String, String> requestInfo) {
    String key = requestInfo.get("key");
    redisTemplate.opsForHash().delete(KEY, key);
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
    return redisTemplate.opsForHash().entries(KEY);
  }


}

