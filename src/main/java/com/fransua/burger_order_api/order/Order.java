package com.fransua.burger_order_api.order;

import com.fransua.burger_order_api.burger.Burger;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity(name = "orders")
@Table(name = "orders")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Order {

  @Id
  @EqualsAndHashCode.Include
  @SequenceGenerator(
      name = "order_id_sequence",
      sequenceName = "order_id_sequence",
      allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_id_sequence")
  private Long id;

  @NotNull private Instant createdAt;

  @NotEmpty
  @ManyToMany
  @JoinTable(
      name = "order_burgers",
      joinColumns =
          @JoinColumn(name = "order_id", foreignKey = @ForeignKey(name = "fk_order_burgers_order")),
      inverseJoinColumns =
          @JoinColumn(
              name = "burger_id",
              foreignKey = @ForeignKey(name = "fk_order_burgers_burger")))
  private List<Burger> burgers;
}
