package com.fransua.burger_order_api;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fransua.burger_order_api.burger.dto.request.BurgerRequest;
import com.fransua.burger_order_api.burger.dto.response.BurgerResponse;
import java.math.BigDecimal;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class BurgerTestFactory {

  private final TestRestTemplate testRestTemplate;

  public BurgerTestFactory(TestRestTemplate testRestTemplate) {
    this.testRestTemplate = testRestTemplate;
  }

  public ResponseEntity<BurgerResponse> createTestBurgerAndValidate(String name, BigDecimal price) {
    ResponseEntity<BurgerResponse> response = createTestBurger(name, price);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getId()).isNotNull();

    return response;
  }

  public ResponseEntity<BurgerResponse> createTestBurger(String name, BigDecimal price) {
    BurgerRequest burgerToCreate = new BurgerRequest();

    burgerToCreate.setName(name);
    burgerToCreate.setUnitPrice(price);

    return testRestTemplate.postForEntity("/api/burger", burgerToCreate, BurgerResponse.class);
  }

  public ResponseEntity<String> createTestBurgerExpectingError(String name, BigDecimal price) {
    BurgerRequest burgerToCreate = new BurgerRequest();

    burgerToCreate.setName(name);
    burgerToCreate.setUnitPrice(price);

    return testRestTemplate.postForEntity("/api/burger", burgerToCreate, String.class);
  }
}
