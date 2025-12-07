package com.fransua.burger_order_api.dto.response;

import java.time.Instant;
import lombok.Data;

@Data
public class OrderResponseCsv {

  private Long id;
  private Instant createdAt;
  private String burgers;
}
