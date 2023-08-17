package com.gangbean.stockservice.controller;

import com.gangbean.stockservice.dto.ExceptionResponse;
import com.gangbean.stockservice.dto.StockDetailInfoResponse;
import com.gangbean.stockservice.dto.StockListResponse;
import com.gangbean.stockservice.exception.stock.StockNotFoundException;
import com.gangbean.stockservice.service.StockService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class StockController {

    public static final String LAST_HISTORY_HEADER = "Last-History-Index";
    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }
    
    @GetMapping("/stocks")
    public ResponseEntity<StockListResponse> stockList() {
        return ResponseEntity.ok(stockService.respondsOfAllStock());
    }

    @GetMapping("/stocks/{stockId}")
    public ResponseEntity<StockDetailInfoResponse> stockDetail(@Valid HttpServletRequest request, @PathVariable Long stockId) {
        LocalDateTime lastEntityIndex = (request.getHeader(LAST_HISTORY_HEADER) == null) ? null
            : LocalDateTime.parse(request.getHeader(LAST_HISTORY_HEADER));

        System.out.println(lastEntityIndex);

        StockDetailInfoResponse response = stockService.responseOfStockDetail(stockId, lastEntityIndex);
        HttpHeaders headers = new HttpHeaders(new LinkedMultiValueMap<>(Map.of(
            LAST_HISTORY_HEADER, List.of(String.valueOf(response.lastIndex())))
        ));
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleError(StockNotFoundException e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.NOT_FOUND);
    }
}
