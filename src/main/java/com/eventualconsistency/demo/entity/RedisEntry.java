package com.eventualconsistency.demo.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

/**
 * @description: some desc
 * @author: Xiaoyu Wu
 * @email: wu.xiaoyu@northeastern.edu
 * @date: 10/8/22 3:45 PM
 */
@RedisHash("redis_cache")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedisEntry implements Serializable {
  private static final long serialVersionUID =  -1717263197540570145L;
  @Id
  private String csKey;
  private String csValue;

}