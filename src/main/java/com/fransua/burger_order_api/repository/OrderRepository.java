package com.fransua.burger_order_api.repository;

import com.fransua.burger_order_api.entity.Order;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  @NonNull
  @Override
  @EntityGraph(attributePaths = { "burgers" })
  Optional<Order> findById(@NonNull Long id);

  @Query(value = "SELECT DISTINCT o.* FROM orders o "
      + "JOIN orders_burgers ob ON o.id = ob.order_id "
      + "JOIN burger b ON b.id = ob.burgers_id "
      + "WHERE (CAST(:orderCreatedAtFrom AS timestamp) IS NULL OR o.created_at >= CAST(:orderCreatedAtFrom AS timestamp)) "
      + "AND (CAST(:orderCreatedAtTo AS timestamp) IS NULL OR o.created_at <= CAST(:orderCreatedAtTo AS timestamp)) "
      + "AND (CAST(:burgerName AS text) IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', CAST(:burgerName AS text), '%')))", nativeQuery = true)
  Stream<Order> findOrdersByFilter(
      @Param("orderCreatedAtFrom") Instant orderCreatedAtFrom,
      @Param("orderCreatedAtTo") Instant orderCreatedAtTo,
      @Param("burgerName") String burgerName);

  @Query("SELECT o.id FROM Order o LEFT JOIN o.burgers b GROUP BY o.id " +
      "ORDER BY " +
      "CASE WHEN :isDesc = false THEN SUM(COALESCE(b.unitPrice, 0)) END ASC, " +
      "CASE WHEN :isDesc = true THEN SUM(COALESCE(b.unitPrice, 0)) END DESC")
  Page<Long> findIdsSotedByTotalBurgersPrice(@Param("isDesc") boolean isDesc, Pageable pageable);

  @EntityGraph(attributePaths = { "burgers" })
  List<Order> findAllByIdIn(List<Long> ids, Sort sort);
}
