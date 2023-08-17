package com.gangbean.stockservice.repository;

import com.gangbean.stockservice.domain.AccountStock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountStockRepository extends JpaRepository<AccountStock, Long> {
}
