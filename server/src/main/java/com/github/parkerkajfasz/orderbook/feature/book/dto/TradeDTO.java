package com.github.parkerkajfasz.orderbook.feature.book.dto;

public record TradeDTO(String timestamp, int volumeTraded, int priceTraded, String makerSide) {
}
