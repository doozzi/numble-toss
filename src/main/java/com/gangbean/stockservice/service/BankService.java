package com.gangbean.stockservice.service;

import com.gangbean.stockservice.dto.BankInfoResponse;
import com.gangbean.stockservice.exception.account.BankNotFoundException;
import com.gangbean.stockservice.repository.BankRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
public class BankService {

    private final BankRepository bankRepository;

    public BankService(BankRepository bankRepository) {
        this.bankRepository = bankRepository;
    }

    public BankInfoResponse existingBank(String name, Long number) {
        return BankInfoResponse.responseOf(bankRepository.findByNameAndNumber(name, number)
                .orElseThrow(() -> new BankNotFoundException(
                        String.format("은행 이름과 번호에 해당하는 은행이 존재하지 않습니다: %s / %d", name, number))));
    }
}
