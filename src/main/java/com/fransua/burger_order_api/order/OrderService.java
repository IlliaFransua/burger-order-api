package com.fransua.burger_order_api.order;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fransua.burger_order_api.burger.Burger;
import com.fransua.burger_order_api.burger.BurgerRepository;
import com.fransua.burger_order_api.email.RabbitMQConfig;
import com.fransua.burger_order_api.exception.NotFoundResourceException;
import com.fransua.burger_order_api.exception.TechnicalFailureException;
import com.fransua.burger_order_api.order.dto.request.FilterCriteriaRequest;
import com.fransua.burger_order_api.order.dto.request.OrderCreatedEmailNotificationRequest;
import com.fransua.burger_order_api.order.dto.request.OrderRequest;
import com.fransua.burger_order_api.order.dto.response.OrderResponse;
import com.fransua.burger_order_api.order.dto.response.OrderResponseCsv;
import com.fransua.burger_order_api.order.dto.response.UploadStatsResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class OrderService {

  private final ObjectReader orderReader;
  private final OrderRepository orderRepository;
  private final BurgerRepository burgerRepository;
  private final OrderMapper orderMapper;
  private final RabbitTemplate rabbitTemplate;

  public OrderService(
      @Qualifier("orderReader") ObjectReader orderReader,
      OrderRepository orderRepository,
      BurgerRepository burgerRepository,
      OrderMapper orderMapper,
      RabbitTemplate rabbitTemplate) {
    this.orderReader = orderReader;
    this.orderRepository = orderRepository;
    this.burgerRepository = burgerRepository;
    this.orderMapper = orderMapper;
    this.rabbitTemplate = rabbitTemplate;
  }

  public OrderResponse createOrder(OrderRequest orderRequest) {
    List<Burger> foundBurgers = findAndValidateBurgers(orderRequest.getBurgerIds());

    Order order = orderMapper.toOrder(orderRequest);
    order.setBurgers(foundBurgers);
    order.setCreatedAt(Instant.now());

    Order savedOrder = orderRepository.save(order);

    OrderCreatedEmailNotificationRequest request =
        new OrderCreatedEmailNotificationRequest(
            "example@example.example",
            "New Order #" + savedOrder.getId(),
            "Order details: " + savedOrder.toString());

    rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_QUEUE_NAME, request);

    return orderMapper.toResponse(savedOrder);
  }

  private List<Burger> findAndValidateBurgers(List<Long> requestBurgerIds) {
    List<Burger> foundBurgers = burgerRepository.findAllByIdIn(requestBurgerIds);

    if (foundBurgers.size() != requestBurgerIds.size()) {
      throw new NotFoundResourceException(
          "One or more burger IDs are invalid or not found in database.");
    }

    return foundBurgers;
  }

  public OrderResponse findOrder(Long id) {
    Order foundOrder =
        orderRepository
            .findById(id)
            .orElseThrow(
                () -> new NotFoundResourceException("Order with ID '" + id + "' is not found."));
    return orderMapper.toResponse(foundOrder);
  }

  @Transactional
  public OrderResponse updateOrder(Long id, OrderRequest orderRequest) {
    List<Burger> foundBurgers = findAndValidateBurgers(orderRequest.getBurgerIds());

    Order foundOrder =
        orderRepository
            .findById(id)
            .orElseThrow(
                () -> new NotFoundResourceException("Order with ID '" + id + "' is not found."));
    foundOrder.setBurgers(foundBurgers);

    return orderMapper.toResponse(foundOrder);
  }

  public void deleteOrder(Long id) {
    Order orderToDelete =
        orderRepository
            .findById(id)
            .orElseThrow(
                () -> new NotFoundResourceException("Order with ID '" + id + "' is not found."));
    orderRepository.delete(orderToDelete);
  }

  public Page<OrderResponse> getPaginatedOrders(Pageable pageable) {
    boolean isSortingByTotalPrice =
        pageable.getSort().stream().anyMatch(order -> "totalPrice".equals(order.getProperty()));

    if (isSortingByTotalPrice) {
      return getPaginatedOrdersByTotalPrice(pageable);
    }

    Page<Order> orderPage = orderRepository.findAll(pageable);
    List<OrderResponse> responseContent = orderMapper.toResponseList(orderPage.getContent());
    return new PageImpl<>(responseContent, orderPage.getPageable(), orderPage.getTotalElements());
  }

  private Page<OrderResponse> getPaginatedOrdersByTotalPrice(Pageable pageable) {
    boolean isDesc =
        pageable.getSort().stream().map(Sort.Order::isDescending).findFirst().orElse(false);

    Pageable cleanPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

    Page<Long> idsPage = orderRepository.findIdsSotedByTotalBurgersPrice(isDesc, cleanPageable);

    if (idsPage.isEmpty()) {
      return Page.empty(pageable);
    }

    List<Order> fullOrders = orderRepository.findAllByIdIn(idsPage.getContent(), Sort.unsorted());

    List<OrderResponse> sortedFullOrders =
        idsPage.getContent().stream()
            .map(
                id ->
                    fullOrders.stream()
                        .filter(order -> order.getId().equals(id))
                        .findFirst()
                        .orElseThrow())
            .map(orderMapper::toResponse)
            .toList();

    return new PageImpl<>(sortedFullOrders, pageable, idsPage.getTotalElements());
  }

  @Transactional(readOnly = true)
  public void generateReport(FilterCriteriaRequest filter, OutputStream outputStream) {
    CsvMapper mapper = new CsvMapper();

    mapper.findAndRegisterModules();

    CsvSchema schema = mapper.schemaFor(OrderResponseCsv.class).withHeader();

    try (Stream<Order> orderStream =
            orderRepository.findOrdersByFilter(
                filter.getOrderCreatedAtFrom(),
                filter.getOrderCreatedAtTo(),
                filter.getBurgerName());
        SequenceWriter writer = mapper.writer(schema).writeValues(outputStream)) {

      orderStream.forEach(
          order -> {
            try {
              String burgers =
                  order.getBurgers().stream()
                      .map(Burger::toString)
                      .collect(Collectors.joining("; "));

              OrderResponseCsv csvResponse = new OrderResponseCsv();

              csvResponse.setId(order.getId());
              csvResponse.setCreatedAt(order.getCreatedAt());
              csvResponse.setBurgers(burgers);

              writer.write(csvResponse);
            } catch (IOException e) {
              throw new UncheckedIOException(e);
            }
          });

      outputStream.flush();

    } catch (UncheckedIOException e) {
      throw new TechnicalFailureException("Report streaming failed after start.", e);
    } catch (IOException e) {
      throw new TechnicalFailureException("Failed to initialize report resources.", e);
    }
  }

  @Transactional
  public UploadStatsResponse uploadOrders(InputStream inputStream) {
    UploadStatsResponse stats = new UploadStatsResponse();

    try (MappingIterator<OrderRequest> iterator = orderReader.readValues(inputStream)) {
      while (iterator.hasNext()) {
        try {
          OrderRequest orderToImport = iterator.next();
          createOrder(orderToImport);
          stats.incrementSuccessfulCount();

        } catch (RuntimeException e) {
          stats.incrementFailedCount();
          log.error("Parsing failed at record: {}", iterator.getCurrentLocation(), e);
        }
      }
    } catch (Exception e) {
      throw new TechnicalFailureException("Failed to import orders.", e);
    }

    return stats;
  }
}
