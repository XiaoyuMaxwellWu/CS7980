package com.eventualconsistency.demo.dao;

import com.eventualconsistency.demo.entity.RedisEntry;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * @description: some desc
 * @author: Xiaoyu Wu
 * @email: wu.xiaoyu@northeastern.edu
 * @date: 10/7/22 11:07 PM
 */
@Repository
public class RedisDao {
//
//  @Autowired
//  private RedisTemplate template;
//
//  public static final String HASH_KEY = "Key";
//
//  public RedisEntry save(RedisEntry redisEntry) {
//    template.opsForHash().put(HASH_KEY, redisEntry.getKey(), redisEntry);
//    return redisEntry;
//  }
//  public List<RedisEntry> findAll(){
//    return template.opsForHash().values(HASH_KEY);
//  }
//  public RedisEntry findEntryById(int id){
//    return (RedisEntry) template.opsForHash().get(HASH_KEY, id);
//  }


}
