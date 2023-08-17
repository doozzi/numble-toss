package com.gangbean.stockservice.controller;

import com.gangbean.stockservice.domain.Member;
import com.gangbean.stockservice.dto.*;
import com.gangbean.stockservice.exception.accountstock.AccountStockNotEnoughBalanceException;
import com.gangbean.stockservice.exception.accountstock.AccountStockNotExistsException;
import com.gangbean.stockservice.exception.stock.StockNotEnoughBalanceException;
import com.gangbean.stockservice.exception.stock.StockNotFoundException;
import com.gangbean.stockservice.exception.account.AccountNotEnoughBalanceException;
import com.gangbean.stockservice.exception.account.AccountNotExistsException;
import com.gangbean.stockservice.exception.account.AccountNotOwnedByLoginUser;
import com.gangbean.stockservice.exception.account.AccountException;
import com.gangbean.stockservice.exception.stock.StockBuyForOverCurrentPriceException;
import com.gangbean.stockservice.exception.stock.StockSellForBelowCurrentPriceException;
import com.gangbean.stockservice.service.AccountStockService;
import com.gangbean.stockservice.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/api")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class AccountStockController {

    private final MemberService memberService;
    private final AccountStockService accountStockService;

    public AccountStockController(MemberService memberService, AccountStockService accountStockService) {
        this.memberService = memberService;
        this.accountStockService = accountStockService;
    }

    @PostMapping("/accounts/{accountId}/stocks/{stockId}/selling")
    public ResponseEntity<StockSellResponse> sellStock(@PathVariable Long accountId, @PathVariable Long stockId
            , @AuthenticationPrincipal User loginUser, @RequestBody StockSellRequest request) {
        Member member = memberService.memberOf(loginUser.getUsername()).asMember();
        StockSellResponse response = accountStockService.responseOfSell(member
                , accountId, stockId, request.getAmount(), request.getPrice(), LocalDateTime.now());
        return ResponseEntity
                .created(URI.create(String.format("/api/accounts/%d/stocks/%d/selling", accountId, stockId)))
                .body(response);
    }

    @PostMapping("/accounts/{accountId}/stocks/{stockId}")
    public ResponseEntity<StockBuyResponse> buyStock(@PathVariable Long accountId, @PathVariable Long stockId
            , @AuthenticationPrincipal User loginUser, @RequestBody StockBuyRequest request) {
        Member member = memberService.memberOf(loginUser.getUsername()).asMember();
        StockBuyResponse response = accountStockService.responseOfBuy(member, accountId
                , stockId, request.getAmount(), request.getPrice(), LocalDateTime.now());
        return ResponseEntity
                .created(URI.create(String.format("/api/accounts/%d/stocks/%d", accountId, stockId)))
                .body(response);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(AccountNotExistsException e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(StockNotFoundException e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(AccountNotOwnedByLoginUser e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(StockNotEnoughBalanceException e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(AccountNotEnoughBalanceException e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(StockSellForBelowCurrentPriceException e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(AccountStockNotExistsException e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(AccountStockNotEnoughBalanceException e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(StockBuyForOverCurrentPriceException e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(AccountException e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
