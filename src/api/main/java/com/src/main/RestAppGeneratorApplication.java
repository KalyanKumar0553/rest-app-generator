package com.src.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RestAppGeneratorApplication {
  public static void main(String[] args) {
    SpringApplication.run(RestAppGeneratorApplication.class, args);
  }
}
