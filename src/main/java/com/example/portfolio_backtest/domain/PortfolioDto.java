package com.example.portfolio_backtest.domain;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

public class PortfolioDto {
    private Map<String, Double> allocations;
    private YearMonth startDate;
    private YearMonth endDate;
    private double initialCapital;
    private double monthlyInvestment;
    private String LatestIPOTicker;
    // Getter/Setter
    public Map<String, Double> getAllocations() {
        return allocations;
    }

    public void setAllocations(Map<String, Double> allocations) {
        this.allocations = allocations;
    }

    public YearMonth getStartDate() {
        return startDate;
    }
    public void setStartDate(YearMonth startDate) {
        this.startDate = startDate;
    }

    public YearMonth getEndDate() {
        return endDate;
    }
    public void setEndDate(YearMonth endDate) {
        this.endDate = endDate;
    }

    public double getInitialCapital() {
        return initialCapital;
    }
    public void setInitialCapital(double initialCapital) {
        this.initialCapital = initialCapital;
    }

    public double getMonthlyInvestment() {
        return monthlyInvestment;
    }

    public void setMonthlyInvestment(double monthlyInvestment) {
        this.monthlyInvestment = monthlyInvestment;
    }

    public String getLatestIPOTicker() {
        return LatestIPOTicker;
    }

    public void setLatestIPOTicker(String latestIPOTicker) {
        LatestIPOTicker = latestIPOTicker;
    }
}
