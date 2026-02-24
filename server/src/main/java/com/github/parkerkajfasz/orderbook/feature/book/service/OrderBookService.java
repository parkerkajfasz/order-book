package com.github.parkerkajfasz.orderbook.feature.book.service;

import com.github.parkerkajfasz.orderbook.feature.book.domain.OrderBook;
import com.github.parkerkajfasz.orderbook.feature.book.dto.BestBidOfferDTO;
import com.github.parkerkajfasz.orderbook.feature.order.domain.Order;
import org.springframework.stereotype.Service;

@Service
public class OrderBookService {

    private final OrderBook orderBook;

    public OrderBookService(OrderBook orderBook) {
        this.orderBook = orderBook;
    }

    public Order addToOrderBook(Order order) {
        orderBook.addOrder(order);
        return order;
    }

    public BestBidOfferDTO getBestBidOfferData() {
        return new BestBidOfferDTO(orderBook.getBestBidPrice(), orderBook.getBestBidVolume(), orderBook.getBestAskPrice(), orderBook.getBestAskVolume());
    }
//    public void getMarketByPrice() {}
//
//    public void getMarketByOrder() {}
}
