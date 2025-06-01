package com.brokerage.api.service.base;

import com.brokerage.api.model.Order;
import com.brokerage.api.model.dto.OrderDTO;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    @Transactional
    OrderDTO createOrder(OrderDTO order);

    List<OrderDTO> getOrdersByCustomerAndDateRange(String customerId, LocalDateTime start, LocalDateTime end);

    List<OrderDTO> getOrdersByCustomerId(String customerId);

    @Transactional
    boolean cancelOrder(Long orderId);

    OrderDTO getOrderById(Long orderId);

    @Transactional
    void completeOrder(OrderDTO order);
}
