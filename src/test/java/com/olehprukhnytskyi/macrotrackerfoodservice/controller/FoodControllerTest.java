package com.olehprukhnytskyi.macrotrackerfoodservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.ApiError;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.ApiResponse;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.FoodPatchRequestDto;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.FoodRequestDto;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.FoodResponseDto;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.NutrimentsDto;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.NutrimentsPatchDto;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.PagedResponse;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.Pagination;
import com.olehprukhnytskyi.macrotrackerfoodservice.service.FoodService;
import com.olehprukhnytskyi.macrotrackerfoodservice.service.S3StorageService;
import com.olehprukhnytskyi.macrotrackerfoodservice.util.CustomHeaders;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import software.amazon.awssdk.services.s3.S3Client;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FoodControllerTest {
    protected static MockMvc mockMvc;
    @MockitoBean
    private FoodService foodService;
    @MockitoBean
    private S3Client s3Client;
    @MockitoBean
    private S3StorageService s3StorageService;
    @Autowired
    private ObjectMapper objectMapper;

    private FoodRequestDto foodRequestDto;
    private FoodResponseDto foodResponseDto;

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .build();
    }

    @BeforeEach
    void setUp() {
        NutrimentsDto nutrimentsDto = new NutrimentsDto();
        nutrimentsDto.setKcal(BigDecimal.valueOf(100));
        nutrimentsDto.setCarbohydrates(BigDecimal.valueOf(100));
        nutrimentsDto.setFat(BigDecimal.valueOf(100));
        nutrimentsDto.setProteins(BigDecimal.valueOf(100));

        foodRequestDto = new FoodRequestDto();
        foodRequestDto.setCode("12345678");
        foodRequestDto.setProductName("Product name");
        foodRequestDto.setGenericName("Generic name");
        foodRequestDto.setBrands("Brands");
        foodRequestDto.setImageUrl("https://image.url");
        foodRequestDto.setNutriments(nutrimentsDto);

        foodResponseDto = new FoodResponseDto();
        foodResponseDto.setCode("12345678");
        foodResponseDto.setProductName("Product name");
        foodResponseDto.setGenericName("Generic name");
        foodResponseDto.setBrands("Brands");
        foodResponseDto.setImageUrl("https://image.url");
        foodResponseDto.setNutriments(nutrimentsDto);
    }

    @Test
    @DisplayName("When food exist, should return 200 OK and DTO")
    void findById_whenFoodExist_shouldReturn200AndDto() throws Exception {
        // Given
        String foodId = "123";
        FoodResponseDto foodDto = new FoodResponseDto();
        foodDto.setId(foodId);

        when(foodService.findById(foodId)).thenReturn(foodDto);

        // When
        MvcResult mvcResult = mockMvc.perform(get("/api/foods/" + foodId))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String expected = objectMapper.writeValueAsString(foodDto);
        assertEquals(expected, mvcResult.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("When food exist, should return 200 OK and DTO")
    void findByQuery_whenFoodExist_shouldReturn200AndDto() throws Exception {
        // Given
        FoodResponseDto dto = new FoodResponseDto();
        dto.setId("123");

        Pagination pagination = new Pagination(0, 10, 1);
        PagedResponse<FoodResponseDto> pagedResponse = new PagedResponse<>(
                List.of(dto), pagination);
        String expected = objectMapper.writeValueAsString(pagedResponse);

        when(foodService.findByQuery("apple", 0, 10)).thenReturn(pagedResponse.getData());

        // When
        MvcResult mvcResult = mockMvc.perform(get("/api/foods")
                        .param("query", "apple")
                        .param("offset", "0")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        assertEquals(expected, mvcResult.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("When food does not exist, should return 204 No Content and an empty list")
    void findByQuery_whenFoodDoesNotExist_shouldReturnNoContentAndEmptyList()
            throws Exception {
        // Given
        Pagination pagination = new Pagination(0, 10, 0);
        PagedResponse<FoodResponseDto> pagedResponse = new PagedResponse<>(
                Collections.emptyList(), pagination);
        String expected = objectMapper.writeValueAsString(pagedResponse);

        when(foodService.findByQuery("apple", 0, 10)).thenReturn(Collections.emptyList());

        // When
        MvcResult mvcResult = mockMvc.perform(get("/api/foods")
                        .param("query", "apple")
                        .param("offset", "0")
                        .param("limit", "10"))
                .andExpect(status().isNoContent())
                .andReturn();

        // Then
        assertEquals(expected, mvcResult.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("When offset is negative, should return 400 Bad Request and error response")
    void findByQuery_whenOffsetIsNegative_shouldReturnBadRequestAndErrorResponse()
            throws Exception {
        // Given
        ApiResponse<List<FoodResponseDto>> pagedResponse = ApiResponse.error(
                new ApiError("validation",
                        "findByQuery.offset must be greater than or equal to 0"));
        String expected = objectMapper.writeValueAsString(pagedResponse);

        // When
        MvcResult mvcResult = mockMvc.perform(get("/api/foods")
                        .param("query", "apple")
                        .param("offset", "-10")
                        .param("limit", "10"))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Then
        assertEquals(expected, mvcResult.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("When limit is negative, should return 400 Bad Request and error response")
    void findByQuery_whenLimitIsNegative_shouldReturnBadRequestAndErrorResponse()
            throws Exception {
        // Given
        ApiResponse<List<FoodResponseDto>> pagedResponse = ApiResponse.error(
                new ApiError("validation", "findByQuery.limit must be greater than or equal to 1"));
        String expected = objectMapper.writeValueAsString(pagedResponse);

        // When
        MvcResult mvcResult = mockMvc.perform(get("/api/foods")
                        .param("query", "apple")
                        .param("offset", "0")
                        .param("limit", "-10"))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Then
        assertEquals(expected, mvcResult.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("When query is valid and suggestions exist,"
            + " should return 200 OK and suggestion list")
    void getSearchSuggestions_whenSuggestionsExist_shouldReturnOkAndList()
            throws Exception {
        // Given
        List<String> mockSuggestions = List.of("apple pie", "apple juice");
        String expected = objectMapper.writeValueAsString(mockSuggestions);

        when(foodService.getSearchSuggestions("apple")).thenReturn(mockSuggestions);

        // When & Then
        mockMvc.perform(get("/api/foods/search-suggestions")
                        .param("query", "apple"))
                .andExpect(status().isOk())
                .andExpect(content().json(expected));
    }

    @Test
    @DisplayName("When query is valid but no suggestions found, should return 204 No Content")
    void getSearchSuggestions_whenNoSuggestions_shouldReturnNoContent()
            throws Exception {
        // Given
        when(foodService.getSearchSuggestions("xyz"))
                .thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/foods/search-suggestions")
                        .param("query", "xyz"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("When query param is missing, should return 400 Bad Request")
    void getSearchSuggestions_whenQueryMissing_shouldReturnBadRequest()
            throws Exception {
        mockMvc.perform(get("/api/foods/search-suggestions"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("When request is valid, should return 201 Created and saved FoodResponseDto")
    void save_whenRequestIsValid_shouldReturnCreated() throws Exception {
        // Given
        String foodJson = objectMapper.writeValueAsString(foodRequestDto);
        String expected = objectMapper.writeValueAsString(foodResponseDto);

        MockMultipartFile foodPart = new MockMultipartFile(
                "food",
                "food.json",
                MediaType.APPLICATION_JSON_VALUE,
                foodJson.getBytes()
        );

        MockMultipartFile imagePart = new MockMultipartFile(
                "image",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "image-content".getBytes()
        );

        when(foodService.createProductWithImages(any(), any(), anyLong()))
                .thenReturn(foodResponseDto);

        // When
        MvcResult mvcResult = mockMvc.perform(multipart("/api/foods")
                        .file(foodPart)
                        .file(imagePart)
                        .header(CustomHeaders.X_USER_ID, 1L)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .content(foodJson))
                .andExpect(status().isCreated())
                .andReturn();

        // Then
        assertEquals(expected, mvcResult.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("When request has validation errors, should return 400 Bad Request with error")
    void save_whenValidationError_shouldReturnBadRequest() throws Exception {
        // Given
        foodRequestDto.setProductName(null);
        String foodJson = objectMapper.writeValueAsString(foodRequestDto);

        MockMultipartFile foodPart = new MockMultipartFile(
                "food",
                "food.json",
                MediaType.APPLICATION_JSON_VALUE,
                foodJson.getBytes()
        );

        MockMultipartFile imagePart = new MockMultipartFile(
                "image",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake".getBytes()
        );

        ApiResponse<?> response = ApiResponse.error(
                new ApiError("productName", "must not be null"));
        String expected = objectMapper.writeValueAsString(response);

        // When
        MvcResult mvcResult = mockMvc.perform(multipart("/api/foods")
                        .file(foodPart)
                        .file(imagePart)
                        .header(CustomHeaders.X_USER_ID, 1L)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Then
        assertEquals(expected, mvcResult.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("When patch is successful, should return 200 and updated DTO")
    void patch_whenSuccessful_shouldReturn200AndUpdatedDto() throws Exception {
        // Given
        String id = "123";
        String requestJson = objectMapper.writeValueAsString(foodRequestDto);
        String expected = objectMapper.writeValueAsString(foodResponseDto);

        when(foodService.patch(eq(id), any(FoodPatchRequestDto.class)))
                .thenReturn(foodResponseDto);

        // When
        MvcResult result = mockMvc.perform(patch("/api/foods/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        assertEquals(expected, result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("When patch request has validation errors,"
            + " should return 400 Bad Request with error")
    void patch_whenValidationError_shouldReturnBadRequest() throws Exception {
        // Given
        NutrimentsPatchDto nutrimentsPatchDto = new NutrimentsPatchDto();
        nutrimentsPatchDto.setCarbohydrates(BigDecimal.valueOf(-100));

        FoodPatchRequestDto requestDto = new FoodPatchRequestDto();
        requestDto.setNutriments(nutrimentsPatchDto);

        String requestJson = objectMapper.writeValueAsString(requestDto);
        ApiResponse<Object> response = ApiResponse.error(
                new ApiError("nutriments.carbohydrates",
                        "must be greater than or equal to 0.0"));
        String expected = objectMapper.writeValueAsString(response);

        // When
        MvcResult mvcResult = mockMvc.perform(patch("/api/foods/{id}", "123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Then
        assertEquals(expected, mvcResult.getResponse().getContentAsString());
    }
}
