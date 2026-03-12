package com.github.parkerkajfasz.orderbook.feature.book.service;

import com.github.parkerkajfasz.orderbook.feature.book.domain.OrderBook;
import com.github.parkerkajfasz.orderbook.feature.book.dto.*;
import com.github.parkerkajfasz.orderbook.feature.order.domain.Order;
import com.github.parkerkajfasz.orderbook.feature.order.domain.Side;
import com.github.parkerkajfasz.orderbook.feature.order.dto.OrderRequestDTO;
import com.github.parkerkajfasz.orderbook.feature.order.dto.OrderResponseDTO;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderBookService {

    private final OrderBook orderBook;
    private final SimpMessagingTemplate messagingTemplate;
    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);
    private static final Logger log = LoggerFactory.getLogger(OrderBookService.class);

    public OrderBookService(OrderBook orderBook, SimpMessagingTemplate messagingTemplate) {
        this.orderBook = orderBook;
        this.messagingTemplate = messagingTemplate;
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

        processOrder(order);
        return new OrderResponseDTO(order.getId(), order.getOrderType(), order.getTimeInForce(), order.getSide(), order.getPrice(), order.getVolume(), order.getTimestamp());
    }

    /**
     * Matching Engine related code
     */
    public void processOrder(Order incomingOrder) {

        while (incomingOrder.getVolumeRemaining() > 0) {

            Order restingOrder = getRestingOrder(incomingOrder);
            if (restingOrder == null || !crossedBook(incomingOrder, restingOrder)) {
                break;
            }

            trade(incomingOrder, restingOrder);
            if (restingOrder.getVolumeRemaining() == 0) {
                orderBook.removeOrder(restingOrder);
                log.info("Order {} removed from order book", incomingOrder.getId());
            }
        }

        if (incomingOrder.getVolumeRemaining() > 0) { // if incomingOrder volume has volume remaining, but can't cross book, then add to book.
            orderBook.addOrder(incomingOrder);
            log.info("Order {} added to order book", incomingOrder.getId());
        }

        updateBBO(); // broadcast new best bid offer state
        updateMBP(); // broadcast new market by price state
        updateMBO(); // broadcast new market by offer state
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

        log.info("TRADE executed: {} @ {} | maker={} | taker={} | makerId={} | takerId={}", volumeTraded, restingOrder.getPrice(), incomingOrder.getSide(), restingOrder.getSide(), incomingOrder.getId(), restingOrder.getId());
        updateTradeFeed(volumeTraded, incomingOrder, restingOrder); // broadcast executed trade to live trade feed
    }

    private void updateTradeFeed(int volumeTraded, Order incomingOrder, Order restingOrder) {
        TradeDTO trade = new TradeDTO(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")), volumeTraded, restingOrder.getPrice(), incomingOrder.getSide() == Side.BUY ? "B" : "S");
        messagingTemplate.convertAndSend("/topic/trades", trade);
    }

    private void updateBBO() {
        Order bestBid = orderBook.getBestBid();
        Order bestAsk = orderBook.getBestAsk();

        int bestBidPrice = bestBid != null ? bestBid.getPrice() : 0;
        int bestAskPrice = bestAsk != null ? bestAsk.getPrice() : 0;

        BestBidOfferDTO bbo = new BestBidOfferDTO(bestBidPrice, bestAskPrice);
        messagingTemplate.convertAndSend("/topic/bbo", bbo);
    }

    private void updateMBP() {
        List<PriceLevelDTO> bidPriceLevels = new ArrayList<>();
        for (Map.Entry<Integer, Queue<Order>> entry : orderBook.getBids().entrySet()) {
            int orderCount = entry.getValue().size();
            int price = entry.getKey();
            int totalVolume = 0;

            for (Order order : entry.getValue()) {
                totalVolume += order.getVolumeRemaining();
            }

            bidPriceLevels.add(new PriceLevelDTO(orderCount, totalVolume, price));
        }

        List<PriceLevelDTO> askPriceLevels = new ArrayList<>();
        for (Map.Entry<Integer, Queue<Order>> entry : orderBook.getAsks().entrySet()) {
            int orderCount = entry.getValue().size();
            int price = entry.getKey();
            int totalVolume = 0;

            for (Order order : entry.getValue()) {
                totalVolume += order.getVolumeRemaining();
            }

            askPriceLevels.add(new PriceLevelDTO(orderCount, totalVolume, price));
        }

        MarketByPriceDTO mbp = new MarketByPriceDTO(bidPriceLevels, askPriceLevels);
        messagingTemplate.convertAndSend("/topic/mbp", mbp);
    }

    private void updateMBO() {
        List<Order> bids = new ArrayList<>();
        List<Order> asks = new ArrayList<>();

        for (Map.Entry<Integer, Queue<Order>> entry : orderBook.getBids().entrySet()) {
            for (Order order : entry.getValue()) {
                bids.add(order);
            }
        }

        for (Map.Entry<Integer, Queue<Order>> entry : orderBook.getAsks().entrySet()) {
            for (Order order : entry.getValue()) {
                asks.add(order);
            }
        }

        MarketByOrderDTO mbo = new MarketByOrderDTO(bids, asks);
        messagingTemplate.convertAndSend("/topic/mbo", mbo);
    }
}
