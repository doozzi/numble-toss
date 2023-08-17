package com.gangbean.stockservice.service;

import com.gangbean.stockservice.repository.AccountRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

@Service
public class AccountNumberGenerator {

    private final AccountRepository accountRepository;

    public AccountNumberGenerator(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public String newAccountNumber() {
        String nextAccountNumber = new StringBuilder(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")))
            .reverse()
            .substring(0, 14);

        int count = 0;
        while(count++ < 5 && accountRepository.findByNumber(nextAccountNumber).isPresent()) {
            nextAccountNumber = new StringBuilder(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")))
                .reverse()
                .substring(0, 14);
        }
        return nextAccountNumber;
    }
}
