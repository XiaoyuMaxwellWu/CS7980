package com.eventualconsistency.demo.controller;

import com.eventualconsistency.demo.vo.ResponseEntry;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @description: some desc
 * @author: Xiaoyu Wu
 * @email: wu.xiaoyu@northeastern.edu
 * @date: 11/9/22 4:18 PM
 */
public interface Controller {

  ResponseEntry findByKey(@RequestBody Map<String, Object> requestInfo) throws Exception;
}
