package com.gangbean.stockservice.repository;

import com.gangbean.stockservice.domain.Token;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByRefreshToken(String refreshToken);

    void deleteAllByMemberId(Long memberId);
}
