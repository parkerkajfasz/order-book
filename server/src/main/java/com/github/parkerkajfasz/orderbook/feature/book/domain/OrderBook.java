package com.github.parkerkajfasz.orderbook.feature.book.domain;

import com.github.parkerkajfasz.orderbook.feature.order.domain.Order;
import com.github.parkerkajfasz.orderbook.feature.order.domain.Side;
import jakarta.persistence.*;
import org.antlr.v4.runtime.tree.Tree;
import org.springframework.stereotype.Component;

import javax.management.RuntimeErrorException;
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

    public int getBestBidPrice() {
        if (bids.isEmpty()) throw new IllegalStateException("No bids have been added to order book");
        return Objects.requireNonNull(bids.firstEntry().getValue().peek()).getPrice();
    }

    public int getBestBidVolume() {
        if (bids.isEmpty()) throw new IllegalStateException("No bids have been added to order book");
        return Objects.requireNonNull(bids.firstEntry().getValue().peek()).getVolume();
    }

    public int getBestAskPrice() {
        if (bids.isEmpty()) throw new IllegalStateException("No asks have been added to order book");
        return Objects.requireNonNull(asks.firstEntry().getValue().peek()).getPrice();
    }

    public int getBestAskVolume() {
        if (bids.isEmpty()) throw new IllegalStateException("No asks have been added to order book");
        return Objects.requireNonNull(bids.firstEntry().getValue().peek()).getVolume();
    }

    public void addOrder(Order order) {
        if (order.getSide() == Side.BUY) {
            bids.computeIfAbsent(order.getPrice(), k -> new LinkedList<>()).add(order);
        } else {
            asks.computeIfAbsent(order.getPrice(), k -> new LinkedList<>()).add(order);
        }
    }
}