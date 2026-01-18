package com.layerten.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validation annotation for file size limits.
 * Validates that uploaded files do not exceed a maximum size.
 */
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileSizeValidator.class)
@Documented
public @interface ValidFileSize {
    
    String message() default "File size exceeds maximum allowed size";
    
    /**
     * Maximum file size in bytes.
     * Default is 10MB.
     */
    long maxSizeInBytes() default 10 * 1024 * 1024; // 10MB
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
