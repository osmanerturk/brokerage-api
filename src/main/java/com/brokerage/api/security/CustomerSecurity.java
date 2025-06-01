package com.brokerage.api.security;

import com.brokerage.api.model.Customer;
import com.brokerage.api.model.Order;
import com.brokerage.api.model.dto.OrderDTO;
import com.brokerage.api.repository.CustomerRepository;
import com.brokerage.api.service.base.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("customerSecurity")
@RequiredArgsConstructor
public class CustomerSecurity {

    private final OrderService orderService;
    private final CustomerRepository customerRepository;

    public boolean isCustomerSelf(String customerId, Authentication authentication) {
        String username = authentication.getName();
        Customer customer = customerRepository.findByUsername(username)
                .orElse(null);
        return customer != null && customer.getCustomerId().equals(customerId);
    }

    public boolean isCustomerOrderOwner(Long orderId, Authentication authentication) {
        String username = authentication.getName();
        Customer customer = customerRepository.findByUsername(username)
                .orElse(null);

        if (customer == null) {
            return false;
        }

        OrderDTO order = orderService.getOrderById(orderId);
        return order != null && order.getCustomerId().equals(customer.getCustomerId());
    }
}
