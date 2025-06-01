package com.brokerage.api.repository;

import com.brokerage.api.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerIdAndCreateDateBetween(String customerId, LocalDateTime start, LocalDateTime end);
    List<Order> findByCustomerId(String customerId);
    Optional<Order> findById(Long id);
}