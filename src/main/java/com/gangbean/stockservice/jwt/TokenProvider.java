package com.gangbean.stockservice.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class TokenProvider implements InitializingBean {
    public static final String MEMBER_ID = "memberId";
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenProvider.class);
    private static final String AUTHORITIES_KEY = "auth";
    private static final int MILLI_SECONDS = 1000;
    private static final String DELIMITER = ",";
    public static final String EMPTY_PASSWORD = "";

    private final String secret;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;
    private Key key;

    public TokenProvider(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.access-token-validity-in-seconds}") long tokenValidityInSeconds,
        @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidityInMilliseconds) {
        this.secret = secret;
        this.accessTokenValidityInMilliseconds = tokenValidityInSeconds * MILLI_SECONDS;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInMilliseconds * MILLI_SECONDS;
    }

    @Override
    public void afterPropertiesSet() {
        createKey();
    }

    public String newRefreshTokenOf(Authentication authentication, Date expiration, Long memberId) {
        return createToken(authentication, expiration, memberId);
    }

    public String newAccessTokenOf(Authentication authentication, Date expiration, Long memberId) {
        return createToken(authentication, expiration, memberId);
    }

    public Date accessTokenExpirationDateFrom(Date now) {
        return new Date(now.getTime() + this.accessTokenValidityInMilliseconds);
    }

    public Date refreshTokenExpirationDateFrom(Date now) {
        return new Date(now.getTime() + this.refreshTokenValidityInMilliseconds);
    }

    public Authentication getAuthentication(String token) {
        Claims claims = this.asClaim(token);

        Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(DELIMITER))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        User principal = new User(claims.getSubject(), EMPTY_PASSWORD, authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public Claims asClaim(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            LOGGER.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            LOGGER.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            LOGGER.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            LOGGER.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    private void createKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    private String createToken(Authentication authentication, Date expiration, Long memberId) {
        String authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(DELIMITER));

        return Jwts.builder()
            .setSubject(authentication.getName())
            .claim(AUTHORITIES_KEY, authorities)
            .claim(MEMBER_ID, memberId)
            .signWith(key, SignatureAlgorithm.HS512)
            .setExpiration(expiration)
            .compact();
    }
}
