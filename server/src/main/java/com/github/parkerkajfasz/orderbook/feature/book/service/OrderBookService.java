package com.github.parkerkajfasz.orderbook.feature.book.service;

import com.github.parkerkajfasz.orderbook.feature.book.domain.OrderBook;
import com.github.parkerkajfasz.orderbook.feature.book.dto.*;
import com.github.parkerkajfasz.orderbook.feature.order.domain.Order;
import com.github.parkerkajfasz.orderbook.feature.order.domain.OrderType;
import com.github.parkerkajfasz.orderbook.feature.order.domain.Side;
import com.github.parkerkajfasz.orderbook.feature.order.domain.TimeInForce;
import com.github.parkerkajfasz.orderbook.feature.order.dto.OrderRequestDTO;
import com.github.parkerkajfasz.orderbook.feature.order.dto.OrderResponseDTO;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
                LocalTime.now()
        );

        processOrder(order);
        return new OrderResponseDTO(order.getId(), order.getOrderType(), order.getTimeInForce(), order.getSide(), order.getPrice(), order.getVolume(), order.getTimestamp());
    }

    /**
     * Matching Engine related code
     */
    public void processOrder(Order incomingOrder) {

        if (incomingOrder.getOrderType() == OrderType.MARKET) {
            processMarketOrder(incomingOrder);
        } else {
            processLimitOrder(incomingOrder);
        }

        updateBBO(); // broadcast new L1 state
        updateMBP(); // broadcast new L2 state
        updateMBO(); // broadcast new L3 state
    }

    private void processMarketOrder(Order incomingOrder) {

        while (incomingOrder.getVolumeRemaining() > 0) {
            Optional<Order> restingOrderOption = getRestingOrder(incomingOrder);
            if (restingOrderOption.isEmpty()) {
                break;
            }

            Order restingOrder = restingOrderOption.get();
            trade(incomingOrder, restingOrder);

            if (restingOrder.getVolumeRemaining() == 0) {
                orderBook.removeOrder(restingOrder);
                log.info("ORDER {} removed from order book", restingOrder.getId());
            }
        }

        if (incomingOrder.getVolumeRemaining() > 0) {
            log.info("ORDER {} canceled with {} unfilled", incomingOrder.getId(), incomingOrder.getVolumeRemaining());
        }
    }

    private void processLimitOrder(Order incomingOrder) {

        while (incomingOrder.getVolumeRemaining() > 0) {
            Optional<Order> restingOrderOption = getRestingOrder(incomingOrder);
            if (restingOrderOption.isEmpty()) {
                break;
            }

            Order restingOrder = restingOrderOption.get();
            if (!crossBook(incomingOrder, restingOrder)) {
                break;
            }

            trade(incomingOrder, restingOrder);

            if (restingOrder.getVolumeRemaining() == 0) {
                orderBook.removeOrder(restingOrder);
                log.info("ORDER {} removed from order book", restingOrder.getId());
            }
        }

        /**
         * GTC + incomingOrder quantityRemaining == 0 : Do nothing
         * GTC + incomingOrder quantityRemaining >  0 : Add incomingOrder to order book
         * IOC + incomingOrder quantityRemaining == 0 : Do nothing
         * IOC + incomingOrder quantityRemaining >  0 : Do not add incomingOrder to order book
         */

        if (incomingOrder.getVolumeRemaining() > 0) {
            if (incomingOrder.getTimeInForce() == TimeInForce.GOOD_TILL_CANCEL) {
                orderBook.addOrder(incomingOrder);
                log.info("ORDER {} added to order book", incomingOrder.getId());
            } else {
                if (incomingOrder.getVolumeRemaining() < incomingOrder.getVolume()) {
                    log.info("ORDER {} canceled with {} unfilled", incomingOrder.getId(), incomingOrder.getVolumeRemaining());
                }
            }
        }
//        if (incomingOrder.getVolumeRemaining() > 0) { // if incomingOrder volume has volume remaining, but can't cross book, then add to book.
//            orderBook.addOrder(incomingOrder);
//            log.info("ORDER {} added to order book", incomingOrder.getId());
//        }
    }

    private Optional<Order> getRestingOrder(Order incomingOrder) {
        return incomingOrder.getSide() == Side.BUY
                ? orderBook.getBestAsk()
                : orderBook.getBestBid();
    }

    private boolean crossBook(Order incomingOrder, Order restingOrder) {
        return incomingOrder.getSide() == Side.BUY
                ? incomingOrder.getPrice() >= restingOrder.getPrice()   // BUY crosses if its price >= price of best ASK
                : incomingOrder.getPrice() <= restingOrder.getPrice();  // SELL crosses if its price <= price of best BID
    }

    private void trade(Order incomingOrder, Order restingOrder) {
        int volumeTraded = Math.min(incomingOrder.getVolumeRemaining(), restingOrder.getVolumeRemaining());

        incomingOrder.subtractVolume(volumeTraded);
        restingOrder.subtractVolume(volumeTraded);

        log.info("TRADE executed: {} @ {} | maker={} | taker={} | makerId={} | takerId={}", volumeTraded, restingOrder.getPrice(), restingOrder.getSide(), incomingOrder.getSide(), restingOrder.getId(), incomingOrder.getId());
        updateTradeFeed(volumeTraded, incomingOrder, restingOrder); // broadcast executed trade to live trade feed
    }

    private void updateTradeFeed(int volumeTraded, Order incomingOrder, Order restingOrder) {
        TradeDTO trade = new TradeDTO(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")), volumeTraded, restingOrder.getPrice(), incomingOrder.getSide() == Side.BUY ? "B" : "S");
        messagingTemplate.convertAndSend("/topic/trades", trade);
    }

    private void updateBBO() {
        Optional<Order> bestBid = orderBook.getBestBid();
        Optional<Order> bestAsk = orderBook.getBestAsk();

        int bestBidPrice = bestBid.isPresent() ? bestBid.get().getPrice() : 0;
        int bestAskPrice = bestAsk.isPresent() ? bestAsk.get().getPrice() : 0;

        BestBidOfferDTO bbo = new BestBidOfferDTO(bestBidPrice, bestAskPrice);
        messagingTemplate.convertAndSend("/topic/bbo", bbo);
    }

    private void updateMBP() {
        List<PriceLevelDTO> bidPriceLevels = new ArrayList<>();
        for (Map.Entry<Integer, Queue<Order>> entry : orderBook.getBids().entrySet()) {

            int totalVolume = 0;
            for (Order order : entry.getValue()) {
                totalVolume += order.getVolumeRemaining();
            }
            bidPriceLevels.add(new PriceLevelDTO(entry.getValue().size(), totalVolume, entry.getKey()));
        }

        List<PriceLevelDTO> askPriceLevels = new ArrayList<>();
        for (Map.Entry<Integer, Queue<Order>> entry : orderBook.getAsks().entrySet()) {

            int totalVolume = 0;
            for (Order order : entry.getValue()) {
                totalVolume += order.getVolumeRemaining();
            }
            askPriceLevels.add(new PriceLevelDTO(entry.getValue().size(), totalVolume, entry.getKey()));
        }

        MarketByPriceDTO mbp = new MarketByPriceDTO(bidPriceLevels, askPriceLevels);
        messagingTemplate.convertAndSend("/topic/mbp", mbp);
    }

    private void updateMBO() {
        List<MarketByOrderEntryDTO> bids = new ArrayList<>();
        for (Map.Entry<Integer, Queue<Order>> entry : orderBook.getBids().entrySet()) {
            for (Order order : entry.getValue()) {
                MarketByOrderEntryDTO mboEntry = new MarketByOrderEntryDTO(
                        order.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")),
                        order.getVolumeRemaining(),
                        order.getPrice(),
                        order.getSide()
                );
                bids.add(mboEntry);
            }
        }

        List<MarketByOrderEntryDTO> asks = new ArrayList<>();
        for (Map.Entry<Integer, Queue<Order>> entry : orderBook.getAsks().entrySet()) {
            for (Order order : entry.getValue()) {
                MarketByOrderEntryDTO mboEntry = new MarketByOrderEntryDTO(
                        order.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")),
                        order.getVolumeRemaining(),
                        order.getPrice(),
                        order.getSide()
                );
                asks.add(mboEntry);
            }
        }

        MarketByOrderDTO mbo = new MarketByOrderDTO(bids, asks);
        messagingTemplate.convertAndSend("/topic/mbo", mbo);
    }
}
