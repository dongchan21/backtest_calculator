package com.example.portfolio_backtest.controller;

import com.example.portfolio_backtest.domain.PortfolioDto;
import com.example.portfolio_backtest.entity.StockPrice;
import com.example.portfolio_backtest.service.BacktestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.YearMonth;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

        // 날짜 파싱 (YYYY-MM 형식)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        if (startDate != null) {
            portfolioDto.setStartDate(YearMonth.parse(startDate, formatter));
        }
        if (endDate != null) {
            portfolioDto.setEndDate(YearMonth.parse(endDate, formatter));
        }

        // ✅ 1. 주식명에서 티커(symbol)만 추출
        List<String> tickers = extractTickers(assets);
        System.out.println("Extracted Tickers: " + tickers);
        tickers.add("KRW=X"); // 환율 티커 추가


        //Python 서버로 날짜 넘겨주기
        LocalDate start = LocalDate.parse(startDate + "-01");
        LocalDate end = LocalDate.parse(endDate + "-01");

        // 📌 Python 서버에 주식 데이터 요청
        RestTemplate restTemplate = new RestTemplate();
        String pythonApiUrl = "http://localhost:5000/update_stock_data";  // Python Flask API URL

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("tickers", tickers);
        requestBody.put("start_date", start.toString());
        requestBody.put("end_date", end.toString());

        restTemplate.postForObject(pythonApiUrl, requestBody, String.class);

        // ✅ 주가 및 환율 데이터 가져오기
        Map<String, List<StockPrice>> stockData = backtestService.getStockDataWithKRW(tickers, start, end);

        // ✅ 원화 주가 계산
        Map<String, List<StockPrice>> stockDataInKRW = backtestService.convertToKRW(stockData);

        model.addAttribute("stockData", stockData);
        model.addAttribute("stockDataInKRW", stockDataInKRW);
        model.addAttribute("assets", tickers);
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        model.addAttribute("allocationsMap", allocationsMap);

        // 상장일 기준으로 필터링
        Map<String, List<StockPrice>> filteredStockData = backtestService.filterStockDataAfterLatestIPO(stockData);

        // 모델에 필터링된 데이터 추가
        model.addAttribute("stockData", filteredStockData);

        // 2) 백테스트 서비스 로직 호출
        Map<String, Object> result = backtestService.runBacktest(portfolioDto);

        // 💡 allocationsMap을 모델에 추가
        model.addAttribute("allocations", allocationsMap);

        // ✅ 월별 시드 계산
        List<Map<String, Object>> monthlySeedResults = backtestService.calculateMonthlySeed(portfolioDto, stockDataInKRW);
        // 모델에 추가
        model.addAttribute("monthlySeedResults", monthlySeedResults);

        // ✅ 투자 원금, 수익금 데이터 생성 (그래프 용)
        List<Double> principalAmounts = new ArrayList<>();
        List<Double> totalSeeds = new ArrayList<>(); // ✅ 누적 시드 (Seed) 저장
        List<String> months = new ArrayList<>();

        for (Map<String, Object> entry : monthlySeedResults) {
            double seed = (double) entry.get("seed");  // 누적 시드 (Seed)
            double principal = (double) entry.get("principal");

            principalAmounts.add(principal);
            months.add(entry.get("date").toString());
            totalSeeds.add(seed);  // ✅ 누적 시드 값 저장
        }


        model.addAttribute("months", months);
        model.addAttribute("principalAmounts", principalAmounts);
        model.addAttribute("totalSeeds", totalSeeds);

        // 3) 결과를 모델에 담아서 뷰로 전달
        model.addAttribute("result", result);

        // ✅ CAGR 계산
        double cagr = backtestService.calculateCAGR(monthlySeedResults);
        model.addAttribute("cagr", cagr);

        // templates/backtestResult.html 로 이동
        return "backtestResult";
    }
    /**
     * ✅ 주식명에서 티커(symbol)만 추출하는 메서드
     * 예: ["Apple Inc. (AAPL)", "Tesla Inc. (TSLA)"] → ["AAPL", "TSLA"]
     */
    private List<String> extractTickers(List<String> assetNames) {
        Pattern pattern = Pattern.compile("\\((.*?)\\)");  // 괄호 안의 티커 추출 정규식
        return assetNames.stream()
                .map(asset -> {
                    Matcher matcher = pattern.matcher(asset);
                    return matcher.find() ? matcher.group(1) : asset;  // 괄호가 없으면 원래 값 반환
                })
                .collect(Collectors.toList());
    }
}

