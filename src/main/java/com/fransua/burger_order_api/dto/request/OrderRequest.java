package com.fransua.burger_order_api.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class OrderRequest {

  @NotEmpty(message = "Order must contain at least one burger")
  private List<Long> burgerIds;
}
