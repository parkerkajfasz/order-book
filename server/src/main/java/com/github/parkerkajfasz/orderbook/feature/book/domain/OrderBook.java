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

    public Optional<Order> getBestBid() {
        if (bids.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(bids.firstEntry().getValue().peek());
    }

    public Optional<Order> getBestAsk() {
        if (asks.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(asks.firstEntry().getValue().peek());
    }

    public void addOrder(Order order) {
        if (order.getSide() == Side.BUY) {
            bids.computeIfAbsent(order.getPrice(), k -> new LinkedList<>()).add(order);
        } else {
            asks.computeIfAbsent(order.getPrice(), k -> new LinkedList<>()).add(order);
        }
    }

    public void removeOrder(Order order) {
        TreeMap<Integer, Queue<Order>> bookSide = order.getSide() == Side.BUY ? bids : asks;

        Queue<Order> orders = bookSide.get(order.getPrice());
        if (orders == null) return; // don't remove order if the level doesn't exist

        orders.remove(order);

        if (orders.isEmpty()) { // if the level itself is empty, remove it from the book side
            bookSide.remove(order.getPrice());
        }
    }
}