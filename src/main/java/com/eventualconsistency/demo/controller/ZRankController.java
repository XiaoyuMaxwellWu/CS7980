package com.eventualconsistency.demo.controller;


import com.eventualconsistency.demo.constants.Constant;
import com.eventualconsistency.demo.dao.MysqlRepository;
import com.eventualconsistency.demo.entity.MysqlTab;
import com.eventualconsistency.demo.vo.ResponseEntry;
import com.eventualconsistency.demo.vo.ZRankEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/capstone")
@Slf4j
public class ZRankController extends Controller {

  @Autowired
  private MysqlRepository mysqlRepository;

  @Autowired
  private MysqlRedisController mysqlRedisController;

  @Autowired
  private RedisTemplate redisTemplate;

  @Autowired
  private HashOperations hashOperations;

  @PostMapping("/updateMysqlCache")
  public void updateCache(@RequestBody MysqlTab mysqlTab) throws InterruptedException {
    saveInRedis(mysqlTab);
  }

  public void saveInRedis(MysqlTab mysqlTab) {
    mysqlRepository.save(mysqlTab);
    log.info("Saved entry in Mysql: " + mysqlTab);
  }

  public void set(String key, String value, int time) throws InterruptedException {
    redisTemplate.opsForHash().put(Constant.ZRANK_KEY, key, value);
    Thread.sleep(time);
    mysqlRedisController.deleteInRedis(key);
  }

  Jedis jedis = null;
  static final String DATASOURCE_URL = "jdbc:mysql://localhost:3306/CS7980";
  static final int DATASOURCE_SORT = 6379;
  static final String DATASOURCE_PASS = "root";
  static final int DATASOURCE_SELECT = 1;


  @PostMapping("/zsetadd")
  public void ZAdd() {
    jedis.zadd("sortedSet", 10, "value:10");
    Map<String, Double> map = new HashMap<String, Double>();
    for (int i = 0; i <= 10; i++) {
      map.put("value:" + i, Double.valueOf(i));
    }
    jedis.zadd("sortedSet", map);
  }

  @PostMapping("/zsetcount")
  public void ZCount() {
    Map<String, Double> map = new HashMap<String, Double>();
    for (int i = 0; i < 10; i++) {
      map.put("value:" + i, Double.valueOf(i));
    }
    jedis.zadd("sortedSet", map);
    Long count = jedis.zcount("sortedSet", 0, 999);
  }

  @PostMapping("/zsetrank")
  public void ZRank() {
    Map<String, Double> map = new HashMap<String, Double>();
    for (int i = 0; i < 10; i++) {
      map.put("value:" + i, Double.valueOf(i));
    }
    jedis.zadd("sortedSet", map);
    jedis.zremrangeByRank("sortedSet", 1, 999);
  }

  private List<ZRankEntry> getClientList() throws IOException {
    List<ZRankEntry> list = new ArrayList<>();
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      for (Object o : redisTemplate.opsForHash().entries(Constant.ZRANK_KEY).keySet()) {
        ZRankEntry zRankEntry = objectMapper
            .readValue(redisTemplate.opsForHash().get(Constant.ZRANK_KEY, o + "") + "",
                ZRankEntry.class);
        list.add(zRankEntry);
      }

    } catch (Exception e) {
      log.info("no redis entry");
    }

//    for (Object o : clientList) {
//      ZRankEntry zRankEntry = objectMapper.readValue(o + "", ZRankEntry.class);
//      list.add(zRankEntry);
//    }
    return list;
  }

  @GetMapping("/zrankGet")
  public ResponseEntry zrankGet(@RequestBody Map<String, Object> requestInfo)
      throws IOException, InterruptedException {
    String key = requestInfo.get("csKey") + "";
    List<ZRankEntry> list = getClientList();
    ZRankEntry redisEntry = null;
    for (ZRankEntry entry : list) {
      if (entry.getCsKey().equals(key)) {
        redisEntry = entry;
        break;
      }
    }
    if (redisEntry == null) {
      // redis does not exist, look from MySQL
      HashMap<String, Object> map = new HashMap<>();
      map.put("csKey", key);
      MysqlTab mysqlTab = mysqlRepository.findByCsKey(key);
      if (mysqlTab == null) {
        return new ResponseEntry(key, null, false);
      }
      ZRankEntry zRankEntry = new ZRankEntry(mysqlTab.getCsKey(), mysqlTab.getCsValue(),
          System.currentTimeMillis(), 1);
      String entryJson = new ObjectMapper().writeValueAsString(zRankEntry);
      set(key, entryJson, 1);
      return new ResponseEntry(mysqlTab.getCsKey(), mysqlTab.getCsValue(), false);
    }
    // redis exists
    redisEntry.setHitCount(redisEntry.getHitCount() + 1);
    // sort by hit count
    Collections.sort(list, Comparator.comparingInt(ZRankEntry::getHitCount));
    int position = 0;
    for (int i = 0; i < list.size(); i++) {
      if (list.get(i).getCsKey().equals(key)) {
        position = i;
        break;
      }
    }
    String entryJson = new ObjectMapper().writeValueAsString(redisEntry);
    // time passed in seconds
    long timePass = (System.currentTimeMillis() - redisEntry.getInsertTime()) / 1000;
    long expireTime = (position < list.size() / 2 ? 1 : 3) * 1000 - timePass;
    set(key, entryJson, expireTime < 0 ? 0 : (int) expireTime);
    return new ResponseEntry(redisEntry.getCsKey(), redisEntry.getCsValue(), true);

  }


  @Override
  public ResponseEntry findByKey(Map<String, Object> requestInfo) throws Exception {
    return zrankGet(requestInfo);
  }
}



