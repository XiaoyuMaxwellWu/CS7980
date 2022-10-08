package com.eventualconsistency.demo.entity;

import java.io.Serializable;
import javax.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

/**
 * @description: some desc
 * @author: Xiaoyu Wu
 * @email: wu.xiaoyu@northeastern.edu
 * @date: 10/7/22 9:40 PM
 */
@RedisHash("redis_cache")
@Data
@AllArgsConstructor
public class RedisEntry implements Serializable {
  @Id
  private String key;
  private String value;


}