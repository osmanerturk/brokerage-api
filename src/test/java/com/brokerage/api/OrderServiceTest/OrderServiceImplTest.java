package com.brokerage.api.OrderServiceTest;

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
import com.brokerage.api.service.OrderServiceImpl;
import com.brokerage.api.service.base.AssetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {

    private OrderRepository orderRepository;
    private AssetService assetService;
    private OrderServiceImpl orderService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        assetService = mock(AssetService.class);
        objectMapper = mock(ObjectMapper.class);
        orderService = new OrderServiceImpl(orderRepository, assetService, objectMapper);
    }

    // createOrder - BUY - sufficient TRY balance
    @Test
    void createOrder_buyOrder_sufficientTryBalance_success() {
        OrderDTO order = new OrderDTO();
        order.setCustomerId("cust1");
        order.setOrderSide(OrderSide.BUY);
        order.setSize(10.0);
        order.setPrice(5.0);

        AssetDTO tryAsset = new AssetDTO();
        tryAsset.setId(null);
        tryAsset.setCustomerId("cust1");
        tryAsset.setAssetName("TRY");
        tryAsset.setSize(1000.0);
        tryAsset.setUsableSize(1000.0);

        when(assetService.getAsset("cust1", "TRY")).thenReturn(Optional.of(tryAsset));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderDTO result = orderService.createOrder(order);

        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertNotNull(result.getCreateDate());
        assertEquals(1000.0 - 50.0, tryAsset.getUsableSize());
        verify(assetService).save(tryAsset);
        verify(orderRepository).save(objectMapper.convertValue(result, Order.class));
    }

    // createOrder - BUY - insufficient TRY balance
    @Test
    void createOrder_buyOrder_insufficientTryBalance_throws() {
        OrderDTO order = new OrderDTO();
        order.setCustomerId("cust1");
        order.setOrderSide(OrderSide.BUY);
        order.setSize(100.0);
        order.setPrice(20.0);

        AssetDTO tryAsset = new AssetDTO();
        tryAsset.setId(null);
        tryAsset.setCustomerId("cust1");
        tryAsset.setAssetName("TRY");
        tryAsset.setSize(1000.0);
        tryAsset.setUsableSize(1000.0);

        tryAsset.setUsableSize(1500.0); // Intentionally set higher usableSize but keep total size 1000.0 to simulate insufficient usable funds
        when(assetService.getAsset("cust1", "TRY")).thenReturn(Optional.of(tryAsset));

        InsufficientBalanceException ex = assertThrows(InsufficientBalanceException.class,
                () -> orderService.createOrder(order));
        assertEquals("Insufficient TRY balance", ex.getMessage());

        verify(assetService, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    // createOrder - BUY - TRY asset not found
    @Test
    void createOrder_buyOrder_tryAssetNotFound_throws() {
        OrderDTO order = new OrderDTO();
        order.setCustomerId("cust1");
        order.setOrderSide(OrderSide.BUY);
        order.setSize(10.0);
        order.setPrice(5.0);

        when(assetService.getAsset("cust1", "TRY")).thenReturn(Optional.empty());

        AssetNotFoundException ex = assertThrows(AssetNotFoundException.class,
                () -> orderService.createOrder(order));
        assertEquals("TRY asset not found", ex.getMessage());

        verify(assetService, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    // createOrder - SELL - sufficient asset size
    @Test
    void createOrder_sellOrder_sufficientAssetSize_success() {
        OrderDTO order = new OrderDTO();
        order.setCustomerId("cust1");
        order.setOrderSide(OrderSide.SELL);
        order.setAssetName("TRY");
        order.setSize(50.0);
        order.setPrice(10.0);

        AssetDTO asset = new AssetDTO();
        asset.setId(null);
        asset.setCustomerId("cust1");
        asset.setAssetName("TRY");
        asset.setSize(100.0);
        asset.setUsableSize(100.0);

        when(assetService.getAsset("cust1", "TRY")).thenReturn(Optional.of(asset));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderDTO result = orderService.createOrder(order);

        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertNotNull(result.getCreateDate());
        assertEquals(50.0, asset.getUsableSize());
        verify(assetService).save(asset);
        verify(orderRepository).save(objectMapper.convertValue(result, Order.class));

    }

    // createOrder - SELL - insufficient asset size
    @Test
    void createOrder_sellOrder_insufficientAssetSize_throws() {
        OrderDTO order = new OrderDTO();
        order.setCustomerId("cust1");
        order.setOrderSide(OrderSide.SELL);
        order.setAssetName("TRY");
        order.setSize(100.0);
        order.setPrice(5.0);

        AssetDTO asset = new AssetDTO();
        asset.setId(null);
        asset.setCustomerId("cust1");
        asset.setAssetName("TRY");
        asset.setSize(50.0);
        asset.setUsableSize(50.0);

        when(assetService.getAsset("cust1", "TRY")).thenReturn(Optional.of(asset));

        InsufficientBalanceException ex = assertThrows(InsufficientBalanceException.class,
                () -> orderService.createOrder(order));
        assertEquals("Insufficient asset size", ex.getMessage());

        verify(assetService, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    // createOrder - SELL - asset not found
    @Test
    void createOrder_sellOrder_assetNotFound_throws() {
        OrderDTO order = new OrderDTO();
        order.setCustomerId("cust1");
        order.setOrderSide(OrderSide.SELL);
        order.setAssetName("TRY");
        order.setSize(10.0);
        order.setPrice(5.0);

        when(assetService.getAsset("cust1", "TRY")).thenReturn(Optional.empty());

        AssetNotFoundException ex = assertThrows(AssetNotFoundException.class,
                () -> orderService.createOrder(order));
        assertEquals("Asset not found: TRY", ex.getMessage());

        verify(assetService, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    // cancelOrder - success for PENDING BUY order
    @Test
    void cancelOrder_pendingBuyOrder_success() {
        Order order = new Order();
        order.setId(1L);
        order.setCustomerId("cust1");
        order.setOrderSide(OrderSide.BUY);
        order.setSize(10.0);
        order.setPrice(5.0);
        order.setStatus(OrderStatus.PENDING);

        AssetDTO asset = new AssetDTO();
        asset.setId(null);
        asset.setCustomerId("cust1");
        asset.setAssetName("TRY");
        asset.setSize(1000.0);
        asset.setUsableSize(950.0);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(assetService.getAsset("cust1", "TRY")).thenReturn(Optional.of(asset));

        boolean canceled = orderService.cancelOrder(1L);

        assertTrue(canceled);
        assertEquals(OrderStatus.CANCELED, order.getStatus());
        assertEquals(950.0 + 50.0, asset.getUsableSize());
        verify(assetService).save(asset);
        verify(orderRepository).save(order);
    }

    // cancelOrder - success for PENDING SELL order
    @Test
    void cancelOrder_pendingSellOrder_success() {
        Order order = new Order();
        order.setId(1L);
        order.setCustomerId("cust1");
        order.setOrderSide(OrderSide.SELL);
        order.setAssetName("TRY");
        order.setSize(10.0);
        order.setPrice(5.0);
        order.setStatus(OrderStatus.PENDING);

        AssetDTO asset = new AssetDTO();
        asset.setId(null);
        asset.setCustomerId("cust1");
        asset.setAssetName("TRY");
        asset.setSize(50.0);
        asset.setUsableSize(40.0);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(assetService.getAsset("cust1", "TRY")).thenReturn(Optional.of(asset));

        boolean canceled = orderService.cancelOrder(1L);

        assertTrue(canceled);
        assertEquals(OrderStatus.CANCELED, order.getStatus());
        assertEquals(40.0 + 10.0, asset.getUsableSize());
        verify(assetService).save(asset);
        verify(orderRepository).save(order);
    }

    // cancelOrder - order not found
    @Test
    void cancelOrder_orderNotFound_throws() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        OrderNotFoundException ex = assertThrows(OrderNotFoundException.class,
                () -> orderService.cancelOrder(1L));
        assertEquals("Order not found with id 1", ex.getMessage());

        verify(assetService, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    // cancelOrder - invalid order status
    @Test
    void cancelOrder_invalidStatus_throws() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.MATCHED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        InvalidOrderStateException ex = assertThrows(InvalidOrderStateException.class,
                () -> orderService.cancelOrder(1L));
        assertEquals("Only PENDING orders can be canceled", ex.getMessage());

        verify(assetService, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    // getOrderById - not found
    @Test
    void getOrderById_notFound_throws() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        OrderNotFoundException ex = assertThrows(OrderNotFoundException.class,
                () -> orderService.getOrderById(1L));
        assertEquals("Order not found with id 1", ex.getMessage());
    }

    // completeOrder - BUY order success
    @Test
    void completeOrder_buyOrder_success() {
        OrderDTO order = new OrderDTO();
        order.setCustomerId("cust1");
        order.setOrderSide(OrderSide.BUY);
        order.setAssetName("TRY");
        order.setSize(10.0);
        order.setPrice(5.0);
        order.setStatus(OrderStatus.PENDING);

        AssetDTO tryAsset = new AssetDTO();
        tryAsset.setId(null);
        tryAsset.setCustomerId("cust1");
        tryAsset.setAssetName("TRY");
        tryAsset.setSize(1000.0);
        tryAsset.setUsableSize(1000.0);

        AssetDTO stockAsset = new AssetDTO();
        stockAsset.setId(null);
        stockAsset.setCustomerId("cust1");
        stockAsset.setAssetName("TRY");
        stockAsset.setSize(100.0);
        stockAsset.setUsableSize(100.0);

        when(assetService.getAsset("cust1", "TRY")).thenReturn(Optional.of(tryAsset));
        when(assetService.getAsset("cust1", "TRY")).thenReturn(Optional.of(stockAsset));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        when(assetService.getAsset("cust1", "TRY"))
                .thenReturn(Optional.of(tryAsset))
                .thenReturn(Optional.of(stockAsset));

        orderService.completeOrder(order);

        assertEquals(OrderStatus.MATCHED, order.getStatus());
        assertEquals(1000.0 - 50.0, tryAsset.getSize());
        assertEquals(100.0 + 10.0, stockAsset.getSize());

        verify(assetService, times(2)).save(any());
        verify(orderRepository).save(objectMapper.convertValue(order, Order.class));
    }

    // completeOrder - BUY order insufficient TRY balance
    @Test
    void completeOrder_buyOrder_insufficientTryBalance_throws() {
        OrderDTO order = new OrderDTO();
        order.setCustomerId("cust1");
        order.setOrderSide(OrderSide.BUY);
        order.setAssetName("TRY");
        order.setSize(10.0);
        order.setPrice(5.0);

        AssetDTO tryAsset = new AssetDTO();
        tryAsset.setId(null);
        tryAsset.setCustomerId("cust1");
        tryAsset.setAssetName("TRY");
        tryAsset.setSize(40.0);
        tryAsset.setUsableSize(40.0);

        when(assetService.getAsset("cust1", "TRY")).thenReturn(Optional.of(tryAsset));

        InsufficientBalanceException ex = assertThrows(InsufficientBalanceException.class,
                () -> orderService.completeOrder(order));
        assertEquals("TRY balance inconsistent", ex.getMessage());

        verify(assetService, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    // completeOrder - SELL order success
    @Test
    void completeOrder_sellOrder_success() {
        OrderDTO order = new OrderDTO();
        order.setCustomerId("cust1");
        order.setOrderSide(OrderSide.SELL);
        order.setAssetName("TRY");
        order.setSize(10.0);
        order.setPrice(5.0);
        order.setStatus(OrderStatus.PENDING);

        AssetDTO asset = new AssetDTO();
        asset.setId(null);
        asset.setCustomerId("cust1");
        asset.setAssetName("TRY");
        asset.setSize(100.0);
        asset.setUsableSize(100.0);

        AssetDTO tryAsset = new AssetDTO();
        tryAsset.setId(null);
        tryAsset.setCustomerId("cust1");
        tryAsset.setAssetName("TRY");
        tryAsset.setSize(1000.0);
        tryAsset.setUsableSize(1000.0);

        when(assetService.getAsset("cust1", "TRY"))
                .thenReturn(Optional.of(asset))
                .thenReturn(Optional.of(tryAsset));

        orderService.completeOrder(order);

        assertEquals(OrderStatus.MATCHED, order.getStatus());
        assertEquals(90.0, asset.getSize());
        assertEquals(1050.0, tryAsset.getSize());

        verify(assetService, times(2)).save(any());
        verify(orderRepository).save(objectMapper.convertValue(order, Order.class));
    }

    // completeOrder - SELL order insufficient asset size
    @Test
    void completeOrder_sellOrder_insufficientAssetSize_throws() {
        OrderDTO order = new OrderDTO();
        order.setCustomerId("cust1");
        order.setOrderSide(OrderSide.SELL);
        order.setAssetName("TRY");
        order.setSize(200.0);
        order.setPrice(5.0);

        AssetDTO asset = new AssetDTO();
        asset.setId(null);
        asset.setCustomerId("cust1");
        asset.setAssetName("TRY");
        asset.setSize(100.0);
        asset.setUsableSize(100.0);

        when(assetService.getAsset("cust1", "TRY")).thenReturn(Optional.of(asset));

        InsufficientBalanceException ex = assertThrows(InsufficientBalanceException.class,
                () -> orderService.completeOrder(order));
        assertEquals("Asset size inconsistent", ex.getMessage());

        verify(assetService, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    // completeOrder - SELL asset not found
    @Test
    void completeOrder_sellOrder_assetNotFound_throws() {
        OrderDTO order = new OrderDTO();
        order.setCustomerId("cust1");
        order.setOrderSide(OrderSide.SELL);
        order.setAssetName("TRY");
        order.setSize(10.0);
        order.setPrice(5.0);

        when(assetService.getAsset("cust1", "TRY")).thenReturn(Optional.empty());

        AssetNotFoundException ex = assertThrows(AssetNotFoundException.class,
                () -> orderService.completeOrder(order));
        assertEquals("Asset not found: TRY", ex.getMessage());

        verify(assetService, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    // getOrdersByCustomerId - returns list
    @Test
    void getOrdersByCustomerId_returnsOrdersId() {
        Order order1 = new Order();
        order1.setId(1L);
        order1.setCustomerId("cust1");
        OrderDTO orderDTO1 = objectMapper.convertValue(order1, OrderDTO.class);
        Order order2 = new Order();
        order2.setId(2L);
        order2.setCustomerId("cust1");
        OrderDTO orderDTO2 = objectMapper.convertValue(order2, OrderDTO.class);
        when(orderRepository.findByCustomerId("cust1")).thenReturn(List.of(order1, order2));

        List<OrderDTO> orders = orderService.getOrdersByCustomerId("cust1");

        assertEquals(2, orders.size());
        assertTrue(orders.contains(orderDTO1));
        assertTrue(orders.contains(orderDTO2));
    }

}
