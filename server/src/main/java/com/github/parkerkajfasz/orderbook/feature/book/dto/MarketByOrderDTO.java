package com.github.parkerkajfasz.orderbook.feature.book.dto;

import com.github.parkerkajfasz.orderbook.feature.order.domain.Order;

import java.util.List;

public record MarketByOrderDTO(List<MarketByOrderEntryDTO> bids, List<MarketByOrderEntryDTO> asks) {
}
