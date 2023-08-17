package com.gangbean.stockservice.controller;

import com.gangbean.stockservice.domain.Member;
import com.gangbean.stockservice.dto.AccountTransferRequest;
import com.gangbean.stockservice.dto.AccountTransferResponse;
import com.gangbean.stockservice.dto.ExceptionResponse;
import com.gangbean.stockservice.exception.account.*;
import com.gangbean.stockservice.service.AccountService;
import com.gangbean.stockservice.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/accounts/{accountId}")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class TradeController {

    private final MemberService memberService;

    private final AccountService accountService;

    public TradeController(MemberService memberService, AccountService accountService) {
        this.memberService = memberService;
        this.accountService = accountService;
    }

    @PostMapping("/trades")
    public ResponseEntity<AccountTransferResponse> makeTransfer(@PathVariable Long accountId
            , @AuthenticationPrincipal User loginUser, @RequestBody AccountTransferRequest request) {
        Member member = memberService.memberOf(loginUser.getUsername()).asMember();
        AccountTransferResponse response = accountService.responseOfTransfer(member, accountId
                , request.getReceiverAccountNumber(), LocalDateTime.now(), request.getAmount());
        return ResponseEntity.created(URI.create(String.format("/api/accounts/%s/trades", accountId))).body(response);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(AccountNotExistsException e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(AccountNotOwnedByLoginUser e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler()
    public ResponseEntity<ExceptionResponse> handleException(TradeBetweenSameAccountsException e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler()
    public ResponseEntity<ExceptionResponse> handleException(AccountCannotDepositBelowZeroAmountException e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(AccountTransferBelowZeroAmountException e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(AccountNotEnoughBalanceException e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(AccountException e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
