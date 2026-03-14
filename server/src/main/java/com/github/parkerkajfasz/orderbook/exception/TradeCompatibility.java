package com.github.parkerkajfasz.orderbook.exception;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TradeCompatibilityValidator.class)
public @interface TradeCompatibility {
    String message() default "OrderType and TimeInForce are incompatible";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
