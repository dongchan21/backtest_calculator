#!/bin/bash
set -e

echo "▶️ Creating table if not exists..."
mysql -uroot -p"$MYSQL_ROOT_PASSWORD" my_stock_db <<EOF
CREATE TABLE IF NOT EXISTS tickers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticker VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255)
);
EOF

echo "📥 Inserting CSV data..."
tail -n +2 /docker-entrypoint-initdb.d/tickers.csv | while IFS=',' read -r ticker name
do
  # 작은따옴표 처리 (SQL injection이나 깨짐 방지)
  ticker_cleaned=$(printf "%s" "$ticker" | sed "s/'/''/g")
  name_cleaned=$(printf "%s" "$name" | sed "s/'/''/g")

  mysql -uroot -p"$MYSQL_ROOT_PASSWORD" my_stock_db -e \
    "INSERT INTO tickers (ticker, name) VALUES ('$ticker_cleaned', '$name_cleaned') ON DUPLICATE KEY UPDATE name='$name_cleaned';"
done