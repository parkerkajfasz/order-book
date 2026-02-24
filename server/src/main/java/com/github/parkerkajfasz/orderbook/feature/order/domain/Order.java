package com.github.parkerkajfasz.orderbook.feature.order.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class Order {

    private Long id;
    private OrderType orderType;
    private TimeInForce timeInForce;
    private Side side;
    private int price;
    private int volume;
    private Instant timestamp;

    public Order(Long id, OrderType orderType, TimeInForce timeInForce, Side side, int price, int volume, Instant timestamp) {
        this.id = id;
        this.orderType = orderType;
        this.timeInForce = timeInForce;
        this.side = side;
        this.price = price;
        this.volume = volume;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }

    public OrderType getOrderType() {
        return orderType;
    }

    public TimeInForce getTimeInForce() { return timeInForce; }

    public Side getSide() {
        return side;
    }

    public int getPrice() {
        return price;
    }

    public int getVolume() {
        return volume;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}