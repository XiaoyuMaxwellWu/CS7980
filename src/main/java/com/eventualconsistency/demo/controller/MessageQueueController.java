package com.eventualconsistency.demo.controller;

import com.eventualconsistency.demo.constants.Constant;
import com.eventualconsistency.demo.dao.MysqlRepository;
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
public class MessageQueueController {
    @Autowired
    private MysqlRepository mysqlRepository;

    @Autowired
    private HashOperations hashOperations;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/findByKeyLock")
    public ResponseEntry findByKeyLock(@RequestBody Map<String, Object> requestInfo)
            throws InterruptedException {
        String key = requestInfo.get("csKey") + "";
        String value;
        Object tryEntry = hashOperations.get(Constant.KEY, key);
        return null;
    }
}
