package com.github.parkerkajfasz.orderbook.feature.book.domain;

import com.github.parkerkajfasz.orderbook.feature.order.domain.Order;
import jakarta.persistence.*;

import java.util.Map;
import java.util.Queue;

@Entity
public class OrderBook {

    @Id
    @GeneratedValue
    private Long id;
    @Transient
    private Map<Integer, Queue<Order>> bids; // Descending queue of bids (Top being highest price buyer is willing to pay)
    @Transient
    private Map<Integer, Queue<Order>> asks; // Ascending queue of asks (Top is the lowest someone is willing to sell)

    protected OrderBook() {};

    public OrderBook(Map<Integer, Queue<Order>> bids, Map<Integer, Queue<Order>> asks) {
        this.bids = bids;
        this.asks = asks;
    }

    public Long getId() {
        return id;
    }

    public Map<Integer, Queue<Order>> getBids() {
        return bids;
    }

    public Map<Integer, Queue<Order>> getAsks() {
        return asks;
    }
}