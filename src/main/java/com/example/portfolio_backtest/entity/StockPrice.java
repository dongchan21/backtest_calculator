package com.example.portfolio_backtest.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "stock_prices")
public class StockPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ticker;
    private LocalDate date;

    @Column(name = "close_price")  // ✅ 테이블 컬럼명과 매칭
    private Double closePrice;

    @Column(name = "krw_price", nullable = true)  // ✅ 원화 주가 추가
    private Double krwPrice;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(Double closePrice) {
        this.closePrice = closePrice;
    }

    public Double getKrwPrice() {
        return krwPrice;
    }

    public void setKrwPrice(Double krwPrice) {
        this.krwPrice = krwPrice;
    }

    @Override
    public String toString() {
        return "StockPrice{" +
                "ticker='" + ticker + '\'' +
                ", date=" + date +
                ", closePrice=" + closePrice +
                ", krwPrice=" + krwPrice +
                '}';
    }
}
