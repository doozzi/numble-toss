package com.gangbean.stockservice.dto;

import com.gangbean.stockservice.domain.Stock;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class StockListResponse {
    private List<StockInfoResponse> stocks;

    public static StockListResponse responseOf(List<Stock> stocks) {
        return new StockListResponse(stocks.stream()
                .map(StockInfoResponse::responseOf)
                .collect(Collectors.toList()));
    }
}
