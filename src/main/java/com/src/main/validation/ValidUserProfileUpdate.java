package com.src.main.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UserProfileUpdateValidator.class)
public @interface ValidUserProfileUpdate {

    String message() default "Invalid profile update request";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
