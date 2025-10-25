package com.selimhorri.app.unit;

import com.selimhorri.app.ShippingServiceApplication;
import com.selimhorri.app.helper.OrderItemMappingHelper;
import com.selimhorri.app.domain.OrderItem;
import com.selimhorri.app.dto.OrderItemDto;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ShippingServiceApplicationUnitTest {

    @Test
    void testMainMethodExists() {
        try {
            java.lang.reflect.Method main = ShippingServiceApplication.class.getMethod("main", String[].class);
            int modifiers = main.getModifiers();
            assertTrue(java.lang.reflect.Modifier.isPublic(modifiers));
            assertTrue(java.lang.reflect.Modifier.isStatic(modifiers));
        } catch (NoSuchMethodException e) {
            fail("main method not found");
        }
    }

    @Test
    void testOrderItemMappingHelperMapToDto() {
        OrderItem orderItem = OrderItem.builder().productId(1).orderId(2).orderedQuantity(3).build();
        OrderItemDto dto = OrderItemMappingHelper.map(orderItem);
        assertEquals(1, dto.getProductId());
        assertEquals(2, dto.getOrderId());
        assertEquals(3, dto.getOrderedQuantity());
        assertNotNull(dto.getProductDto());
        assertNotNull(dto.getOrderDto());
    }

    @Test
    void testOrderItemMappingHelperMapToEntity() {
        OrderItemDto dto = OrderItemDto.builder().productId(1).orderId(2).orderedQuantity(3).build();
        OrderItem entity = OrderItemMappingHelper.map(dto);
        assertEquals(1, entity.getProductId());
        assertEquals(2, entity.getOrderId());
        assertEquals(3, entity.getOrderedQuantity());
    }

    @Test
    void testOrderItemDtoBuilder() {
        OrderItemDto dto = OrderItemDto.builder().productId(10).orderId(20).orderedQuantity(30).build();
        assertEquals(10, dto.getProductId());
        assertEquals(20, dto.getOrderId());
        assertEquals(30, dto.getOrderedQuantity());
    }

    @Test
    void testOrderItemBuilder() {
        OrderItem entity = OrderItem.builder().productId(10).orderId(20).orderedQuantity(30).build();
        assertEquals(10, entity.getProductId());
        assertEquals(20, entity.getOrderId());
        assertEquals(30, entity.getOrderedQuantity());
    }
}
