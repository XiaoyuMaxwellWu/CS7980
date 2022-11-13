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
import org.omg.PortableServer.THREAD_POLICY_ID;
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
public class DistributedLockController implements Controller {

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
  // (2) 6. no entry, has lock, sleep 100 ms multiple times till redis has the entry 
  // 7. mysql updates, and then redis lock deletes, and then updates redis
  @PostMapping("/findByKeyLock")
  @Override
  public ResponseEntry findByKey(@RequestBody Map<String, Object> requestInfo)
      throws InterruptedException {
    if (1 == 1) {
      log.info("hhhh");
      return null;
    }
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
    // has lock, keep waiting
    while (redisTemplate.opsForValue().get(key) != null) {
      // if entry is existed in Redis, return
      if ((tryEntry = hashOperations.get(Constant.KEY, key)) != null) {
        return new ResponseEntry(key, tryEntry.toString(), true);
      }
      Thread.sleep(100);
    }
    // no lock, add a lock 
    redisTemplate.opsForValue().set(key, uuid, 2, TimeUnit.SECONDS);
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
 
