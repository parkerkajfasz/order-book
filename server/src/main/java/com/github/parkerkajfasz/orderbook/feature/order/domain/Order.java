package com.github.parkerkajfasz.orderbook.feature.order.domain;

import java.time.Instant;

public class Order {

    private Long id;
    private OrderType orderType;
    private TimeInForce timeInForce;
    private Side side;
    private int price;
    private int volume;
    private Instant timestamp;
    private int remVolume;

    public Order(Long id, OrderType orderType, TimeInForce timeInForce, Side side, int price, int volume, Instant timestamp) {
        this.id = id;
        this.orderType = orderType;
        this.timeInForce = timeInForce;
        this.side = side;
        this.price = price;
        this.volume = volume;
        this.timestamp = timestamp;
        this.remVolume = volume;
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

    public int getRemVolume() { return remVolume; }

    public void fillRemVolume(int subtractedVolume) {
        this.remVolume -= subtractedVolume;
    };
}