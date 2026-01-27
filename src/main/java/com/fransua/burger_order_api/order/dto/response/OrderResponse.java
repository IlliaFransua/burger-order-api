package com.fransua.burger_order_api.order.dto.response;

import com.fransua.burger_order_api.burger.dto.response.BurgerResponse;
import java.time.Instant;
import java.util.List;
import lombok.Data;

@Data
public class OrderResponse {

  private Long id;
  private Instant createdAt;
  private List<BurgerResponse> burgers;
}
