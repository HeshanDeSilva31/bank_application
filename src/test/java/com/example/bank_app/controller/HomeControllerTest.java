package com.example.bank_app.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.bank_app.repository.AccountRepository;
import com.example.bank_app.repository.TransactionRepository;
import com.example.bank_app.service.BankingService;

class HomeControllerTest {
    private BankingService bankingService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        bankingService = mock(BankingService.class);
        HomeController controller = new HomeController(
                mock(AccountRepository.class), mock(TransactionRepository.class), bankingService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void transferFormSubmitsAndShowsSuccess() throws Exception {
        mockMvc.perform(post("/transfer")
                        .param("fromAccount", "123456")
                        .param("toAccount", "654321")
                        .param("amount", "60000.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("success", "Transfer completed successfully."));

        verify(bankingService).transfer(
                "123456", "654321", new BigDecimal("60000.00"), false);
    }

    @Test
    void simulatedTransferFailureIsNotReportedAsSuccess() throws Exception {
        mockMvc.perform(post("/transfer")
                        .param("fromAccount", "123456")
                        .param("toAccount", "654321")
                        .param("amount", "60000.00")
                        .param("simulateFailure", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute(
                        "error", "Simulated transfer failure; balances were not changed."));

        verify(bankingService).transfer(
                "123456", "654321", new BigDecimal("60000.00"), true);
    }
}
