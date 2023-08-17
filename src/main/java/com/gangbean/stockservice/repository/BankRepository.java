package com.gangbean.stockservice.repository;

import com.gangbean.stockservice.domain.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankRepository extends JpaRepository<Bank, Long> {

    Optional<Bank> findByNameAndNumber(String name, Long number);
}
