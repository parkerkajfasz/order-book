package com.github.parkerkajfasz.orderbook.feature.book.controller;

import com.github.parkerkajfasz.orderbook.feature.book.service.OrderBookService;
import com.github.parkerkajfasz.orderbook.feature.order.domain.OrderType;
import com.github.parkerkajfasz.orderbook.feature.order.domain.Side;
import com.github.parkerkajfasz.orderbook.feature.order.domain.TimeInForce;
import com.github.parkerkajfasz.orderbook.feature.order.dto.OrderRequestDTO;
import com.github.parkerkajfasz.orderbook.feature.order.dto.OrderResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class OrderBookControllerUnitTest {

    @MockitoBean
    private OrderBookService orderBookService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void submitOrder_ValidOrderRequestAndReturnsResponse() throws Exception {

        when(orderBookService.addToOrderBook(any(OrderRequestDTO.class))).thenReturn(new OrderResponseDTO(1L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.BUY, 55, 289, LocalTime.parse("2026-02-22T15:30:00Z")));

        String validRequest = "{\"orderType\":\"LIMIT\",\"timeInForce\":\"GOOD_TILL_CANCEL\",\"side\":\"SELL\",\"price\":55,\"volume\":289,\"timestamp\":\"15:30:00\"}";
        mockMvc.perform(post("/orderbook/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest))
               .andExpect(status().isOk());

        verify(orderBookService, times(1)).addToOrderBook(any(OrderRequestDTO.class));
    }

    @Test
    void submitOrder_InvalidOrderRequestAndReturnsException() throws Exception {

        String invalidRequest = "{\"orderType\":\"GOOD_TILL_CANCEL\",\"timeInForce\":\"LIMIT\",\"side\":\"BUY\",\"price\":55,\"volume\":289,\"timestamp\":\"15:30:00\"}"; // OrderType and TimeInForce values are flipped, making it invalid
        mockMvc.perform(post("/orderbook/orders")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(invalidRequest))
               .andExpect(result -> assertTrue(result.getResolvedException() instanceof HttpMessageNotReadableException));

        verify(orderBookService, times(0)).addToOrderBook(any(OrderRequestDTO.class));
    }
}