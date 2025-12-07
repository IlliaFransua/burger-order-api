package com.fransua.burger_order_api.dto.response;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class BurgerResponse {

  private Long id;
  private String name;
  private BigDecimal unitPrice;
}
