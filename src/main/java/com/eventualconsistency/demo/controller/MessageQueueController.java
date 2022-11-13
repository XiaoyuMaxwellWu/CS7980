package com.eventualconsistency.demo.controller;

import com.eventualconsistency.demo.constants.Constant;
import com.eventualconsistency.demo.dao.MysqlRepository;
import com.eventualconsistency.demo.kafka.KafkaSender;
import com.eventualconsistency.demo.vo.ResponseEntry;
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
public class MessageQueueController implements Controller {

  @Autowired
  private HashOperations hashOperations;


  @Autowired
  private KafkaSender kafkaSender;


  @PostMapping("/findByKeyMessageQueue")
  public ResponseEntry findByKeyMessageQueue(@RequestBody Map<String, Object> requestInfo)
      throws InterruptedException {
    String key = requestInfo.get("csKey") + "";
    kafkaSender.send(Constant.topic, key);
    Object value;
    while ((value = hashOperations.get(Constant.KEY, key)) == null) {
      Thread.sleep(100);
    }
    if (!"null".equals(value + "")) {
      return new ResponseEntry(key, value + "", true);
    }
    return null;
  }

  @Override
  public ResponseEntry findByKey(Map<String, Object> requestInfo) throws Exception {
    return findByKeyMessageQueue(requestInfo);
  }
}
