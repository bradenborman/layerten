package com.layerten.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

/**
 * Validator for file size limits.
 * Validates that uploaded files do not exceed a maximum size.
 */
public class FileSizeValidator implements ConstraintValidator<ValidFileSize, MultipartFile> {
    
    private long maxSizeInBytes;
    
    @Override
    public void initialize(ValidFileSize constraintAnnotation) {
        this.maxSizeInBytes = constraintAnnotation.maxSizeInBytes();
    }
    
    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return true; // Use @NotNull or @NotEmpty for required validation
        }
        
        return file.getSize() <= maxSizeInBytes;
    }
}
