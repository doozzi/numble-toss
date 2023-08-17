package com.gangbean.stockservice.controller;


import com.gangbean.stockservice.dto.ExceptionResponse;
import com.gangbean.stockservice.dto.SignupRequest;
import com.gangbean.stockservice.dto.SignupResponse;
import com.gangbean.stockservice.exception.member.MemberDuplicateException;
import com.gangbean.stockservice.jwt.TokenProvider;
import com.gangbean.stockservice.service.MemberService;
import com.gangbean.stockservice.util.SecurityUtil;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
public class MemberController {

    private final MemberService memberService;

    private final TokenProvider tokenProvider;

    public MemberController(MemberService memberService, TokenProvider tokenProvider) {
        this.memberService = memberService;
        this.tokenProvider = tokenProvider;
    }

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("hello");
    }

    @PostMapping("/test-redirect")
    public void testRedirect(HttpServletResponse response) throws IOException {
        response.sendRedirect("/api/user");
    }

    @PostMapping("/members")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest signupRequest) {
        return ResponseEntity.ok(memberService.signup(signupRequest));
    }

    @GetMapping("/members")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<SignupResponse> getMyUserInfo() {
        return ResponseEntity.ok(memberService.getMyUserWithAuthorities());
    }

    @GetMapping("/members/{username}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<SignupResponse> getUserInfo(@PathVariable String username) {
        return ResponseEntity.ok(memberService.getUserWithAuthorities(username));
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(MemberDuplicateException e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.CONFLICT);
    }

    @DeleteMapping("/members")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity withdraw(@Valid HttpServletRequest httpServletRequest) {
        Long loginMemberId = tokenProvider
            .asClaim(SecurityUtil.resolveToken(httpServletRequest))
            .get(TokenProvider.MEMBER_ID, Long.class);
        memberService.withdraw(loginMemberId);
        return ResponseEntity.noContent().build();
    }
}