import yfinance as yf
import pymysql
import pandas as pd
from datetime import datetime, date, timedelta
from pandas import to_datetime
from concurrent.futures import ThreadPoolExecutor, as_completed
from flask import Flask, request, jsonify

app = Flask(__name__)

# 📌 주가 데이터를 MySQL에 저장하는 함수
# 📌 MySQL에 데이터 저장하는 함수
def save_to_db(ticker, prices, dividends, dividends_krw):
    conn = pymysql.connect(host='mysql', user='root', password='qzwxec!&93', database='my_stock_db')
    cursor = conn.cursor()

    # ✅ 테이블 생성 (배당금 + 원화 배당금 컬럼 추가)
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS stock_prices (
            id INT AUTO_INCREMENT PRIMARY KEY,
            ticker VARCHAR(10),
            date DATE,
            close_price FLOAT,
            dividends FLOAT DEFAULT 0,
            dividends_krw FLOAT DEFAULT 0,  -- ✅ 원화 환산 배당금 컬럼 추가
            UNIQUE KEY unique_ticker_date (ticker, date)
        )
    """)

    # ✅ 데이터 삽입 (배당금 + 원화 배당금 포함)
    for date, price in prices.items():
        dividend = dividends.get(date, 0)
        dividend_krw = dividends_krw.get(date, 0)

        try:
            # ✅ KRW=X (환율)일 경우에는 배당금 없이 저장
            if ticker == "KRW=X":

                if price < 1:
                    price *= 10000

                cursor.execute("""
                    INSERT INTO stock_prices (ticker, date, close_price)
                    VALUES (%s, %s, %s)
                    ON DUPLICATE KEY UPDATE 
                        close_price = VALUES(close_price);
                """, (ticker, date, price))
            else:
                cursor.execute("""
                    INSERT INTO stock_prices (ticker, date, close_price, dividends, dividends_krw)
                    VALUES (%s, %s, %s, %s, %s)
                    ON DUPLICATE KEY UPDATE 
                        close_price = VALUES(close_price),
                        dividends = VALUES(dividends),
                        dividends_krw = VALUES(dividends_krw);
                """, (ticker, date, price, dividend, dividend_krw))

        except Exception as e:
            print(f"Error inserting {ticker}, {date}, {price}, {dividend}, {dividend_krw}: {e}")

    conn.commit()
    cursor.close()
    conn.close()

# 📌 Yahoo Finance에서 주가 및 배당금 가져오기
def get_monthly_prices(ticker, start_date, end_date):
    end_date_obj = datetime.strptime(end_date, "%Y-%m-%d") + timedelta(days=1)
    stock = yf.Ticker(ticker)
    
    # ✅ 주가 데이터 가져오기 (월별 1일 기준)
    data = stock.history(start=start_date, end=end_date_obj.strftime("%Y-%m-%d"), interval="1mo")

    # ✅ 배당금 데이터 가져오기
    dividends = stock.dividends
    dividend_data = {d.strftime("%Y-%m-%d"): v for d, v in dividends.items()}  # 딕셔너리 변환

    prices = {}
    dividend_results = {}

    for date, row in data.iterrows():
        date_str = date.strftime("%Y-%m-%d")
        prices[date_str] = row["Close"]

        # ✅ 배당이 지급된 달에만 해당 월 1일에 반영
        month_key = date.strftime("%Y-%m")  # "YYYY-MM" 형식
        monthly_dividend = 0

        for d, value in dividend_data.items():
            if d.startswith(month_key):  # 해당 월에 배당이 지급되었는지 확인
                monthly_dividend += value

        dividend_results[date_str] = monthly_dividend

    return prices, dividend_results

# 📌 KRW=X (환율) 가져오기
def get_exchange_rates(start_date, end_date):
    stock = yf.Ticker("KRW=X")
    data = stock.history(start=start_date, end=end_date, interval="1mo")
    
    exchange_rates = {}
    for date, row in data.iterrows():
        exchange_rates[date.strftime("%Y-%m-%d")] = row["Close"]

    return exchange_rates

# 📌 Java(Spring Boot)에서 요청받아 데이터 업데이트
@app.route('/update_stock_data', methods=['POST'])
def update_stock_data():
    request_data = request.json
    tickers = request_data["tickers"]
    start_date = request_data["start_date"]
    end_date = request_data["end_date"]

    # ✅ 환율 데이터 가져오기
    exchange_rates = get_exchange_rates(start_date, end_date)

    for ticker in tickers:
        prices, dividends = get_monthly_prices(ticker, start_date, end_date)

        # ✅ 원화 환산 배당금 계산
        dividends_krw = {date: dividends[date] * exchange_rates.get(date, 0) for date in dividends}

        save_to_db(ticker, prices, dividends, dividends_krw)
    
    return jsonify({"message": "Stock data updated successfully"}), 200

if __name__ == "__main__":
    app.run(port=5000, host="0.0.0.0")