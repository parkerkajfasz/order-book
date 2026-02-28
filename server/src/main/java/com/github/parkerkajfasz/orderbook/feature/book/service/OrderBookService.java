package com.github.parkerkajfasz.orderbook.feature.book.service;

import com.github.parkerkajfasz.orderbook.feature.book.domain.OrderBook;
import com.github.parkerkajfasz.orderbook.feature.book.dto.BestBidOfferResponseDTO;
import com.github.parkerkajfasz.orderbook.feature.order.domain.Order;
import com.github.parkerkajfasz.orderbook.feature.order.dto.OrderRequestDTO;
import com.github.parkerkajfasz.orderbook.feature.order.dto.OrderResponseDTO;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderBookService {

    private final OrderBook orderBook;
    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);

    public OrderBookService(OrderBook orderBook) {
        this.orderBook = orderBook;
    }

    public OrderResponseDTO addToOrderBook(OrderRequestDTO orderRequest) {

        Order order = new Order(
                ID_GENERATOR.getAndIncrement(),
                orderRequest.orderType(),
                orderRequest.timeInForce(),
                orderRequest.side(),
                orderRequest.price(),
                orderRequest.volume(),
                orderRequest.timestamp()
        );
        orderBook.addOrder(order);

        return new OrderResponseDTO(order.getId(), order.getOrderType(), order.getTimeInForce(), order.getSide(), order.getPrice(), order.getVolume(), order.getTimestamp());
    }

    public BestBidOfferResponseDTO getBestBidOfferData() {
        return new BestBidOfferResponseDTO(orderBook.getBestBid().getPrice(), orderBook.getBestBid().getVolume(), orderBook.getBestAsk().getPrice(), orderBook.getBestAsk().getVolume());
    }
//    public void getMarketByPrice() {}
//
//    public void getMarketByOrder() {}
}
