package com.waregang.receiving_service.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = AsnRequestValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAsnRequest {
    String message() default "Invalid ASN request";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}