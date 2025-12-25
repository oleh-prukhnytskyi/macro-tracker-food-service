package com.olehprukhnytskyi.macrotrackerfoodservice.service;

import com.olehprukhnytskyi.exception.InternalServerException;
import com.olehprukhnytskyi.exception.error.CommonErrorCode;
import com.olehprukhnytskyi.macrotrackerfoodservice.model.Food;
import java.io.ByteArrayInputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodAssetService {
    private static final int FOOD_IMAGE_SIZE = 400;
    private final S3StorageService s3StorageService;
    private final ImageService imageService;

    public void processAndUploadImage(Food food, MultipartFile image) {
        if (image == null) {
            return;
        }
        log.debug("Processing image for food id={}", food.getId());
        try {
            imageService.validateImage(image);
            ByteArrayInputStream resizedStream = imageService
                    .resizeImage(image, FOOD_IMAGE_SIZE);
            String imageKey = imageService
                    .generateImageKey(image, food.getId(), FOOD_IMAGE_SIZE);
            String imageUrl = s3StorageService.uploadFile(resizedStream,
                    resizedStream.available(), imageKey, image.getContentType());
            food.setImageUrl(imageUrl);
            log.trace("Image uploaded successfully key={}", imageKey);
        } catch (Exception e) {
            log.error("Error processing image for food id={}", food.getId(), e);
            throw new InternalServerException(CommonErrorCode.INTERNAL_ERROR,
                    "Error processing image", e);
        }
    }
}
