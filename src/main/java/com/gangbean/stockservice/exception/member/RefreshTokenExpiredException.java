package com.gangbean.stockservice.exception.member;

public class RefreshTokenExpiredException extends MemberException {

    public RefreshTokenExpiredException(String message) {
        super(message);
    }
}
