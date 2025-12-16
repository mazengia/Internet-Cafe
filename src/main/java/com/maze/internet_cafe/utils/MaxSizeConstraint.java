package com.maze.internet_cafe.utils;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Constraint(validatedBy = MaxSizeConstraintValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxSizeConstraint {
    String message() default "The candidates list cannot contain more than 5 .";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
