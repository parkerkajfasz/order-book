package com.github.parkerkajfasz.orderbook.feature.book.service;

import com.github.parkerkajfasz.orderbook.feature.book.domain.OrderBook;
import com.github.parkerkajfasz.orderbook.feature.book.dto.BestBidOfferResponseDTO;
import com.github.parkerkajfasz.orderbook.feature.order.domain.Order;
import com.github.parkerkajfasz.orderbook.feature.order.domain.Side;
import com.github.parkerkajfasz.orderbook.feature.order.dto.OrderRequestDTO;
import com.github.parkerkajfasz.orderbook.feature.order.dto.OrderResponseDTO;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderBookService {

    private final OrderBook orderBook;
    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);
    private static final Logger log = LoggerFactory.getLogger(OrderBookService.class);

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
        log.info("Order {} added to order book", order.getId());
        scanOrderBook(order);

        return new OrderResponseDTO(order.getId(), order.getOrderType(), order.getTimeInForce(), order.getSide(), order.getPrice(), order.getVolume(), order.getTimestamp());
    }

    public BestBidOfferResponseDTO getBestBidOfferData() {
        return new BestBidOfferResponseDTO(orderBook.getBestBid().getPrice(), orderBook.getBestBid().getVolume(), orderBook.getBestAsk().getPrice(), orderBook.getBestAsk().getVolume());
    }

    /**
     * Matching Engine related code. Will run each time new order comes through controller
     */
    public void scanOrderBook(Order incomingOrder) {


        while (incomingOrder.getVolumeRemaining() > 0) {

            Order restingOrder = getRestingOrder(incomingOrder);
            if (restingOrder == null || !crossedBook(incomingOrder, restingOrder)) {
                break;
            }

            trade(incomingOrder, restingOrder);
            if (restingOrder.getVolumeRemaining() == 0) orderBook.removeOrder(restingOrder);
        }

        if (incomingOrder.getVolumeRemaining() > 0) { // if incomingOrder volume has volume remaining, but can't cross book, then add to book.
            orderBook.addOrder(incomingOrder);
            log.info("Order {} added to order book", incomingOrder.getId());
        }
    }

    private Order getRestingOrder(Order incomingOrder) {
        Order restingOrder;

        if (incomingOrder.getSide() == Side.BUY) {
            restingOrder = orderBook.getBestAsk();
        } else {
            restingOrder = orderBook.getBestBid();
        }
        return restingOrder;
    }

    private boolean crossedBook(Order incomingOrder, Order restingOrder) {
        if (incomingOrder == null || restingOrder == null || incomingOrder.getVolumeRemaining() == 0 || restingOrder.getVolumeRemaining() == 0) return false;

        if (incomingOrder.getSide() == Side.BUY) {
            return incomingOrder.getPrice() >= restingOrder.getPrice(); // BUY crosses if its price >= price of best ASK
        } else {
            return incomingOrder.getPrice() <= restingOrder.getPrice(); // SELL crosses if its price <= price of best BID
        }
    }

    private void trade(Order incomingOrder, Order restingOrder) {
        int volumeTraded = Math.min(incomingOrder.getVolumeRemaining(), restingOrder.getVolumeRemaining());

        incomingOrder.subtractVolume(volumeTraded);
        restingOrder.subtractVolume(volumeTraded);
        log.info("TRADE executed: {} SYMBL @ {} | maker={} | taker={} | makerId={} | takerId={}", volumeTraded, restingOrder.getPrice(), incomingOrder.getSide(), restingOrder.getSide(), incomingOrder.getId(), restingOrder.getId());
    }
}
