package com.github.parkerkajfasz.orderbook.feature.book.repository;

import com.github.parkerkajfasz.orderbook.feature.book.domain.OrderBook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderBookRepository extends JpaRepository<OrderBook, Long> {
}
