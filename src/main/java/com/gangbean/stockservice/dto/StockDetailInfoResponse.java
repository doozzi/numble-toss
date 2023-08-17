package com.gangbean.stockservice.dto;

import com.gangbean.stockservice.domain.Stock;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class StockDetailInfoResponse {
    private Long stockId;
    private String stockName;
    private List<StockHistoryInfoResponse> histories;

    public StockDetailInfoResponse(Long stockId, String stockName, List<StockHistoryInfoResponse> histories) {
        this.stockId = stockId;
        this.stockName = stockName;
        this.histories = histories;
    }

    public static StockDetailInfoResponse responseOf(Stock stock) {
        return new StockDetailInfoResponse(stock.id(), stock.name(),
                stock.histories().stream()
                        .map(StockHistoryInfoResponse::responseOf)
                        .collect(Collectors.toList()));
    }

    public LocalDateTime lastIndex() {
        return (histories.size() == 0) ? LocalDateTime.MIN : histories.get(histories.size() - 1).getCreatedAt();
    }
}
