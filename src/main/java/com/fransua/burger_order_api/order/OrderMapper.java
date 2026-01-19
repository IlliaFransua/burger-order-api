package com.fransua.burger_order_api.order;

import com.fransua.burger_order_api.burger.BurgerMapper;
import com.fransua.burger_order_api.order.dto.request.OrderRequest;
import com.fransua.burger_order_api.order.dto.response.OrderResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    uses = {BurgerMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

  Order toOrder(OrderRequest orderRequest);

  OrderResponse toResponse(Order order);

  List<OrderResponse> toResponseList(List<Order> orders);
}
