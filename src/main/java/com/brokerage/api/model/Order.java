package com.brokerage.api.model;

import com.brokerage.api.model.enums.OrderSide;
import com.brokerage.api.model.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ORDERS")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String customerId;
    @Column
    private String assetName;
    @Column
    @Enumerated(EnumType.STRING)
    private OrderSide orderSide;
    @Column
    private Double size;
    @Column
    private Double price;
    @Column
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    @Column
    private LocalDateTime createDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id) &&
                Objects.equals(customerId, order.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, customerId);
    }

}
