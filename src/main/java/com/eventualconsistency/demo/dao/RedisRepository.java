package com.eventualconsistency.demo.dao;

import com.eventualconsistency.demo.entity.RedisEntry;
import org.springframework.data.repository.CrudRepository;

/**
 * @description: some desc
 * @author: Xiaoyu Wu
 * @email: wu.xiaoyu@northeastern.edu
 * @date: 10/7/22 9:27 PM
 */
public interface RedisRepository  extends CrudRepository<RedisEntry, String> {
  RedisEntry findByKey(String key);
  void deleteByKey(String key);
}
