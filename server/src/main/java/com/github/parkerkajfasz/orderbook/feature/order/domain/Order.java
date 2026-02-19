package com.github.parkerkajfasz.orderbook.feature.order.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class Order {

    @Id
    @GeneratedValue
    private Long id;
    private OrderType orderType;
    private Side side;
    private int price;
    private int volume;
    private Instant timestamp;

    protected Order() {};

    public Order(OrderType orderType, Side side, int price, int volume, Instant timestamp) {
        this.orderType = orderType;
        this.side = side;
        this.price = price;
        this.volume = volume;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public OrderType getOrderType() {
        return orderType;
    }

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