package com.fransua.burger_order_api.order.dto.request;

import java.time.Instant;
import lombok.Data;

@Data
public class FilterCriteriaRequest {

  private Instant orderCreatedAtFrom;
  private Instant orderCreatedAtTo;
  private String burgerName;
}
