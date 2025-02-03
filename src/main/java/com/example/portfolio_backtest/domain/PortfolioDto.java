package com.example.portfolio_backtest.domain;
import java.time.LocalDate;
import java.util.Map;

public class PortfolioDto {
    private Map<String, Double> allocations;
    private LocalDate startDate;
    private LocalDate endDate;
    private double initialCapital;

    // Getter/Setter
    public Map<String, Double> getAllocations() {
        return allocations;
    }
    public void setAllocations(Map<String, Double> allocations) {
        this.allocations = allocations;
    }

    public LocalDate getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public double getInitialCapital() {
        return initialCapital;
    }
    public void setInitialCapital(double initialCapital) {
        this.initialCapital = initialCapital;
    }
}
