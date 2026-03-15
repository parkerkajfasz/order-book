package com.github.parkerkajfasz.orderbook.feature.book.controller;

import com.github.parkerkajfasz.orderbook.feature.book.service.OrderBookService;
import com.github.parkerkajfasz.orderbook.feature.order.domain.OrderType;
import com.github.parkerkajfasz.orderbook.feature.order.domain.Side;
import com.github.parkerkajfasz.orderbook.feature.order.domain.TimeInForce;
import com.github.parkerkajfasz.orderbook.feature.order.dto.OrderRequestDTO;
import com.github.parkerkajfasz.orderbook.feature.order.dto.OrderResponseDTO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Time;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class OrderBookControllerUnitTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderBookService orderBookService;

    @Test
    void submitOrder_LimitAndGTC() throws Exception {
        OrderResponseDTO response = new OrderResponseDTO(1L, OrderType.LIMIT, TimeInForce.GOOD_TILL_CANCEL, Side.SELL, 55, 289, LocalTime.now());
        Mockito.when(orderBookService.processOrder(any(OrderRequestDTO.class))).thenReturn(response);

        String request = """
        {
          "orderType":"LIMIT",
          "timeInForce":"GOOD_TILL_CANCEL",
          "side":"SELL",
          "price":55,
          "volume":289
        }
        """;
        mockMvc.perform(post("/api/v1/orderbook/orders")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(request))
               .andExpect(status().isOk());

        Mockito.verify(orderBookService, Mockito.times(1))
               .processOrder(any(OrderRequestDTO.class));
    }

    @Test
    void submitOrder_LimitAndIOC() throws Exception {
        OrderResponseDTO response = new OrderResponseDTO(1L, OrderType.LIMIT, TimeInForce.IMMEDIATE_OR_CANCEL, Side.SELL, 55, 289, LocalTime.now());
        Mockito.when(orderBookService.processOrder(any(OrderRequestDTO.class))).thenReturn(response);

        String request = """
        {
          "orderType":"LIMIT",
          "timeInForce":"IMMEDIATE_OR_CANCEL",
          "side":"SELL",
          "price":55,
          "volume":289
        }
        """;
        mockMvc.perform(post("/api/v1/orderbook/orders")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(request))
               .andExpect(status().isOk());

        Mockito.verify(orderBookService, Mockito.times(1))
               .processOrder(any(OrderRequestDTO.class));
    }

    @Test
    void submitOrder_MarketAndGTC() throws Exception {
        String request = """
        {
          "orderType":"MARKET",
          "timeInForce":"GOOD_TILL_CANCEL",
          "side":"SELL",
          "price":55,
          "volume":289
        }
        """;
        mockMvc.perform(post("/api/v1/orderbook/orders")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(request))
               .andExpect(status().isBadRequest());

        Mockito.verify(orderBookService, Mockito.never())
               .processOrder(any(OrderRequestDTO.class));
    }

    @Test
    void submitOrder_MarketAndIOC() throws Exception {
        OrderResponseDTO response = new OrderResponseDTO(1L, OrderType.LIMIT, TimeInForce.IMMEDIATE_OR_CANCEL, Side.SELL, 55, 289, LocalTime.now());
        Mockito.when(orderBookService.processOrder(any(OrderRequestDTO.class))).thenReturn(response);

        String request = """
        {
          "orderType":"MARKET",
          "timeInForce":"IMMEDIATE_OR_CANCEL",
          "side":"SELL",
          "price":55,
          "volume":289
        }
        """;
        mockMvc.perform(post("/api/v1/orderbook/orders")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(request))
               .andExpect(status().isOk());

        Mockito.verify(orderBookService, Mockito.times(1))
               .processOrder(any(OrderRequestDTO.class));
    }
}