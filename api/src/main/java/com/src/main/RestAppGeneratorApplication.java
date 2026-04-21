package com.src.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.src.main.communication.config.CommunicationModuleConfiguration;

@SpringBootApplication
@ComponentScan(basePackages = "com.src.main")
@Import(CommunicationModuleConfiguration.class)
@EnableScheduling
public class RestAppGeneratorApplication {
  public static void main(String[] args) {
    SpringApplication.run(RestAppGeneratorApplication.class, args);
  }
}
