package com.example.portfolio_backtest.service;

import com.example.portfolio_backtest.domain.TickerEntity;
import com.example.portfolio_backtest.repository.TickerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TickerService {

    private final TickerRepository tickerRepository;

    public TickerService(TickerRepository tickerRepository) {
        this.tickerRepository = tickerRepository;
    }

    public List<TickerEntity> search(String query) {
        return tickerRepository.findByTickerContainingIgnoreCaseOrNameContainingIgnoreCase(query, query);
    }

    // ✅ 개별 주식 존재 여부 확인 기능 추가
    public boolean existsByTicker(String ticker) {
        return tickerRepository.existsByTickerIgnoreCase(ticker);
    }

}