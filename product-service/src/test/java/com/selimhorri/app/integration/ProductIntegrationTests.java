package com.selimhorri.app.integration;

import com.selimhorri.app.domain.Product;
import com.selimhorri.app.domain.Category;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.repository.ProductRepository;
import com.selimhorri.app.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import java.util.Optional;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductIntegrationTests {

    @Autowired
    private ProductServiceImpl service;

    @MockBean
    private ProductRepository repo;

    @Test
    void contextLoads() {
        assertNotNull(service);
    }

    @Test
    void testSaveIntegration() {
        ProductDto dto = ProductDto.builder().productId(10).productTitle("Test").priceUnit(99.99).categoryDto(CategoryDto.builder().categoryId(1).build()).build();
        Product entity = Product.builder().productId(10).productTitle("Test").priceUnit(99.99).category(Category.builder().categoryId(1).build()).build();
        Mockito.when(repo.save(Mockito.any())).thenReturn(entity);
        ProductDto result = service.save(dto);
        assertEquals(dto.getProductId(), result.getProductId());
    }

    @Test
    void testUpdateIntegration() {
        ProductDto dto = ProductDto.builder().productId(10).productTitle("Test").priceUnit(99.99).categoryDto(CategoryDto.builder().categoryId(1).build()).build();
        Product entity = Product.builder().productId(10).productTitle("Test").priceUnit(99.99).category(Category.builder().categoryId(1).build()).build();
        Mockito.when(repo.save(Mockito.any())).thenReturn(entity);
        ProductDto result = service.update(dto);
        assertEquals(dto.getProductId(), result.getProductId());
    }

    @Test
    void testDeleteByIdIntegration() {
        Product product = Product.builder().productId(10).productTitle("Test").priceUnit(99.99).category(Category.builder().categoryId(1).build()).build();
        Mockito.when(repo.findById(10)).thenReturn(Optional.of(product));
        service.deleteById(10);
        Mockito.verify(repo).delete(Mockito.any());
    }

    @Test
    void testFindByIdIntegration() {
        Product product = Product.builder().productId(10).productTitle("Test").priceUnit(99.99).category(Category.builder().categoryId(1).build()).build();
        Mockito.when(repo.findById(10)).thenReturn(Optional.of(product));
        ProductDto result = service.findById(10);
        assertEquals(product.getProductId(), result.getProductId());
    }

    @Test
    void testFindAllIntegration() {
        Product product = Product.builder().productId(10).productTitle("Test").priceUnit(99.99).category(Category.builder().categoryId(1).build()).build();
        Mockito.when(repo.findAll()).thenReturn(Collections.singletonList(product));
        assertFalse(service.findAll().isEmpty());
    }
}