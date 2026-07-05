package com.dawood.peeng.incident.validator.constraints;

import com.dawood.peeng.incident.validator.impl.DateFilterValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateFilterValidator.class)
public @interface ValidDateFilter {
    String message() default  "Only one date filter strategy is allowed.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
