package com.fransua.burger_order_api.service;

import com.fransua.burger_order_api.dto.request.BurgerRequest;
import com.fransua.burger_order_api.dto.response.BurgerResponse;
import com.fransua.burger_order_api.entity.Burger;
import com.fransua.burger_order_api.exception.DuplicateResourceException;
import com.fransua.burger_order_api.exception.NotFoundResourceException;
import com.fransua.burger_order_api.mapper.BurgerMapper;
import com.fransua.burger_order_api.repository.BurgerRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Service;

@Service
public class BurgerService {

  private final BurgerRepository burgerRepository;
  private final BurgerMapper burgerMapper;

  public BurgerService(BurgerRepository burgerRepository, BurgerMapper burgerMapper) {
    this.burgerRepository = burgerRepository;
    this.burgerMapper = burgerMapper;
  }

  public BurgerResponse createBurger(BurgerRequest burgerRequest) {
    if (burgerRepository.existsByName(burgerRequest.getName())) {
      throw new DuplicateResourceException(
          "Burger with name '" + burgerRequest.getName() + "' is already exists.");
    }
    Burger burger = burgerMapper.toBurger(burgerRequest);
    Burger savedBurger = burgerRepository.save(burger);
    return burgerMapper.toResponse(savedBurger);
  }

  public List<BurgerResponse> findAllBurgers() {
    Iterable<Burger> burgerIterable = burgerRepository.findAll();
    List<Burger> foundBurgers = StreamSupport.stream(burgerIterable.spliterator(), false).toList();
    return burgerMapper.toResponseList(foundBurgers);
  }

  @Transactional
  public BurgerResponse updateBurger(Long id, BurgerRequest burgerRequest) {
    Burger foundBurger = burgerRepository.findById(id)
        .orElseThrow(
            () -> new NotFoundResourceException("Burger with ID " + id + " is not found."));

    if (!foundBurger.getName().equals(burgerRequest.getName())) {
      if (burgerRepository.existsByName(burgerRequest.getName())) {
        throw new DuplicateResourceException(
            "Burger with name '" + burgerRequest.getName() + "' is already exists.");
      }
    }

    burgerMapper.updateBurgerFromRequest(burgerRequest, foundBurger);
    burgerRepository.save(foundBurger);

    return burgerMapper.toResponse(foundBurger);
  }

  @Transactional
  public void deleteBurger(Long id) {
    Burger burgerToDelete = burgerRepository.findById(id).orElseThrow(
        () -> new NotFoundResourceException("Burger with ID '" + id + "' is not found."));
    burgerRepository.delete(burgerToDelete);
  }
}
