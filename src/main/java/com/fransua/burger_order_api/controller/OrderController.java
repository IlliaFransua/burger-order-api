package com.fransua.burger_order_api.controller;

import com.fransua.burger_order_api.dto.request.FilterCriteriaRequest;
import com.fransua.burger_order_api.dto.request.OrderRequest;
import com.fransua.burger_order_api.dto.response.OrderResponse;
import com.fransua.burger_order_api.dto.response.UploadStatsResponse;
import com.fransua.burger_order_api.exception.TechnicalFailureException;
import com.fransua.burger_order_api.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/order")
public class OrderController {

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping
  public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
    OrderResponse response = orderService.createOrder(orderRequest);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrderResponse> findOrder(@PathVariable Long id) {
    OrderResponse response = orderService.findOrder(id);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @PutMapping("/{id}")
  public ResponseEntity<OrderResponse> updateOrder(@PathVariable Long id,
      @Valid @RequestBody OrderRequest orderRequest) {
    OrderResponse response = orderService.updateOrder(id, orderRequest);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
    orderService.deleteOrder(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/_list")
  public ResponseEntity<Page<OrderResponse>> getPaginatedOrders(
      @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
    Page<OrderResponse> orderPage = orderService.getPaginatedOrders(pageable);
    return new ResponseEntity<>(orderPage, HttpStatus.OK);
  }

  @PostMapping("/_report")
  public ResponseEntity<StreamingResponseBody> downloadReport(
      @Valid @RequestBody FilterCriteriaRequest filter) {
    HttpHeaders headers = new HttpHeaders();

    headers.setContentType(MediaType.parseMediaType("text/csv"));
    headers.setContentDisposition(
        ContentDisposition.parse("attachment; filename=\"orders_report.csv\""));

    StreamingResponseBody streamingResponseBody = outputStream -> {
      orderService.generateReport(filter, outputStream);
    };

    return new ResponseEntity<>(streamingResponseBody, headers, HttpStatus.OK);
  }

  @PostMapping(value = "/upload", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public ResponseEntity<UploadStatsResponse> uploadOrders(HttpServletRequest request) {
    try {
      UploadStatsResponse stats = orderService.uploadOrders(request.getInputStream());
      return new ResponseEntity<>(stats, HttpStatus.OK);
    } catch (IOException e) {
      throw new TechnicalFailureException("Failed to get input stream from request.", e);
    }
  }

}
