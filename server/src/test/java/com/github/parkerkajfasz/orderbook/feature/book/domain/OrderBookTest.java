package com.github.parkerkajfasz.orderbook.feature.book.domain;

import com.github.parkerkajfasz.orderbook.feature.order.domain.Order;
import com.github.parkerkajfasz.orderbook.feature.order.domain.OrderType;
import com.github.parkerkajfasz.orderbook.feature.order.domain.Side;
import com.github.parkerkajfasz.orderbook.feature.order.domain.TimeInForce;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

public class OrderBookTest {

//    @Test
//    void AddOrderValidation() {
//        OrderBook orderBook = new OrderBook();
//
//        Order bid1 = new Order(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 100, 105, Instant.now());
//        Order bid2 = new Order(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 50, 104, Instant.now());
//        Order bid3 = new Order(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 100, 103, Instant.now());
//        Order ask1 = new Order(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 80, 102, Instant.now());
//        Order ask2 = new Order(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 50, 101, Instant.now());
//        Order ask3 = new Order(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 80, 100, Instant.now());
//
//        orderBook.addOrder(bid1);
//        orderBook.addOrder(bid2);
//        orderBook.addOrder(bid3);
//        orderBook.addOrder(ask1);
//        orderBook.addOrder(ask2);
//        orderBook.addOrder(ask3);
//
//        TreeMap<Integer, Queue<Order>> bids = orderBook.getBids();
//        for (Map.Entry<Integer, Queue<Order>> entry : bids.entrySet()) {
//            System.out.println("Bids Price Level: " + entry.getKey());
//
//            Queue<Order> orders = entry.getValue();
//
//            for (Order order : orders) {
//                System.out.println("Order: " + order.getVolume());
//            }
//        }
//
//        TreeMap<Integer, Queue<Order>> asks = orderBook.getAsks();
//        for (Map.Entry<Integer, Queue<Order>> entry : asks.entrySet()) {
//            System.out.println("Asks Price Level: " + entry.getKey());
//
//            Queue<Order> orders = entry.getValue();
//
//            for (Order order : orders) {
//                System.out.println("Order: " + order.getVolume());
//            }
//        }
//    }

    @Test
    void RemoveOrderValidation() {
        
    }
}
