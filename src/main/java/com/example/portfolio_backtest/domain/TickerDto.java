package com.example.portfolio_backtest.domain;

public class TickerDto {
    private String ticker;    // 예: "QLD"
    private String fullName;  // 예: "ProShares Ultra QQQ (QLD)"

    public TickerDto() { }

    public TickerDto(String ticker, String fullName) {
        this.ticker = ticker;
        this.fullName = fullName;
    }

    public String getTicker() {
        return ticker;
    }
    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
