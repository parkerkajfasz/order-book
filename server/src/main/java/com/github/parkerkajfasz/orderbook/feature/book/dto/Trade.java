package com.github.parkerkajfasz.orderbook.feature.book.dto;

import com.github.parkerkajfasz.orderbook.feature.order.domain.Side;

import java.time.LocalTime;

public record Trade(LocalTime timestamp, int volumeTraded, int priceTraded, Side makerSide) {
}
