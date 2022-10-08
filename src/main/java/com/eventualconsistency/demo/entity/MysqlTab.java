package com.eventualconsistency.demo.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.redis.core.RedisHash;

/**
 * @description: some desc
 * @author: Xiaoyu Wu
 * @email: wu.xiaoyu@northeastern.edu
 * @date: 10/7/22 4:35 PM
 */


@Data
@Entity
@Table(name = "mysql_tab")
@AllArgsConstructor
@NoArgsConstructor
public class MysqlTab {
  @Id
  private String csKey;
  private String csValue;
  
  
}
