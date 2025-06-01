package com.brokerage.api.service;

import com.brokerage.api.exceptions.AssetNotFoundException;
import com.brokerage.api.exceptions.InsufficientBalanceException;
import com.brokerage.api.exceptions.InvalidOrderStateException;
import com.brokerage.api.exceptions.OrderNotFoundException;
import com.brokerage.api.model.Order;
import com.brokerage.api.model.dto.AssetDTO;
import com.brokerage.api.model.dto.OrderDTO;
import com.brokerage.api.model.enums.OrderSide;
import com.brokerage.api.model.enums.OrderStatus;
import com.brokerage.api.repository.OrderRepository;
import com.brokerage.api.service.base.AssetService;
import com.brokerage.api.service.base.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final AssetService assetService;
    private final ObjectMapper objectMapper;

    @Transactional
    @Override
    public OrderDTO createOrder(OrderDTO order) {
        if (order.getOrderSide() == OrderSide.BUY) {
            AssetDTO tryAsset = assetService.getAsset(order.getCustomerId(), "TRY")
                    .orElseThrow(() -> new AssetNotFoundException("TRY asset not found"));
            double totalCost = order.getSize() * order.getPrice();
            if (tryAsset.getUsableSize() < totalCost) {
                throw new InsufficientBalanceException("Insufficient TRY balance");
            }
            tryAsset.setUsableSize(tryAsset.getUsableSize() - totalCost);
            assetService.save(tryAsset);
        } else {
            AssetDTO stockAsset = assetService.getAsset(order.getCustomerId(), order.getAssetName())
                    .orElseThrow(() -> new AssetNotFoundException("Asset not found: " + order.getAssetName()));
            if (stockAsset.getUsableSize() < order.getSize()) {
                throw new InsufficientBalanceException("Insufficient asset size");
            }
            stockAsset.setUsableSize(stockAsset.getUsableSize() - order.getSize());
            assetService.save(stockAsset);
        }

        order.setStatus(OrderStatus.PENDING);
        order.setCreateDate(LocalDateTime.now());
        order.setId(null);

        Order orderSave = objectMapper.convertValue(order,Order.class);
        orderRepository.save(orderSave);
        return order;
    }

    @Override
    public List<OrderDTO> getOrdersByCustomerAndDateRange(String customerId, LocalDateTime start, LocalDateTime end) {
        List<Order> orders = orderRepository.findByCustomerIdAndCreateDateBetween(customerId, start, end);

        return orders.stream()
                .map(order -> objectMapper.convertValue(order, OrderDTO.class))
                .collect(Collectors.toList());
    }


    @Override
    public List<OrderDTO> getOrdersByCustomerId(String customerId) {
        List<Order> orders = orderRepository.findByCustomerId(customerId);

        return orders.stream()
                .map(order -> objectMapper.convertValue(order, OrderDTO.class))
                .collect(Collectors.toList());
    }


    @Transactional
    @Override
    public boolean cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id " + orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Only PENDING orders can be canceled");
        }

        AssetDTO asset;
        if (order.getOrderSide() == OrderSide.BUY) {
            asset = assetService.getAsset(order.getCustomerId(), "TRY")
                    .orElseThrow(() -> new AssetNotFoundException("TRY asset not found for customer " + order.getCustomerId()));
            double refund = order.getSize() * order.getPrice();
            asset.setUsableSize(asset.getUsableSize() + refund);
        } else {
            asset = assetService.getAsset(order.getCustomerId(), order.getAssetName())
                    .orElseThrow(() -> new AssetNotFoundException("Asset not found: " + order.getAssetName()));
            asset.setUsableSize(asset.getUsableSize() + order.getSize());
        }

        assetService.save(asset);
        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);

        return true;
    }

    @Override
    public OrderDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id " + orderId));

        return objectMapper.convertValue(order, OrderDTO.class);
    }


    @Transactional
    @Override
    public void completeOrder(OrderDTO order) {
        String customerId = order.getCustomerId();
        String assetName = order.getAssetName();
        double size = order.getSize();
        double price = order.getPrice();

        if (order.getOrderSide() == OrderSide.BUY) {
            AssetDTO tryAsset = assetService.getAsset(customerId, "TRY")
                    .orElseThrow(() -> new AssetNotFoundException("TRY asset not found"));
            double totalCost = size * price;
            if (tryAsset.getSize() < totalCost) {
                throw new InsufficientBalanceException("TRY balance inconsistent");
            }
            tryAsset.setSize(tryAsset.getSize() - totalCost);
            assetService.save(tryAsset);

            AssetDTO stockAsset = assetService.getAsset(customerId, assetName)
                    .orElseGet(() -> {
                        AssetDTO newAsset = new AssetDTO();
                        newAsset.setCustomerId(customerId);
                        newAsset.setAssetName(assetName);
                        newAsset.setSize(0.0);
                        newAsset.setUsableSize(0.0);
                        return newAsset;
                    });

            stockAsset.setSize(stockAsset.getSize() + size);
            stockAsset.setUsableSize(stockAsset.getUsableSize() + size);

            assetService.save(stockAsset);


        } else { // SELL
            AssetDTO stockAsset = assetService.getAsset(customerId, assetName)
                    .orElseThrow(() -> new AssetNotFoundException("Asset not found: " + assetName));
            if (stockAsset.getSize() < size) {
                throw new InsufficientBalanceException("Stock balance inconsistent");
            }
            stockAsset.setSize(stockAsset.getSize() - size);
            assetService.save(stockAsset);

            AssetDTO tryAsset = assetService.getAsset(customerId, "TRY")
                    .orElseThrow(() -> new AssetNotFoundException("TRY asset not found"));
            double totalGain = size * price;
            tryAsset.setSize(tryAsset.getSize() + totalGain);
            assetService.save(tryAsset);
        }
        order.setStatus(OrderStatus.MATCHED);
        Order updatedOrder = objectMapper.convertValue(order,Order.class );

        orderRepository.save(updatedOrder);
    }


}
