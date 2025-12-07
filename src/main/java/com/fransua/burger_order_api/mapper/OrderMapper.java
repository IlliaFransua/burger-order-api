package com.fransua.burger_order_api.mapper;

import com.fransua.burger_order_api.dto.request.OrderRequest;
import com.fransua.burger_order_api.dto.response.OrderResponse;
import com.fransua.burger_order_api.entity.Order;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

  Order toOrder(OrderRequest orderRequest);

  OrderResponse toResponse(Order order);

  List<OrderResponse> toResponseList(List<Order> orders);
}
