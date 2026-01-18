package com.fransua.burger_order_api.burger.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class BurgerRequest {

  @NotBlank(message = "Burger name can't be empty")
  @Size(min = 5, message = "Burger name must be at least 5 characters long")
  private String name;

  @NotNull(message = "Unit price is required")
  private BigDecimal unitPrice;
}
