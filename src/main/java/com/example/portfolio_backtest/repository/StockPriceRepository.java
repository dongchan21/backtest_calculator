package com.example.portfolio_backtest.repository;

import com.example.portfolio_backtest.entity.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {
    List<StockPrice> findByTickerAndDateBetween(String ticker, LocalDate start, LocalDate end);
}
