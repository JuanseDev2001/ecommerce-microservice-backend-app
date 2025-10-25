package com.selimhorri.app.integration;

import com.selimhorri.app.domain.Order;
import com.selimhorri.app.domain.Cart;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.repository.OrderRepository;
import com.selimhorri.app.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderIntegrationTests {

    @Autowired
    private OrderServiceImpl service;

    @MockBean
    private OrderRepository repo;

    @Test
    void contextLoads() {
        assertNotNull(service);
    }

    @Test
    void testSaveIntegration() {
        OrderDto dto = OrderDto.builder().orderId(10).orderDate(LocalDateTime.now()).orderDesc("desc").orderFee(5.0).cartDto(CartDto.builder().cartId(1).build()).build();
        Order entity = Order.builder().orderId(10).orderDate(dto.getOrderDate()).orderDesc(dto.getOrderDesc()).orderFee(dto.getOrderFee()).cart(Cart.builder().cartId(1).build()).build();
        Mockito.when(repo.save(Mockito.any())).thenReturn(entity);
        OrderDto result = service.save(dto);
        assertEquals(dto.getOrderId(), result.getOrderId());
    }

    @Test
    void testUpdateIntegration() {
        OrderDto dto = OrderDto.builder().orderId(10).orderDate(LocalDateTime.now()).orderDesc("desc").orderFee(5.0).cartDto(CartDto.builder().cartId(1).build()).build();
        Order entity = Order.builder().orderId(10).orderDate(dto.getOrderDate()).orderDesc(dto.getOrderDesc()).orderFee(dto.getOrderFee()).cart(Cart.builder().cartId(1).build()).build();
        Mockito.when(repo.save(Mockito.any())).thenReturn(entity);
        OrderDto result = service.update(dto);
        assertEquals(dto.getOrderId(), result.getOrderId());
    }

    @Test
    void testDeleteByIdIntegration() {
        Order order = Order.builder().orderId(10).orderDate(LocalDateTime.now()).orderDesc("desc").orderFee(5.0).cart(Cart.builder().cartId(1).build()).build();
        Mockito.when(repo.findById(10)).thenReturn(Optional.of(order));
        service.deleteById(10);
        Mockito.verify(repo).delete(Mockito.any());
    }

    @Test
    void testFindByIdIntegration() {
        Order order = Order.builder().orderId(10).orderDate(LocalDateTime.now()).orderDesc("desc").orderFee(5.0).cart(Cart.builder().cartId(1).build()).build();
        Mockito.when(repo.findById(10)).thenReturn(Optional.of(order));
        OrderDto result = service.findById(10);
        assertEquals(order.getOrderId(), result.getOrderId());
    }

    @Test
    void testFindAllIntegration() {
        Order order = Order.builder().orderId(10).orderDate(LocalDateTime.now()).orderDesc("desc").orderFee(5.0).cart(Cart.builder().cartId(1).build()).build();
        Mockito.when(repo.findAll()).thenReturn(Collections.singletonList(order));
        assertFalse(service.findAll().isEmpty());
    }
}