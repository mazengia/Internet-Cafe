package com.maze.internet_cafe.utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MaxSizeConstraintValidator implements ConstraintValidator<MaxSizeConstraint, long[]> {
    @Override
    public boolean isValid(long[] values, ConstraintValidatorContext context) {
        return values.length <= 11;
    }
}
