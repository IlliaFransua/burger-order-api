package com.fransua.burger_order_api.dto.response;

import java.time.Instant;
import java.util.List;
import lombok.Data;

@Data
public class OrderResponse {

  private Long id;
  private Instant createdAt;
  private List<BurgerResponse> burgers;
}
