package com.gangbean.stockservice.controller;

import com.gangbean.stockservice.domain.Member;
import com.gangbean.stockservice.dto.ExceptionResponse;
import com.gangbean.stockservice.dto.LoginRequest;
import com.gangbean.stockservice.dto.LoginResponse;
import com.gangbean.stockservice.exception.member.MemberNotFoundException;
import com.gangbean.stockservice.exception.member.RefreshTokenExpiredException;
import com.gangbean.stockservice.exception.member.RefreshTokenNotFoundException;
import com.gangbean.stockservice.service.MemberService;
import com.gangbean.stockservice.service.TokenService;
import com.gangbean.stockservice.util.SecurityUtil;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class AuthController {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String SET_COOKIE_HEADER = "Set-Cookie";
    public static final String REFRESH_TOKEN_PREFIX = "refresh_token=";
    public static final String REFRESH_TOKEN_SUFFIX = "; HttpOnly";
    public static final String ACCESS_TOKEN_PREFIX = "Bearer ";

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final MemberService memberService;
    private final TokenService tokenService;

    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder
        , MemberService memberService, TokenService tokenService) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.memberService = memberService;
        this.tokenService = tokenService;
    }

    @PostMapping("/reissue")
    @Transactional
    public ResponseEntity<LoginResponse> reissue(@Valid HttpServletRequest httpServletRequest
        , @Valid @RequestBody LoginResponse tokenDto) {
        String accessToken = SecurityUtil.resolveToken(httpServletRequest);

        LoginResponse reissuedTokenDto = reissuedTokenWithInfoOf(accessToken, new Date(), tokenDto.getRefreshToken());

        HttpHeaders responseHeaders = responseHeadersWithTokenValuesOf(
            ACCESS_TOKEN_PREFIX + reissuedTokenDto.getAccessToken(),
            REFRESH_TOKEN_PREFIX + reissuedTokenDto.getRefreshToken() + REFRESH_TOKEN_SUFFIX);

        return new ResponseEntity<>(reissuedTokenDto, responseHeaders, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        Member loginMember = memberService.memberOf(loginRequest.getUsername(), loginRequest.getPassword());

        Authentication loginUserAuthentication = newAuthentication(loginMember.getId(), loginRequest.getPassword());

        LoginResponse loginMemberToken = tokenService.newToken(loginUserAuthentication, loginMember);

        HttpHeaders httpHeaders = responseHeadersWithTokenValuesOf(
            ACCESS_TOKEN_PREFIX + loginMemberToken.getAccessToken(),
            REFRESH_TOKEN_PREFIX + loginMemberToken.getRefreshToken() + REFRESH_TOKEN_SUFFIX);

        return new ResponseEntity<>(loginMemberToken, httpHeaders, HttpStatus.OK);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(MemberNotFoundException e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(RefreshTokenNotFoundException e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(RefreshTokenExpiredException e) {
        return new ResponseEntity<>(new ExceptionResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    private Authentication newAuthentication(Long principal, String credential) {
        UsernamePasswordAuthenticationToken newToken =
            new UsernamePasswordAuthenticationToken(principal, credential);

        Authentication authentication = newAuthentication(newToken);

        saveAuthentication(authentication);

        return authentication;
    }

    private Authentication newAuthentication(UsernamePasswordAuthenticationToken newToken) {
        return authenticationManagerBuilder.getObject().authenticate(newToken);
    }

    private void saveAuthentication(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private HttpHeaders responseHeadersWithTokenValuesOf (String accessToken, String refreshToken) {
        return new HttpHeaders(new LinkedMultiValueMap<>(Map.of(
            AUTHORIZATION_HEADER, List.of(accessToken),
            SET_COOKIE_HEADER, List.of(refreshToken))
        ));
    }

    private LoginResponse reissuedTokenWithInfoOf (String accessToken, Date now, String refreshToken) {
        return tokenService.reissue(accessToken, now, refreshToken);
    }
}