package com.example.portfolio_backtest.domain;
import com.example.portfolio_backtest.entity.StockPrice;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class FilteredStockResult {
    private final Map<String, List<StockPrice>> filteredData;
    private final String latestTicker;
    private final LocalDate latestIPODate;

    // constructor, getters


    public FilteredStockResult(Map<String, List<StockPrice>> filteredData, String latestTicker, LocalDate latestIPODate) {
        this.filteredData = filteredData;
        this.latestTicker = latestTicker;
        this.latestIPODate = latestIPODate;
    }

    public Map<String, List<StockPrice>> getFilteredData() {
        return filteredData;
    }

    public String getLatestTicker() {
        return latestTicker;
    }
}
