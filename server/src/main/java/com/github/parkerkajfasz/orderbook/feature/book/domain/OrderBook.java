package com.github.parkerkajfasz.orderbook.feature.book.domain;

import com.github.parkerkajfasz.orderbook.feature.order.domain.Order;
import com.github.parkerkajfasz.orderbook.feature.order.domain.Side;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class OrderBook {

    private TreeMap<Integer, Queue<Order>> bids;
    private TreeMap<Integer, Queue<Order>> asks;

    public OrderBook() {
        this.bids = new TreeMap<>(Collections.reverseOrder()); // Descending queue of bids (Top is the highest price someone is willing to buy)
        this.asks = new TreeMap<>();                           // Ascending queue of asks (Top is the lowest price someone is willing to sell)
    }

    public TreeMap<Integer, Queue<Order>> getBids() {
        return bids;
    }

    public TreeMap<Integer, Queue<Order>> getAsks() {
        return asks;
    }

    public Order getBestBid() {
        if (bids.isEmpty()) throw new IllegalStateException("No bids have been added to order book");
        return Objects.requireNonNull(bids.firstEntry().getValue().peek());
    }

    public Order getBestAsk() {
        if (bids.isEmpty()) throw new IllegalStateException("No asks have been added to order book");
        return Objects.requireNonNull(asks.firstEntry().getValue().peek());
    }

    public void addOrder(Order order) {
        if (order.getSide() == Side.BUY) {
            bids.computeIfAbsent(order.getPrice(), k -> new LinkedList<>()).add(order);
        } else {
            asks.computeIfAbsent(order.getPrice(), k -> new LinkedList<>()).add(order);
        }
    }

    public void removeTopBidPriceLevel() {
        Map.Entry<Integer, Queue<Order>> topLevelBids = bids.firstEntry();

        if (bids.firstEntry().getValue().isEmpty()) {
            bids.remove(topLevelBids.getKey(), topLevelBids.getValue());
        }
    }

    public void removeTopAskPriceLevel() {
        Map.Entry<Integer, Queue<Order>> topLevelAsks = asks.firstEntry();

        if (topLevelAsks.getValue().isEmpty()) {
            asks.remove(topLevelAsks.getKey(), topLevelAsks.getValue());
        }
    }
}