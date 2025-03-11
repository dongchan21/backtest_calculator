package com.example.portfolio_backtest.repository;

import com.example.portfolio_backtest.entity.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {
    List<StockPrice> findByTickerAndDateBetweenOrderByDateAsc(String ticker, LocalDate start, LocalDate end);
    // 🔹 특정 주식의 가장 빠른 날짜 데이터를 가져오는 메서드 추가
    Optional<StockPrice> findFirstByTickerOrderByDateAsc(String ticker);

}
