package com.eventualconsistency.demo.controller;

import com.eventualconsistency.demo.constants.Constant;
import com.eventualconsistency.demo.dao.MysqlRepository;
import com.eventualconsistency.demo.entity.MysqlTab;
import com.eventualconsistency.demo.kafka.KafkaSender;
import com.eventualconsistency.demo.vo.ResponseEntry;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/capstone")
@Slf4j
public class MessageQueueController extends Controller {

  @Autowired
  private HashOperations hashOperations;
 

  @Autowired
  private KafkaSender kafkaSender;


  @PostMapping("/findByKeyMessageQueue")
  public ResponseEntry findByKeyMessageQueue(@RequestBody Map<String, Object> requestInfo)
      throws InterruptedException {
    String key = requestInfo.get("csKey") + "";
    kafkaSender.send(Constant.TOPIC, key);
    Object value;
    while ((value = hashOperations.get(Constant.KEY, key)) == null) {
      Thread.sleep(100);
    }
    if (!"null".equals(value + "")) {
      return new ResponseEntry(key, value + "", true);
    }
    while (true){
      if(isReadRedisMap.get(1)!=0){
        return new ResponseEntry(key, value + "", isReadRedisMap.get(1) == 1 ? true : false);
      }
      Thread.sleep(200);
    }
  }

  public static int mysqlCnt = 0;
  public static int redisCnt = 0;
  public static Map<Integer, Integer> isReadRedisMap = new HashMap<>();
  // key: request id; value: 0 no result, 1 read from redis, 2 mysql

  @Override
  public ResponseEntry findByKey(Map<String, Object> requestInfo) throws Exception {
    return findByKeyMessageQueue(requestInfo);
  }
}
