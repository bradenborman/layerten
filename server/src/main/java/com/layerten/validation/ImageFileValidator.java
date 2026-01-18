package com.layerten.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * Validator for image file types.
 * Validates that uploaded files are valid image formats.
 */
public class ImageFileValidator implements ConstraintValidator<ValidImageFile, MultipartFile> {
    
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/gif",
        "image/webp"
    );
    
    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return true; // Use @NotNull or @NotEmpty for required validation
        }
        
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        
        return ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase());
    }
}
