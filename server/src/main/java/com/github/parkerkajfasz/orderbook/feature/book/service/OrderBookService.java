package com.github.parkerkajfasz.orderbook.feature.book.service;

import com.github.parkerkajfasz.orderbook.feature.book.domain.OrderBook;
import com.github.parkerkajfasz.orderbook.feature.book.dto.BestBidOfferDTO;
import com.github.parkerkajfasz.orderbook.feature.order.domain.Order;
import com.github.parkerkajfasz.orderbook.feature.order.domain.OrderType;
import com.github.parkerkajfasz.orderbook.feature.order.domain.Side;
import com.github.parkerkajfasz.orderbook.feature.order.domain.TimeInForce;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class OrderBookService {

    private final OrderBook orderBook;

    public OrderBookService(OrderBook orderBook) {
        this.orderBook = orderBook;
    }

    public BestBidOfferDTO getBestBidOfferData() {
        return new BestBidOfferDTO(orderBook.getBestBidPrice(), orderBook.getBestBidVolume(), orderBook.getBestAskPrice(), orderBook.getBestAskVolume());
    }
//    public void getMarketByPrice() {}
//
//    public void getMarketByOrder() {}
}
