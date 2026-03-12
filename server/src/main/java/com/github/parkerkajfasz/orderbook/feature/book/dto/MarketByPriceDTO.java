package com.github.parkerkajfasz.orderbook.feature.book.dto;

import java.util.List;

public record MarketByPriceDTO(List<PriceLevelDTO> bidPriceLevels, List<PriceLevelDTO> askPriceLevels) {
}