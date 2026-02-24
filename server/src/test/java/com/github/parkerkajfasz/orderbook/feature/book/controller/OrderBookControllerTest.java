package com.github.parkerkajfasz.orderbook.feature.book.controller;

import com.github.parkerkajfasz.orderbook.feature.book.service.OrderBookService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

//@WebMvcTest
//@AutoConfigureRestTestClient
//class OrderBookControllerTest {
//
//    @Autowired
//    private RestTestClient restTestClient;
//
//    @MockitoBean
//    private OrderBookService orderBookService;
//
//    // does it matter whether I test this method vs. one in order book service
//    @Test
//    public void testGetLevelOneData() {
//        when(orderBookService.getBestBidOfferData())
//    }
//}
