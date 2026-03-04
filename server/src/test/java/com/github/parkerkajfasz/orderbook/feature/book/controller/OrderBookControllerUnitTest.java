package com.github.parkerkajfasz.orderbook.feature.book.controller;

import com.github.parkerkajfasz.orderbook.feature.book.service.OrderBookService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class OrderBookControllerTest {

    @MockitoBean
    private OrderBookService orderBookService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getLevelOneData_EmptyAndReturnsResponse() throws Exception {

        mockMvc.perform(get("/orderbook/l1"))
               .andExpect(status().isOk());
    }

    @Test
    void createOrder_RequestsBuyOrderAndReturnsResponse() throws Exception {

        String requestBody = "{\"orderType\":\"LIMIT\",\"timeInForce\":\"GOOD_TILL_CANCEL\",\"side\":\"BUY\",\"price\":55,\"volume\":289,\"timestamp\":\"2026-02-22T15:30:00Z\"}";
        mockMvc.perform(post("/orderbook/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void createOrder_RequestsSellOrderAndReturnsResponse() throws Exception {

        String requestBody = "{\"orderType\":\"LIMIT\",\"timeInForce\":\"GOOD_TILL_CANCEL\",\"side\":\"SELL\",\"price\":55,\"volume\":289,\"timestamp\":\"2026-02-22T15:30:00Z\"}";
        mockMvc.perform(post("/orderbook/orders")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(requestBody))
                       .andExpect(status().isOk());
    }

    @Test
    void createOrder_RequestsBuyOrderAndReturnsException() throws Exception {

        String requestBody = "{\"orderType\":\"SomeField\",\"timeInForce\":\"GOOD_TILL_CANCEL\",\"side\":\"BUY\",\"price\":55,\"volume\":289,\"timestamp\":\"2026-02-22T15:30:00Z\"}";
        mockMvc.perform(post("/orderbook/orders")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(requestBody))
                       .andExpect(result -> assertTrue(result.getResolvedException() instanceof HttpMessageNotReadableException));
    }

    @Test
    void createOrder_RequestsSellOrderAndReturnsException() throws Exception {

        String requestBody = "{\"orderType\":\"SomeField\",\"timeInForce\":\"GOOD_TILL_CANCEL\",\"side\":\"BUY\",\"price\":55,\"volume\":289,\"timestamp\":\"2026-02-22T15:30:00Z\"}";
        mockMvc.perform(post("/orderbook/orders")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(requestBody))
                       .andExpect(result -> assertTrue(result.getResolvedException() instanceof HttpMessageNotReadableException));
    }
}