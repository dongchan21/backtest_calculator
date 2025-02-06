package com.example.portfolio_backtest.controller;

import com.example.portfolio_backtest.domain.PortfolioDto;
import com.example.portfolio_backtest.service.BacktestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
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
            @RequestParam("assets") List<String> assets,
            @RequestParam("allocations") List<Double> allocations,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("initialSeed") Double initialCapital,
            @RequestParam("monthlyInvestment") Double monthlyInvestment,
            Model model) {

        // 로그 추가 (디버깅용)
        System.out.println("Received assets: " + assets);
        System.out.println("Received allocations: " + allocations);


        //assets, allocations Map으로 변환
        Map<String, Double> allocationsMap = new HashMap<>();
        for (int i = 0; i < assets.size(); i++) {
            allocationsMap.put(assets.get(i), allocations.get(i));
        }

        // 1) 사용자가 입력한 티커/비중 정보를 PortfolioDto 형태로 변환 (간단 예시)
        PortfolioDto portfolioDto = new PortfolioDto();

        // set
        portfolioDto.setAllocations(allocationsMap);
        portfolioDto.setInitialCapital(initialCapital);
        portfolioDto.setMonthlyInvestment(monthlyInvestment);
        // 날짜 파싱(간단하게 처리)
        if (startDate != null) {
            portfolioDto.setStartDate(LocalDate.parse(startDate));
        }
        if (endDate != null) {
            portfolioDto.setEndDate(LocalDate.parse(endDate));
        }


        // 2) 백테스트 서비스 로직 호출
        Map<String, Object> result = backtestService.runBacktest(portfolioDto);

        // 3) 결과를 모델에 담아서 뷰로 전달
        model.addAttribute("result", result);

        // templates/backtestResult.html 로 이동
        return "backtestResult";
    }
}

