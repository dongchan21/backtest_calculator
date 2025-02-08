package com.example.portfolio_backtest.service;

import org.springframework.stereotype.Service;

@Service
public class CurrencyConversionService {
    public Double convertToKRW(Double amount, Double exchangeRate) {
        if (exchangeRate == null || exchangeRate <= 0) {
            throw new IllegalArgumentException("Invalid exchange rate");
        }
        return amount * exchangeRate;
    }
}
