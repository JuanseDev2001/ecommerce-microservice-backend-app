package com.selimhorri.app.integration;

import com.selimhorri.app.domain.Favourite;
import com.selimhorri.app.domain.id.FavouriteId;
import com.selimhorri.app.dto.FavouriteDto;
import com.selimhorri.app.repository.FavouriteRepository;
import com.selimhorri.app.service.impl.FavouriteServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FavouriteIntegrationTests {

    @Autowired
    private FavouriteServiceImpl service;

    @MockBean
    private FavouriteRepository repo;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void contextLoads() {
        assertNotNull(service);
    }

    @Test
    void testSaveIntegration() {
        FavouriteDto dto = FavouriteDto.builder().userId(1).productId(2).likeDate(LocalDateTime.now()).build();
        Favourite entity = Favourite.builder().userId(1).productId(2).likeDate(dto.getLikeDate()).build();
        Mockito.when(repo.save(Mockito.any())).thenReturn(entity);
        FavouriteDto result = service.save(dto);
        assertEquals(dto.getUserId(), result.getUserId());
    }

    @Test
    void testUpdateIntegration() {
        FavouriteDto dto = FavouriteDto.builder().userId(1).productId(2).likeDate(LocalDateTime.now()).build();
        Favourite entity = Favourite.builder().userId(1).productId(2).likeDate(dto.getLikeDate()).build();
        Mockito.when(repo.save(Mockito.any())).thenReturn(entity);
        FavouriteDto result = service.update(dto);
        assertEquals(dto.getProductId(), result.getProductId());
    }

    @Test
    void testDeleteByIdIntegration() {
        FavouriteId id = new FavouriteId(1, 2, LocalDateTime.now());
        service.deleteById(id);
        Mockito.verify(repo).deleteById(id);
    }

    @Test
    void testFindByIdIntegration() {
        FavouriteId id = new FavouriteId(1, 2, LocalDateTime.now());
        Favourite entity = Favourite.builder().userId(1).productId(2).likeDate(id.getLikeDate()).build();
        Mockito.when(repo.findById(id)).thenReturn(Optional.of(entity));
        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(com.selimhorri.app.dto.UserDto.class))).thenReturn(null);
        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(com.selimhorri.app.dto.ProductDto.class))).thenReturn(null);
        FavouriteDto result = service.findById(id);
        assertEquals(id.getUserId(), result.getUserId());
    }
}