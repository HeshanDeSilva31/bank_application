package com.example.bank_app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.example.bank_app.entity.Account;
import com.example.bank_app.entity.Transaction;
import com.example.bank_app.repository.AccountRepository;
import com.example.bank_app.repository.TransactionRepository;

class BankingServiceTest {
    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final TransactionRepository transactionRepository = mock(TransactionRepository.class);
    private final BankingService bankingService = new BankingService(accountRepository, transactionRepository);

    @Test
    void transferDeductsOnlyTheAmountSent() {
        Account sender = new Account("123456", "Sender", "sender@example.com", 100_000.00);
        Account receiver = new Account("654321", "Receiver", "receiver@example.com", 20_000.00);
        when(accountRepository.findByAccountNumber("123456")).thenReturn(Optional.of(sender));
        when(accountRepository.findByAccountNumber("654321")).thenReturn(Optional.of(receiver));

        bankingService.transfer("123456", "654321", new BigDecimal("60000.00"), false);

        assertEquals(40_000.00, sender.getBalance(), 0.001);
        assertEquals(80_000.00, receiver.getBalance(), 0.001);
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        assertEquals(new BigDecimal("0.00"), transactionCaptor.getValue().getFee());
        assertEquals(Transaction.Status.SUCCESS, transactionCaptor.getValue().getStatus());
        verify(accountRepository, org.mockito.Mockito.times(2)).save(any(Account.class));
    }
}
