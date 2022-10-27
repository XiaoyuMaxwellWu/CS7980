package com.eventualconsistency.demo.controller;


import com.eventualconsistency.demo.dao.MysqlRepository;
import com.eventualconsistency.demo.entity.MysqlTab;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/capstone")
@Slf4j
public class ZRankController {

    @Autowired
    private MysqlRepository mysqlRepository;

    @Autowired
    private MysqlRedisController mysqlRedisController;


    @PostMapping("/updateMysqlCache")
    public void updateCache(@RequestBody MysqlTab mysqlTab) throws InterruptedException {
        saveInRedis(mysqlTab);
        set(mysqlTab.getCsKey());
    }

    public void saveInRedis(MysqlTab mysqlTab) {
        mysqlRepository.save(mysqlTab);
        log.info("Saved entry in Mysql: " + mysqlTab);
    }

    public void set(String key) throws InterruptedException {
        Thread.sleep(1000);
        mysqlRedisController.deleteInRedis(key);
    }

    Jedis jedis = null;
    static final String DATASOURCE_URL = "jdbc:mysql://localhost:3306/CS7980";
    static final int DATASOURCE_SORT = 6379;
    static final String DATASOURCE_PASS = "root";
    static final int DATASOURCE_SELECT = 1;

    public void RedisSortedSet() {
        //基本配置
        jedis = new Jedis(DATASOURCE_URL, DATASOURCE_SORT);
        jedis.auth(DATASOURCE_PASS);
        jedis.select(DATASOURCE_SELECT);
    }


    @PostMapping("/zsetadd")
    public void ZAdd(){
        jedis.zadd("sortedSet",10,"value:10");
        Map<String,Double> map = new HashMap<String,Double>();
        for(int i=0;i<=10;i++){
            map.put("value:"+i,Double.valueOf(i));
        }
        jedis.zadd("sortedSet",map);
    }

    @PostMapping("/zsetcount")
    public void ZCount(){
        Map<String,Double> map = new HashMap<String,Double>();
        for(int i=0;i<10;i++){
            map.put("value:"+i,Double.valueOf(i));
        }
        jedis.zadd("sortedSet",map);
        Long count = jedis.zcount("sortedSet",0,999);
    }

    @PostMapping("/zsetrank")
    public void ZRank(){
        Map<String,Double> map = new HashMap<String,Double>();
        for(int i=0;i<10;i++){
            map.put("value:"+i,Double.valueOf(i));
        }
        jedis.zadd("sortedSet",map);
        jedis.zremrangeByRank("sortedSet",1,999);
    }




}



