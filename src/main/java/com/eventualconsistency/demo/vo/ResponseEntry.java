package com.eventualconsistency.demo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: some desc
 * @author: Xiaoyu Wu
 * @email: wu.xiaoyu@northeastern.edu
 * @date: 10/8/22 4:11 PM
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseEntry {

  private String csKey;
  private String csValue;


}
