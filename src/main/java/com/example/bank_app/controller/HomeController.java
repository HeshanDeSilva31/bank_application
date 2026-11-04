package com.example.bank_app.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.bank_app.entity.Account;
import com.example.bank_app.entity.Transaction;
import com.example.bank_app.repository.AccountRepository;
import com.example.bank_app.repository.TransactionRepository;
import com.example.bank_app.service.BankingService;

@Controller
public class HomeController {
    private static final String BANK_ACCOUNT = "999999";

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final BankingService bankingService;

    public HomeController(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            BankingService bankingService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.bankingService = bankingService;
    }

    @GetMapping("/")
    public String home(@RequestParam(required = false) String keyword, Model model) {
        List<Account> allAccounts = accountRepository.findByAccountNumberIsNotNull();
        List<Account> accounts = keyword == null || keyword.isBlank()
                ? allAccounts.stream()
                        .filter(account -> !BANK_ACCOUNT.equals(account.getAccountNumber()))
                        .toList()
                : accountRepository.findByAccountHolderNameContainingIgnoreCaseAndAccountNumberIsNotNull(keyword)
                        .stream()
                        .filter(account -> !BANK_ACCOUNT.equals(account.getAccountNumber()))
                        .toList();

        model.addAttribute("allAccounts", allAccounts.stream()
                .filter(account -> !BANK_ACCOUNT.equals(account.getAccountNumber()))
                .toList());
        model.addAttribute("accounts", accounts);
        model.addAttribute("transactions", transactionRepository.findTop10ByOrderByCreatedAtDesc());
        model.addAttribute("totalAccounts", allAccounts.stream()
                .filter(account -> !BANK_ACCOUNT.equals(account.getAccountNumber()))
                .count());
        model.addAttribute("totalBalance", allAccounts.stream()
                .filter(account -> !BANK_ACCOUNT.equals(account.getAccountNumber()))
                .map(account -> BigDecimal.valueOf(account.getBalance()))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        model.addAttribute("successCount", transactionRepository.countByStatus(Transaction.Status.SUCCESS));
        model.addAttribute("failedCount", transactionRepository.countByStatus(Transaction.Status.FAILED));
        model.addAttribute("keyword", keyword);
        model.addAttribute("transferRequest", new TransferForm());
        model.addAttribute("accountForm", new AccountForm());
        return "bank";
    }

    @PostMapping("/accounts")
    public String addAccount(@ModelAttribute AccountForm form, RedirectAttributes redirectAttributes) {
        try {
            bankingService.addAccount(form.getFullName(), form.getEmail(),
                    form.getAccountNumber(), form.getBalance());
            redirectAttributes.addFlashAttribute("success", "Account saved successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/deposit")
    public String depositPage(Model model) {
        model.addAttribute("accounts", accountRepository.findByAccountNumberIsNotNull().stream()
                .filter(account -> !BANK_ACCOUNT.equals(account.getAccountNumber()))
                .toList());
        return "deposit";
    }

    @PostMapping("/deposit")
    public String deposit(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount,
            RedirectAttributes redirectAttributes) {
        try {
            bankingService.deposit(accountNumber, amount);
            redirectAttributes.addFlashAttribute("success", "Deposit completed successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/deposit";
    }

    @GetMapping("/withdraw")
    public String withdrawPage(Model model) {
        model.addAttribute("accounts", accountRepository.findByAccountNumberIsNotNull().stream()
                .filter(account -> !BANK_ACCOUNT.equals(account.getAccountNumber()))
                .toList());
        return "withdraw";
    }

    @PostMapping("/withdraw")
    public String withdraw(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount,
            RedirectAttributes redirectAttributes) {
        try {
            bankingService.withdraw(accountNumber, amount);
            redirectAttributes.addFlashAttribute("success", "Withdrawal completed successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/withdraw";
    }

    @PostMapping("/transfer")
    public String transfer(@ModelAttribute TransferForm form, RedirectAttributes redirectAttributes) {
        try {
            bankingService.transfer(form.getFromAccount(), form.getToAccount(),
                    form.getAmount(), form.isSimulateFailure());
            redirectAttributes.addFlashAttribute("success", "Transfer completed successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/large-transfers")
    public String largeTransfers(
            @RequestParam(defaultValue = "10000") BigDecimal min,
            Model model) {
        model.addAttribute("min", min);
        model.addAttribute("transfers", transactionRepository
                .findByStatusAndAmountGreaterThanOrderByCreatedAtDesc(Transaction.Status.SUCCESS, min));
        return "large-transfers";
    }

    @GetMapping("/accounts/{accountNumber}/history")
    public String history(@PathVariable String accountNumber, Model model, RedirectAttributes redirectAttributes) {
        Account account = accountRepository.findByAccountNumber(accountNumber).orElse(null);
        if (account == null || BANK_ACCOUNT.equals(accountNumber)) {
            redirectAttributes.addFlashAttribute("error", "Account was not found.");
            return "redirect:/";
        }

        List<Transaction> transactions = transactionRepository
                .findByFromAccountOrToAccountOrderByCreatedAtDesc(accountNumber, accountNumber);
        BigDecimal totalSent = transactions.stream()
                .filter(transaction -> transaction.getStatus() == Transaction.Status.SUCCESS
                        && accountNumber.equals(transaction.getFromAccount()))
                .map(transaction -> transaction.getAmount().add(transaction.getFee()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("account", account);
        model.addAttribute("transactions", transactions);
        model.addAttribute("totalSent", totalSent);
        return "history";
    }

    public static class AccountForm {
        private String fullName;
        private String email;
        private String accountNumber;
        private BigDecimal balance = BigDecimal.ZERO;

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        public BigDecimal getBalance() { return balance; }
        public void setBalance(BigDecimal balance) { this.balance = balance; }
    }

    public static class TransferForm {
        private String fromAccount;
        private String toAccount;
        private BigDecimal amount = BigDecimal.ZERO;
        private boolean simulateFailure;

        public String getFromAccount() { return fromAccount; }
        public void setFromAccount(String fromAccount) { this.fromAccount = fromAccount; }
        public String getToAccount() { return toAccount; }
        public void setToAccount(String toAccount) { this.toAccount = toAccount; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public boolean isSimulateFailure() { return simulateFailure; }
        public void setSimulateFailure(boolean simulateFailure) { this.simulateFailure = simulateFailure; }
    }
}
