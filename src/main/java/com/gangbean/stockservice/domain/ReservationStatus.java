package com.gangbean.stockservice.domain;

import com.gangbean.stockservice.exception.reservation.TradeReservationException;

public enum ReservationStatus {
    READY("준비됨"),
    COMPLETED("완료됨"),
    ERROR("처리 중 오류발생");

    private final String description;

    ReservationStatus(String description) {
        this.description = description;
    }

    public ReservationStatus complete() {
        if (this != READY) {
            throw new TradeReservationException("준비중 상태가 아닙니다: " + this.description);
        }
        return COMPLETED;
    }

    public ReservationStatus error() {
        return ERROR;
    }
}
