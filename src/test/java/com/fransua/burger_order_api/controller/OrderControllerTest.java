package com.fransua.burger_order_api.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fransua.burger_order_api.dto.request.FilterCriteriaRequest;
import com.fransua.burger_order_api.dto.request.OrderRequest;
import com.fransua.burger_order_api.dto.response.BurgerResponse;
import com.fransua.burger_order_api.dto.response.OrderResponse;
import com.fransua.burger_order_api.dto.response.UploadStatsResponse;
import com.fransua.burger_order_api.entity.Burger;
import com.fransua.burger_order_api.factory.BurgerTestFactory;
import com.fransua.burger_order_api.repository.BurgerRepository;
import com.fransua.burger_order_api.repository.OrderRepository;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class OrderControllerTest {

  private final TestRestTemplate testRestTemplate;
  private final OrderRepository orderRepository;
  private final BurgerTestFactory burgerTestFactory;
  private BurgerRepository burgerRepository;

  @Autowired
  public OrderControllerTest(TestRestTemplate testRestTemplate,
      BurgerRepository burgerRepository, OrderRepository orderRepository) {
    this.testRestTemplate = testRestTemplate;
    this.burgerRepository = burgerRepository;
    this.orderRepository = orderRepository;
    this.burgerTestFactory = new BurgerTestFactory(testRestTemplate);
  }

  @AfterEach
  public void deleteAllCreatedOrdersAndBurgers() {
    orderRepository.deleteAll();
    burgerRepository.deleteAll();
  }

  private List<Long> createTestBurgers() {
    List<Long> ids = new ArrayList<>();

    String name1 = "TestBurger_" + UUID.randomUUID().toString();
    BigDecimal price1 = new BigDecimal("15.4");
    ResponseEntity<BurgerResponse> response1 = burgerTestFactory.createTestBurgerAndValidate(name1,
        price1);
    ids.add(response1.getBody().getId());

    String name2 = "TestBurger_" + UUID.randomUUID().toString();
    BigDecimal price2 = new BigDecimal("3.2");
    ResponseEntity<BurgerResponse> response2 = burgerTestFactory.createTestBurgerAndValidate(name2,
        price2);
    ids.add(response2.getBody().getId());

    String name3 = "TestBurger_" + UUID.randomUUID().toString();
    BigDecimal price3 = new BigDecimal("7.6");
    ResponseEntity<BurgerResponse> response3 = burgerTestFactory.createTestBurgerAndValidate(name3,
        price3);
    ids.add(response3.getBody().getId());

    return ids;
  }

  private OrderResponse createOrderAndValidate(List<Long> burgerIds) {
    OrderRequest orderRequest = new OrderRequest();
    orderRequest.setBurgerIds(burgerIds);

    ResponseEntity<OrderResponse> responseEntity = testRestTemplate.exchange(
        "/api/order",
        HttpMethod.POST,
        new HttpEntity<>(orderRequest),
        OrderResponse.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(responseEntity.getBody()).isNotNull();

    OrderResponse response = responseEntity.getBody();

    assertThat(response.getId()).isNotNull();
    assertThat(response.getCreatedAt()).isNotNull();
    assertThat(response.getBurgers()).isNotNull();

    List<Long> linkedBurgerIds = response.getBurgers().stream().map(Burger::getId).toList();

    assertThat(linkedBurgerIds.stream().sorted().toList()).isEqualTo(
        burgerIds.stream().sorted().toList());

    orderRepository.findById(response.getId()).orElseThrow(
        () -> new AssertionError("Order with ID '" + response.getId() + "' is not found."));

    return response;
  }

  private ResponseEntity<String> createOrder_expectError(List<Long> burgerIds) {
    OrderRequest orderRequest = new OrderRequest();
    orderRequest.setBurgerIds(burgerIds);

    return testRestTemplate.exchange(
        "/api/order",
        HttpMethod.POST,
        new HttpEntity<>(orderRequest),
        String.class);
  }

  private OrderResponse createTestOrder() {
    List<Long> testBurgers = createTestBurgers();

    return createOrderAndValidate(testBurgers);
  }

  private File createTestJsonFileWithOrdersToUpload() throws IOException {
    Path pathToUploadFile = Path.of("./orders_to_upload_test.json");

    if (Files.exists(pathToUploadFile)) {
      Files.delete(pathToUploadFile);
    }

    List<Long> burgerIds1 = createTestBurgers();
    OrderRequest orderRequest1 = new OrderRequest();
    orderRequest1.setBurgerIds(burgerIds1);

    List<Long> burgerIds2 = createTestBurgers();
    OrderRequest orderRequest2 = new OrderRequest();
    orderRequest2.setBurgerIds(burgerIds2);

    List<Long> burgerIds3 = createTestBurgers();
    OrderRequest orderRequest3 = new OrderRequest();
    orderRequest3.setBurgerIds(burgerIds3);

    List<OrderRequest> orderRequests = List.of(orderRequest1, orderRequest2, orderRequest3);

    ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
    String json = writer.writeValueAsString(orderRequests);

    FileUtils.writeStringToFile(pathToUploadFile.toFile(), json, StandardCharsets.UTF_8);

    return pathToUploadFile.toFile();
  }

  private File createTestJsonFileWithOrdersToUpload_withInvalidBurgerIds() throws IOException {
    Path pathToUploadFile = Path.of("./orders_to_upload_test.json");

    if (Files.exists(pathToUploadFile)) {
      Files.delete(pathToUploadFile);
    }

    OrderRequest orderRequest1 = new OrderRequest();
    orderRequest1.setBurgerIds(List.of(111L, 222L, 333L));

    OrderRequest orderRequest2 = new OrderRequest();
    orderRequest2.setBurgerIds(List.of(444L, 555L, 666L));

    OrderRequest orderRequest3 = new OrderRequest();
    orderRequest3.setBurgerIds(List.of(777L, 888L, 999L));

    List<OrderRequest> orderRequests = List.of(orderRequest1, orderRequest2, orderRequest3);

    ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
    String json = writer.writeValueAsString(orderRequests);

    FileUtils.writeStringToFile(pathToUploadFile.toFile(), json, StandardCharsets.UTF_8);

    return pathToUploadFile.toFile();
  }

  private File createTestJsonFileWithOrdersToUpload_withMixedValidAndInvalidBurgerIds()
      throws IOException {
    Path pathToUploadFile = Path.of("./orders_to_upload_test.json");

    if (Files.exists(pathToUploadFile)) {
      Files.delete(pathToUploadFile);
    }

    OrderRequest orderRequest1 = new OrderRequest();
    orderRequest1.setBurgerIds(List.of(111L, 222L, 333L));

    List<Long> burgerIds2 = createTestBurgers();
    OrderRequest orderRequest2 = new OrderRequest();
    orderRequest2.setBurgerIds(burgerIds2);

    List<Long> burgerIds3 = createTestBurgers();
    OrderRequest orderRequest3 = new OrderRequest();
    orderRequest3.setBurgerIds(List.of(burgerIds3.getFirst(), 444L, burgerIds3.getLast()));

    List<OrderRequest> orderRequests = List.of(orderRequest1, orderRequest2, orderRequest3);

    ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
    String json = writer.writeValueAsString(orderRequests);

    FileUtils.writeStringToFile(pathToUploadFile.toFile(), json, StandardCharsets.UTF_8);

    return pathToUploadFile.toFile();
  }

  // createOrder

  @Test
  @WithMockUser
  public void createOrder_withExistingBurgerIds() {
    List<Long> testBurgers = createTestBurgers();

    OrderResponse createdOrder = createOrderAndValidate(testBurgers);
  }

  @Test
  @WithMockUser
  public void createOrder_withNotExistingBurgerIds() {
    List<Long> testBurgers = List.of(777L, 888L, 999L);

    ResponseEntity<String> badResponse = createOrder_expectError(testBurgers);

    assertThat(badResponse.getStatusCode()).isNotNull();
    assertThat(badResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    assertThat(badResponse.getBody()).isNotNull();
    assertThat(badResponse.getBody()).contains("not found in database");
  }

  @Test
  @WithMockUser
  public void createOrder_withOnlyOneOfThreeExistingBurgerIds() {
    List<Long> testBurgers = createTestBurgers();

    testBurgers.set(1, 888L);

    ResponseEntity<String> badResponse = createOrder_expectError(testBurgers);

    assertThat(badResponse.getStatusCode()).isNotNull();
    assertThat(badResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    assertThat(badResponse.getBody()).isNotNull();
    assertThat(badResponse.getBody()).contains("not found in database");
  }

  @Test
  @WithMockUser
  public void createOrder_withEmptyBurgerIdsList() {
    ResponseEntity<String> badResponse = createOrder_expectError(List.of());

    assertThat(badResponse.getStatusCode()).isNotNull();
    assertThat(badResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  @WithMockUser
  public void createOrder_withNullBurgerIdsList() {
    ResponseEntity<String> badResponse = createOrder_expectError(null);

    assertThat(badResponse.getStatusCode()).isNotNull();
    assertThat(badResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  // findOrder

  @Test
  @WithMockUser
  public void findOrder_existingOrderId() {
    List<Long> testBurgers = createTestBurgers();

    // it will also check if the order exists actually in repository
    OrderResponse createdOrder = createOrderAndValidate(testBurgers);

    ResponseEntity<OrderResponse> response = testRestTemplate.getForEntity(
        "/api/order/" + createdOrder.getId(),
        OrderResponse.class);

    assertThat(response.getStatusCode()).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getBody()).isNotNull();

    OrderResponse foundOrder = response.getBody();

    assertThat(foundOrder.getId()).isNotNull();
    assertThat(foundOrder.getId()).isEqualTo(createdOrder.getId());

    assertThat(foundOrder.getCreatedAt()).isNotNull();
    assertThat(foundOrder.getCreatedAt()).isEqualTo(createdOrder.getCreatedAt());

    assertThat(foundOrder.getBurgers()).isNotNull();
    assertThat(foundOrder.getBurgers()).isEqualTo(createdOrder.getBurgers());
  }

  @Test
  @WithMockUser
  public void findOrder_notExistingOrderId() {
    ResponseEntity<String> badResponse = testRestTemplate.getForEntity(
        "/api/order/8888",
        String.class);

    assertThat(badResponse.getStatusCode()).isNotNull();
    assertThat(badResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    assertThat(badResponse.getBody()).isNotNull();
    assertThat(badResponse.getBody()).contains("not found");
  }

  // updateOrder

  @Test
  @WithMockUser
  public void updateOrder_existingOrderId_existingNewBurgerIds() {
    OrderResponse createdOrder = createTestOrder();

    List<Long> burgerIdsToUpdate = createTestBurgers();

    OrderRequest request = new OrderRequest();
    request.setBurgerIds(burgerIdsToUpdate);

    ResponseEntity<OrderResponse> response = testRestTemplate.exchange(
        "/api/order/" + createdOrder.getId(),
        HttpMethod.PUT,
        new HttpEntity<>(request),
        OrderResponse.class);

    assertThat(response.getStatusCode()).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();

    OrderResponse updatedOrder = response.getBody();

    assertThat(updatedOrder.getId()).isNotNull();
    assertThat(updatedOrder.getId()).isEqualTo(createdOrder.getId());

    assertThat(updatedOrder.getCreatedAt()).isNotNull();
    assertThat(updatedOrder.getCreatedAt()).isEqualTo(createdOrder.getCreatedAt());

    assertThat(updatedOrder.getBurgers()).isNotNull();

    List<Long> updatedBurgerIds = updatedOrder.getBurgers().stream().map(Burger::getId).toList();

    assertThat(updatedBurgerIds).isEqualTo(burgerIdsToUpdate);
  }

  @Test
  @WithMockUser
  public void updateOrder_existingOrderId_notExistingNewBurgerIds() {
    OrderResponse createdOrder = createTestOrder();

    List<Long> burgerIdsToUpdate = List.of(7777L, 8888L, 9999L);

    OrderRequest request = new OrderRequest();
    request.setBurgerIds(burgerIdsToUpdate);

    ResponseEntity<String> badResponse = testRestTemplate.exchange(
        "/api/order/" + createdOrder.getId(),
        HttpMethod.PUT,
        new HttpEntity<>(request),
        String.class);

    assertThat(badResponse.getStatusCode()).isNotNull();
    assertThat(badResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    assertThat(badResponse.getBody()).isNotNull();
    assertThat(badResponse.getBody()).contains("not found");
  }

  @Test
  @WithMockUser
  public void updateOrder_notExistingOrderId_existingNewBurgerIds() {
    List<Long> burgerIdsToUpdate = createTestBurgers();

    OrderRequest request = new OrderRequest();
    request.setBurgerIds(burgerIdsToUpdate);

    ResponseEntity<String> badResponse = testRestTemplate.exchange(
        "/api/order/" + 8888L,
        HttpMethod.PUT,
        new HttpEntity<>(request),
        String.class);

    assertThat(badResponse.getStatusCode()).isNotNull();
    assertThat(badResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    assertThat(badResponse.getBody()).isNotNull();
    assertThat(badResponse.getBody()).contains("not found");
  }

  @Test
  @WithMockUser
  public void updateOrder_notExistingOrderId_notExistingNewBurgerIds() {
    List<Long> burgerIdsToUpdate = List.of(7777L, 8888L, 9999L);

    OrderRequest request = new OrderRequest();
    request.setBurgerIds(burgerIdsToUpdate);

    ResponseEntity<String> badResponse = testRestTemplate.exchange(
        "/api/order/" + 8888L,
        HttpMethod.PUT,
        new HttpEntity<>(request),
        String.class);

    assertThat(badResponse.getStatusCode()).isNotNull();
    assertThat(badResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    assertThat(badResponse.getBody()).isNotNull();
    assertThat(badResponse.getBody()).contains("not found");
  }

  // deleteOrder

  @Test
  @WithMockUser
  public void deleteOrder_existingOrderId() {
    OrderResponse createdOrder = createTestOrder();

    ResponseEntity<String> response = testRestTemplate.exchange(
        "/api/order/" + createdOrder.getId(),
        HttpMethod.DELETE,
        null,
        String.class);

    assertThat(response.getStatusCode()).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    assertThat(response.getBody()).isNull();
  }

  @Test
  @WithMockUser
  public void deleteOrder_notExistingOrderId() {
    ResponseEntity<String> badResponse = testRestTemplate.exchange(
        "/api/order/8888",
        HttpMethod.DELETE,
        null,
        String.class);

    assertThat(badResponse.getStatusCode()).isNotNull();
    assertThat(badResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    assertThat(badResponse.getBody()).isNotNull();
    assertThat(badResponse.getBody()).contains("not found");
  }

  // getPaginatedOrders

  @Test
  @WithMockUser
  public void getPaginatedOrders_defaultPageable() {
    for (int i = 0; i < 15; ++i) {
      createTestOrder();
    }

    ResponseEntity<Map<String, Object>> response = testRestTemplate.exchange(
        "/api/order/_list",
        HttpMethod.POST,
        null,
        new ParameterizedTypeReference<Map<String, Object>>() {
        });

    assertThat(response.getStatusCode()).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getBody()).isNotNull();
    Map<String, Object> pageResponse = response.getBody();

    assertThat(pageResponse.get("content")).isNotNull();
    assertThat(pageResponse.get("totalElements")).isEqualTo(15);
    assertThat(pageResponse.get("totalPages")).isEqualTo(2);
    assertThat(pageResponse.get("size")).isEqualTo(10);
    assertThat(pageResponse.get("number")).isEqualTo(0);
  }

  @Test
  @WithMockUser
  public void getPaginatedOrders_customPageable() {
    for (int i = 0; i < 25; ++i) {
      createTestOrder();
    }

    ResponseEntity<Map<String, Object>> response = testRestTemplate.exchange(
        "/api/order/_list?page=1&size=5&sort=createdAt,desc",
        HttpMethod.POST,
        null,
        new ParameterizedTypeReference<Map<String, Object>>() {
        });

    assertThat(response.getStatusCode()).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getBody()).isNotNull();
    Map<String, Object> pageResponse = response.getBody();

    assertThat(pageResponse.get("totalElements")).isEqualTo(25);
    assertThat(pageResponse.get("totalPages")).isEqualTo(5);
    assertThat(pageResponse.get("size")).isEqualTo(5);
    assertThat(pageResponse.get("number")).isEqualTo(1);
  }

  // downloadReport

  @Test
  @WithMockUser
  public void downloadReport_withValidFilter_fromToDate() {
    OrderResponse orderResponse1 = createTestOrder();
    OrderResponse orderResponse2 = createTestOrder();
    OrderResponse orderResponse3 = createTestOrder();

    FilterCriteriaRequest filter = new FilterCriteriaRequest();
    filter.setOrderCreatedAtFrom(Instant.now().minus(1, ChronoUnit.DAYS));
    filter.setOrderCreatedAtTo(Instant.now().plus(1, ChronoUnit.DAYS));

    ResponseEntity<byte[]> response = testRestTemplate.exchange(
        "/api/order/_report",
        HttpMethod.POST,
        new HttpEntity<>(filter),
        byte[].class);

    assertThat(response.getStatusCode()).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getHeaders()).isNotNull();

    assertThat(response.getHeaders().getContentType()).isNotNull();
    assertThat(response.getHeaders().getContentType()).isEqualTo(
        MediaType.parseMediaType("text/csv"));

    assertThat(response.getHeaders().getContentDisposition()).isNotNull();

    assertThat(response.getHeaders().getContentDisposition().getFilename()).isNotNull();
    assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo(
        "orders_report.csv");

    assertThat(response.getBody()).isNotNull();
    String csvContent = new String(response.getBody(), StandardCharsets.UTF_8);

    assertThat(csvContent).contains("id");
    assertThat(csvContent).contains("createdAt");
    assertThat(csvContent).contains("burgers");

    assertThat(csvContent).contains(orderResponse1.getId().toString());
    assertThat(csvContent).contains(orderResponse2.getId().toString());
    assertThat(csvContent).contains(orderResponse3.getId().toString());
  }

  @Test
  @WithMockUser
  public void downloadReport_withValidFilter_onlyFromDate() {
    OrderResponse orderResponse1 = createTestOrder();
    OrderResponse orderResponse2 = createTestOrder();
    OrderResponse orderResponse3 = createTestOrder();

    FilterCriteriaRequest filter = new FilterCriteriaRequest();
    filter.setOrderCreatedAtFrom(Instant.now().minus(1, ChronoUnit.DAYS));

    ResponseEntity<byte[]> response = testRestTemplate.exchange(
        "/api/order/_report",
        HttpMethod.POST,
        new HttpEntity<>(filter),
        byte[].class);

    assertThat(response.getStatusCode()).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getHeaders()).isNotNull();

    assertThat(response.getHeaders().getContentType()).isNotNull();
    assertThat(response.getHeaders().getContentType()).isEqualTo(
        MediaType.parseMediaType("text/csv"));

    assertThat(response.getHeaders().getContentDisposition()).isNotNull();

    assertThat(response.getHeaders().getContentDisposition().getFilename()).isNotNull();
    assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo(
        "orders_report.csv");

    assertThat(response.getBody()).isNotNull();
    String csvContent = new String(response.getBody(), StandardCharsets.UTF_8);

    assertThat(csvContent).contains("id");
    assertThat(csvContent).contains("createdAt");
    assertThat(csvContent).contains("burgers");

    assertThat(csvContent).contains(orderResponse1.getId().toString());
    assertThat(csvContent).contains(orderResponse2.getId().toString());
    assertThat(csvContent).contains(orderResponse3.getId().toString());
  }

  @Test
  @WithMockUser
  public void downloadReport_withValidFilter_onlyToDate() {
    OrderResponse orderResponse1 = createTestOrder();
    OrderResponse orderResponse2 = createTestOrder();
    OrderResponse orderResponse3 = createTestOrder();

    FilterCriteriaRequest filter = new FilterCriteriaRequest();
    filter.setOrderCreatedAtTo(Instant.now().plus(1, ChronoUnit.DAYS));

    ResponseEntity<byte[]> response = testRestTemplate.exchange(
        "/api/order/_report",
        HttpMethod.POST,
        new HttpEntity<>(filter),
        byte[].class);

    assertThat(response.getStatusCode()).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getHeaders()).isNotNull();

    assertThat(response.getHeaders().getContentType()).isNotNull();
    assertThat(response.getHeaders().getContentType()).isEqualTo(
        MediaType.parseMediaType("text/csv"));

    assertThat(response.getHeaders().getContentDisposition()).isNotNull();

    assertThat(response.getHeaders().getContentDisposition().getFilename()).isNotNull();
    assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo(
        "orders_report.csv");

    assertThat(response.getBody()).isNotNull();
    String csvContent = new String(response.getBody(), StandardCharsets.UTF_8);

    assertThat(csvContent).contains("id");
    assertThat(csvContent).contains("createdAt");
    assertThat(csvContent).contains("burgers");

    assertThat(csvContent).contains(orderResponse1.getId().toString());
    assertThat(csvContent).contains(orderResponse2.getId().toString());
    assertThat(csvContent).contains(orderResponse3.getId().toString());
  }

  @Test
  @WithMockUser
  public void downloadReport_withValidFilter_ordersIncludeConcreteBurgerName() {
    OrderResponse orderResponse1 = createTestOrder();
    OrderResponse orderResponse2 = createTestOrder();
    OrderResponse orderResponse3 = createTestOrder();

    FilterCriteriaRequest filter = new FilterCriteriaRequest();
    filter.setBurgerName(orderResponse2.getBurgers().get(1).getName());

    ResponseEntity<byte[]> response = testRestTemplate.exchange(
        "/api/order/_report",
        HttpMethod.POST,
        new HttpEntity<>(filter),
        byte[].class);

    assertThat(response.getStatusCode()).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getHeaders()).isNotNull();

    assertThat(response.getHeaders().getContentType()).isNotNull();
    assertThat(response.getHeaders().getContentType()).isEqualTo(
        MediaType.parseMediaType("text/csv"));

    assertThat(response.getHeaders().getContentDisposition()).isNotNull();

    assertThat(response.getHeaders().getContentDisposition().getFilename()).isNotNull();
    assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo(
        "orders_report.csv");

    assertThat(response.getBody()).isNotNull();
    String csvContent = new String(response.getBody(), StandardCharsets.UTF_8);

    assertThat(csvContent).contains("id");
    assertThat(csvContent).contains("createdAt");
    assertThat(csvContent).contains("burgers");

    assertThat(csvContent).contains(orderResponse2.getId().toString());
    assertThat(csvContent).contains(orderResponse2.getBurgers().get(1).getName());
  }

  @Test
  @WithMockUser
  public void downloadReport_withEmptyResult_onlyFromDate() {
    OrderResponse orderResponse1 = createTestOrder();
    OrderResponse orderResponse2 = createTestOrder();
    OrderResponse orderResponse3 = createTestOrder();

    FilterCriteriaRequest filter = new FilterCriteriaRequest();
    filter.setOrderCreatedAtFrom(Instant.now().plus(1, ChronoUnit.DAYS));

    ResponseEntity<byte[]> response = testRestTemplate.exchange(
        "/api/order/_report",
        HttpMethod.POST,
        new HttpEntity<>(filter),
        byte[].class);

    assertThat(response.getStatusCode()).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getHeaders()).isNotNull();

    assertThat(response.getHeaders().getContentType()).isNotNull();
    assertThat(response.getHeaders().getContentType()).isEqualTo(
        MediaType.parseMediaType("text/csv"));

    assertThat(response.getHeaders().getContentDisposition()).isNotNull();

    assertThat(response.getHeaders().getContentDisposition().getFilename()).isNotNull();
    assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo(
        "orders_report.csv");

    assertThat(response.getBody()).isNotNull();
    String csvContent = new String(response.getBody(), StandardCharsets.UTF_8);

    assertThat(csvContent).contains("id");
    assertThat(csvContent).contains("createdAt");
    assertThat(csvContent).contains("burgers");

    assertThat(csvContent).doesNotContain(orderResponse1.getId().toString());
    assertThat(csvContent).doesNotContain(orderResponse2.getId().toString());
    assertThat(csvContent).doesNotContain(orderResponse3.getId().toString());
  }

  @Test
  @WithMockUser
  public void downloadReport_withEmptyResult_onlyToDate() {
    OrderResponse orderResponse1 = createTestOrder();
    OrderResponse orderResponse2 = createTestOrder();
    OrderResponse orderResponse3 = createTestOrder();

    FilterCriteriaRequest filter = new FilterCriteriaRequest();
    filter.setOrderCreatedAtTo(Instant.now().minus(1, ChronoUnit.DAYS));

    ResponseEntity<byte[]> response = testRestTemplate.exchange(
        "/api/order/_report",
        HttpMethod.POST,
        new HttpEntity<>(filter),
        byte[].class);

    assertThat(response.getStatusCode()).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getHeaders()).isNotNull();

    assertThat(response.getHeaders().getContentType()).isNotNull();
    assertThat(response.getHeaders().getContentType()).isEqualTo(
        MediaType.parseMediaType("text/csv"));

    assertThat(response.getHeaders().getContentDisposition()).isNotNull();

    assertThat(response.getHeaders().getContentDisposition().getFilename()).isNotNull();
    assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo(
        "orders_report.csv");

    assertThat(response.getBody()).isNotNull();
    String csvContent = new String(response.getBody(), StandardCharsets.UTF_8);

    assertThat(csvContent).contains("id");
    assertThat(csvContent).contains("createdAt");
    assertThat(csvContent).contains("burgers");

    assertThat(csvContent).doesNotContain(orderResponse1.getId().toString());
    assertThat(csvContent).doesNotContain(orderResponse2.getId().toString());
    assertThat(csvContent).doesNotContain(orderResponse3.getId().toString());
  }

  @Test
  @WithMockUser
  public void downloadReport_withEmptyResult_ordersIncludeConcreteBurgerName() {
    OrderResponse orderResponse1 = createTestOrder();
    OrderResponse orderResponse2 = createTestOrder();
    OrderResponse orderResponse3 = createTestOrder();

    FilterCriteriaRequest filter = new FilterCriteriaRequest();
    filter.setBurgerName("pokemon");

    ResponseEntity<byte[]> response = testRestTemplate.exchange(
        "/api/order/_report",
        HttpMethod.POST,
        new HttpEntity<>(filter),
        byte[].class);

    assertThat(response.getStatusCode()).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getHeaders()).isNotNull();

    assertThat(response.getHeaders().getContentType()).isNotNull();
    assertThat(response.getHeaders().getContentType()).isEqualTo(
        MediaType.parseMediaType("text/csv"));

    assertThat(response.getHeaders().getContentDisposition()).isNotNull();

    assertThat(response.getHeaders().getContentDisposition().getFilename()).isNotNull();
    assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo(
        "orders_report.csv");

    assertThat(response.getBody()).isNotNull();
    String csvContent = new String(response.getBody(), StandardCharsets.UTF_8);

    assertThat(csvContent).contains("id");
    assertThat(csvContent).contains("createdAt");
    assertThat(csvContent).contains("burgers");

    assertThat(csvContent).doesNotContain(orderResponse1.getId().toString());
    assertThat(csvContent).doesNotContain(orderResponse2.getId().toString());
    assertThat(csvContent).doesNotContain(orderResponse3.getId().toString());
  }

  @Test
  @WithMockUser
  public void downloadReport_withNullFilter() {
    OrderResponse orderResponse1 = createTestOrder();
    OrderResponse orderResponse2 = createTestOrder();
    OrderResponse orderResponse3 = createTestOrder();

    ResponseEntity<byte[]> response = testRestTemplate.exchange(
        "/api/order/_report",
        HttpMethod.POST,
        new HttpEntity<>(null),
        byte[].class);

    assertThat(response.getStatusCode()).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  // uploadOrders

  @Test
  @WithMockUser
  public void uploadOrders_validJson() throws IOException {
    File ordersToUploadFile = createTestJsonFileWithOrdersToUpload();

    try (InputStream fileInputStream = new FileInputStream(ordersToUploadFile)) {
      InputStreamResource resource = new InputStreamResource(fileInputStream);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
      headers.setContentLength(ordersToUploadFile.length());

      ResponseEntity<UploadStatsResponse> response = testRestTemplate.exchange(
          "/api/order/upload",
          HttpMethod.POST,
          new HttpEntity<InputStreamResource>(resource, headers),
          UploadStatsResponse.class);

      assertThat(response.getStatusCode()).isNotNull();
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

      assertThat(response.getBody()).isNotNull();

      UploadStatsResponse stats = response.getBody();

      assertThat(stats.getTotalRecords()).as("getTotalRecords").isNotNull();
      assertThat(stats.getTotalRecords()).as("getTotalRecords").isEqualTo(3);

      assertThat(stats.getSuccessfulCount()).as("getSuccessfulCount").isNotNull();
      assertThat(stats.getSuccessfulCount()).as("getSuccessfulCount").isEqualTo(3);

      assertThat(stats.getFailedCount()).as("getFailedCount").isNotNull();
      assertThat(stats.getFailedCount()).as("getFailedCount").isEqualTo(0);
    }
  }

  @Test
  @WithMockUser
  public void uploadOrders_invalidJson_invalidBurgerIds() throws IOException {
    File ordersToUploadFile = createTestJsonFileWithOrdersToUpload_withInvalidBurgerIds();

    try (InputStream fileInputStream = new FileInputStream(ordersToUploadFile)) {
      InputStreamResource resource = new InputStreamResource(fileInputStream);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
      headers.setContentLength(ordersToUploadFile.length());

      ResponseEntity<UploadStatsResponse> response = testRestTemplate.exchange(
          "/api/order/upload",
          HttpMethod.POST,
          new HttpEntity<InputStreamResource>(resource, headers),
          UploadStatsResponse.class);

      assertThat(response.getStatusCode()).isNotNull();
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

      assertThat(response.getBody()).isNotNull();

      UploadStatsResponse stats = response.getBody();

      assertThat(stats.getTotalRecords()).as("getTotalRecords").isNotNull();
      assertThat(stats.getTotalRecords()).as("getTotalRecords").isEqualTo(3);

      assertThat(stats.getSuccessfulCount()).as("getSuccessfulCount").isNotNull();
      assertThat(stats.getSuccessfulCount()).as("getSuccessfulCount").isEqualTo(0);

      assertThat(stats.getFailedCount()).as("getFailedCount").isNotNull();
      assertThat(stats.getFailedCount()).as("getFailedCount").isEqualTo(3);
    }
  }

  @Test
  @WithMockUser
  public void uploadOrders_invalidJson_mixedValidAndInvalidBurgerIds() throws IOException {
    File ordersToUploadFile = createTestJsonFileWithOrdersToUpload_withMixedValidAndInvalidBurgerIds();

    try (InputStream fileInputStream = new FileInputStream(ordersToUploadFile)) {
      InputStreamResource resource = new InputStreamResource(fileInputStream);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
      headers.setContentLength(ordersToUploadFile.length());

      ResponseEntity<UploadStatsResponse> response = testRestTemplate.exchange(
          "/api/order/upload",
          HttpMethod.POST,
          new HttpEntity<InputStreamResource>(resource, headers),
          UploadStatsResponse.class);

      assertThat(response.getStatusCode()).isNotNull();
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

      assertThat(response.getBody()).isNotNull();

      UploadStatsResponse stats = response.getBody();

      assertThat(stats.getTotalRecords()).as("getTotalRecords").isNotNull();
      assertThat(stats.getTotalRecords()).as("getTotalRecords").isEqualTo(3);

      assertThat(stats.getSuccessfulCount()).as("getSuccessfulCount").isNotNull();
      assertThat(stats.getSuccessfulCount()).as("getSuccessfulCount").isEqualTo(1);

      assertThat(stats.getFailedCount()).as("getFailedCount").isNotNull();
      assertThat(stats.getFailedCount()).as("getFailedCount").isEqualTo(2);
    }
  }

  @Test
  @WithMockUser
  public void uploadOrders_invalidJson_emptyJsonList() throws IOException {
    Path pathToUploadFile = Path.of("empty_orders_list_test.json");

    if (Files.exists(pathToUploadFile)) {
      Files.delete(pathToUploadFile);
    }

    File ordersToUploadFile = Files.createFile(pathToUploadFile).toFile();

    try (InputStream fileInputStream = new FileInputStream(ordersToUploadFile)) {
      InputStreamResource resource = new InputStreamResource(fileInputStream);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
      headers.setContentLength(ordersToUploadFile.length());

      ResponseEntity<UploadStatsResponse> response = testRestTemplate.exchange(
          "/api/order/upload",
          HttpMethod.POST,
          new HttpEntity<InputStreamResource>(resource, headers),
          UploadStatsResponse.class);

      assertThat(response.getStatusCode()).isNotNull();
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

      assertThat(response.getBody()).isNotNull();

      UploadStatsResponse stats = response.getBody();

      assertThat(stats.getTotalRecords()).as("getTotalRecords").isNotNull();
      assertThat(stats.getTotalRecords()).as("getTotalRecords").isEqualTo(0);

      assertThat(stats.getSuccessfulCount()).as("getSuccessfulCount").isNotNull();
      assertThat(stats.getSuccessfulCount()).as("getSuccessfulCount").isEqualTo(0);

      assertThat(stats.getFailedCount()).as("getFailedCount").isNotNull();
      assertThat(stats.getFailedCount()).as("getFailedCount").isEqualTo(0);
    }
  }

  @Test
  @WithMockUser
  public void uploadOrders_invalidJson_withOutBody() throws IOException {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

    ResponseEntity<UploadStatsResponse> response = testRestTemplate.exchange(
        "/api/order/upload",
        HttpMethod.POST,
        new HttpEntity<InputStreamResource>(null, headers),
        UploadStatsResponse.class);

    assertThat(response.getStatusCode()).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getBody()).isNotNull();

    UploadStatsResponse stats = response.getBody();

    assertThat(stats.getTotalRecords()).as("getTotalRecords").isNotNull();
    assertThat(stats.getTotalRecords()).as("getTotalRecords").isEqualTo(0);

    assertThat(stats.getSuccessfulCount()).as("getSuccessfulCount").isNotNull();
    assertThat(stats.getSuccessfulCount()).as("getSuccessfulCount").isEqualTo(0);

    assertThat(stats.getFailedCount()).as("getFailedCount").isNotNull();
    assertThat(stats.getFailedCount()).as("getFailedCount").isEqualTo(0);
  }
}
