package com.brokerage.api.controller;

import com.brokerage.api.model.dto.OrderDTO;
import com.brokerage.api.model.enums.OrderStatus;
import com.brokerage.api.service.base.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create-order")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderDTO order) {
        order.setCreateDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        OrderDTO savedOrder = orderService.createOrder(order);
        return ResponseEntity.ok(savedOrder);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("@customerSecurity.isCustomerSelf(#customerId, authentication) or hasRole('ADMIN')")
    public ResponseEntity<List<OrderDTO>> getOrdersByCustomer(@PathVariable String customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomerId(customerId));
    }

    @GetMapping("/customer/{customerId}/range")
    @PreAuthorize("@customerSecurity.isCustomerSelf(#customerId, authentication) or hasRole('ADMIN')")
    public ResponseEntity<List<OrderDTO>> getOrdersByDateRange(
            @PathVariable String customerId,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return ResponseEntity.ok(orderService.getOrdersByCustomerAndDateRange(customerId, start, end));
    }

    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('ADMIN') or @customerSecurity.isCustomerOrderOwner(#orderId, authentication)")
    public ResponseEntity<Boolean> cancelOrder(@PathVariable Long orderId) {
        boolean success = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(success);
    }

    @PostMapping("/{orderId}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> completeOrder(@PathVariable Long orderId) {
        OrderDTO order = orderService.getOrderById(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        if (order.getStatus() != com.brokerage.api.model.enums.OrderStatus.PENDING) {
            return ResponseEntity.badRequest().body("Only PENDING orders can be completed");
        }
        try {
            orderService.completeOrder(order);
            return ResponseEntity.ok("Order completed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to complete order: " + e.getMessage());
        }
    }
}
