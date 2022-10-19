package com.eventualconsistency.demo.controller;

import com.eventualconsistency.demo.constants.Constant;
import com.eventualconsistency.demo.dao.MysqlRepository;
import com.eventualconsistency.demo.entity.MysqlTab;
import com.eventualconsistency.demo.entity.RedisEntry;
import com.eventualconsistency.demo.vo.ResponseEntry;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: some desc
 * @author: Xiaoyu Wu
 * @email: wu.xiaoyu@northeastern.edu
 * @date: 10/12/22 8:58 PM
 */

@RestController
@RequestMapping("/capstone")
@Slf4j
public class DistributedLockController {

  @Autowired
  private MysqlRepository mysqlRepository;

  @Autowired
  private HashOperations hashOperations;

  @Autowired
  private MysqlRedisController mysqlRedisController;

  @Autowired
  private RedisTemplate redisTemplate;

  // 1. assign every request a unique id
  // 2. check redis, has entry, return
  // (1) 3. no entry, no lock, add a new redis lock, with entry key as key, unique id as value, and a time(2s)
  // 4. fetch entry from mysql, check if the lock with entry key has same unique id, has, update
  // 5. not the same one, not update
  // (2) 6. no entry, has lock, sleep 100 ms, return to step 3
  // 7. mysql updates, and then redis lock deletes, and then updates redis
  @PostMapping("/findByKeyLock")
  public ResponseEntry findByKeyLock(@RequestBody Map<String, Object> requestInfo)
      throws InterruptedException {
    String key = requestInfo.get("csKey") + "";
    String value;
    Object tryEntry = hashOperations.get(Constant.KEY, key);
    // entry is in redis, return directly
    if (tryEntry != null) {
      value = tryEntry.toString();
      log.info("look from Redis");
      return new ResponseEntry(key, value, true);
    }
    String uuid = UUID.randomUUID().toString();
    // keep waiting until lock is expired
    while (!redisTemplate.opsForValue().setIfAbsent(key, uuid, 2, TimeUnit.SECONDS)) {
      Thread.sleep(100);
    }
    log.info("look from Mysql");
    MysqlTab mysqlTab = mysqlRepository.findByCsKey(key);
    if (mysqlTab == null) {
      return null;
    }
    Object tryLock = redisTemplate.opsForValue().get(key);
    // update redis only if the key has lock with the same request ID 
    if (tryLock != null && uuid.equals(tryLock.toString())) {
      hashOperations.put(Constant.KEY, key, mysqlTab.getCsValue());
    }
    return new ResponseEntry(key, mysqlTab.getCsValue(), false);

  }

  @PostMapping("/test")
  @GetMapping("/test")
  public void test() throws InterruptedException {
//    redisTemplate.opsForValue().set("K2", "aaa", 2, TimeUnit.SECONDS);
    System.out.println(redisTemplate.opsForValue().setIfAbsent("K2", "b", 2, TimeUnit.SECONDS));
    System.out.println(redisTemplate.opsForValue().get("K2"));
  }
  
  
  @PostMapping("/updateMysqlLock")
  public void updateMysqlLock(@RequestBody MysqlTab mysqlTab) {
    // update mysql with new entry
    mysqlRedisController.saveInMysql(mysqlTab);
    // delete redis lock
    redisTemplate.opsForValue().set(mysqlTab.getCsKey(), "", 1, TimeUnit.MILLISECONDS);
    RedisEntry redisEntry = new RedisEntry();
    BeanUtils.copyProperties(mysqlTab, redisEntry);
    // update redis entry
    mysqlRedisController.updateRedis(redisEntry);
  }

}
