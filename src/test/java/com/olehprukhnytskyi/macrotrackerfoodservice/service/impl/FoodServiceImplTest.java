package com.olehprukhnytskyi.macrotrackerfoodservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.when;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.util.ObjectBuilder;
import com.mongodb.DuplicateKeyException;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.FoodListCacheWrapper;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.FoodPatchRequestDto;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.FoodRequestDto;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.FoodResponseDto;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.NutrimentsDto;
import com.olehprukhnytskyi.macrotrackerfoodservice.exception.BadRequestException;
import com.olehprukhnytskyi.macrotrackerfoodservice.exception.ConflictException;
import com.olehprukhnytskyi.macrotrackerfoodservice.exception.InternalServerErrorException;
import com.olehprukhnytskyi.macrotrackerfoodservice.exception.NotFoundException;
import com.olehprukhnytskyi.macrotrackerfoodservice.exception.SearchServiceException;
import com.olehprukhnytskyi.macrotrackerfoodservice.mapper.FoodMapper;
import com.olehprukhnytskyi.macrotrackerfoodservice.mapper.NutrimentsMapper;
import com.olehprukhnytskyi.macrotrackerfoodservice.model.Food;
import com.olehprukhnytskyi.macrotrackerfoodservice.model.Nutriments;
import com.olehprukhnytskyi.macrotrackerfoodservice.repository.mongo.FoodRepository;
import com.olehprukhnytskyi.macrotrackerfoodservice.service.CounterService;
import com.olehprukhnytskyi.macrotrackerfoodservice.service.GeminiService;
import com.olehprukhnytskyi.macrotrackerfoodservice.service.ImageService;
import com.olehprukhnytskyi.macrotrackerfoodservice.service.S3StorageService;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.services.s3.S3Client;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class FoodServiceImplTest {
    @Mock
    private FoodRepository foodRepository;
    @Mock
    private CounterService counterService;
    @Mock
    private ElasticsearchClient elasticsearchClient;
    @Mock
    private GeminiService geminiService;
    @Mock
    private NutrimentsMapper nutrimentsMapper;
    @Mock
    private FoodMapper foodMapper;
    @Mock
    private ImageService imageService;
    @MockitoBean
    private S3Client s3Client;
    @Mock
    private S3StorageService s3StorageService;

    @InjectMocks
    private FoodServiceImpl foodService;

    @Mock
    private Nutriments nutriments;
    private FoodRequestDto foodRequestDto;
    private Food food;
    private MockMultipartFile image;

    @BeforeEach
    void setUp() {
        NutrimentsDto nutrimentsDto = new NutrimentsDto();
        nutrimentsDto.setKcal(BigDecimal.ONE);
        nutrimentsDto.setCarbohydrates(BigDecimal.ONE);
        nutrimentsDto.setFat(BigDecimal.ONE);
        nutrimentsDto.setProteins(BigDecimal.ONE);

        nutrimentsDto.setKcal(BigDecimal.ONE);
        nutrimentsDto.setCarbohydrates(BigDecimal.ONE);
        nutrimentsDto.setFat(BigDecimal.ONE);
        nutrimentsDto.setProteins(BigDecimal.ONE);

        foodRequestDto = new FoodRequestDto();
        foodRequestDto.setCode("code");
        foodRequestDto.setProductName("product_name");
        foodRequestDto.setBrands("brands");
        foodRequestDto.setGenericName("generic_name");
        foodRequestDto.setNutriments(nutrimentsDto);

        food = new Food();
        food.setProductName("product_name");
        food.setBrands("brands");
        food.setGenericName("generic_name");
        food.setNutriments(nutriments);

        try {
            BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(testImage, "jpg", os);
            image = new MockMultipartFile(
                    "image",
                    "image.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    os.toByteArray()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("When food with same code and fields exists, should return existing DTO")
    void createFoodWithImages_whenSameCodeExists_shouldReturnExistingDto() {
        // Given
        when(nutrimentsMapper.toModel(any())).thenReturn(nutriments);
        when(foodRepository.findById(any())).thenReturn(Optional.of(food));
        when(foodMapper.toDto(any())).thenReturn(new FoodResponseDto());

        // When
        FoodResponseDto result = foodService.createFoodWithImages(foodRequestDto, image, 1L);

        // Then
        assertNotNull(result);
        verify(foodRepository, times(1)).findById(anyString());
        verify(foodRepository, never()).save(any());
    }

    @Test
    @DisplayName("When food do not exist, should save and return DTO")
    void save_whenFoodDoNotExist_shouldCreateFoodWithImagesAndReturnDto() {
        // Given
        foodRequestDto.setCode(null);

        when(foodMapper.toModel(any())).thenReturn(food);
        when(foodMapper.toDto(any())).thenReturn(new FoodResponseDto());
        when(imageService.resizeImage(any(), anyInt()))
                .thenReturn(new ByteArrayInputStream("fake".getBytes()));
        when(imageService.generateImageKey(any(), anyString(), anyInt()))
                .thenReturn("images/products/test.png");
        when(foodRepository.save(any())).thenReturn(food);

        // When
        FoodResponseDto result = foodService.createFoodWithImages(foodRequestDto, image, 1L);

        // Then
        assertNotNull(result);
        verify(foodRepository, never()).findById(anyString());
        verify(foodRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("When food with same code exists but different data,"
            + " should throw ConflictException")
    void createFoodWithImages_whenSameCodeExistsButDifferentData_shouldThrowException() {
        // Given
        Food differentFood = new Food();
        differentFood.setProductName("new_product_name");
        differentFood.setGenericName("new_generic_name");

        when(foodRepository.findById(anyString())).thenReturn(Optional.of(differentFood));

        // When
        ConflictException conflictException = assertThrows(ConflictException.class,
                () -> foodService.createFoodWithImages(foodRequestDto, image, 1L));

        // Then
        String expected = "Product with this code already exists with different data";
        assertEquals(expected, conflictException.getMessage());
    }

    @Test
    @DisplayName("When DuplicateKeyException occurs max times, should throw ConflictException")
    void createFoodWithImages_whenDuplicateKeyExceptionMaxTimes_shouldThrowException() {
        // Given
        foodRequestDto.setCode(null);

        when(foodRepository.save(any())).thenThrow(DuplicateKeyException.class);
        when(foodMapper.toModel(any())).thenReturn(food);
        when(imageService.resizeImage(any(), anyInt()))
                .thenReturn(new ByteArrayInputStream("fake".getBytes()));
        when(imageService.generateImageKey(any(), anyString(), anyInt()))
                .thenReturn("images/products/test.png");

        // When
        ConflictException conflictException = assertThrows(ConflictException.class,
                () -> foodService.createFoodWithImages(foodRequestDto, image, 1L));

        // Then
        String expected = "Duplicate key error after max retries";
        assertEquals(expected, conflictException.getMessage());
    }

    @Test
    @DisplayName("When DataIntegrityViolation occurs, should throw BadRequestException")
    void createFoodWithImages_whenDataIntegrityViolationOccurs_shouldThrowBadRequestException() {
        // Given
        foodRequestDto.setCode(null);

        when(foodRepository.save(any())).thenThrow(DataIntegrityViolationException.class);
        when(foodMapper.toModel(any())).thenReturn(food);
        when(imageService.resizeImage(any(), anyInt()))
                .thenReturn(new ByteArrayInputStream("fake".getBytes()));
        when(imageService.generateImageKey(any(), anyString(), anyInt()))
                .thenReturn("images/products/test.png");

        // When
        BadRequestException badRequestException = assertThrows(
                BadRequestException.class,
                () -> foodService.createFoodWithImages(foodRequestDto, image, 1L)
        );

        // Then
        String expected = "Invalid data for saving food";
        assertEquals(expected, badRequestException.getMessage());
    }

    @Test
    @DisplayName("When query is null, should throw BadRequestException")
    void findByQuery_whenQueryIsNull_shouldThrowBadRequestException() {
        assertThrows(BadRequestException.class, () -> foodService.findByQuery(null, 0, 10));
    }

    @Test
    @DisplayName("When query is empty, should throw BadRequestException")
    void findByQuery_whenQueryIsEmpty_shouldThrowBadRequestException() {
        assertThrows(BadRequestException.class, () -> foodService.findByQuery("   ", 0, 10));
    }

    @Test
    @DisplayName("When search succeeds, should return mapped DTO")
    void findByQuery_whenSearchSucceeds_shouldReturnMappedDto() throws IOException {
        // given
        FoodResponseDto dto = new FoodResponseDto();
        dto.setId("123");

        Hit<Food> hit = Hit.of(h -> h
                .id("123")
                .index("index")
                .source(food)
        );

        SearchResponse<Food> mockResponse = SearchResponse.of(r -> r
                .timedOut(false)
                .shards(b -> b
                        .failed(0)
                        .total(1)
                        .successful(1)
                )
                .took(5)
                .hits(h -> h
                        .hits(List.of(hit))
                )
        );

        when(elasticsearchClient.search(
                ArgumentMatchers.<Function<SearchRequest.Builder,
                        ObjectBuilder<SearchRequest>>>any(),
                eq(Food.class)
        )).thenReturn(mockResponse);
        when(foodMapper.toDto(food)).thenReturn(dto);

        // when
        List<FoodResponseDto> result = foodService.findByQuery("apple juice", 0, 10).getItems();

        // then
        assertEquals(1, result.size());
        assertEquals("123", result.get(0).getId());
    }

    @Test
    @DisplayName("When search response has null hits, should return an empty list")
    void findByQuery_whenSearchResponseHasNullHits_shouldReturnEmptyList() throws IOException {
        // Given
        when(elasticsearchClient.search(
                ArgumentMatchers.<Function<SearchRequest.Builder,
                        ObjectBuilder<SearchRequest>>>any(),
                eq(Food.class)
        )).thenReturn(null);

        // When
        FoodListCacheWrapper result = foodService.findByQuery("banana", 0, 10);

        // Then
        assertNull(result.getItems());
    }

    @Test
    @DisplayName("When Elasticsearch throws an IOException, should throw SearchServiceException")
    void findByQuery_whenElasticsearchThrowsIoException_shouldThrowSearchServiceException()
            throws IOException {
        // Given
        when(elasticsearchClient.search(
                ArgumentMatchers.<Function<SearchRequest.Builder,
                        ObjectBuilder<SearchRequest>>>any(),
                eq(Food.class)
        )).thenThrow(new IOException("Elastic down"));

        // When & Then
        assertThrows(SearchServiceException.class, () ->
                foodService.findByQuery("milk", 0, 10));
    }

    @Test
    @DisplayName("When UnexpectedException occurs, should throw InternalServerErrorException")
    void findByQuery_whenUnexpectedExceptionOccurs_shouldThrowInternalServerErrorException()
            throws IOException {
        // Given
        when(elasticsearchClient.search(
                ArgumentMatchers.<Function<SearchRequest.Builder,
                        ObjectBuilder<SearchRequest>>>any(),
                eq(Food.class)
        )).thenThrow(new RuntimeException("Something went wrong"));

        // When & Then
        assertThrows(InternalServerErrorException.class, () ->
                foodService.findByQuery("milk", 0, 10));
    }

    @Test
    @DisplayName("When query is null, should return an empty list")
    void getSearchSuggestions_whenQueryIsNull_shouldReturnEmptyList() {
        List<String> result = foodService.getSearchSuggestions(null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("When query is blank, should return an empty list")
    void getSearchSuggestions_whenQueryIsBlank_shouldReturnEmptyList() {
        List<String> result = foodService.getSearchSuggestions("   ");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("When search returns results, should return product names")
    void getSearchSuggestions_whenSearchReturnsResults_shouldReturnProductNames()
            throws IOException {
        // given
        Food food1 = new Food();
        food1.setProductName("Apple Juice");

        Food food2 = new Food();
        food2.setProductName("Apple Cider");

        Hit<Food> hit1 = mock(Hit.class);
        Hit<Food> hit2 = mock(Hit.class);

        SearchResponse<Food> response = mock(SearchResponse.class);
        HitsMetadata<Food> hitsMetadata = mock(HitsMetadata.class);

        when(hit1.source()).thenReturn(food1);
        when(hit2.source()).thenReturn(food2);
        when(hitsMetadata.hits()).thenReturn(List.of(hit1, hit2));
        when(response.hits()).thenReturn(hitsMetadata);
        when(elasticsearchClient.search(any(Function.class), eq(Food.class)))
                .thenReturn(response);

        // when
        List<String> result = foodService.getSearchSuggestions("apple");

        // then
        assertEquals(2, result.size());
        assertTrue(result.contains("Apple Juice"));
        assertTrue(result.contains("Apple Cider"));
    }

    @Test
    @DisplayName("When hits contain null sources, should filter them out")
    void getSearchSuggestions_whenHitsContainNullSources_shouldFilterThemOut()
            throws IOException {
        // given
        Hit<Food> hit1 = mock(Hit.class);

        SearchResponse<Food> response = mock(SearchResponse.class);
        HitsMetadata<Food> hitsMetadata = mock(HitsMetadata.class);

        when(hit1.source()).thenReturn(null);
        when(hitsMetadata.hits()).thenReturn(List.of(hit1));
        when(response.hits()).thenReturn(hitsMetadata);
        when(elasticsearchClient.search(any(Function.class), eq(Food.class)))
                .thenReturn(response);

        // when
        List<String> result = foodService.getSearchSuggestions("juice");

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("When when Elasticsearch throws IOException,"
            + " should throw SearchServiceException")
    void getSearchSuggestions_whenElasticsearchThrowsIoException_shouldThrowSearchServiceException()
            throws IOException {
        // Given
        when(elasticsearchClient.search(any(Function.class), eq(Food.class)))
                .thenThrow(new IOException("ES down"));

        // When & Then
        assertThrows(SearchServiceException.class, () -> foodService.getSearchSuggestions("juice"));
    }

    @Test
    @DisplayName("When food not found, should throw NotFoundException")
    void patch_whenFoodNotFound_shouldThrowNotFoundException() {
        // Given
        String id = "123";
        FoodPatchRequestDto dto = new FoodPatchRequestDto();

        when(foodRepository.findById(id)).thenReturn(Optional.empty());

        // When, Then
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                foodService.patch(id, dto)
        );
        assertEquals("Food not found with id: 123", exception.getMessage());
    }

    @Test
    @DisplayName("When food exists, should update and return DTO")
    void patch_whenFoodExists_shouldUpdateAndReturnDto() {
        // Given
        String id = "123";
        Food existingFood = new Food();
        existingFood.setId(id);
        Food updatedFood = new Food();
        updatedFood.setId(id);

        FoodResponseDto expectedDto = new FoodResponseDto();
        expectedDto.setId(id);

        when(foodRepository.findById(id)).thenReturn(Optional.of(existingFood));
        when(foodRepository.save(existingFood)).thenReturn(updatedFood);
        when(foodMapper.toDto(updatedFood)).thenReturn(expectedDto);

        // When
        FoodResponseDto result = foodService.patch(id, new FoodPatchRequestDto());

        // Then
        assertEquals(id, result.getId());
    }

    @Test
    @DisplayName("When unexpected error occurs, should throw InternalServerErrorException")
    void patch_whenUnexpectedErrorOccurs_shouldThrowInternalServerErrorException() {
        // Given
        String id = "123";
        FoodPatchRequestDto dto = new FoodPatchRequestDto();

        when(foodRepository.findById(id)).thenThrow(new RuntimeException("Unexpected error"));

        // When
        InternalServerErrorException exception = assertThrows(
                InternalServerErrorException.class, () -> foodService.patch(id, dto)
        );

        // Then
        assertEquals("Unexpected error while patching food", exception.getMessage());
    }
}
