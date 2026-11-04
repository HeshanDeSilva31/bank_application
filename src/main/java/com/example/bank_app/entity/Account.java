package com.example.bank_app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor
@Entity 
@Table (name = "accounts")
public class Account {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_holder_name")
    private String accountHolderName;

    @Column(name = "account_number", unique = true, length = 6)
    private String accountNumber;

    @Column(unique = true)
    private String email;

    private double balance;

    public Account(Long id, String accountHolderName, double balance) {
        this.id = id;
        this.accountHolderName = accountHolderName;
        this.balance = balance;
    }

    public Account(String accountNumber, String accountHolderName, String email, double balance) {
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.email = email;
        this.balance = balance;
    }

    public String getFullName() {
        return accountHolderName;
    }

    public void setFullName(String fullName) {
        this.accountHolderName = fullName;
    }
}
