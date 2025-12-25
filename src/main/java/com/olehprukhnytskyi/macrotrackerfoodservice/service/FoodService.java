package com.olehprukhnytskyi.macrotrackerfoodservice.service;

import com.olehprukhnytskyi.exception.BadRequestException;
import com.olehprukhnytskyi.exception.ConflictException;
import com.olehprukhnytskyi.exception.InternalServerException;
import com.olehprukhnytskyi.exception.NotFoundException;
import com.olehprukhnytskyi.exception.error.CommonErrorCode;
import com.olehprukhnytskyi.exception.error.FoodErrorCode;
import com.olehprukhnytskyi.macrotrackerfoodservice.dao.FoodSearchDao;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.FoodListCacheWrapper;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.FoodPatchRequestDto;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.FoodRequestDto;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.FoodResponseDto;
import com.olehprukhnytskyi.macrotrackerfoodservice.event.FoodCreatedEvent;
import com.olehprukhnytskyi.macrotrackerfoodservice.mapper.FoodMapper;
import com.olehprukhnytskyi.macrotrackerfoodservice.mapper.NutrimentsMapper;
import com.olehprukhnytskyi.macrotrackerfoodservice.model.Food;
import com.olehprukhnytskyi.macrotrackerfoodservice.repository.mongo.FoodRepository;
import com.olehprukhnytskyi.model.OutboxEvent;
import com.olehprukhnytskyi.repository.jpa.OutboxRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodService {
    private final NutrimentsMapper nutrimentsMapper;
    private final FoodRepository foodRepository;
    private final FoodMapper foodMapper;
    private final OutboxRepository outboxRepository;
    private final FoodAssetService foodAssetService;
    private final FoodSearchDao foodSearchDao;
    private final FoodCodeGenerator foodCodeGenerator;
    private final ApplicationEventPublisher eventPublisher;
    private final RetryTemplate retryTemplate;

    @Transactional
    public FoodResponseDto createFoodWithImages(FoodRequestDto dto,
                                                MultipartFile image, Long userId) {
        log.info("Creating new food item for userId={}", userId);
        try {
            Optional<Food> existingSameProduct = tryFindExistingSameProduct(dto);
            if (existingSameProduct.isPresent()) {
                log.info("Returning existing food id={}", existingSameProduct.get().getId());
                return foodMapper.toDto(existingSameProduct.get());
            }
            Food food = prepareNewFood(dto, userId);
            foodAssetService.processAndUploadImage(food, image);
            Food saved = retryTemplate.execute(context -> foodRepository.save(food));
            eventPublisher.publishEvent(new FoodCreatedEvent(saved.getId(), userId));
            log.info("Food created successfully userId={} foodId={}", userId, saved.getId());
            return foodMapper.toDto(saved);
        } catch (ConflictException | BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while saving food userId={}", userId, e);
            throw new InternalServerException(CommonErrorCode.INTERNAL_ERROR,
                    "Unexpected error while saving food", e);
        }
    }

    @Cacheable(
            value = "search:results",
            key = "T(org.springframework.util.DigestUtils).md5DigestAsHex((#query"
                    + ".trim().toLowerCase() + '-' + #offset + '-' + #limit).getBytes())",
            unless = "#result == null || #result.items.isEmpty()"
    )
    public FoodListCacheWrapper findByQuery(String query, int offset, int limit) {
        log.debug("Searching foods query='{}' offset={} limit={}", query, offset, limit);
        List<Food> foods = foodSearchDao.search(query, offset, limit);
        return new FoodListCacheWrapper(foodMapper.toDto(foods));
    }

    @Cacheable(value = "food:data", key = "#id")
    public FoodResponseDto findById(String id) {
        log.debug("Fetching food by id={}", id);
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(FoodErrorCode.FOOD_NOT_FOUND,
                        "Food not found with id: " + id));
        return foodMapper.toDto(food);
    }

    @Cacheable(
            value = "search:suggestions",
            key = "T(org.springframework.util.DigestUtils)"
                    + ".md5DigestAsHex(#query.trim().toLowerCase().getBytes())",
            unless = "#result == null || #result.isEmpty()"
    )
    public List<String> getSearchSuggestions(String query) {
        log.trace("Fetching search suggestions query='{}'", query);
        return foodSearchDao.getSuggestions(query);
    }

    @CacheEvict(value = "food:data", key = "#id")
    public FoodResponseDto patch(String id, FoodPatchRequestDto dto) {
        log.info("Updating food id={}", id);
        try {
            Food existing = foodRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException(FoodErrorCode.FOOD_NOT_FOUND,
                            "Food not found with id: " + id));
            foodMapper.updateFoodFromPatchDto(dto, existing);
            Food saved = foodRepository.save(existing);
            return foodMapper.toDto(saved);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerException(CommonErrorCode.INTERNAL_ERROR,
                    "Unexpected error while patching food", e);
        }
    }

    @CacheEvict(value = "food:data", key = "#id")
    @Transactional
    public void deleteByIdAndUserId(String id, Long userId) {
        log.info("Deleting food id={} userId={}", id, userId);
        foodRepository.deleteByIdAndUserId(id, userId);
        outboxRepository.save(OutboxEvent.builder()
                .aggregateType("FOOD")
                .aggregateId(id)
                .eventType("FOOD_DELETED")
                .build());
        log.debug("Food deleted successfully id={} userId={}", id, userId);
    }

    private Food prepareNewFood(FoodRequestDto request, Long userId) {
        Food food = foodMapper.toModel(request);
        String code = foodCodeGenerator.resolveCode(request);
        food.setUserId(userId);
        food.setId(code);
        food.setCode(code);
        return food;
    }

    private boolean isSameProduct(Food existingFood, FoodRequestDto newRequest) {
        return existingFood.getProductName().equals(newRequest.getProductName())
                && existingFood.getBrands().equals(newRequest.getBrands())
                && existingFood.getGenericName().equals(newRequest.getGenericName())
                && existingFood.getNutriments().equals(
                        nutrimentsMapper.toModel(newRequest.getNutriments()));
    }

    private Optional<Food> tryFindExistingSameProduct(FoodRequestDto dto) {
        if (dto.getCode() != null) {
            Optional<Food> existing = foodRepository.findById(dto.getCode());
            if (existing.isPresent()) {
                if (isSameProduct(existing.get(), dto)) {
                    log.debug("Duplicate food detected with same data, "
                            + "returning existing id={}", existing.get().getId());
                    return existing;
                }
                throw new ConflictException(FoodErrorCode.FOOD_ALREADY_EXISTS,
                        "Food with this code already exists with different data");
            }
        }
        return Optional.empty();
    }
}
