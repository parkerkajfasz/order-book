package com.github.parkerkajfasz.orderbook.feature.book.service;

import com.github.parkerkajfasz.orderbook.feature.book.domain.OrderBook;
import com.github.parkerkajfasz.orderbook.feature.order.domain.OrderType;
import com.github.parkerkajfasz.orderbook.feature.order.domain.Side;
import com.github.parkerkajfasz.orderbook.feature.order.domain.TimeInForce;
import com.github.parkerkajfasz.orderbook.feature.order.dto.OrderRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrderBookServiceUnitTest {

    @Mock
    SimpMessagingTemplate messagingTemplate;

    private OrderBook orderBook;
    private OrderBookService orderBookService;

    @BeforeEach
    void setUp() {
        orderBook = new OrderBook();
        orderBookService = new OrderBookService(orderBook, messagingTemplate);
    }

    @Test
    void noMatchBothOrdersRest() {
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 100));
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 12, 50));

        assertTrue(orderBook.getBids().containsKey(10));
        assertTrue(orderBook.getAsks().containsKey(12));
        assertEquals(1, orderBook.getBids().size());
        assertEquals(1, orderBook.getAsks().size());
    }

    @Test
    void exactMatchRemovesBothOrders() {
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 100));
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 100));

        assertFalse(orderBook.getBids().containsKey(10));
        assertFalse(orderBook.getAsks().containsKey(10));
        assertEquals(0, orderBook.getBids().size());
        assertEquals(0, orderBook.getAsks().size());
    }

    @Test
    void partialFillBuyLargerThanSell() {
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 40));
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 100));

        assertTrue(orderBook.getBids().containsKey(10));
        assertFalse(orderBook.getAsks().containsKey(10));
        assertEquals(1, orderBook.getBids().size());
        assertEquals(0, orderBook.getAsks().size());
    }

    @Test
    void partialFillSellLargerThanBuy() {
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 30));
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 80));

        assertFalse(orderBook.getBids().containsKey(10));
        assertTrue(orderBook.getAsks().containsKey(10));
        assertEquals(0, orderBook.getBids().size());
        assertEquals(1, orderBook.getAsks().size());
    }

    @Test
    void buySweepsMultipleAskLevels() {
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 9, 20));
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 30));
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 11, 40));

        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 60));

        assertFalse(orderBook.getAsks().containsKey(9));
        assertFalse(orderBook.getAsks().containsKey(10));
        assertTrue(orderBook.getAsks().containsKey(11));
        assertTrue(orderBook.getBids().containsKey(10));
        assertEquals(1, orderBook.getBids().size());
        assertEquals(1, orderBook.getAsks().size());
        assertEquals(40, orderBook.getAsks().get(11).peek().getVolumeRemaining());
    }

    @Test
    void sellSweepsMultipleBidLevels() {
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 11, 15));
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 20));
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 9, 25));

        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 40));

        assertFalse(orderBook.getBids().containsKey(11));
        assertFalse(orderBook.getBids().containsKey(10));
        assertTrue(orderBook.getBids().containsKey(9));
        assertTrue(orderBook.getAsks().containsKey(10));
        assertEquals(1, orderBook.getBids().size());
        assertEquals(1, orderBook.getAsks().size());
        assertEquals(25, orderBook.getBids().get(9).peek().getVolumeRemaining());
    }

    @Test
    void timePriorityAtSamePrice() {
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 30));
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 50));

        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 60));

        assertFalse(orderBook.getBids().containsKey(10));
        assertTrue(orderBook.getAsks().containsKey(10));
        assertEquals(0, orderBook.getBids().size());
        assertEquals(1, orderBook.getAsks().size());
        assertEquals(20, orderBook.getAsks().get(10).peek().getVolumeRemaining());
    }

    @Test
    void crossingOrderStopsAtLimitPrice() {
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 8, 10));
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 9, 10));
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 10));

        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 9, 50));

        assertFalse(orderBook.getAsks().containsKey(8));
        assertFalse(orderBook.getAsks().containsKey(9));
        assertTrue(orderBook.getAsks().containsKey(10));
        assertTrue(orderBook.getBids().containsKey(9));
        assertEquals(1, orderBook.getBids().size());
        assertEquals(1, orderBook.getAsks().size());
        assertEquals(10, orderBook.getAsks().get(10).peek().getVolumeRemaining());
    }

    @Test
    void marketBuySweepsMultiplePriceLevels() {
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 20));
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 11, 30));

        orderBookService.processOrder(new OrderRequestDTO(OrderType.MARKET, TimeInForce.IMMEDIATE_OR_CANCEL, Side.BUY, 0, 40));

        assertFalse(orderBook.getAsks().containsKey(10));
        assertTrue(orderBook.getAsks().containsKey(11));
        assertEquals(10, orderBook.getAsks().get(11).peek().getVolumeRemaining());
    }

    @Test
    void marketOrderPartialFillCancelsRemainder() {
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 20));
        orderBookService.processOrder(new OrderRequestDTO(OrderType.MARKET, TimeInForce.IMMEDIATE_OR_CANCEL, Side.BUY, 0, 50));

        assertEquals(0, orderBook.getAsks().size());
    }

    @Test
    void limitIOCPartialFillCancelsRemainder() {
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 10));
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.IMMEDIATE_OR_CANCEL, Side.BUY, 10, 20));

        assertEquals(0, orderBook.getAsks().size());
        assertFalse(orderBook.getBids().containsKey(10));
    }

    @Test
    void limitIOCNoMatchCancelsEntireOrder() {
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.IMMEDIATE_OR_CANCEL, Side.BUY, 10, 50));

        assertEquals(0, orderBook.getBids().size());
    }

    @Test
    void marketSellSweepsMultipleBidLevels() {
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 20));
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 9, 30));

        orderBookService.processOrder(new OrderRequestDTO(OrderType.MARKET, TimeInForce.IMMEDIATE_OR_CANCEL, Side.SELL, 0, 40));

        assertFalse(orderBook.getBids().containsKey(10));
        assertTrue(orderBook.getBids().containsKey(9));
        assertEquals(10, orderBook.getBids().get(9).peek().getVolumeRemaining());
    }

    @Test
    void marketOrderOnEmptyBookIsCancelled() {
        orderBookService.processOrder(new OrderRequestDTO(OrderType.MARKET, TimeInForce.IMMEDIATE_OR_CANCEL, Side.BUY, 0, 50));

        assertTrue(orderBook.getBids().isEmpty());
        assertTrue(orderBook.getAsks().isEmpty());
    }

    @Test
    void limitSellIOCPartialFillCancelsRemainder() {
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 15));
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.IMMEDIATE_OR_CANCEL, Side.SELL, 10, 25));

        assertEquals(0, orderBook.getBids().size());
        assertEquals(0, orderBook.getAsks().size());
    }

    @Test
    void complexScenarioProcessesCorrectly() {
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 50));
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 11, 50));
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.IMMEDIATE_OR_CANCEL, Side.SELL, 10, 60));

        assertFalse(orderBook.getBids().containsKey(11));
        assertEquals(40, orderBook.getBids().get(10).peek().getVolumeRemaining());

        orderBookService.processOrder(new OrderRequestDTO(OrderType.MARKET, TimeInForce.IMMEDIATE_OR_CANCEL, Side.SELL, 0, 20));

        assertEquals(20, orderBook.getBids().get(10).peek().getVolumeRemaining());

        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 12, 100));

        assertEquals(1, orderBook.getAsks().size());
        assertTrue(orderBook.getAsks().containsKey(12));

        orderBookService.processOrder(new OrderRequestDTO(OrderType.MARKET, TimeInForce.IMMEDIATE_OR_CANCEL, Side.BUY, 0, 150));

        assertEquals(0, orderBook.getAsks().size());
        assertEquals(20, orderBook.getBids().get(10).peek().getVolumeRemaining());
    }

    @Test
    void mixedScenarioLimitGtcIocAndMarketSellFlow() {
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 11, 40));
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 12, 50));

        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.IMMEDIATE_OR_CANCEL, Side.BUY, 12, 60));

        assertFalse(orderBook.getAsks().containsKey(11));
        assertTrue(orderBook.getAsks().containsKey(12));
        assertEquals(30, orderBook.getAsks().get(12).peek().getVolumeRemaining());

        orderBookService.processOrder(new OrderRequestDTO(OrderType.MARKET, TimeInForce.IMMEDIATE_OR_CANCEL, Side.BUY, 0, 20));

        assertEquals(10, orderBook.getAsks().get(12).peek().getVolumeRemaining());

        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 25));

        assertTrue(orderBook.getBids().containsKey(10));
        assertEquals(25, orderBook.getBids().get(10).peek().getVolumeRemaining());

        orderBookService.processOrder(new OrderRequestDTO(OrderType.MARKET, TimeInForce.IMMEDIATE_OR_CANCEL, Side.SELL, 0, 40));

        assertFalse(orderBook.getBids().containsKey(10));
        assertTrue(orderBook.getAsks().containsKey(12));
        assertEquals(10, orderBook.getAsks().get(12).peek().getVolumeRemaining());
    }

    @Test
    void mixedScenarioMarketSellThenLimitGtcThenIocBuy() {
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 30));
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 9, 40));

        orderBookService.processOrder(new OrderRequestDTO(OrderType.MARKET, TimeInForce.IMMEDIATE_OR_CANCEL, Side.SELL, 0, 50));

        assertFalse(orderBook.getBids().containsKey(10));
        assertTrue(orderBook.getBids().containsKey(9));
        assertEquals(20, orderBook.getBids().get(9).peek().getVolumeRemaining());

        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 11, 35));

        assertTrue(orderBook.getAsks().containsKey(11));
        assertEquals(35, orderBook.getAsks().get(11).peek().getVolumeRemaining());

        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.IMMEDIATE_OR_CANCEL, Side.BUY, 11, 25));

        assertTrue(orderBook.getAsks().containsKey(11));
        assertEquals(10, orderBook.getAsks().get(11).peek().getVolumeRemaining());
        assertTrue(orderBook.getBids().containsKey(9));
        assertEquals(20, orderBook.getBids().get(9).peek().getVolumeRemaining());
    }

    @Test
    void mixedScenarioIocMissesThenGtcRestsThenMarketCrossesBook() {
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.IMMEDIATE_OR_CANCEL, Side.BUY, 10, 40));

        assertTrue(orderBook.getBids().isEmpty());
        assertTrue(orderBook.getAsks().isEmpty());

        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 12, 30));
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 13, 50));

        assertTrue(orderBook.getAsks().containsKey(12));
        assertTrue(orderBook.getAsks().containsKey(13));

        orderBookService.processOrder(new OrderRequestDTO(OrderType.MARKET, TimeInForce.IMMEDIATE_OR_CANCEL, Side.BUY, 0, 60));

        assertFalse(orderBook.getAsks().containsKey(12));
        assertTrue(orderBook.getAsks().containsKey(13));
        assertEquals(20, orderBook.getAsks().get(13).peek().getVolumeRemaining());

        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 11, 15));

        assertTrue(orderBook.getBids().containsKey(11));
        assertEquals(15, orderBook.getBids().get(11).peek().getVolumeRemaining());
    }

    @Test
    void mixedScenarioRestingBookSurvivesSeveralDifferentOrderStyles() {
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 40));
        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 9, 30));

        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.IMMEDIATE_OR_CANCEL, Side.SELL, 10, 25));

        assertTrue(orderBook.getBids().containsKey(10));
        assertEquals(15, orderBook.getBids().get(10).peek().getVolumeRemaining());

        orderBookService.processOrder(new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 12, 50));

        assertTrue(orderBook.getAsks().containsKey(12));
        assertEquals(50, orderBook.getAsks().get(12).peek().getVolumeRemaining());

        orderBookService.processOrder(new OrderRequestDTO(OrderType.MARKET, TimeInForce.IMMEDIATE_OR_CANCEL, Side.BUY, 0, 20));

        assertTrue(orderBook.getAsks().containsKey(12));
        assertEquals(30, orderBook.getAsks().get(12).peek().getVolumeRemaining());

        orderBookService.processOrder(new OrderRequestDTO(OrderType.MARKET, TimeInForce.IMMEDIATE_OR_CANCEL, Side.SELL, 0, 40));

        assertFalse(orderBook.getBids().containsKey(10));
        assertTrue(orderBook.getBids().containsKey(9));
        assertEquals(5, orderBook.getBids().get(9).peek().getVolumeRemaining());
        assertTrue(orderBook.getAsks().containsKey(12));
        assertEquals(30, orderBook.getAsks().get(12).peek().getVolumeRemaining());
    }
}