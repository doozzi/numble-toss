package com.gangbean.stockservice.service;

import com.gangbean.stockservice.domain.Member;
import com.gangbean.stockservice.domain.Token;
import com.gangbean.stockservice.dto.LoginResponse;
import com.gangbean.stockservice.exception.member.RefreshTokenNotFoundException;
import com.gangbean.stockservice.jwt.TokenProvider;
import com.gangbean.stockservice.repository.TokenRepository;
import java.util.Date;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;
    private final TokenProvider tokenProvider;

    public TokenService(TokenRepository tokenRepository, TokenProvider tokenProvider) {
        this.tokenRepository = tokenRepository;
        this.tokenProvider = tokenProvider;
    }

    public LoginResponse newToken(Authentication loginUserAuthentication, Member requestMember) {
        Date now = new Date();
        String accessToken = tokenProvider.newAccessTokenOf(loginUserAuthentication,
            tokenProvider.accessTokenExpirationDateFrom(now),
            requestMember.getId());

        Date refreshTokenExpiration = tokenProvider.refreshTokenExpirationDateFrom(now);
        String refreshToken = tokenProvider.newRefreshTokenOf(loginUserAuthentication,
            refreshTokenExpiration,
            requestMember.getId());

        tokenRepository.save(Token.builder()
            .member(requestMember)
            .refreshToken(refreshToken)
            .expiration(refreshTokenExpiration)
            .build());

        return new LoginResponse.Builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }

    public LoginResponse reissue(String accessToken, Date requestDate, String refreshToken) {
        Long loginMemberId = tokenProvider.asClaim(accessToken)
            .get(TokenProvider.MEMBER_ID, Long.class);

        Token validToken = notExpiredAndMatchedToken(loginMemberId, requestDate, refreshToken);
        Authentication loginAuthentication = currentAuthentication();

        String reissuedAccessToken = tokenProvider.newAccessTokenOf(loginAuthentication,
            tokenProvider.accessTokenExpirationDateFrom(requestDate), loginMemberId);

        validToken.reissue(tokenProvider.refreshTokenExpirationDateFrom(requestDate));
        tokenRepository.save(validToken);

        return new LoginResponse.Builder()
            .accessToken(reissuedAccessToken)
            .refreshToken(validToken.getRefreshToken())
            .build();
    }

    private Token notExpiredAndMatchedToken(Long loginMemberId, Date now, String refreshToken) {
        Token matchedToken = tokenRepository
            .findByRefreshToken(refreshToken)
            .orElseThrow(() -> new RefreshTokenNotFoundException("일치하는 Refresh Token이 존재하지 않습니다: " + refreshToken));

        matchedToken.isExpired(now);
        matchedToken.isOwnedBy(loginMemberId);

        return matchedToken;
    }

    private Authentication currentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
