package com.example.portfolio_backtest.service;

import com.example.portfolio_backtest.domain.PortfolioDto;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class BacktestService {
    /**
     * 임시로 백테스트를 수행하는 메서드 예시
     */
    public Map<String, Object> runBacktest(PortfolioDto portfolioDto) {
        // 나중에 실제 계산 로직(주가 데이터 등)을 붙일 수 있음

        // 예시 결과: "총 수익률", "그래프 데이터" 등
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("totalReturn", 0.12); // 12% 수익이라고 가정
        resultMap.put("duration", "2020-01-01 ~ 2020-12-31");
        resultMap.put("initialCapital", portfolioDto.getInitialCapital());

        // ...필요한 데이터들을 채워서 반환
        return resultMap;
    }
}
