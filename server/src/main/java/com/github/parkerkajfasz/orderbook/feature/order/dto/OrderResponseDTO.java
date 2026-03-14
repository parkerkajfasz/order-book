package com.github.parkerkajfasz.orderbook.feature.order.dto;

import com.github.parkerkajfasz.orderbook.feature.order.domain.OrderType;
import com.github.parkerkajfasz.orderbook.feature.order.domain.Side;
import com.github.parkerkajfasz.orderbook.feature.order.domain.TimeInForce;

import java.time.LocalTime;

public record OrderResponseDTO(Long id, OrderType orderType, TimeInForce timeInForce, Side side, int price, int volume, LocalTime timestamp) {
}
