package com.example.bank_app.mapper;

import com.example.bank_app.dto.AccountDto;
import com.example.bank_app.entity.Account;

public class AccountMapper {
    public static Account mapAccount(AccountDto accountDto) {
        Account account = new Account(
                accountDto.getId(),
                accountDto.getAccountHolderName(),
                accountDto.getBalance()
        );
       
        return account;
    }

    public static AccountDto mapAccountDto(Account account) {
        AccountDto accountDto = new AccountDto(
                account.getId(),
                account.getAccountHolderName(),
                account.getBalance()
        );
        
        return accountDto;
    }

    public static AccountDto mapToAccountDto(Account savedAccount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mapToAccountDto'");
    }

}
