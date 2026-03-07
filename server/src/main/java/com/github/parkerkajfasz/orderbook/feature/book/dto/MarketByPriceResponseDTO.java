package com.github.parkerkajfasz.orderbook.feature.book.dto;

import java.util.TreeMap;

public record MarketByPriceResponseDTO(TreeMap<Integer, Integer> bidMBPData, TreeMap<Integer, Integer> askMBPData) {
}
