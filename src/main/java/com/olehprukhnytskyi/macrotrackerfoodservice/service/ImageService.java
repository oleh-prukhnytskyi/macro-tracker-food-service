package com.olehprukhnytskyi.macrotrackerfoodservice.service;

import com.olehprukhnytskyi.macrotrackerfoodservice.exception.BadRequestException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageService {
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    public void validateImage(MultipartFile image) {
        if (image.isEmpty()) {
            throw new BadRequestException("Empty file uploaded");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(image.getContentType())) {
            throw new BadRequestException("Invalid file type: only JPG, PNG, and WEBP are allowed");
        }
        if (image.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size must not exceed 5 MB");
        }
    }

    public ByteArrayInputStream resizeImage(MultipartFile file, int size) {
        try {
            BufferedImage resizedImage = Thumbnails.of(ImageIO.read(file.getInputStream()))
                    .width(size)
                    .keepAspectRatio(true)
                    .asBufferedImage();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, detectImageFormat(file), baos);
            return new ByteArrayInputStream(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to resize image", e);
        }
    }

    public String detectImageFormat(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            ImageInputStream iis = ImageIO.createImageInputStream(is);
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                return reader.getFormatName().toLowerCase();
            } else {
                throw new IllegalArgumentException("Unsupported image format");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to detect image format", e);
        }
    }

    public String generateImageKey(MultipartFile file, String foodId, int imageWidth) {
        String format = detectImageFormat(file);
        return "images/products/" + foodId + "/" + imageWidth + "." + format;
    }
}
