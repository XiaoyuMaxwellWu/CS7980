package com.eventualconsistency.demo.config.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @description: some desc
 * @author: Xiaoyu Wu
 * @email: wu.xiaoyu@northeastern.edu
 * @date: 10/7/22 9:23 PM
 */
@Configuration
public class RedisCfg {

  @Bean
  JedisConnectionFactory jedisConnectionFactory() {
    return new JedisConnectionFactory();
  }


  @Bean
  @Primary
  public RedisTemplate<String, Object> redisTemplate() {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(jedisConnectionFactory());
//    template.setKeySerializer(new StringRedisSerializer());
//    template.setHashKeySerializer(new StringRedisSerializer());
//    template.setHashKeySerializer(new JdkSerializationRedisSerializer());
//    template.setValueSerializer(new JdkSerializationRedisSerializer());
//    template.setEnableTransactionSupport(true);
//    template.afterPropertiesSet();
    return template;
  }

  @Bean
  public HashOperations hashOperations() {
    return redisTemplate().opsForHash();
  }


}
