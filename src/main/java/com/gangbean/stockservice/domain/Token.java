package com.gangbean.stockservice.domain;

import com.gangbean.stockservice.exception.member.RefreshTokenExpiredException;
import com.gangbean.stockservice.exception.member.RefreshTokenNotMatchedException;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    private String refreshToken;

    private Date expiration;

    public void reissue(Date newExpiration) {
        this.expiration = newExpiration;
    }

    public boolean isExpired(Date now) {
        if (expiration.before(now)) {
            throw new RefreshTokenExpiredException("이미 만료된 Refresh Token 입니다.");
        }
        return false;
    }

    public void isOwnedBy(Long loginMemberId) {
        if (!Objects.equals(member.getId(), loginMemberId)) {
            throw new RefreshTokenNotMatchedException("로그인한 유저의 Refresh Token이 아닙니다.");
        }
    }
}
