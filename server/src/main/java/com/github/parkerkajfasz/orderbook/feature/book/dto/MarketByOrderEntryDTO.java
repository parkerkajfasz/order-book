package com.github.parkerkajfasz.orderbook.feature.book.dto;

import com.github.parkerkajfasz.orderbook.feature.order.domain.Side;

public record MarketByOrderEntryDTO(String timestamp, int volume, int price, Side side) {
}
