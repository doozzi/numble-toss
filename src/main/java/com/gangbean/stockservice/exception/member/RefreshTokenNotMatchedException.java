package com.gangbean.stockservice.exception.member;

public class RefreshTokenNotMatchedException extends MemberException{

    public RefreshTokenNotMatchedException(String message) {
        super(message);
    }
}
