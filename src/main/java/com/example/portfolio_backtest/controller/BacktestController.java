package com.example.portfolio_backtest.controller;

import com.example.portfolio_backtest.domain.PortfolioDto;
import com.example.portfolio_backtest.service.BacktestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/backtest")
public class BacktestController {

    private final BacktestService backtestService;

    @Autowired
    public BacktestController(BacktestService backtestService) {
        this.backtestService = backtestService;
    }

    /**
     * 백테스트 폼 페이지로 이동
     */
    @GetMapping("/form")
    public String showBacktestForm() {
        // templates/backtestForm.html 로 이동
        return "backtestForm";
    }

    /**
     * 백테스트 실행 후 결과 페이지로 이동
     */
    @PostMapping("/run")
    public String runBacktest(
            @RequestParam(required = false) String ticker1,
            @RequestParam(required = false) Double allocation1,
            @RequestParam(required = false) String ticker2,
            @RequestParam(required = false) Double allocation2,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "10000") double initialCapital,
            Model model) {

        // 1) 사용자가 입력한 티커/비중 정보를 PortfolioDto 형태로 변환 (간단 예시)
        PortfolioDto portfolioDto = new PortfolioDto();
        // 실제로는 여러 종목을 받을 수 있도록 Map을 생성
        // 여기선 예시로 2개 종목만 처리
        Map<String, Double> allocations = new java.util.HashMap<>();
        if (ticker1 != null && allocation1 != null) {
            allocations.put(ticker1, allocation1);
        }
        if (ticker2 != null && allocation2 != null) {
            allocations.put(ticker2, allocation2);
        }
        portfolioDto.setAllocations(allocations);

        // 날짜 파싱(간단하게 처리)
        if (startDate != null) {
            portfolioDto.setStartDate(LocalDate.parse(startDate));
        }
        if (endDate != null) {
            portfolioDto.setEndDate(LocalDate.parse(endDate));
        }
        portfolioDto.setInitialCapital(initialCapital);

        // 2) 백테스트 서비스 로직 호출
        Map<String, Object> result = backtestService.runBacktest(portfolioDto);

        // 3) 결과를 모델에 담아서 뷰로 전달
        model.addAttribute("result", result);

        // templates/backtestResult.html 로 이동
        return "backtestResult";
    }
}

