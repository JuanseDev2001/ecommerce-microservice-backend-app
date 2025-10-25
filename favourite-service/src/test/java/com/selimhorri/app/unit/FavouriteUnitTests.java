package com.selimhorri.app.unit;

import com.selimhorri.app.domain.Favourite;
import com.selimhorri.app.dto.FavouriteDto;
import com.selimhorri.app.helper.FavouriteMappingHelper;
import com.selimhorri.app.service.impl.FavouriteServiceImpl;
import com.selimhorri.app.repository.FavouriteRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import com.selimhorri.app.domain.id.FavouriteId;

class FavouriteUnitTests {

    @Test
    void testMapFavouriteToDto() {
        Favourite favourite = Favourite.builder().userId(1).productId(2).likeDate(LocalDateTime.now()).build();
        FavouriteDto dto = FavouriteMappingHelper.map(favourite);
        assertEquals(favourite.getUserId(), dto.getUserId());
        assertEquals(favourite.getProductId(), dto.getProductId());
        assertNotNull(dto.getUserDto());
        assertNotNull(dto.getProductDto());
    }

    @Test
    void testMapDtoToFavourite() {
        FavouriteDto dto = FavouriteDto.builder().userId(1).productId(2).likeDate(LocalDateTime.now()).build();
        Favourite favourite = FavouriteMappingHelper.map(dto);
        assertEquals(dto.getUserId(), favourite.getUserId());
        assertEquals(dto.getProductId(), favourite.getProductId());
    }

    @Test
    void testSaveCallsRepository() {
        FavouriteRepository repo = Mockito.mock(FavouriteRepository.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        FavouriteServiceImpl service = new FavouriteServiceImpl(repo, restTemplate);
        FavouriteDto dto = FavouriteDto.builder().userId(1).productId(2).likeDate(LocalDateTime.now()).build();
        Favourite entity = FavouriteMappingHelper.map(dto);
        Mockito.when(repo.save(Mockito.any())).thenReturn(entity);
        FavouriteDto result = service.save(dto);
        assertEquals(dto.getUserId(), result.getUserId());
    }

    @Test
    void testUpdateCallsRepository() {
        FavouriteRepository repo = Mockito.mock(FavouriteRepository.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        FavouriteServiceImpl service = new FavouriteServiceImpl(repo, restTemplate);
        FavouriteDto dto = FavouriteDto.builder().userId(1).productId(2).likeDate(LocalDateTime.now()).build();
        Favourite entity = FavouriteMappingHelper.map(dto);
        Mockito.when(repo.save(Mockito.any())).thenReturn(entity);
        FavouriteDto result = service.update(dto);
        assertEquals(dto.getProductId(), result.getProductId());
    }

    @Test
    void testDeleteByIdCallsRepository() {
        FavouriteRepository repo = Mockito.mock(FavouriteRepository.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        FavouriteServiceImpl service = new FavouriteServiceImpl(repo, restTemplate);
        FavouriteId id = new FavouriteId(1, 2, LocalDateTime.now());
        service.deleteById(id);
        Mockito.verify(repo).deleteById(id);
    }
}