package com.fransua.burger_order_api.mapper;

import com.fransua.burger_order_api.dto.request.BurgerRequest;
import com.fransua.burger_order_api.dto.response.BurgerResponse;
import com.fransua.burger_order_api.entity.Burger;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BurgerMapper {

  Burger toBurger(BurgerRequest burgerRequest);

  BurgerResponse toResponse(Burger burger);

  List<BurgerResponse> toResponseList(List<Burger> burgers);

  void updateBurgerFromRequest(BurgerRequest burgerRequest, @MappingTarget Burger burger);
}
