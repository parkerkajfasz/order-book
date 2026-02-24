package com.github.parkerkajfasz.orderbook.feature.book.domain;

import com.github.parkerkajfasz.orderbook.feature.order.domain.Order;
import com.github.parkerkajfasz.orderbook.feature.order.domain.OrderType;
import com.github.parkerkajfasz.orderbook.feature.order.domain.Side;
import com.github.parkerkajfasz.orderbook.feature.order.domain.TimeInForce;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.Instant;

//class OrderBookTest {
//
////    @Mock
////    private OrderBook orderBook;
//
//    @Test
//    void testAddOrder() {
//
//        // can i just have OrderBook orderBook = new OrderBook();, or would i use mockito?
//
//        OrderBook orderBook = new OrderBook();
//
//        Order bid1 = new Order(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 100, 105, Instant.now());
//        Order ask1 = new Order(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 101, 105, Instant.now());
//        Order bid2 = new Order(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 102, 105, Instant.now());
//        Order ask2 = new Order(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 102, 104, Instant.now());
//        Order bid3 = new Order(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 101, 105, Instant.now());
//        Order ask3 = new Order(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 103, 104, Instant.now());
//        Order bid4 = new Order(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 102, 105, Instant.now());
//        Order ask4 = new Order(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 104, 104, Instant.now());
//        Order bid5 = new Order(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 101, 105, Instant.now());
//        Order ask5 = new Order(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 102, 104, Instant.now());
//
//        orderBook.addOrder(bid1);
//        orderBook.addOrder(ask1);
//        orderBook.addOrder(bid2);
//        orderBook.addOrder(ask2);
//        orderBook.addOrder(bid3);
//        orderBook.addOrder(ask3);
//        orderBook.addOrder(bid4);
//        orderBook.addOrder(ask4);
//        orderBook.addOrder(bid5);
//        orderBook.addOrder(ask5);
//
//        /**
//         *
//         * Bid Expected Result (By Price Level)
//         * 102: Bid 2, Bid 4
//         * 101: Bid 3, Bid 5
//         * 100: Bid 1
//         *
//         */
//
//        System.out.println(orderBook.getBids());
//        System.out.println(orderBook.getAsks());
//
//
//        /**
//         *
//         * Ask Expected Result (By Price Level)
//         * 102: Ask 2, Ask 5
//         * 101: Ask 1
//         * 100: Ask 3, Ask 4
//         *
//         */
//    }
//}
