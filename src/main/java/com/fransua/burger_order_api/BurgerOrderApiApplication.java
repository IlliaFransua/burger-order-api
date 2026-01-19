package com.fransua.burger_order_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BurgerOrderApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(BurgerOrderApiApplication.class, args);
  }
}
