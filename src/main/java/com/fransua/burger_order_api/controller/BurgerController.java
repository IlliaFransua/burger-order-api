package com.fransua.burger_order_api.controller;

import com.fransua.burger_order_api.dto.request.BurgerRequest;
import com.fransua.burger_order_api.dto.response.BurgerResponse;
import com.fransua.burger_order_api.service.BurgerService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/burger")
public class BurgerController {

  private final BurgerService burgerService;

  public BurgerController(BurgerService burgerService) {
    this.burgerService = burgerService;
  }

  // TODO: return StreamingResponseBody instead of List<BurgerResponse>
  @GetMapping
  public ResponseEntity<List<BurgerResponse>> findAllBurgers() {
    List<BurgerResponse> responses = burgerService.findAllBurgers();
    return new ResponseEntity<>(responses, HttpStatus.OK);
  }

  @PostMapping
  public ResponseEntity<BurgerResponse> createBurger(
      @Valid @RequestBody BurgerRequest burgerRequest) {
    BurgerResponse response = burgerService.createBurger(burgerRequest);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  public ResponseEntity<BurgerResponse> updateBurger(@PathVariable Long id,
      @Valid @RequestBody BurgerRequest burgerRequest) {
    BurgerResponse response = burgerService.updateBurger(id, burgerRequest);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteBurger(@PathVariable Long id) {
    burgerService.deleteBurger(id);
    return ResponseEntity.noContent().build();
  }
}
