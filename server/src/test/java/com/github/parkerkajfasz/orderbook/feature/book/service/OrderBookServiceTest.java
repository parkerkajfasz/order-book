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

public class OrderBookServiceTest {

    @Test
    public void addToOrderBook_addsBuyOrderAndReturnsResponse() {
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
    public void addToOrderBook_addsSellOrderAndReturnsResponse() {
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
}
