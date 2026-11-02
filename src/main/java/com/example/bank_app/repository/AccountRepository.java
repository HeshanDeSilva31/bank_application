package com.example.bank_app.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.bank_app.entity.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

}
