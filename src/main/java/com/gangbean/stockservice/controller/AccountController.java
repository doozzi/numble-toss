package com.gangbean.stockservice.controller;

import com.gangbean.stockservice.domain.Member;
import com.gangbean.stockservice.dto.AccountDetailInfoResponse;
import com.gangbean.stockservice.dto.AccountInfoListResponse;
import com.gangbean.stockservice.dto.AccountInfoResponse;
import com.gangbean.stockservice.dto.AccountOpenRequest;
import com.gangbean.stockservice.dto.AccountPaymentRequest;
import com.gangbean.stockservice.dto.AccountPaymentResponse;
import com.gangbean.stockservice.dto.BankInfoResponse;
import com.gangbean.stockservice.dto.ExceptionResponse;
import com.gangbean.stockservice.exception.account.AccountException;
import com.gangbean.stockservice.exception.account.AccountNotExistsException;
import com.gangbean.stockservice.exception.account.AccountNotOwnedByLoginUser;
import com.gangbean.stockservice.service.AccountService;
import com.gangbean.stockservice.service.BankService;
import com.gangbean.stockservice.service.MemberService;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class AccountController {

    public static final String LAST_TRADE_HEADER = "Last-Trade-Id";
    public static final String LAST_ACCOUNT_HEADER = "Last-Account-Id";

    private final AccountService accountService;

    private final BankService bankService;

    private final MemberService memberService;

    public AccountController(AccountService accountService, BankService bankService, MemberService memberService) {
        this.accountService = accountService;
        this.bankService = bankService;
        this.memberService = memberService;
    }

    @PostMapping("/accounts/{id}/payments")
    public ResponseEntity<AccountPaymentResponse> pay(@PathVariable Long id, @AuthenticationPrincipal User loginUser
            , @RequestBody AccountPaymentRequest request) {
        Member member = memberService.memberOf(loginUser.getUsername()).asMember();
        AccountPaymentResponse response = accountService.responseOfPayment(member, id, LocalDateTime.now(), request.getPrice());
        return ResponseEntity.created(URI.create("/api/accounts/" + id + "/payments")).body(response);
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<AccountDetailInfoResponse> accountDetail(@Valid HttpServletRequest request
        , @PathVariable Long id, @AuthenticationPrincipal User loginUser) {
        Member member = memberService.memberOf(loginUser.getUsername()).asMember();

        Long lastEntityId = (request.getHeader(LAST_TRADE_HEADER) == null) ? null
            : Long.parseLong(request.getHeader(LAST_TRADE_HEADER));

        AccountDetailInfoResponse response = accountService.responseOfAccountDetail(id, member, lastEntityId);
        HttpHeaders headers = new HttpHeaders(new LinkedMultiValueMap<>(Map.of(
            LAST_TRADE_HEADER, List.of(String.valueOf(response.lastIndex())))
        ));
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

    @GetMapping("/accounts")
    public ResponseEntity<AccountInfoListResponse> accountList(@Valid HttpServletRequest request, @AuthenticationPrincipal User loginUser) {
        Member member = memberService.memberOf(loginUser.getUsername()).asMember();

        Long lastEntityId = (request.getHeader(LAST_ACCOUNT_HEADER) == null) ? null
            : Long.parseLong(request.getHeader(LAST_ACCOUNT_HEADER));
        AccountInfoListResponse response = accountService.allAccounts(member.getId(), lastEntityId);

        HttpHeaders headers = new HttpHeaders(new LinkedMultiValueMap<>(Map.of(
            LAST_ACCOUNT_HEADER, List.of(String.valueOf(response.lastIndex())))
        ));
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<Void> closeAccount(@PathVariable Long id, @AuthenticationPrincipal User loginUser) {
        Member member = memberService.memberOf(loginUser.getUsername()).asMember();
        accountService.close(id, member);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/accounts")
    public ResponseEntity<AccountInfoResponse> openAccount(@RequestBody AccountOpenRequest request
            , @AuthenticationPrincipal User loginUser) {
        BankInfoResponse bankInfoResponse = bankService.existingBank(request.bankName(), request.bankNumber());
        Member member = memberService.memberOf(loginUser.getUsername()).asMember();
        AccountInfoResponse response = accountService.responseOfAccountOpen(member, bankInfoResponse.asBank(), request.getBalance());
        return ResponseEntity.created(URI.create("/api/accounts/" + response.getId())).body(response);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(AccountException e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(AccountNotExistsException e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(AccountNotOwnedByLoginUser e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.FORBIDDEN);
    }

}


