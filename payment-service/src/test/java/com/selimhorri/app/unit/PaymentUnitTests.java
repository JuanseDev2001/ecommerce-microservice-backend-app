package com.selimhorri.app.unit;

import com.selimhorri.app.domain.Payment;
import com.selimhorri.app.dto.PaymentDto;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.helper.PaymentMappingHelper;
import com.selimhorri.app.repository.PaymentRepository;
import com.selimhorri.app.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.*;

class PaymentUnitTests {


    @Test
    void testMapPaymentToDto() {
        Payment payment = Payment.builder().paymentId(1).isPayed(true).paymentStatus(null).orderId(2).build();
        PaymentDto dto = PaymentMappingHelper.map(payment);
        assertEquals(payment.getPaymentId(), dto.getPaymentId());
        assertEquals(payment.getIsPayed(), dto.getIsPayed());
        assertEquals(payment.getPaymentStatus(), dto.getPaymentStatus());
    }

    @Test
    void testMapDtoToPayment() {
        OrderDto orderDto = OrderDto.builder().orderId(2).build();
        PaymentDto dto = PaymentDto.builder().paymentId(1).isPayed(true).paymentStatus(null).orderDto(orderDto).build();
        Payment payment = PaymentMappingHelper.map(dto);
        assertEquals(dto.getPaymentId(), payment.getPaymentId());
        assertEquals(dto.getIsPayed(), payment.getIsPayed());
        assertEquals(dto.getPaymentStatus(), payment.getPaymentStatus());
    }


    @Test
    void testSaveCallsRepository() {
        PaymentRepository repo = Mockito.mock(PaymentRepository.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        PaymentServiceImpl service = new PaymentServiceImpl(repo, restTemplate);
        OrderDto orderDto = OrderDto.builder().orderId(2).build();
        PaymentDto dto = PaymentDto.builder().paymentId(1).isPayed(true).paymentStatus(null).orderDto(orderDto).build();
        Payment entity = PaymentMappingHelper.map(dto);
        Mockito.when(repo.save(Mockito.any())).thenReturn(entity);
        PaymentDto result = service.save(dto);
        assertEquals(dto.getPaymentId(), result.getPaymentId());
    }

    @Test
    void testUpdateCallsRepository() {
        PaymentRepository repo = Mockito.mock(PaymentRepository.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        PaymentServiceImpl service = new PaymentServiceImpl(repo, restTemplate);
        OrderDto orderDto = OrderDto.builder().orderId(2).build();
        PaymentDto dto = PaymentDto.builder().paymentId(1).isPayed(true).paymentStatus(null).orderDto(orderDto).build();
        Payment entity = PaymentMappingHelper.map(dto);
        Mockito.when(repo.save(Mockito.any())).thenReturn(entity);
        PaymentDto result = service.update(dto);
        assertEquals(dto.getPaymentId(), result.getPaymentId());
    }

    @Test
    void testDeleteByIdCallsRepository() {
        PaymentRepository repo = Mockito.mock(PaymentRepository.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        PaymentServiceImpl service = new PaymentServiceImpl(repo, restTemplate);
        service.deleteById(1);
        Mockito.verify(repo).deleteById(1);
    }
}