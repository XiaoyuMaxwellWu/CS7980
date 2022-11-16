package com.eventualconsistency.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DemoApplication {
  public static void main(String[] args) {
//    SpringApplication.run(DemoApplication.class, args);
    SpringApplicationBuilder builder = new SpringApplicationBuilder(DemoApplication.class);

    builder.headless(false);

    builder.run(args);
  }

}
