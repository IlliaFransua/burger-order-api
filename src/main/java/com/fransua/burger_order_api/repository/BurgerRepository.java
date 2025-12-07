package com.fransua.burger_order_api.repository;

import com.fransua.burger_order_api.entity.Burger;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BurgerRepository extends JpaRepository<Burger, Long> {

  boolean existsByName(String name);

  List<Burger> findAllByIdIn(List<Long> ids);
}
