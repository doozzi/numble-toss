package com.gangbean.stockservice.repository;

import com.gangbean.stockservice.domain.TradeReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeReservationRepository extends JpaRepository<TradeReservation, Long> {

    void deleteAllByAccountId(Long accountId);
}
