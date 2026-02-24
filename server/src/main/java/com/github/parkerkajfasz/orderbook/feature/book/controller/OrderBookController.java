package com.github.parkerkajfasz.orderbook.feature.book.controller;

import com.github.parkerkajfasz.orderbook.feature.book.dto.BestBidOfferDTO;
import com.github.parkerkajfasz.orderbook.feature.book.service.OrderBookService;
import com.github.parkerkajfasz.orderbook.feature.order.domain.Order;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orderbook")
public class OrderBookController {

    private final OrderBookService orderBookService;

    public OrderBookController(OrderBookService orderBookService) {
        this.orderBookService = orderBookService;
    }

    @PostMapping("/orders")
    public Order createOrder(@RequestBody Order newOrder) {
        Order createdOrder = orderBookService.addToOrderBook(newOrder);
        return createdOrder;
    }

    @GetMapping("/l1")
    public BestBidOfferDTO getLevelOneData() {
        return orderBookService.getBestBidOfferData();
    }
}
