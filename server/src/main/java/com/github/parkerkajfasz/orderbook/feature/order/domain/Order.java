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
    private int volumeRemaining;

    public Order(Long id, OrderType orderType, TimeInForce timeInForce, Side side, int price, int volume, Instant timestamp) {
        this.id = id;
        this.orderType = orderType;
        this.timeInForce = timeInForce;
        this.side = side;
        this.price = price;
        this.volume = volume;
        this.timestamp = timestamp;
        this.volumeRemaining = volume;
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

    public int getVolumeRemaining() { return volumeRemaining; }

    public void subtractVolume(int subtractedVolume) {
        this.volumeRemaining -= subtractedVolume;
    };
}