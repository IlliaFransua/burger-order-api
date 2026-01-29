package com.fransua.burger_order_api.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

  @GetMapping("/ping")
  public String ping() {
    return "PONG";
  }
}
