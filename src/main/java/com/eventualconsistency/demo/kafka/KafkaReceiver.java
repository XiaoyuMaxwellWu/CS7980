package com.eventualconsistency.demo.kafka;

import com.eventualconsistency.demo.constants.Constant;
import com.eventualconsistency.demo.controller.MysqlRedisController;
import com.eventualconsistency.demo.dao.MysqlRepository;
import com.eventualconsistency.demo.entity.MysqlTab;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Slf4j
@Component
public class KafkaReceiver {
    @Autowired
    private MysqlRedisController mysqlRedisController;
    @Autowired
    private HashOperations hashOperations;

    @KafkaListener(topics = "reqToMysql", groupId = "group")
    public void receive(String key) throws InterruptedException {
        HashMap<String, Object> requestInfo = new HashMap<>();
        requestInfo.put("csKey", key);
        if (null == mysqlRedisController.findByKey(requestInfo)) {
            hashOperations.put(Constant.KEY, key, "#Null");
        }
    }


}
