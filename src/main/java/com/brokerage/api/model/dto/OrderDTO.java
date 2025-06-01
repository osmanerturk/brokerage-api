package com.brokerage.api.model.dto;

import com.brokerage.api.model.enums.OrderSide;
import com.brokerage.api.model.enums.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderDTO {
    private Long id;
    private String customerId;
    private String assetName;
    private OrderSide orderSide;
    private Double size;
    private Double price;
    private OrderStatus status;
    private LocalDateTime createDate;
}
