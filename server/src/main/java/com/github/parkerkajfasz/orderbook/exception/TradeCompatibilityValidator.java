package com.github.parkerkajfasz.orderbook.exception;

import com.github.parkerkajfasz.orderbook.feature.order.domain.OrderType;
import com.github.parkerkajfasz.orderbook.feature.order.domain.TimeInForce;
import com.github.parkerkajfasz.orderbook.feature.order.dto.OrderRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TradeCompatibilityValidator implements ConstraintValidator<TradeCompatibility, Object> {
    @Override
    public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
        OrderRequestDTO orderRequest = (OrderRequestDTO) o;

        if (orderRequest.orderType() == OrderType.MARKET && orderRequest.timeInForce() == TimeInForce.GOOD_TILL_CANCEL) {
            return false;
        }
        return true;
    }
}
