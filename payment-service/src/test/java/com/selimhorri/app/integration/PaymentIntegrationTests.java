package com.selimhorri.app.integration;

import com.selimhorri.app.domain.Payment;
import com.selimhorri.app.dto.PaymentDto;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.repository.PaymentRepository;
import com.selimhorri.app.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentIntegrationTests {

    @Autowired
    private PaymentServiceImpl service;

    @MockBean
    private PaymentRepository repo;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void contextLoads() {
        assertNotNull(service);
    }


    @Test
    void testSaveIntegration() {
        // Set orderDto to avoid NullPointerException in mapping/service logic
        OrderDto orderDto = OrderDto.builder().orderId(2).build();
        PaymentDto dto = PaymentDto.builder().paymentId(1).isPayed(true).paymentStatus(null).orderDto(orderDto).build();
        Payment entity = Payment.builder().paymentId(1).isPayed(true).paymentStatus(null).orderId(2).build();
        Mockito.when(repo.save(Mockito.any())).thenReturn(entity);
        PaymentDto result = service.save(dto);
        assertEquals(dto.getPaymentId(), result.getPaymentId());
    }

    @Test
    void testUpdateIntegration() {
        OrderDto orderDto = OrderDto.builder().orderId(2).build();
        PaymentDto dto = PaymentDto.builder().paymentId(1).isPayed(true).paymentStatus(null).orderDto(orderDto).build();
        Payment entity = Payment.builder().paymentId(1).isPayed(true).paymentStatus(null).orderId(2).build();
        Mockito.when(repo.save(Mockito.any())).thenReturn(entity);
        PaymentDto result = service.update(dto);
        assertEquals(dto.getPaymentId(), result.getPaymentId());
    }

    @Test
    void testDeleteByIdIntegration() {
        service.deleteById(1);
        Mockito.verify(repo).deleteById(1);
    }

    @Test
    void testFindByIdIntegration() {
        Payment entity = Payment.builder().paymentId(1).isPayed(true).paymentStatus(null).orderId(2).build();
        Mockito.when(repo.findById(1)).thenReturn(Optional.of(entity));
        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.any())).thenReturn(com.selimhorri.app.dto.OrderDto.builder().orderId(2).build());
        PaymentDto result = service.findById(1);
        assertEquals(entity.getPaymentId(), result.getPaymentId());
    }
}