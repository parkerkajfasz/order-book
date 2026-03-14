package com.github.parkerkajfasz.orderbook.feature.book.service;

import com.github.parkerkajfasz.orderbook.feature.book.domain.OrderBook;
import com.github.parkerkajfasz.orderbook.feature.order.domain.Order;
import com.github.parkerkajfasz.orderbook.feature.order.domain.OrderType;
import com.github.parkerkajfasz.orderbook.feature.order.domain.Side;
import com.github.parkerkajfasz.orderbook.feature.order.domain.TimeInForce;
import com.github.parkerkajfasz.orderbook.feature.order.dto.OrderRequestDTO;
import com.github.parkerkajfasz.orderbook.feature.order.dto.OrderResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalTime;

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

//    @Test
//    void addToOrderBook_addsBuyOrderAndReturnsResponse() {
//        LocalTime instant = LocalTime.now();
//        OrderRequestDTO orderRequest = new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 5, 100, instant);
//        OrderResponseDTO orderResponse = orderBookService.addToOrderBook(orderRequest);
//
//        assertFalse(orderBook.getBids().isEmpty());
//
//        Order order = orderBook.getBids().firstEntry().getValue().peek();
//        assertNotNull(order.getId());
//        assertEquals(OrderType.LIMIT, order.getOrderType());
//        assertEquals(TimeInForce.GOOD_TILL_CANCEL, order.getTimeInForce());
//        assertEquals(Side.BUY, order.getSide());
//        assertEquals(5, order.getPrice());
//        assertEquals(100, order.getVolume());
//        assertEquals(instant, order.getTimestamp());
//    }
//
//    @Test
//    void addToOrderBook_addsSellOrderAndReturnsResponse() {
//        LocalTime instant = LocalTime.now();
//        OrderRequestDTO orderRequest = new OrderRequestDTO(OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 5, 100, instant);
//        OrderResponseDTO orderResponse = orderBookService.addToOrderBook(orderRequest);
//
//        assertFalse(orderBook.getAsks().isEmpty());
//
//        Order order = orderBook.getAsks().firstEntry().getValue().peek();
//        assertNotNull(order.getId());
//        assertEquals(OrderType.LIMIT, order.getOrderType());
//        assertEquals(TimeInForce.GOOD_TILL_CANCEL, order.getTimeInForce());
//        assertEquals(Side.SELL, order.getSide());
//        assertEquals(5, order.getPrice());
//        assertEquals(100, order.getVolume());
//        assertEquals(instant, order.getTimestamp());
//    }

    @Test
    void matchingEngine_noMatchBothSidesRest() {
        Order restingOrder = new Order(1L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 100, LocalTime.now());
        orderBook.addOrder(restingOrder);

        Order incomingOrder = new Order(2L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 12, 50, LocalTime.now());
        orderBookService.processOrder(incomingOrder);

        assertTrue(orderBook.getBids()
                            .get(restingOrder.getPrice())
                            .contains(restingOrder));
        assertTrue(orderBook.getAsks()
                            .get(incomingOrder.getPrice())
                            .contains(incomingOrder));

        assertEquals(1, orderBook.getBids().size());
        assertEquals(1, orderBook.getAsks().size());
    }

    @Test
    void matchingEngine_exactMatchSameQuantity() {
        Order restingOrder = new Order(1L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 100, LocalTime.now());
        orderBook.addOrder(restingOrder);

        Order incomingOrder = new Order(2L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 100, LocalTime.now());
        orderBookService.processOrder(incomingOrder);

        assertFalse(orderBook.getBids().containsKey(incomingOrder.getPrice()));
        assertFalse(orderBook.getAsks().containsKey(restingOrder.getPrice()));

        assertEquals(0, orderBook.getBids().size());
        assertEquals(0, orderBook.getAsks().size());
    }

    @Test
    void matchingEngine_partialFillIncomingBuyLargerThanRestingSell() {
        Order restingOrder = new Order(1L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 40, LocalTime.now());
        orderBook.addOrder(restingOrder);

        Order incomingOrder = new Order(2L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 100, LocalTime.now());
        orderBookService.processOrder(incomingOrder);

        assertTrue(orderBook.getBids().containsKey(incomingOrder.getPrice()));
        assertFalse(orderBook.getAsks().containsKey(restingOrder.getPrice()));

        assertEquals(1, orderBook.getBids().size());
        assertEquals(0, orderBook.getAsks().size());
        assertEquals(60, incomingOrder.getVolumeRemaining());
    }

    @Test
    void matchingEngine_partialFillIncomingSellLargerThanRestingBuy() {
        Order restingOrder = new Order(1L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 30, LocalTime.now());
        orderBook.addOrder(restingOrder);

        Order incomingOrder = new Order(2L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 80, LocalTime.now());
        orderBookService.processOrder(incomingOrder);

        assertFalse(orderBook.getBids().containsKey(restingOrder.getPrice()));
        assertTrue(orderBook.getAsks().containsKey(incomingOrder.getPrice()));

        assertEquals(0, orderBook.getBids().size());
        assertEquals(1, orderBook.getAsks().size());
        assertEquals(50, incomingOrder.getVolumeRemaining());
    }

    @Test
    void matchingEngine_incomingBuySweepsMultipleAskLevels() {
        Order restingOrder1 = new Order(1L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 9, 20, LocalTime.now());
        Order restingOrder2 = new Order(2L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 30, LocalTime.now());
        Order restingOrder3 = new Order(3L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 11, 40, LocalTime.now());
        orderBook.addOrder(restingOrder1);
        orderBook.addOrder(restingOrder2);
        orderBook.addOrder(restingOrder3);

        Order incomingOrder = new Order(4L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 60, LocalTime.now());
        orderBookService.processOrder(incomingOrder);

        assertFalse(orderBook.getAsks().containsKey(restingOrder1.getPrice()));
        assertFalse(orderBook.getAsks().containsKey(restingOrder2.getPrice()));
        assertTrue(orderBook.getAsks().containsKey(restingOrder3.getPrice()));
        assertTrue(orderBook.getBids().containsKey(incomingOrder.getPrice()));

        assertEquals(1, orderBook.getBids().size());
        assertEquals(1, orderBook.getAsks().size());
        assertEquals(40, restingOrder3.getVolumeRemaining());
        assertEquals(10, incomingOrder.getVolumeRemaining());
    }

    @Test
    void matchingEngine_incomingSellSweepsMultipleBidLevels() {
        Order restingOrder1 = new Order(1L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 11, 15, LocalTime.now());
        Order restingOrder2 = new Order(2L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 20, LocalTime.now());
        Order restingOrder3 = new Order(3L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 9, 25, LocalTime.now());
        orderBook.addOrder(restingOrder1);
        orderBook.addOrder(restingOrder2);
        orderBook.addOrder(restingOrder3);

        Order incomingOrder = new Order(4L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 40, LocalTime.now());
        orderBookService.processOrder(incomingOrder);

        assertFalse(orderBook.getBids().containsKey(restingOrder1.getPrice()));
        assertFalse(orderBook.getBids().containsKey(restingOrder2.getPrice()));
        assertTrue(orderBook.getBids().containsKey(restingOrder3.getPrice()));
        assertTrue(orderBook.getAsks().containsKey(incomingOrder.getPrice()));

        assertEquals(1, orderBook.getBids().size());
        assertEquals(1, orderBook.getAsks().size());
        assertEquals(25, restingOrder3.getVolumeRemaining());
        assertEquals(5, incomingOrder.getVolumeRemaining());
    }

    @Test
    void matchingEngine_timePriorityAtSamePrice() {
        Order restingOrder1 = new Order(1L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 30, LocalTime.now());
        Order restingOrder2 = new Order(2L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 50, LocalTime.now());
        orderBook.addOrder(restingOrder1);
        orderBook.addOrder(restingOrder2);

        Order incomingOrder = new Order(3L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 60, LocalTime.now());
        orderBookService.processOrder(incomingOrder);

        assertFalse(orderBook.getBids().containsKey(incomingOrder.getPrice()));
        assertTrue(orderBook.getAsks().containsKey(restingOrder2.getPrice()));

        assertEquals(0, orderBook.getBids().size());
        assertEquals(1, orderBook.getAsks().size());
        assertEquals(20, restingOrder2.getVolumeRemaining());
    }

    @Test
    void matchingEngine_betterPriceBeatsEarlierWorsePrice() {
        Order restingOrder1 = new Order(1L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 30, LocalTime.now());
        Order restingOrder2 = new Order(2L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 50, LocalTime.now());
        orderBook.addOrder(restingOrder1);
        orderBook.addOrder(restingOrder2);

        Order incomingOrder = new Order(3L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 60, LocalTime.now());
        orderBookService.processOrder(incomingOrder);

        assertFalse(orderBook.getBids().containsKey(incomingOrder.getPrice()));
        assertTrue(orderBook.getAsks().containsKey(restingOrder2.getPrice()));

        assertEquals(0, orderBook.getBids().size());
        assertEquals(1, orderBook.getAsks().size());
        assertEquals(20, restingOrder2.getVolumeRemaining());
    }

    @Test
    void matchingEngine_crossingOrderStopsAtItsOwnLimit() {
        Order restingOrder1 = new Order(1L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 8, 10, LocalTime.now());
        Order restingOrder2 = new Order(2L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 9, 10, LocalTime.now());
        Order restingOrder3 = new Order(3L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 10, LocalTime.now());
        orderBook.addOrder(restingOrder1);
        orderBook.addOrder(restingOrder2);
        orderBook.addOrder(restingOrder3);

        Order incomingOrder = new Order(4L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 9, 50, LocalTime.now());
        orderBookService.processOrder(incomingOrder);

        assertFalse(orderBook.getAsks().containsKey(restingOrder1.getPrice()));
        assertFalse(orderBook.getAsks().containsKey(restingOrder2.getPrice()));
        assertTrue(orderBook.getAsks().containsKey(restingOrder3.getPrice()));
        assertTrue(orderBook.getBids().containsKey(incomingOrder.getPrice()));

        assertEquals(1, orderBook.getBids().size());
        assertEquals(1, orderBook.getAsks().size());
        assertEquals(10, restingOrder3.getVolumeRemaining());
        assertEquals(30, incomingOrder.getVolumeRemaining());
    }

    @Test
    void marketOrder_sweepsMultiplePriceLevels() {
        Order ask1 = new Order(1L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 20, LocalTime.of(10,0));
        Order ask2 = new Order(2L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 11, 30, LocalTime.of(10,0));
        orderBook.addOrder(ask1);
        orderBook.addOrder(ask2);

        Order marketBuy = new Order(3L, OrderType.MARKET, TimeInForce.IMMEDIATE_OR_CANCEL, Side.BUY, 0, 40, LocalTime.of(10,0));

        orderBookService.processOrder(marketBuy);

        assertFalse(orderBook.getAsks().containsKey(10));
        assertTrue(orderBook.getAsks().containsKey(11));
        assertEquals(10, ask2.getVolumeRemaining());
    }

    @Test
    void marketOrder_partialFillThenCancelRemainder() {
        Order ask = new Order(1L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 20, LocalTime.of(10,0));
        orderBook.addOrder(ask);

        Order marketBuy = new Order(2L, OrderType.MARKET, TimeInForce.IMMEDIATE_OR_CANCEL, Side.BUY, 0, 50, LocalTime.of(10,0));

        orderBookService.processOrder(marketBuy);

        assertEquals(0, orderBook.getAsks().size());
        assertEquals(30, marketBuy.getVolumeRemaining());
    }

    @Test
    void limitIOC_partialFillCancelsRemainder() {
        Order ask = new Order(1L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 10, 10, LocalTime.of(10,0));
        orderBook.addOrder(ask);

        Order incoming = new Order(2L, OrderType.LIMIT, TimeInForce.IMMEDIATE_OR_CANCEL, Side.BUY, 10, 20, LocalTime.of(10,0));

        orderBookService.processOrder(incoming);

        assertEquals(0, orderBook.getAsks().size());
        assertEquals(10, incoming.getVolumeRemaining());
        assertFalse(orderBook.getBids().containsKey(10));
    }

    @Test
    void limitIOC_noMatchCancelsEntireOrder() {
        Order incoming = new Order(1L, OrderType.LIMIT, TimeInForce.IMMEDIATE_OR_CANCEL, Side.BUY, 10, 50, LocalTime.of(10,0));

        orderBookService.processOrder(incoming);

        assertEquals(0, orderBook.getBids().size());
    }

    @Test
    void marketSell_sweepsMultipleBidLevels() {
        Order bid1 = new Order(1L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 20, LocalTime.of(10,0));
        Order bid2 = new Order(2L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 9, 30, LocalTime.of(10,0));
        orderBook.addOrder(bid1);
        orderBook.addOrder(bid2);

        Order marketSell = new Order(3L, OrderType.MARKET, TimeInForce.IMMEDIATE_OR_CANCEL, Side.SELL, 0, 40, LocalTime.of(10,0));
        orderBookService.processOrder(marketSell);

        // The best bid (10) should be wiped out
        assertFalse(orderBook.getBids().containsKey(10));
        // The worse bid (9) should be partially filled
        assertTrue(orderBook.getBids().containsKey(9));
        assertEquals(10, bid2.getVolumeRemaining());
    }

    @Test
    void marketOrder_emptyBook_isCancelledImmediately() {
        Order marketBuy = new Order(1L, OrderType.MARKET, TimeInForce.IMMEDIATE_OR_CANCEL, Side.BUY, 0, 50, LocalTime.now());
        orderBookService.processOrder(marketBuy);

        // The book should remain completely empty, and the order should not rest
        assertTrue(orderBook.getBids().isEmpty());
        assertTrue(orderBook.getAsks().isEmpty());
        assertEquals(50, marketBuy.getVolumeRemaining()); // Or assert order status == CANCELLED if you track that
    }

    @Test
    void limitSellIOC_partialFillCancelsRemainder() {
        Order bid = new Order(1L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 15, LocalTime.of(10,0));
        orderBook.addOrder(bid);

        Order limitSellIOC = new Order(2L, OrderType.LIMIT, TimeInForce.IMMEDIATE_OR_CANCEL, Side.SELL, 10, 25, LocalTime.of(10,0));
        orderBookService.processOrder(limitSellIOC);

        // The bid should be consumed
        assertEquals(0, orderBook.getBids().size());
        // The remainder of the IOC (10) should vanish, not enter the ask book
        assertEquals(0, orderBook.getAsks().size());
        assertEquals(10, limitSellIOC.getVolumeRemaining());
    }

    @Test
    void complexScenario_mixedOrdersProcessCorrectly() {
        // 1. Setup initial book with two resting Limit Buys
        Order restingBid1 = new Order(1L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 10, 50, LocalTime.of(10, 0));
        Order restingBid2 = new Order(2L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 11, 50, LocalTime.of(10, 1));
        orderBook.addOrder(restingBid1);
        orderBook.addOrder(restingBid2);

        // 2. Hit the book with a Limit Sell IOC for 60 at price 10.
        // It should take all 50 from the price 11 bid, and 10 from the price 10 bid.
        Order limitSellIOC = new Order(3L, OrderType.LIMIT, TimeInForce.IMMEDIATE_OR_CANCEL, Side.SELL, 10, 60, LocalTime.of(10, 2));
        orderBookService.processOrder(limitSellIOC);

        assertFalse(orderBook.getBids().containsKey(11)); // bid2 is gone
        assertEquals(40, restingBid1.getVolumeRemaining()); // bid1 has 40 left

        // 3. Hit the book with a Market Sell IOC for 20.
        // It should take 20 more from the remaining price 10 bid.
        Order marketSellIOC = new Order(4L, OrderType.MARKET, TimeInForce.IMMEDIATE_OR_CANCEL, Side.SELL, 0, 20, LocalTime.of(10, 3));
        orderBookService.processOrder(marketSellIOC);

        assertEquals(20, restingBid1.getVolumeRemaining()); // bid1 has 20 left

        // 4. Submit a Limit Sell GTC at price 12. It shouldn't cross, so it rests.
        Order limitSellGTC = new Order(5L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 12, 100, LocalTime.of(10, 4));
        orderBookService.processOrder(limitSellGTC);

        assertEquals(1, orderBook.getAsks().size());
        assertTrue(orderBook.getAsks().containsKey(12));

        // 5. Submit a Market Buy IOC for 150.
        // It should take all 100 from the ask, and the remaining 50 should vanish.
        Order marketBuyIOC = new Order(6L, OrderType.MARKET, TimeInForce.IMMEDIATE_OR_CANCEL, Side.BUY, 0, 150, LocalTime.of(10, 5));
        orderBookService.processOrder(marketBuyIOC);

        assertEquals(0, orderBook.getAsks().size()); // Ask is gone
        assertEquals(50, marketBuyIOC.getVolumeRemaining()); // 50 vanished
        assertEquals(20, restingBid1.getVolumeRemaining()); // Original bid at 10 is untouched by sells since step 3
    }
}
