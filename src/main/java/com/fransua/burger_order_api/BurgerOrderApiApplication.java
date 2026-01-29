package com.fransua.burger_order_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaRepositories("com.fransua.burger_order_api")
@EnableElasticsearchRepositories("com.fransua.burger_order_api.email")
@SpringBootApplication
@EnableScheduling
public class BurgerOrderApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(BurgerOrderApiApplication.class, args);
  }
}
