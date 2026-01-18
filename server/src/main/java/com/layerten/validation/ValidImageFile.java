package com.layerten.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validation annotation for image file types.
 * Validates that uploaded files are valid image formats (JPEG, PNG, GIF, WebP).
 */
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ImageFileValidator.class)
@Documented
public @interface ValidImageFile {
    
    String message() default "File must be a valid image format (JPEG, PNG, GIF, WebP)";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
