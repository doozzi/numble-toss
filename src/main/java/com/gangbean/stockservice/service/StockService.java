package com.gangbean.stockservice.service;

import com.gangbean.stockservice.domain.Stock;
import com.gangbean.stockservice.dto.StockDetailInfoResponse;
import com.gangbean.stockservice.dto.StockListResponse;
import com.gangbean.stockservice.exception.stock.StockNotFoundException;
import com.gangbean.stockservice.repository.StockRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
public class StockService {
    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public StockListResponse respondsOfAllStock() {
        return StockListResponse.responseOf(stockRepository.findAllByOrderByNameDesc());
    }

    public StockDetailInfoResponse responseOfStockDetail(Long stockId, LocalDateTime prevLastEntityIndex) {
        Stock stock = ((prevLastEntityIndex == null) ? stockRepository.findTop10ByIdOrderByHistoriesWrittenAtDesc(stockId)
            : stockRepository.findTop10ByIdAndHistoriesWrittenAtGreaterThanOrderByWrittenAtDesc(stockId, prevLastEntityIndex))
                .orElseThrow(() -> new StockNotFoundException("입력된 ID에 해당하는 주식이 존재하지 않습니다: " + stockId));
        return StockDetailInfoResponse.responseOf(stock);
    }
}
