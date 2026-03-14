package com.github.parkerkajfasz.orderbook.feature.order.dto;

import com.github.parkerkajfasz.orderbook.exception.TradeCompatibility;
import com.github.parkerkajfasz.orderbook.feature.order.domain.OrderType;
import com.github.parkerkajfasz.orderbook.feature.order.domain.Side;
import com.github.parkerkajfasz.orderbook.feature.order.domain.TimeInForce;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalTime;

@TradeCompatibility()
public record OrderRequestDTO(
        @NotNull(message = "must not be null")
        OrderType orderType,
        @NotNull(message = "must not be null")
        TimeInForce timeInForce,
        @NotNull(message = "must not be null")
        Side side,
        @Positive(message = "must be positive")
        int price,
        @Positive(message = "must be positive")
        int volume
//        @NotNull(message = "must not be null")
//        LocalTime timestamp
) {}
