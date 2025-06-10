package com.olehprukhnytskyi.macrotrackerfoodservice.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.mongodb.DuplicateKeyException;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.FoodPatchRequestDto;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.FoodRequestDto;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.FoodResponseDto;
import com.olehprukhnytskyi.macrotrackerfoodservice.exception.BadRequestException;
import com.olehprukhnytskyi.macrotrackerfoodservice.exception.ConflictException;
import com.olehprukhnytskyi.macrotrackerfoodservice.exception.InternalServerErrorException;
import com.olehprukhnytskyi.macrotrackerfoodservice.exception.NotFoundException;
import com.olehprukhnytskyi.macrotrackerfoodservice.exception.SearchServiceException;
import com.olehprukhnytskyi.macrotrackerfoodservice.mapper.FoodMapper;
import com.olehprukhnytskyi.macrotrackerfoodservice.mapper.NutrimentsMapper;
import com.olehprukhnytskyi.macrotrackerfoodservice.model.Food;
import com.olehprukhnytskyi.macrotrackerfoodservice.repository.FoodRepository;
import com.olehprukhnytskyi.macrotrackerfoodservice.service.CounterService;
import com.olehprukhnytskyi.macrotrackerfoodservice.service.FoodService;
import com.olehprukhnytskyi.macrotrackerfoodservice.service.GeminiService;
import com.olehprukhnytskyi.macrotrackerfoodservice.service.S3StorageService;
import com.olehprukhnytskyi.macrotrackerfoodservice.util.HashUtils;
import com.olehprukhnytskyi.macrotrackerfoodservice.util.ImageUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FoodServiceImpl implements FoodService {
    private static final int MAX_RETRIES = 3;
    private static final int FOOD_IMAGE_SIZE = 400;
    private final ElasticsearchClient elasticsearchClient;
    private final NutrimentsMapper nutrimentsMapper;
    private final FoodRepository foodRepository;
    private final CounterService counterService;
    private final GeminiService geminiService;
    private final FoodMapper foodMapper;
    private final S3StorageService s3StorageService;

    @Transactional
    @Override
    public FoodResponseDto createFoodWithImages(FoodRequestDto dto,
                                                MultipartFile image, Long userId) {
        ImageUtils.validateImage(image);
        try {
            if (dto.getCode() != null) {
                Optional<Food> existing = foodRepository.findById(dto.getCode());
                if (existing.isPresent()) {
                    if (isSameProduct(existing.get(), dto)) {
                        return foodMapper.toDto(existing.get());
                    }
                    throw new ConflictException("Product with this code"
                                                + " already exists with different data");
                }
            }

            Food food = createNewFood(dto, userId);

            ByteArrayInputStream resizedStream = ImageUtils.resizeImage(image, FOOD_IMAGE_SIZE);
            String imageKey = ImageUtils.generateImageKey(image, food.getId(), FOOD_IMAGE_SIZE);
            String imageUrl = s3StorageService.uploadFile(resizedStream,
                    resizedStream.available(), imageKey, image.getContentType());
            food.setImageUrl(imageUrl);

            int attempts = 0;
            while (attempts < MAX_RETRIES) {
                try {
                    Food saved = foodRepository.save(food);
                    return foodMapper.toDto(saved);
                } catch (DuplicateKeyException e) {
                    attempts++;
                    if (attempts == MAX_RETRIES) {
                        throw new ConflictException("Duplicate key error after max retries", e);
                    }
                } catch (DataIntegrityViolationException e) {
                    throw new BadRequestException("Invalid data for saving food", e);
                }
            }
            throw new InternalServerErrorException(
                    "Failed to save food after " + MAX_RETRIES + " attempts");
        } catch (ConflictException | BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException("Unexpected error while saving food", e);
        }
    }

    @Override
    public List<FoodResponseDto> findByQuery(String query, int offset, int limit) {
        if (query == null || query.trim().isEmpty()) {
            throw new BadRequestException("Query must not be null or empty");
        }
        String normalizedQuery = query.trim().toLowerCase()
                .replaceAll("[^\\p{L}\\p{N}\\s]", "");
        String[] tokens = normalizedQuery.split("\\s+");

        Query searchQuery;
        try {
            searchQuery = Query.of(q -> q.bool(b -> {
                for (String token : tokens) {
                    String fuzziness = token.length() > 3 ? "AUTO" : "2";
                    b.should(s -> s.multiMatch(mm -> mm
                            .fields("product_name^4", "_keywords^3",
                                    "generic_name^2", "brands^2")
                            .query(token)
                            .fuzziness(fuzziness)
                    ));
                    if (isValidCode(token)) {
                        String tokenNoZeros = token.replaceFirst("^0+(?!$)", "");
                        processBarcode(b, tokenNoZeros);
                    }
                }
                b.minimumShouldMatch("1");
                return b;
            }));
        } catch (Exception e) {
            throw new BadRequestException("Failed to construct search query", e);
        }

        try {
            SearchResponse<Food> response = elasticsearchClient.search(
                    s -> s.index("macro_tracker.foods")
                            .query(searchQuery)
                            .from(offset)
                            .size(limit),
                    Food.class
            );
            if (response == null
                    || response.hits() == null
                    || response.hits().hits() == null) {
                return Collections.emptyList();
            }
            return response.hits().hits().stream()
                    .map(hit -> {
                        if (hit.source() == null) {
                            return null;
                        }
                        hit.source().setId(hit.id());
                        return foodMapper.toDto(hit.source());
                    })
                    .filter(Objects::nonNull)
                    .toList();
        } catch (IOException e) {
            throw new SearchServiceException("Failed to execute search request", e);
        } catch (Exception e) {
            throw new InternalServerErrorException("Unexpected error during search", e);
        }
    }

    @Override
    public FoodResponseDto findById(String id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Food not found with id: " + id));
        return foodMapper.toDto(food);
    }

    @Override
    public List<String> getSearchSuggestions(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String normalized = query.trim().toLowerCase();
        try {
            SearchResponse<Food> response = elasticsearchClient.search(
                    s -> s.index("macro_tracker.foods")
                            .query(q -> q.bool(b -> b
                                    .should(s1 -> s1.matchPhrase(mp -> mp
                                            .field("product_name")
                                            .query(normalized)
                                            .boost(10f)))
                                    .should(s2 -> s2.multiMatch(m -> m
                                            .fields("product_name",
                                                    "product_name._2gram",
                                                    "product_name._3gram")
                                            .query(normalized)
                                            .type(TextQueryType.BoolPrefix)
                                            .boost(4f)))
                                    .should(s3 -> s3.match(m -> m
                                            .field("product_name_ngram")
                                            .query(normalized)
                                            .fuzziness("AUTO")
                                            .boost(2f)))
                                    .minimumShouldMatch("1")
                            )),
                    Food.class
            );
            if (response == null || response.hits() == null || response.hits().hits() == null) {
                return Collections.emptyList();
            }
            return response.hits().hits().stream()
                    .map(hit -> hit.source() != null ? hit.source().getProductName() : null)
                    .filter(Objects::nonNull)
                    .distinct()
                    .limit(16)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new SearchServiceException(
                    "Failed to fetch search suggestions from Elasticsearch", e);
        }
    }

    @Override
    public FoodResponseDto patch(String id, FoodPatchRequestDto dto) {
        try {
            Food existing = foodRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Food not found with id: " + id));
            foodMapper.updateFoodFromPatchDto(dto, existing);
            Food saved = foodRepository.save(existing);
            return foodMapper.toDto(saved);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException("Unexpected error while patching food", e);
        }
    }

    @Transactional
    @Override
    public void deleteByIdAndUserId(String id, Long userId) {
        foodRepository.deleteByIdAndUserId(id, userId);
        s3StorageService.deleteFolder("images/products/" + id + "/");
    }

    private Food createNewFood(FoodRequestDto request, Long userId) {
        Food food = foodMapper.toModel(request);
        String code = request.getCode() != null && isValidCode(request.getCode())
                ? request.getCode()
                : generateInternalCode();
        food.setUserId(userId);
        food.setKeywords(geminiService.generateKeywords(food));
        food.setId(code);
        food.setCode(code);
        return food;
    }

    private boolean isValidCode(String code) {
        return code.matches("\\d{8}|\\d{12}|\\d{13}|\\d{24}");
    }

    private String generateInternalCode() {
        Long sequence = counterService.getNextSequence("food_code");
        return "200" + String.format("%010d", sequence);
    }

    private boolean isSameProduct(Food existingFood, FoodRequestDto newRequest) {
        return existingFood.getProductName().equals(newRequest.getProductName())
                && existingFood.getBrands().equals(newRequest.getBrands())
                && existingFood.getGenericName().equals(newRequest.getGenericName())
                && existingFood.getNutriments().equals(
                        nutrimentsMapper.toModel(newRequest.getNutriments()));
    }

    private String generateDataHash(FoodRequestDto request) {
        String uniqueData = request.getProductName()
                + request.getBrands()
                + request.getGenericName()
                + nutrimentsMapper.toModel(request.getNutriments()).toString();
        return HashUtils.generateSha256Hash(uniqueData);
    }

    private void processBarcode(BoolQuery.Builder b, String tokenNoZeros) {
        String tokenAsEan13 = tokenNoZeros.length() <= 13
                ? String.format("%013d", Long.parseLong(tokenNoZeros))
                : tokenNoZeros;
        b.should(s -> s.term(t -> t
                .field("code")
                .value(tokenAsEan13)
                .boost(5f)
        ));
        String tokenAsEan8 = tokenNoZeros.length() <= 8
                ? String.format("%08d", Long.parseLong(tokenNoZeros))
                : tokenNoZeros;
        b.should(s -> s.term(t -> t
                .field("code")
                .value(tokenAsEan8)
                .boost(5f)
        ));
        String tokenAsUpc = tokenNoZeros.length() <= 12
                ? String.format("%012d", Long.parseLong(tokenNoZeros))
                : tokenNoZeros;
        b.should(s -> s.term(t -> t
                .field("code")
                .value(tokenAsUpc)
                .boost(5f)
        ));
        String tokenAsEan24 = tokenNoZeros.length() <= 24
                ? String.format("%024d", Long.parseLong(tokenNoZeros))
                : tokenNoZeros;
        b.should(s -> s.term(t -> t
                .field("code")
                .value(tokenAsEan24)
                .boost(5f)
        ));
    }
}
