package com.example.bank_app.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.bank_app.entity.Account;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByAccountNumberIsNotNull();
    List<Account> findByAccountHolderNameContainingIgnoreCaseAndAccountNumberIsNotNull(String name);
    java.util.Optional<Account> findByAccountNumber(String accountNumber);
    boolean existsByAccountNumber(String accountNumber);
    boolean existsByEmailIgnoreCase(String email);
}
