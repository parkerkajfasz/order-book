package com.github.parkerkajfasz.orderbook.feature.book.domain;

import com.github.parkerkajfasz.orderbook.feature.order.domain.Order;
import com.github.parkerkajfasz.orderbook.feature.order.domain.Side;
import jakarta.persistence.*;
import org.antlr.v4.runtime.tree.Tree;

import java.util.*;

@Entity
public class OrderBook {

    @Id
    @GeneratedValue
    private Long id;
    @Transient
    private TreeMap<Integer, Queue<Order>> bids;
    @Transient
    private TreeMap<Integer, Queue<Order>> asks;

    public OrderBook() {
        this.bids = new TreeMap<>(Collections.reverseOrder()); // Descending queue of bids (Top is the highest price someone is willing to buy)
        this.asks = new TreeMap<>();                           // Ascending queue of asks (Top is the lowest price someone is willing to sell)
    }

    public Long getId() {
        return id;
    }

    public TreeMap<Integer, Queue<Order>> getBids() {
        return bids;
    }

    public TreeMap<Integer, Queue<Order>> getAsks() {
        return asks;
    }

    public void addOrder(Order order) {
        if (order.getSide() == Side.BUY) {
            bids.computeIfAbsent(order.getPrice(), k -> new LinkedList<>()).add(order);
        } else {
            asks.computeIfAbsent(order.getPrice(), k -> new LinkedList<>()).add(order);
        }
    }
}