package com.example.portfolio_backtest.service;

import com.example.portfolio_backtest.domain.PortfolioDto;
import com.example.portfolio_backtest.entity.StockPrice;
import com.example.portfolio_backtest.repository.StockPriceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BacktestService {
    /**
     * 임시로 백테스트를 수행하는 메서드 예시
     */
    private final StockPriceRepository stockPriceRepository;

    public BacktestService(StockPriceRepository stockPriceRepository) {
        this.stockPriceRepository = stockPriceRepository;
    }

    public Map<String, List<StockPrice>> getStockData(List<String> tickers, LocalDate startDate, LocalDate endDate) {
        return tickers.stream()
                .collect(Collectors.toMap(
                        ticker -> ticker,
                        ticker -> stockPriceRepository.findByTickerAndDateBetweenOrderByDateAsc(ticker, startDate, endDate)
                ));
    }

    public Map<String, Object> runBacktest(PortfolioDto portfolioDto) {
        // 나중에 실제 계산 로직(주가 데이터 등)을 붙일 수 있음


        // 예시 결과: "총 수익률", "그래프 데이터" 등
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("totalReturn", 0.12); // 12% 수익이라고 가정
        resultMap.put("startDate", portfolioDto.getStartDate());
        resultMap.put("endDate", portfolioDto.getEndDate());
        resultMap.put("initialCapital", portfolioDto.getInitialCapital());
        resultMap.put("monthlyInvestment", portfolioDto.getMonthlyInvestment());
        resultMap.put("assets", portfolioDto.getAllocations());

        // ...필요한 데이터들을 채워서 반환
        return resultMap;
    }
}
