package com.github.parkerkajfasz.orderbook.feature.book.service;

import com.github.parkerkajfasz.orderbook.feature.book.domain.OrderBook;
import com.github.parkerkajfasz.orderbook.feature.order.domain.Order;
import com.github.parkerkajfasz.orderbook.feature.order.domain.OrderType;
import com.github.parkerkajfasz.orderbook.feature.order.domain.Side;
import com.github.parkerkajfasz.orderbook.feature.order.domain.TimeInForce;
import com.github.parkerkajfasz.orderbook.feature.order.dto.OrderRequestDTO;
import com.github.parkerkajfasz.orderbook.feature.order.dto.OrderResponseDTO;
import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class OrderBookServiceUnitTest {

    @Test
    void addToOrderBook_addsBuyOrderAndReturnsResponse() {
        OrderBook orderBook = new OrderBook();
        OrderBookService orderBookService = new OrderBookService(orderBook);

        Instant instant = Instant.now();
        OrderRequestDTO orderRequest = new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 5, 100, instant);
        OrderResponseDTO orderResponse = orderBookService.addToOrderBook(orderRequest);

        assertFalse(orderBook.getBids().isEmpty());

        Order order = orderBook.getBids().firstEntry().getValue().peek();
        assertNotNull(order.getId());
        assertEquals(OrderType.LIMIT, order.getOrderType());
        assertEquals(TimeInForce.GOOD_TILL_CANCEL, order.getTimeInForce());
        assertEquals(Side.BUY, order.getSide());
        assertEquals(5, order.getPrice());
        assertEquals(100, order.getVolume());
        assertEquals(instant, order.getTimestamp());
    }

    @Test
    void addToOrderBook_addsSellOrderAndReturnsResponse() {
        OrderBook orderBook = new OrderBook();
        OrderBookService orderBookService = new OrderBookService(orderBook);

        Instant instant = Instant.now();
        OrderRequestDTO orderRequest = new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 5, 100, instant);
        OrderResponseDTO orderResponse = orderBookService.addToOrderBook(orderRequest);

        assertFalse(orderBook.getAsks().isEmpty());

        Order order = orderBook.getAsks().firstEntry().getValue().peek();
        assertNotNull(order.getId());
        assertEquals(OrderType.LIMIT, order.getOrderType());
        assertEquals(TimeInForce.GOOD_TILL_CANCEL, order.getTimeInForce());
        assertEquals(Side.SELL, order.getSide());
        assertEquals(5, order.getPrice());
        assertEquals(100, order.getVolume());
        assertEquals(instant, order.getTimestamp());
    }

    @Test
    void matchingEngine_noMatchBothSidesRest() {
        OrderBook orderBook = new OrderBook();
        OrderBookService orderBookService = new OrderBookService(orderBook);

        Order restingOrder = new Order(1L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 100, Instant.now());
        orderBook.addOrder(restingOrder);

        Order incomingOrder = new Order(2L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 12, 50, Instant.now());
        orderBookService.scanOrderBook(incomingOrder);

        // And the price levels should only contain the orders created
        assertTrue(orderBook.getBids()
                            .get(restingOrder.getPrice())
                            .contains(restingOrder));
        assertTrue(orderBook.getAsks()
                            .get(incomingOrder.getPrice())
                            .contains(incomingOrder));

        // There should only be one price level created for each book side
        assertEquals(1, orderBook.getBids().size());
        assertEquals(1, orderBook.getAsks().size());
    }

    @Test
    void matchingEngine_exactMatchSameQuantity() {
        OrderBook orderBook = new OrderBook();
        OrderBookService orderBookService = new OrderBookService(orderBook);

        Order restingOrder = new Order(1L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 100, Instant.now());
        orderBook.addOrder(restingOrder);

        Order incomingOrder = new Order(2L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 100, Instant.now());
        orderBookService.scanOrderBook(incomingOrder);

        // Each order should be deleted because trade was fully executed
        assertFalse(orderBook.getBids().containsKey(incomingOrder.getPrice()));
        assertFalse(orderBook.getAsks().containsKey(restingOrder.getPrice()));

        // Exact match of BUY & SELL order, so price levels should be deleted after trade is fully executed
        assertEquals(0, orderBook.getBids().size());
        assertEquals(0, orderBook.getAsks().size());
    }

    @Test
    void matchingEngine_partialFillIncomingBuyLargerThanRestingSell() {
        OrderBook orderBook = new OrderBook();
        OrderBookService orderBookService = new OrderBookService(orderBook);

        Order restingOrder = new Order(1L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 40, Instant.now());
        orderBook.addOrder(restingOrder);

        Order incomingOrder = new Order(2L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 100, Instant.now());
        orderBookService.scanOrderBook(incomingOrder);

        // Only SELL order should be deleted
        assertTrue(orderBook.getBids().containsKey(incomingOrder.getPrice()));
        assertFalse(orderBook.getAsks().containsKey(restingOrder.getPrice()));

        // Partial match of BUY & SELL order, resting order should be deleted and incoming order still has 60 remaining volume
        assertEquals(1, orderBook.getBids().size());
        assertEquals(0, orderBook.getAsks().size());
        assertEquals(60, incomingOrder.getVolumeRemaining());
    }

    @Test
    void matchingEngine_partialFillIncomingSellLargerThanRestingBuy() {
        OrderBook orderBook = new OrderBook();
        OrderBookService orderBookService = new OrderBookService(orderBook);

        Order restingOrder = new Order(1L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 30, Instant.now());
        orderBook.addOrder(restingOrder);

        Order incomingOrder = new Order(2L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 80, Instant.now());
        orderBookService.scanOrderBook(incomingOrder);

        // Only BUY order should be deleted
        assertFalse(orderBook.getBids().containsKey(restingOrder.getPrice()));
        assertTrue(orderBook.getAsks().containsKey(incomingOrder.getPrice()));

        // Partial match of BUY & SELL order, resting order should be deleted and incoming order still has 60 remaining volume
        assertEquals(0, orderBook.getBids().size());
        assertEquals(1, orderBook.getAsks().size());
        assertEquals(50, incomingOrder.getVolumeRemaining());
    }

    @Test
    void matchingEngine_incomingBuySweepsMultipleAskLevels() {
        OrderBook orderBook = new OrderBook();
        OrderBookService orderBookService = new OrderBookService(orderBook);

        Order restingOrder1 = new Order(1L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 9, 20, Instant.now());
        Order restingOrder2 = new Order(2L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 30, Instant.now());
        Order restingOrder3 = new Order(3L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 11, 40, Instant.now());
        orderBook.addOrder(restingOrder1);
        orderBook.addOrder(restingOrder2);
        orderBook.addOrder(restingOrder3);

        Order incomingOrder = new Order(4L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 60, Instant.now());
        orderBookService.scanOrderBook(incomingOrder);

        // Only BUY order should be deleted
        assertFalse(orderBook.getAsks().containsKey(restingOrder1.getPrice()));
        assertFalse(orderBook.getAsks().containsKey(restingOrder2.getPrice()));
        assertTrue(orderBook.getAsks().containsKey(restingOrder3.getPrice()));
        assertTrue(orderBook.getBids().containsKey(incomingOrder.getPrice()));

        // Partial match of BUY & SELL order, resting order should be deleted and incoming order still has 60 remaining volume
        assertEquals(1, orderBook.getBids().size());
        assertEquals(1, orderBook.getAsks().size());
        assertEquals(40, restingOrder3.getVolumeRemaining());
        assertEquals(10, incomingOrder.getVolumeRemaining());
    }

    @Test
    void matchingEngine_incomingSellSweepsMultipleBidLevels() {
        OrderBook orderBook = new OrderBook();
        OrderBookService orderBookService = new OrderBookService(orderBook);

        Order restingOrder1 = new Order(1L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 11, 15, Instant.now());
        Order restingOrder2 = new Order(2L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 20, Instant.now());
        Order restingOrder3 = new Order(3L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 9, 25, Instant.now());
        orderBook.addOrder(restingOrder1);
        orderBook.addOrder(restingOrder2);
        orderBook.addOrder(restingOrder3);

        Order incomingOrder = new Order(4L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 40, Instant.now());
        orderBookService.scanOrderBook(incomingOrder);

        // Only BUY order should be deleted
        assertFalse(orderBook.getBids().containsKey(restingOrder1.getPrice()));
        assertFalse(orderBook.getBids().containsKey(restingOrder2.getPrice()));
        assertTrue(orderBook.getBids().containsKey(restingOrder3.getPrice()));
        assertTrue(orderBook.getAsks().containsKey(incomingOrder.getPrice()));

        // Partial match of BUY & SELL order, resting order should be deleted and incoming order still has 60 remaining volume
        assertEquals(1, orderBook.getBids().size());
        assertEquals(1, orderBook.getAsks().size());
        assertEquals(25, restingOrder3.getVolumeRemaining());
        assertEquals(5, incomingOrder.getVolumeRemaining());
    }

    @Test
    void matchingEngine_timePriorityAtSamePrice() {
        OrderBook orderBook = new OrderBook();
        OrderBookService orderBookService = new OrderBookService(orderBook);

        Order restingOrder1 = new Order(1L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 30, Instant.now());
        Order restingOrder2 = new Order(2L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 50, Instant.now());
        orderBook.addOrder(restingOrder1);
        orderBook.addOrder(restingOrder2);

        Order incomingOrder = new Order(3L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 60, Instant.now());
        orderBookService.scanOrderBook(incomingOrder);

        // Only BUY order should be deleted
        assertFalse(orderBook.getBids().containsKey(incomingOrder.getPrice()));
        assertTrue(orderBook.getAsks().containsKey(restingOrder2.getPrice()));

        // Partial match of BUY & SELL order, resting order should be deleted and incoming order still has 60 remaining volume
        assertEquals(0, orderBook.getBids().size());
        assertEquals(1, orderBook.getAsks().size());
        assertEquals(20, restingOrder2.getVolumeRemaining());
    }

    @Test
    void matchingEngine_betterPriceBeatsEarlierWorsePrice() {
        OrderBook orderBook = new OrderBook();
        OrderBookService orderBookService = new OrderBookService(orderBook);

        Order restingOrder1 = new Order(1L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 30, Instant.now());
        Order restingOrder2 = new Order(2L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 50, Instant.now());
        orderBook.addOrder(restingOrder1);
        orderBook.addOrder(restingOrder2);

        Order incomingOrder = new Order(3L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 60, Instant.now());
        orderBookService.scanOrderBook(incomingOrder);

        // Only BUY order should be deleted
        assertFalse(orderBook.getBids().containsKey(incomingOrder.getPrice()));
        assertTrue(orderBook.getAsks().containsKey(restingOrder2.getPrice()));

        // Partial match of BUY & SELL order, resting order should be deleted and incoming order still has 60 remaining volume
        assertEquals(0, orderBook.getBids().size());
        assertEquals(1, orderBook.getAsks().size());
        assertEquals(20, restingOrder2.getVolumeRemaining());
    }

    @Test
    void matchingEngine_crossingOrderStopsAtItsOwnLimit() {
        OrderBook orderBook = new OrderBook();
        OrderBookService orderBookService = new OrderBookService(orderBook);

        Order restingOrder1 = new Order(1L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 8, 10, Instant.now());
        Order restingOrder2 = new Order(2L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 9, 10, Instant.now());
        Order restingOrder3 = new Order(3L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 10, Instant.now());
        orderBook.addOrder(restingOrder1);
        orderBook.addOrder(restingOrder2);
        orderBook.addOrder(restingOrder3);

        Order incomingOrder = new Order(4L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 9, 50, Instant.now());
        orderBookService.scanOrderBook(incomingOrder);

        // Only BUY order should be deleted
        assertFalse(orderBook.getAsks().containsKey(restingOrder1.getPrice()));
        assertFalse(orderBook.getAsks().containsKey(restingOrder2.getPrice()));
        assertTrue(orderBook.getAsks().containsKey(restingOrder3.getPrice()));
        assertTrue(orderBook.getBids().containsKey(incomingOrder.getPrice()));

        // Partial match of BUY & SELL order, resting order should be deleted and incoming order still has 60 remaining volume
        assertEquals(1, orderBook.getBids().size());
        assertEquals(1, orderBook.getAsks().size());
        assertEquals(10, restingOrder3.getVolumeRemaining());
        assertEquals(30, incomingOrder.getVolumeRemaining());
    }
}
