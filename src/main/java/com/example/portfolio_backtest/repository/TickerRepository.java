package com.example.portfolio_backtest.repository;

import com.example.portfolio_backtest.domain.TickerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Repository
public interface TickerRepository extends JpaRepository<TickerEntity, Long> {

    // 티커나 종목명에 검색어가 포함되면 반환
    List<TickerEntity> findByTickerContainingIgnoreCaseOrNameContainingIgnoreCase(String ticker, String name);

    // ✅ 주식 존재 여부 확인 (대소문자 구분 없이 검색)
    boolean existsByTickerIgnoreCase(String ticker);

}


