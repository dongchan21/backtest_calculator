package com.example.portfolio_backtest.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "tickers")
public class TickerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private String ticker;
    private String name;

    // Getter / Setter

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
