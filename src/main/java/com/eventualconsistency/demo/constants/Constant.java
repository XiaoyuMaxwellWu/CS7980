package com.eventualconsistency.demo.constants;

import lombok.Data;

/**
 * @description: some desc
 * @author: Xiaoyu Wu
 * @email: wu.xiaoyu@northeastern.edu
 * @date: 10/8/22 5:26 PM
 */
@Data
public class Constant {

  public static final String KEY = "redis_cache";

  public static final String LOCK = "redis_lock";
  
  public static final int[] num_threads = new int[]{50, 100, 1000};
}
