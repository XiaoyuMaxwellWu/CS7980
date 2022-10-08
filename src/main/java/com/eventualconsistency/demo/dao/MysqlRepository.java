package com.eventualconsistency.demo.dao;

import com.eventualconsistency.demo.entity.MysqlTab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @description: some desc
 * @author: Xiaoyu Wu
 * @email: wu.xiaoyu@northeastern.edu
 * @date: 10/7/22 5:11 PM
 */
public interface MysqlRepository extends JpaRepository<MysqlTab, Long> {
  void deleteByMysqlKey(String mysqlKey);
}