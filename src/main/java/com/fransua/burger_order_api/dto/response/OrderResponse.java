package com.fransua.burger_order_api.dto.response;

import com.fransua.burger_order_api.entity.Burger;
import java.time.Instant;
import java.util.List;
import lombok.Data;

@Data
public class OrderResponse {

  private Long id;
  private Instant createdAt;
  private List<Burger> burgers;
}
