package com.github.parkerkajfasz.orderbook.feature.book.controller;

import com.github.parkerkajfasz.orderbook.feature.book.service.OrderBookService;
import com.github.parkerkajfasz.orderbook.feature.order.dto.OrderRequestDTO;
import com.github.parkerkajfasz.orderbook.feature.order.dto.OrderResponseDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orderbook")
public class OrderBookController {
    private final OrderBookService orderBookService;

    public OrderBookController(OrderBookService orderBookService) {
        this.orderBookService = orderBookService;
    }

    @PostMapping("/orders")
    public OrderResponseDTO submitOrder(@Valid @RequestBody OrderRequestDTO orderRequest) {
        OrderResponseDTO submittedOrder = orderBookService.processOrder(orderRequest); // should be renamed to something more fitting
        return submittedOrder;
    }

    @PostMapping("/orders/batch")
    public List<OrderResponseDTO> submitOrders(@Valid @RequestBody List<OrderRequestDTO> orderRequests) throws InterruptedException {
        List<OrderResponseDTO> submittedOrders = new ArrayList<>();

        for (OrderRequestDTO orderRequest : orderRequests) {
            OrderResponseDTO createdOrder = orderBookService.processOrder(orderRequest);
            Thread.sleep(200);
            submittedOrders.add(createdOrder);
        }
        return submittedOrders;
    }
}
