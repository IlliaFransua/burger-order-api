package com.fransua.burger_order_api;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fransua.burger_order_api.burger.BurgerRepository;
import com.fransua.burger_order_api.burger.dto.request.BurgerRequest;
import com.fransua.burger_order_api.burger.dto.response.BurgerResponse;
import com.fransua.burger_order_api.order.OrderRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class BurgerControllerTest {

  @Autowired private TestRestTemplate testRestTemplate;
  @Autowired private BurgerRepository burgerRepository;
  @Autowired private OrderRepository orderRepository;
  private BurgerTestFactory burgerTestFactory;

  @BeforeEach
  public void setUp() {
    this.burgerTestFactory = new BurgerTestFactory(testRestTemplate);
    orderRepository.deleteAll();
    burgerRepository.deleteAll();
  }

  @Test
  @WithMockUser
  public void createBurger_twoBurgersWithDuplicateName_shouldCreateOnlyFirstBurger() {
    String equalName = "TestBurger_" + UUID.randomUUID().toString();
    BigDecimal price1 = new BigDecimal("7.6");
    BigDecimal price2 = new BigDecimal("15.8");

    ResponseEntity<BurgerResponse> burgerResponse1 =
        burgerTestFactory.createTestBurgerAndValidate(equalName, price1);
    Long newId1 = burgerResponse1.getBody().getId();

    assertThat(burgerResponse1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(burgerResponse1.getBody()).isNotNull();
    assertThat(burgerResponse1.getBody().getId()).isNotNull();
    assertThat(burgerResponse1.getBody().getId()).isEqualTo(newId1);
    assertThat(burgerResponse1.getBody().getName()).isNotNull();
    assertThat(burgerResponse1.getBody().getName()).isEqualTo(equalName);
    assertThat(burgerResponse1.getBody().getUnitPrice()).isNotNull();
    assertThat(burgerResponse1.getBody().getUnitPrice()).isEqualByComparingTo(price1);

    ResponseEntity<String> burgerResponse2 =
        burgerTestFactory.createTestBurgerExpectingError(equalName, price2);
    assertThat(burgerResponse2.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(burgerResponse2.getBody()).isNotNull();
    assertThat(burgerResponse2.getBody()).contains("is already exists");
  }

  @Test
  @WithMockUser
  public void createBurger_and_findAllBurgers_shouldFindAllCreatedBurgers() {
    String name1 = "TestBurger1_" + UUID.randomUUID().toString();
    BigDecimal price1 = new BigDecimal("7.6");

    String name2 = "TestBurger2_" + UUID.randomUUID().toString();
    BigDecimal price2 = new BigDecimal("15.8");

    BurgerResponse burgerResponse1 =
        burgerTestFactory.createTestBurgerAndValidate(name1, price1).getBody();
    Long newId1 = burgerResponse1.getId();

    BurgerResponse burgerResponse2 =
        burgerTestFactory.createTestBurgerAndValidate(name2, price2).getBody();
    Long newId2 = burgerResponse2.getId();

    ParameterizedTypeReference<List<BurgerResponse>> responseType =
        new ParameterizedTypeReference<>() {};

    ResponseEntity<List<BurgerResponse>> getResponse =
        testRestTemplate.exchange("/api/burger", HttpMethod.GET, null, responseType);

    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(getResponse.getBody()).isNotNull();
    List<BurgerResponse> allBurgers = getResponse.getBody();
    assertThat(allBurgers).isNotNull();

    BurgerResponse foundBurger1 =
        allBurgers.stream()
            .filter(burger -> name1.equals(burger.getName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Burger 1 not found."));

    BurgerResponse foundBurger2 =
        allBurgers.stream()
            .filter(burger -> name2.equals(burger.getName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Burger 2 not found."));

    assertThat(foundBurger1.getId()).isNotNull();
    assertThat(foundBurger1.getId()).isEqualTo(newId1);
    assertThat(foundBurger1.getName()).isNotNull();
    assertThat(foundBurger1.getName()).isEqualTo(name1);
    assertThat(foundBurger1.getUnitPrice()).isNotNull();
    assertThat(foundBurger1.getUnitPrice()).isEqualByComparingTo(price1);

    assertThat(foundBurger2.getId()).isNotNull();
    assertThat(foundBurger2.getId()).isEqualTo(newId2);
    assertThat(foundBurger2.getName()).isNotNull();
    assertThat(foundBurger2.getName()).isEqualTo(name2);
    assertThat(foundBurger2.getUnitPrice()).isNotNull();
    assertThat(foundBurger2.getUnitPrice()).isEqualByComparingTo(price2);
  }

  @Test
  @WithMockUser
  public void updateBurger_newNameButTheSamePrice_shouldSuccess() {
    String originName = "TestBurger1_" + UUID.randomUUID().toString();
    BigDecimal originPrice = new BigDecimal("7.6");

    String nameToUpdate = "TestBurger2_" + UUID.randomUUID().toString();

    BurgerResponse burgerResponse1 =
        burgerTestFactory.createTestBurgerAndValidate(originName, originPrice).getBody();
    Long newId = burgerResponse1.getId();

    BurgerRequest burgerRequest = new BurgerRequest();
    burgerRequest.setName(nameToUpdate);
    burgerRequest.setUnitPrice(originPrice);

    ResponseEntity<BurgerResponse> burgerResponse2 =
        testRestTemplate.exchange(
            "/api/burger/" + newId,
            HttpMethod.PUT,
            new HttpEntity<>(burgerRequest),
            BurgerResponse.class);

    assertThat(burgerResponse2.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(burgerResponse2.getBody()).isNotNull();
    assertThat(burgerResponse2.getBody().getId()).isNotNull();
    assertThat(burgerResponse2.getBody().getId()).isEqualTo(newId);
    assertThat(burgerResponse2.getBody().getName()).isNotNull();
    assertThat(burgerResponse2.getBody().getName()).isEqualTo(nameToUpdate);
    assertThat(burgerResponse2.getBody().getUnitPrice()).isNotNull();
    assertThat(burgerResponse2.getBody().getUnitPrice()).isEqualByComparingTo(originPrice);
  }

  @Test
  @WithMockUser
  public void updateBurger_newPriceButTheSameName_shouldSuccess() {
    String originName = "TestBurger1_" + UUID.randomUUID().toString();
    BigDecimal originPrice = new BigDecimal("7.6");

    BigDecimal priceToUpdate = new BigDecimal("15.8");

    BurgerResponse burgerResponse1 =
        burgerTestFactory.createTestBurgerAndValidate(originName, originPrice).getBody();
    Long newId = burgerResponse1.getId();

    BurgerRequest burgerRequest = new BurgerRequest();
    burgerRequest.setName(originName);
    burgerRequest.setUnitPrice(priceToUpdate);

    ResponseEntity<BurgerResponse> burgerResponse2 =
        testRestTemplate.exchange(
            "/api/burger/" + newId,
            HttpMethod.PUT,
            new HttpEntity<>(burgerRequest),
            BurgerResponse.class);

    assertThat(burgerResponse2.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(burgerResponse2.getBody()).isNotNull();
    assertThat(burgerResponse2.getBody().getId()).isNotNull();
    assertThat(burgerResponse2.getBody().getId()).isEqualTo(newId);
    assertThat(burgerResponse2.getBody().getName()).isNotNull();
    assertThat(burgerResponse2.getBody().getName()).isEqualTo(originName);
    assertThat(burgerResponse2.getBody().getUnitPrice()).isNotNull();
    assertThat(burgerResponse2.getBody().getUnitPrice()).isEqualByComparingTo(priceToUpdate);
  }

  @Test
  @WithMockUser
  public void updateBurger_newNameButTheNameIsAlreadyExists_shouldFail() {
    String name1 = "TestBurger1_" + UUID.randomUUID().toString();
    BigDecimal price1 = new BigDecimal("7.6");

    String name2 = "TestBurger2_" + UUID.randomUUID().toString();
    BigDecimal price2 = new BigDecimal("15.8");

    BurgerResponse burgerResponse1 =
        burgerTestFactory.createTestBurgerAndValidate(name1, price1).getBody();
    Long newId1 = burgerResponse1.getId();

    BurgerResponse burgerResponse2 =
        burgerTestFactory.createTestBurgerAndValidate(name2, price2).getBody();
    Long newId2 = burgerResponse2.getId();

    // we will try to update the first burger using the name of second burger that is already exists
    BurgerRequest burgerToUpdateRequest = new BurgerRequest();
    burgerToUpdateRequest.setName(name2);
    burgerToUpdateRequest.setUnitPrice(price1);

    ResponseEntity<String> burgerResponse =
        testRestTemplate.exchange(
            "/api/burger/" + newId1,
            HttpMethod.PUT,
            new HttpEntity<>(burgerToUpdateRequest),
            String.class);

    assertThat(burgerResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(burgerResponse.getBody()).isNotNull();
    assertThat(burgerResponse.getBody()).contains("is already exists");
  }

  @Test
  @WithMockUser
  public void updateBurger_idIsNotExists_shouldFail() {
    BurgerRequest burgerToUpdateRequest = new BurgerRequest();
    burgerToUpdateRequest.setName("TestBurgerName");
    burgerToUpdateRequest.setUnitPrice(new BigDecimal("8888.8888"));

    ResponseEntity<String> burgerResponse =
        testRestTemplate.exchange(
            "/api/burger/" + 8888,
            HttpMethod.PUT,
            new HttpEntity<>(burgerToUpdateRequest),
            String.class);

    assertThat(burgerResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(burgerResponse.getBody()).isNotNull();
    assertThat(burgerResponse.getBody()).contains("is not found");
  }

  @Test
  @WithMockUser
  public void deleteBurger_ifTheBurgerIsNotExists_shouldFail() {
    ResponseEntity<Void> response =
        testRestTemplate.exchange("/api/burger/8888", HttpMethod.DELETE, null, Void.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNull();
  }

  @Test
  @WithMockUser
  public void deleteBurger_ifTheBurgerIsExists_checkIfTheListOfBurgersIsEmpty_shouldSuccess() {
    String name1 = "TestBurger1_" + UUID.randomUUID().toString();
    BigDecimal price1 = new BigDecimal("7.6");

    BurgerResponse burgerResponse1 =
        burgerTestFactory.createTestBurgerAndValidate(name1, price1).getBody();
    Long newId1 = burgerResponse1.getId();

    ResponseEntity<Void> deleteBurgerResponse1 =
        testRestTemplate.exchange("/api/burger/" + newId1, HttpMethod.DELETE, null, Void.class);

    assertThat(deleteBurgerResponse1.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(deleteBurgerResponse1.getBody()).isNull();

    ParameterizedTypeReference<List<BurgerResponse>> responseType =
        new ParameterizedTypeReference<>() {};

    ResponseEntity<List<BurgerResponse>> findAllBurgersResponse =
        testRestTemplate.exchange("/api/burger", HttpMethod.GET, null, responseType);

    assertThat(findAllBurgersResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(findAllBurgersResponse.getBody()).isNotNull();
    List<BurgerResponse> foundBurgers = findAllBurgersResponse.getBody();
    assertThat(foundBurgers.size()).isEqualTo(0);
  }

  @Test
  @WithMockUser
  public void deleteBurger_createThreeBurgersAndCheckIfTheListOfBurgersContainsOnlyOne() {
    String name1 = "TestBurger1_" + UUID.randomUUID().toString();
    BigDecimal price1 = new BigDecimal("7.6");

    String name2 = "TestBurger2_" + UUID.randomUUID().toString();
    BigDecimal price2 = new BigDecimal("15.8");

    String name3 = "TestBurger3_" + UUID.randomUUID().toString();
    BigDecimal price3 = new BigDecimal("3.4");

    BurgerResponse burgerResponse1 =
        burgerTestFactory.createTestBurgerAndValidate(name1, price1).getBody();
    Long newId1 = burgerResponse1.getId();

    BurgerResponse burgerResponse2 =
        burgerTestFactory.createTestBurgerAndValidate(name2, price2).getBody();
    Long newId2 = burgerResponse2.getId();

    BurgerResponse burgerResponse3 =
        burgerTestFactory.createTestBurgerAndValidate(name3, price3).getBody();
    Long newId3 = burgerResponse3.getId();

    ResponseEntity<Void> deleteBurgerResponse1 =
        testRestTemplate.exchange("/api/burger/" + newId1, HttpMethod.DELETE, null, Void.class);

    assertThat(deleteBurgerResponse1.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(deleteBurgerResponse1.getBody()).isNull();

    ResponseEntity<Void> deleteBurgerResponse3 =
        testRestTemplate.exchange("/api/burger/" + newId3, HttpMethod.DELETE, null, Void.class);

    assertThat(deleteBurgerResponse3.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(deleteBurgerResponse3.getBody()).isNull();

    ParameterizedTypeReference<List<BurgerResponse>> responseType =
        new ParameterizedTypeReference<>() {};

    ResponseEntity<List<BurgerResponse>> findAllBurgersResponse =
        testRestTemplate.exchange("/api/burger", HttpMethod.GET, null, responseType);

    assertThat(findAllBurgersResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(findAllBurgersResponse.getBody()).isNotNull();
    List<BurgerResponse> foundBurgers = findAllBurgersResponse.getBody();
    assertThat(foundBurgers.size()).isEqualTo(1);

    BurgerResponse firstBurger = foundBurgers.get(0);
    assertThat(firstBurger.getId()).isEqualTo(burgerResponse2.getId());
    assertThat(firstBurger.getName()).isEqualTo(burgerResponse2.getName());
    assertThat(firstBurger.getUnitPrice()).isEqualByComparingTo(burgerResponse2.getUnitPrice());
  }
}
