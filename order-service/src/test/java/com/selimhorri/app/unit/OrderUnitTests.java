package com.selimhorri.app.unit;

import com.selimhorri.app.domain.Order;
import com.selimhorri.app.domain.Cart;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.helper.OrderMappingHelper;
import com.selimhorri.app.repository.OrderRepository;
import com.selimhorri.app.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.time.LocalDateTime;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

class OrderUnitTests {

    @Test
    void testMapOrderToDto() {
        Cart cart = Cart.builder().cartId(1).build();
        Order order = Order.builder().orderId(10).orderDate(LocalDateTime.now()).orderDesc("desc").orderFee(5.0).cart(cart).build();
        OrderDto dto = OrderMappingHelper.map(order);
        assertEquals(order.getOrderId(), dto.getOrderId());
        assertEquals(order.getOrderDesc(), dto.getOrderDesc());
        assertNotNull(dto.getCartDto());
    }

    @Test
    void testMapDtoToOrder() {
        CartDto cartDto = CartDto.builder().cartId(1).build();
        OrderDto dto = OrderDto.builder().orderId(10).orderDate(LocalDateTime.now()).orderDesc("desc").orderFee(5.0).cartDto(cartDto).build();
        Order order = OrderMappingHelper.map(dto);
        assertEquals(dto.getOrderId(), order.getOrderId());
        assertEquals(dto.getOrderDesc(), order.getOrderDesc());
        assertNotNull(order.getCart());
    }

    @Test
    void testSaveCallsRepository() {
        OrderRepository repo = Mockito.mock(OrderRepository.class);
        OrderServiceImpl service = new OrderServiceImpl(repo);
        OrderDto dto = OrderDto.builder().orderId(10).orderDate(LocalDateTime.now()).orderDesc("desc").orderFee(5.0).cartDto(CartDto.builder().cartId(1).build()).build();
        Order entity = OrderMappingHelper.map(dto);
        Mockito.when(repo.save(Mockito.any())).thenReturn(entity);
        OrderDto result = service.save(dto);
        assertEquals(dto.getOrderId(), result.getOrderId());
    }

    @Test
    void testUpdateCallsRepository() {
        OrderRepository repo = Mockito.mock(OrderRepository.class);
        OrderServiceImpl service = new OrderServiceImpl(repo);
        OrderDto dto = OrderDto.builder().orderId(10).orderDate(LocalDateTime.now()).orderDesc("desc").orderFee(5.0).cartDto(CartDto.builder().cartId(1).build()).build();
        Order entity = OrderMappingHelper.map(dto);
        Mockito.when(repo.save(Mockito.any())).thenReturn(entity);
        OrderDto result = service.update(dto);
        assertEquals(dto.getOrderId(), result.getOrderId());
    }

    @Test
    void testFindAllReturnsList() {
        OrderRepository repo = Mockito.mock(OrderRepository.class);
        OrderServiceImpl service = new OrderServiceImpl(repo);
        Order order = Order.builder().orderId(10).orderDate(LocalDateTime.now()).orderDesc("desc").orderFee(5.0).cart(Cart.builder().cartId(1).build()).build();
        Mockito.when(repo.findAll()).thenReturn(Collections.singletonList(order));
        assertFalse(service.findAll().isEmpty());
    }
}