package com.gangbean.stockservice.controller;

import com.gangbean.stockservice.domain.Member;
import com.gangbean.stockservice.dto.ExceptionResponse;
import com.gangbean.stockservice.dto.PaymentReservationRequest;
import com.gangbean.stockservice.dto.PaymentReservationResponse;
import com.gangbean.stockservice.exception.reservation.TradeReservationAtPastTimeException;
import com.gangbean.stockservice.exception.reservation.TradeReservationBelowZeroAmountException;
import com.gangbean.stockservice.exception.account.AccountNotEnoughBalanceException;
import com.gangbean.stockservice.exception.account.AccountNotExistsException;
import com.gangbean.stockservice.exception.account.AccountNotOwnedByLoginUser;
import com.gangbean.stockservice.exception.reservation.TradeReservationNotHourlyBasisTimeException;
import com.gangbean.stockservice.service.MemberService;
import com.gangbean.stockservice.service.TradeReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class ReservationController {

    private final MemberService memberService;

    private final TradeReservationService tradeReservationService;

    public ReservationController(MemberService memberService, TradeReservationService tradeReservationService) {
        this.memberService = memberService;
        this.tradeReservationService = tradeReservationService;
    }

    @PostMapping("/accounts/{accountId}/reservations")
    public ResponseEntity<PaymentReservationResponse> reserve(@PathVariable Long accountId
        , @AuthenticationPrincipal User loginUser, @RequestBody PaymentReservationRequest request) {
        Member member = memberService.memberOf(loginUser.getUsername()).asMember();
        PaymentReservationResponse response = tradeReservationService.responseOfPaymentReservation(member, accountId, request.getSendAt(), request.getAmount());
        return ResponseEntity.created(URI.create("/accounts/" + accountId + "/reservations")).body(response);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(AccountNotExistsException e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(AccountNotOwnedByLoginUser e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(TradeReservationBelowZeroAmountException e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(TradeReservationAtPastTimeException e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(TradeReservationNotHourlyBasisTimeException e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(AccountNotEnoughBalanceException e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
