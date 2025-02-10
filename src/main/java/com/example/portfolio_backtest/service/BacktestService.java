package com.example.portfolio_backtest.service;

import com.example.portfolio_backtest.domain.PortfolioDto;
import com.example.portfolio_backtest.entity.StockPrice;
import com.example.portfolio_backtest.repository.StockPriceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
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

    public Map<String, List<StockPrice>> getStockDataWithKRW(List<String> tickers, LocalDate startDate, LocalDate endDate) {
        return tickers.stream()
                .collect(Collectors.toMap(
                        ticker -> ticker,
                        ticker -> stockPriceRepository.findByTickerAndDateBetweenOrderByDateAsc(ticker, startDate, endDate)
                ));
    }

    public Map<String, List<StockPrice>> convertToKRW(Map<String, List<StockPrice>> stockData) {
        List<StockPrice> krwExchangeRates = stockData.getOrDefault("KRW=X", Collections.emptyList());

        Map<String, List<StockPrice>> convertedData = new HashMap<>();

        for (Map.Entry<String, List<StockPrice>> entry : stockData.entrySet()) {
            if (entry.getKey().equals("KRW=X")) continue;

            List<StockPrice> pricesInKRW = new ArrayList<>();

            for (StockPrice price : entry.getValue()) {
                Optional<StockPrice> exchangeRateOpt = krwExchangeRates.stream()
                        .filter(krw -> krw.getDate().equals(price.getDate()))
                        .findFirst();

                if (exchangeRateOpt.isPresent()) {
                    double krwPrice = price.getClosePrice() * exchangeRateOpt.get().getClosePrice();
                    price.setKrwPrice(krwPrice);
                }
                pricesInKRW.add(price);
            }

            convertedData.put(entry.getKey(), pricesInKRW);
        }
        return convertedData;
    }

    // ✅ 월별 시드 계산 로직
    public List<Map<String, Object>> calculateMonthlySeed(PortfolioDto portfolioDto, Map<String, List<StockPrice>> stockDataInKRW) {

        List<Map<String, Object>> monthlyResults = new ArrayList<>();
        double currentSeed = portfolioDto.getInitialCapital(); // 초기 시드

        for (YearMonth date = portfolioDto.getStartDate(); !date.isAfter(portfolioDto.getEndDate()); date = date.plusMonths(1)) {
            double monthlyReturn = 0.0; // 월별 수익률

            for (Map.Entry<String, List<StockPrice>> entry : stockDataInKRW.entrySet()) {
                String ticker = entry.getKey();
                List<StockPrice> stockPrices = entry.getValue();

                YearMonth finalDate = date;

                // 이번 달 주가 데이터 찾기
                Optional<StockPrice> currentPriceOpt = stockPrices.stream()
                        .filter(price -> YearMonth.from(price.getDate()).equals(finalDate))
                        .findFirst();

                // 지난달 주가 데이터 찾기
                YearMonth prevDate = finalDate.minusMonths(1);
                Optional<StockPrice> prevPriceOpt = stockPrices.stream()
                        .filter(price -> YearMonth.from(price.getDate()).equals(prevDate))
                        .findFirst();

                // 지난달, 이번 달 주가가 존재하면 수익률 계산
                if (currentPriceOpt.isPresent() && prevPriceOpt.isPresent()) {
                    double currentPrice = currentPriceOpt.get().getKrwPrice();
                    double prevPrice = prevPriceOpt.get().getKrwPrice();


                    // 수익률 계산 (이번 달 주가 대비 지난달 대비 상승률)
                    double returnRate = (currentPrice - prevPrice) / prevPrice;

                    // 비율 가져오기
                    double allocation = portfolioDto.getAllocations().entrySet().stream()
                            .filter(e -> extractTicker(e.getKey()).equals(ticker))
                            .map(Map.Entry::getValue)
                            .findFirst()
                            .orElse(0.0);
                    // 각 종목별 수익률 반영
                    monthlyReturn += returnRate * allocation /100;
                    System.out.println("monthlyReturn = " + monthlyReturn);
                }
            }

            // 💰 투자 수익 반영
            currentSeed += currentSeed * monthlyReturn;
            System.out.println("currentSeed = " + currentSeed);
            currentSeed += portfolioDto.getMonthlyInvestment(); // 월 납입금 추가

            // 결과 저장
            Map<String, Object> result = new HashMap<>();
            result.put("date", date);
            result.put("seed", (double) currentSeed);
            System.out.println("Seed Type: " + result.get("seed").getClass().getSimpleName());
            System.out.println("result = " + result);
            monthlyResults.add(result);


        }

        return monthlyResults;
    }


    // 기존 메서드에 추가된 필터링 로직
    public Map<String, List<StockPrice>> filterStockDataAfterLatestIPO(Map<String, List<StockPrice>> stockData) {
        // 가장 늦은 시작 날짜 계산
        LocalDate latestIPODate = stockData.values().stream()
                .filter(prices -> !prices.isEmpty())
                .map(prices -> prices.get(0).getDate()) // 각 주식 데이터의 첫 번째 날짜
                .max(LocalDate::compareTo)             // 가장 늦은 날짜 찾기
                .orElseThrow(() -> new IllegalStateException("No stock data available"));

        // 각 주식 데이터에서 기준 날짜 이후 데이터만 필터링
        return stockData.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .filter(price -> !price.getDate().isBefore(latestIPODate)) // 기준 날짜 이후만 필터
                                .collect(Collectors.toList())
                ));
    }

    private String extractTicker(String stockName) {
        return stockName.replaceAll(".*\\((.*?)\\)", "$1"); // 괄호 안의 ticker 추출
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
