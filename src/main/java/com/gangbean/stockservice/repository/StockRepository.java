package com.gangbean.stockservice.repository;

import com.gangbean.stockservice.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {

    List<Stock> findAllByOrderByNameDesc();

    @Query(nativeQuery = true, value = "SELECT s.*, sh.*\n" +
            " FROM stock s\n" +
            " LEFT OUTER JOIN stock_histories h ON h.stock_id = s.id\n" +
            " LEFT OUTER JOIN stock_history sh ON h.histories_id = sh.id\n" +
            "WHERE s.id = :id\n" +
            "ORDER BY sh.written_at DESC LIMIT 10")
    Optional<Stock> findTop10ByIdOrderByHistoriesWrittenAtDesc(@Param("id") Long id);

    @Query(nativeQuery = true, value = "SELECT s.*, sh.*\n" +
            " FROM stock s\n" +
            " LEFT OUTER JOIN stock_histories h ON h.stock_id = s.id\n" +
            " LEFT OUTER JOIN stock_history sh ON h.histories_id = sh.id\n" +
            "  AND sh.written_at < :prevWrittenAt\n" +
            "WHERE s.id = :id\n" +
            "ORDER BY sh.written_at DESC LIMIT 10")
    Optional<Stock> findTop10ByIdAndHistoriesWrittenAtGreaterThanOrderByWrittenAtDesc(@Param("id") Long id
        , @Param("prevWrittenAt") LocalDateTime prevLastEntityIndex);

    List<Stock> findTop10ByOrderById();
}
