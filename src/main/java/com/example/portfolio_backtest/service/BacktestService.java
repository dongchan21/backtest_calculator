package com.example.portfolio_backtest.service;

import com.example.portfolio_backtest.domain.FilteredStockResult;
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

    private final StockPriceRepository stockPriceRepository;

    public BacktestService(StockPriceRepository stockPriceRepository) {
        this.stockPriceRepository = stockPriceRepository;
    }

    public LocalDate getFirstStockDate(String ticker) {
        return stockPriceRepository.findFirstByTickerOrderByDateAsc(ticker)
                .map(StockPrice::getDate)
                .orElse(null); // 데이터가 없으면 null 반환
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
        double cumulativeDividends = 0.0; // 누적 배당금
        double totalInvested = portfolioDto.getInitialCapital(); // 누적 투자 금액
        double principal = currentSeed;  // ✅ 투자 원금 저장

        // ✅ 최신 상장 주식 이후 데이터만 필터링
        Map<String, List<StockPrice>> filteredStockData = filterStockDataAfterLatestIPO(stockDataInKRW);


        // ✅ 가장 늦게 상장된 주식의 상장일 찾기
        LocalDate latestIPODate = filteredStockData.values().stream()
                .filter(prices -> !prices.isEmpty())
                .map(prices -> prices.get(0).getDate()) // 각 주식 데이터의 첫 번째 날짜
                .max(LocalDate::compareTo) // 가장 늦게 상장된 날짜 찾기
                .orElse(portfolioDto.getStartDate().atDay(1)); // 기본값: 시작일

        System.out.println("🔹 Latest IPO Date: " + latestIPODate);

        for (YearMonth date = YearMonth.from(latestIPODate); !date.isAfter(portfolioDto.getEndDate()); date = date.plusMonths(1)) {
            double monthlyReturn = 0.0; // 월별 수익률
            double monthlyDividend = 0.0;

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


                    // ✅ 보유 주식 개수 = 원화 시드 / 원화 주가

                    // 비율 가져오기
                    double allocation = portfolioDto.getAllocations().entrySet().stream()
                            .filter(e -> extractTicker(e.getKey()).equals(ticker))
                            .map(Map.Entry::getValue)
                            .findFirst()
                            .orElse(0.0);
                    System.out.println("allocation : " + allocation);

                    double allocatedSeed = (currentSeed * allocation) / 100;
                    double sharesOwned = allocatedSeed / currentPrice;

                    // ✅ 배당금 계산 (보유 주식 개수 × 1주당 배당금)
                    double dividendsKrw = currentPriceOpt.get().getDividendsKrw();
                    monthlyDividend += sharesOwned * dividendsKrw;

                    // 각 종목별 수익률 반영
                    monthlyReturn += returnRate * allocation / 100;
                    System.out.println("monthlyReturn = " + monthlyReturn);
                }
            }

            // 💰 투자 수익 반영
            currentSeed += currentSeed * monthlyReturn;
            System.out.println("currentSeed = " + currentSeed);
            currentSeed += portfolioDto.getMonthlyInvestment(); // 월 납입금 추가
            totalInvested += portfolioDto.getMonthlyInvestment(); // 누적 투자금 증가
            cumulativeDividends += monthlyDividend;
            currentSeed += monthlyDividend; // 배당금도 다시 투자됨

            // 📌 투자 수익금 및 수익률 계산
            double investmentReturn = currentSeed - totalInvested;
            double returnRatePercentage = (investmentReturn / totalInvested) * 100;
            principal += portfolioDto.getMonthlyInvestment();  // ✅ 매달 납입금 누적
            // 결과 저장
            Map<String, Object> result = new HashMap<>();
            result.put("date", date);
            result.put("seed", (double) currentSeed);
            result.put("cumulativeDividends", cumulativeDividends);
            result.put("investmentReturn", investmentReturn);
            result.put("totalInvestment", totalInvested);
            result.put("returnRatePercentage", returnRatePercentage);
            result.put("principal", principal);  // ✅ 투자 원금 저장
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

    public FilteredStockResult filterStockDataAndFindLatest(Map<String, List<StockPrice>> stockData, PortfolioDto portfolioDto) {
        return stockData.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(entry -> Map.entry(entry.getKey(), entry.getValue().get(0).getDate()))
                .max(Map.Entry.comparingByValue())
                .map(latestEntry -> {
                    String latestTicker = latestEntry.getKey();
                    LocalDate latestIPODate = latestEntry.getValue();

                    // 🔥 여기서 주식명(티커) 형태 찾기
                    String fullName = portfolioDto.getAllocations().keySet().stream()
                            .filter(name -> extractTicker(name).equals(latestTicker))
                            .findFirst()
                            .orElse(latestTicker); // 못 찾으면 티커만

                    Map<String, List<StockPrice>> filtered = stockData.entrySet().stream()
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    entry -> entry.getValue().stream()
                                            .filter(price -> !price.getDate().isBefore(latestIPODate))
                                            .collect(Collectors.toList())
                            ));

                    return new FilteredStockResult(filtered, fullName, latestIPODate);
                })
                .orElseThrow(() -> new IllegalStateException("No stock data"));
    }

    private String extractTicker(String stockName) {
        return stockName.replaceAll(".*\\((.*?)\\)", "$1"); // 괄호 안의 ticker 추출
    }

    public double calculateCAGR(List<Map<String, Object>> monthlyResults) {
        if (monthlyResults.size() < 2) return 0.0; // 데이터가 부족하면 0 반환

        double initialSeed = (double) monthlyResults.get(0).get("seed");  // 첫 달 시드
        double finalSeed = (double) monthlyResults.get(monthlyResults.size() - 1).get("seed");  // 마지막 달 시드
        int months = monthlyResults.size(); // 전체 개월 수

        double cagr = Math.pow(finalSeed / initialSeed, 1.0 / (months / 12.0)) - 1;
        return cagr * 100; // 퍼센트 단위로 변환
    }


    public Map<String, Object> runBacktest(PortfolioDto portfolioDto) {
        // 나중에 실제 계산 로직(주가 데이터 등)을 붙일 수 있음

        // 예시 결과: "총 수익률", "그래프 데이터" 등
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("totalReturn", 0.12); // 12% 수익이라고 가정
        resultMap.put("startDate", portfolioDto.getStartDate());
        resultMap.put("endDate", portfolioDto.getEndDate());
        resultMap.put("latestIPOTicker", portfolioDto.getLatestIPOTicker());
        resultMap.put("initialCapital", portfolioDto.getInitialCapital());
        resultMap.put("monthlyInvestment", portfolioDto.getMonthlyInvestment());
        resultMap.put("assets", portfolioDto.getAllocations());

        return resultMap;
    }

    public String formatYearMonth(String raw) {
        String[] parts = raw.split("-");
        return parts[0] + "년 " + Integer.parseInt(parts[1]) + "월";
    }

    public double calculateMDD(List<Map<String, Object>> monthlyResults) {
        if (monthlyResults == null || monthlyResults.isEmpty()) return 0.0;

        double maxPeak = (double) monthlyResults.get(0).get("seed");  // 초기 최고 시드
        double maxDrawdown = 0.0;

        for (Map<String, Object> result : monthlyResults) {
            double currentSeed = (double) result.get("seed");

            if (currentSeed > maxPeak) {
                maxPeak = currentSeed; // 새로운 최고점 갱신
            } else {
                double drawdown = (maxPeak - currentSeed) / maxPeak;
                if (drawdown > maxDrawdown) {
                    maxDrawdown = drawdown; // 최대 낙폭 갱신
                }
            }
        }

        return maxDrawdown * 100; // 퍼센트로 반환 (예: 15.3%)
    }


}

