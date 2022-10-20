package com.eventualconsistency.demo.controller;

import com.eventualconsistency.demo.constants.Constant;
import com.eventualconsistency.demo.dao.MysqlRepository;
import com.eventualconsistency.demo.entity.MysqlTab;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

@RestController
@RequestMapping("/capstone")
@Slf4j
public class ExtendCacheController {

    @Autowired
    private MysqlRepository mysqlRepository;

    @Autowired
    private HashOperations hashOperations;

    @Autowired
    private MysqlRedisController mysqlRedisController;


    @PostMapping("/updateMysqlCache")
    public void updateMysql(@RequestBody MysqlTab mysqlTab) throws InterruptedException {
        saveInMysql(mysqlTab);
        setIfAbsent(mysqlTab.getCsKey());
    }

    public void saveInMysql(MysqlTab mysqlTab) {
        mysqlRepository.save(mysqlTab);
        log.info("Saved entry in Mysql: " + mysqlTab);
    }

    public void setIfAbsent(String key) throws InterruptedException {
        Thread.sleep(1000);
        mysqlRedisController.deleteInRedis(key);

    }




}



