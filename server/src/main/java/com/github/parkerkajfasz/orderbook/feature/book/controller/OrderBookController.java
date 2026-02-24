package com.github.parkerkajfasz.orderbook.feature.book.controller;

import com.github.parkerkajfasz.orderbook.feature.book.dto.BestBidOfferResponseDTO;
import com.github.parkerkajfasz.orderbook.feature.book.service.OrderBookService;
import com.github.parkerkajfasz.orderbook.feature.order.dto.OrderRequestDTO;
import com.github.parkerkajfasz.orderbook.feature.order.dto.OrderResponseDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orderbook")
public class OrderBookController {

    private final OrderBookService orderBookService;

    public OrderBookController(OrderBookService orderBookService) {
        this.orderBookService = orderBookService;
    }

    @PostMapping("/orders")
    public OrderResponseDTO createOrder(@Valid @RequestBody OrderRequestDTO orderRequest) {
        OrderResponseDTO createdOrder = orderBookService.addToOrderBook(orderRequest);
        return createdOrder;
    }

    @GetMapping("/l1")
    public BestBidOfferResponseDTO getLevelOneData() {
        return orderBookService.getBestBidOfferData();
    }
}
