package com.example.portfolio_backtest.controller;

import com.example.portfolio_backtest.domain.TickerDto;
import com.example.portfolio_backtest.domain.TickerEntity;
import com.example.portfolio_backtest.service.TickerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*") // or 적절한 origin
@RestController
@RequestMapping("/api/tickers")
public class TickerSearchController {

    private final TickerService tickerService;

    public TickerSearchController(TickerService tickerService) {
        this.tickerService = tickerService;
    }

    @GetMapping("/exists")
    public boolean checkIfExists(@RequestParam String symbol) {
        return tickerService.existsByTicker(symbol);
    }

    @GetMapping
    public List<TickerDto> search(@RequestParam String query) {

        if (query == null || query.trim().length() < 2) {
            return List.of(); // 빈 리스트 반환 (BadRequest도 고려 가능)
        }

        // DB에서 해당 query로 검색
        List<TickerEntity> entities = tickerService.search(query);

        // 자동완성용 DTO로 변환
        return entities.stream()
                .map(e -> new TickerDto(e.getTicker(), e.getName()))
                .toList();
    }
}