package com.selimhorri.app.unit;

import com.selimhorri.app.domain.Product;
import com.selimhorri.app.domain.Category;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.helper.ProductMappingHelper;
import com.selimhorri.app.repository.ProductRepository;
import com.selimhorri.app.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

class ProductUnitTests {


    @Test
    void testMapProductToDto() {
        Category category = Category.builder().categoryId(1).build();
        Product product = Product.builder().productId(10).productTitle("Test").priceUnit(99.99).category(category).build();
        ProductDto dto = ProductMappingHelper.map(product);
        assertEquals(product.getProductId(), dto.getProductId());
        assertEquals(product.getProductTitle(), dto.getProductTitle());
        assertNotNull(dto.getCategoryDto());
    }


    @Test
    void testMapDtoToProduct() {
        CategoryDto categoryDto = CategoryDto.builder().categoryId(1).build();
        ProductDto dto = ProductDto.builder().productId(10).productTitle("Test").priceUnit(99.99).categoryDto(categoryDto).build();
        Product product = ProductMappingHelper.map(dto);
        assertEquals(dto.getProductId(), product.getProductId());
        assertEquals(dto.getProductTitle(), product.getProductTitle());
        assertNotNull(product.getCategory());
    }


    @Test
    void testSaveCallsRepository() {
        ProductRepository repo = Mockito.mock(ProductRepository.class);
        ProductServiceImpl service = new ProductServiceImpl(repo);
        ProductDto dto = ProductDto.builder().productId(10).productTitle("Test").priceUnit(99.99).categoryDto(CategoryDto.builder().categoryId(1).build()).build();
        Product entity = ProductMappingHelper.map(dto);
        Mockito.when(repo.save(Mockito.any())).thenReturn(entity);
        ProductDto result = service.save(dto);
        assertEquals(dto.getProductId(), result.getProductId());
    }


    @Test
    void testUpdateCallsRepository() {
        ProductRepository repo = Mockito.mock(ProductRepository.class);
        ProductServiceImpl service = new ProductServiceImpl(repo);
        ProductDto dto = ProductDto.builder().productId(10).productTitle("Test").priceUnit(99.99).categoryDto(CategoryDto.builder().categoryId(1).build()).build();
        Product entity = ProductMappingHelper.map(dto);
        Mockito.when(repo.save(Mockito.any())).thenReturn(entity);
        ProductDto result = service.update(dto);
        assertEquals(dto.getProductId(), result.getProductId());
    }


    @Test
    void testFindAllReturnsList() {
        ProductRepository repo = Mockito.mock(ProductRepository.class);
        ProductServiceImpl service = new ProductServiceImpl(repo);
        Product product = Product.builder().productId(10).productTitle("Test").priceUnit(99.99).category(Category.builder().categoryId(1).build()).build();
        Mockito.when(repo.findAll()).thenReturn(Collections.singletonList(product));
        assertFalse(service.findAll().isEmpty());
    }
}