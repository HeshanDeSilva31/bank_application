package com.example.bank_app.service.impl;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.bank_app.dto.AccountDto;
import com.example.bank_app.entity.Account;
import com.example.bank_app.mapper.AccountMapper;
import com.example.bank_app.repository.AccountRepository;
import com.example.bank_app.service.AccountService;
@Service 


public class AccountServiceImpl implements AccountService {

    private AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public AccountDto createAccount(AccountDto accountDto) {
       Account account = AccountMapper.mapAccount(accountDto);
       Account savedAccount = accountRepository.save(account);
       return AccountMapper.mapToAccountDto(savedAccount);
      
    }

    @Override
    public AccountDto getAccountById(Long id) {
      
        Account account = accountRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Account does not exists"));
            return AccountMapper.mapAccountDto(account);
        
    }

    @Override
    public AccountDto deposit(Long id, double amount) {
         Account account = accountRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Account does not exists"));

   double total = account.getBalance() + amount;
   account.setBalance(total);
   Account savedAccount = accountRepository.save(account);
   return AccountMapper.mapAccountDto(savedAccount);
   

}


}

