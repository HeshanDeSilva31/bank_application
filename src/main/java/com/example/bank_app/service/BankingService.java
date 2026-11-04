package com.example.bank_app.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bank_app.entity.Account;
import com.example.bank_app.entity.Transaction;
import com.example.bank_app.repository.AccountRepository;
import com.example.bank_app.repository.TransactionRepository;

@Service
public class BankingService {
    private static final String BANK_ACCOUNT = "999999";
    private static final BigDecimal TRANSFER_FEE_RATE = new BigDecimal("0.01");
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public BankingService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Account addAccount(String fullName, String email, String accountNumber, BigDecimal balance) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        if (accountNumber == null || !accountNumber.matches("\\d{6}")
                || BANK_ACCOUNT.equals(accountNumber)) {
            throw new IllegalArgumentException("Account number must be six digits and cannot be 999999.");
        }
        BigDecimal openingBalance = normalizeAmount(balance, true);
        if (accountRepository.existsByAccountNumber(accountNumber)) {
            throw new IllegalArgumentException("That account number already exists.");
        }
        if (accountRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("That email address is already in use.");
        }
        return accountRepository.save(new Account(accountNumber, fullName.trim(), email.trim(),
                openingBalance.doubleValue()));
    }

    @Transactional
    public void deposit(String accountNumber, BigDecimal amount) {
        Account account = getAccount(accountNumber);
        BigDecimal value = normalizeAmount(amount, false);
        account.setBalance(balanceOf(account).add(value).doubleValue());
        accountRepository.save(account);
        transactionRepository.save(new Transaction("DEPOSIT", accountNumber, value, ZERO,
                Transaction.Status.SUCCESS, "Cash deposit"));
    }

    @Transactional
    public void withdraw(String accountNumber, BigDecimal amount) {
        Account account = getAccount(accountNumber);
        BigDecimal value = normalizeAmount(amount, false);
        ensureSufficientBalance(account, value);
        account.setBalance(balanceOf(account).subtract(value).doubleValue());
        accountRepository.save(account);
        transactionRepository.save(new Transaction(accountNumber, "CASH", value, ZERO,
                Transaction.Status.SUCCESS, "Cash withdrawal"));
    }

    @Transactional
    public void transfer(String fromAccount, String toAccount, BigDecimal amount, boolean simulateFailure) {
        BigDecimal value = normalizeAmount(amount, false);
        if (fromAccount == null || fromAccount.equals(toAccount)) {
            throw new IllegalArgumentException("Choose two different accounts.");
        }
        Account sender = getAccount(fromAccount);
        Account receiver = getAccount(toAccount);
        if (BANK_ACCOUNT.equals(fromAccount) || BANK_ACCOUNT.equals(toAccount)) {
            throw new IllegalArgumentException("Transfers must be between accounts.");
        }

        BigDecimal fee = value.multiply(TRANSFER_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalDebit = value.add(fee);
        ensureSufficientBalance(sender, totalDebit);
        if (simulateFailure) {
            transactionRepository.save(new Transaction(fromAccount, toAccount, value, fee,
                    Transaction.Status.FAILED, "Simulated failure; balances were not changed"));
            return;
        }

        Account bank = accountRepository.findByAccountNumber(BANK_ACCOUNT)
                .orElseGet(() -> new Account(BANK_ACCOUNT, "Bank", "bank@system.local", ZERO.doubleValue()));
        sender.setBalance(balanceOf(sender).subtract(totalDebit).doubleValue());
        receiver.setBalance(balanceOf(receiver).add(value).doubleValue());
        bank.setBalance(balanceOf(bank).add(fee).doubleValue());
        accountRepository.save(sender);
        accountRepository.save(receiver);
        accountRepository.save(bank);
        transactionRepository.save(new Transaction(fromAccount, toAccount, value, fee,
                Transaction.Status.SUCCESS, "Transfer completed; fee: LKR " + fee));
    }

    private Account getAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("Select an account.");
        }
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account was not found."));
    }

    private BigDecimal normalizeAmount(BigDecimal amount, boolean allowZero) {
        if (amount == null || amount.scale() > 2 || amount.signum() < 0
                || (!allowZero && amount.signum() == 0)) {
            throw new IllegalArgumentException(allowZero
                    ? "Balance must be zero or a positive amount with up to two decimals."
                    : "Amount must be positive and have no more than two decimal places.");
        }
        return amount.setScale(2, RoundingMode.UNNECESSARY);
    }

    private BigDecimal balanceOf(Account account) {
        return BigDecimal.valueOf(account.getBalance()).setScale(2, RoundingMode.HALF_UP);
    }

    private void ensureSufficientBalance(Account account, BigDecimal amount) {
        if (balanceOf(account).compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance.");
        }
    }
}
