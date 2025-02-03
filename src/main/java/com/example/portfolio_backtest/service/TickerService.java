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
}