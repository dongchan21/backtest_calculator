package com.example.portfolio_backtest;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Converter(autoApply = true)
public class YearMonthAttributeConverter implements AttributeConverter<YearMonth, String> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    public String convertToDatabaseColumn(YearMonth yearMonth) {
        return (yearMonth == null ? null : yearMonth.format(FORMATTER));
    }

    @Override
    public YearMonth convertToEntityAttribute(String dbData) {
        return (dbData == null ? null : YearMonth.parse(dbData, FORMATTER));
    }
}
