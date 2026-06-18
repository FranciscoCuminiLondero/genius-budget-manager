package com.genius.budgetmanager.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CampaignControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getCampaigns_returnsAllCampaigns() throws Exception {
        mockMvc.perform(get("/api/campaigns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6));
    }

    @Test
    void getCampaignById_existingId_returnsCampaign() throws Exception {
        mockMvc.perform(get("/api/campaigns/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Social Ads Q1 2026"))
                .andExpect(jsonPath("$.client").value("SuenoSimple"))
                .andExpect(jsonPath("$.status").value("active"));
    }

    @Test
    void getCampaignById_nonExistingId_returns404() throws Exception {
        mockMvc.perform(get("/api/campaigns/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getBudgetSummary_existingCampaign_returnsCorrectFields() throws Exception {
        mockMvc.perform(get("/api/campaigns/3/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaignId").value(3))
                .andExpect(jsonPath("$.client").value("SuenoSimple"))
                .andExpect(jsonPath("$.totalBudget").value(120000.0))
                .andExpect(jsonPath("$.spent").value(67800.0))
                .andExpect(jsonPath("$.percentageUsed").exists());
    }

    @Test
    void getBudgetSummary_nonExistingCampaign_returns404() throws Exception {
        mockMvc.perform(get("/api/campaigns/999/summary"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getExpenses_existingCampaign_returnsExpenseList() throws Exception {
        mockMvc.perform(get("/api/campaigns/3/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void getExpenses_campaignWithNoExpenses_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/campaigns/6/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getExpenses_nonExistingCampaign_returns404() throws Exception {
        mockMvc.perform(get("/api/campaigns/999/expenses"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addExpense_validExpense_returnsCreatedWithCorrectData() throws Exception {
        String body = """
                {
                  "description": "TikTok Ads Abril",
                  "amount": 18000.0,
                  "category": "ads_spend",
                  "date": "2026-04-10"
                }
                """;

        mockMvc.perform(post("/api/campaigns/2/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.campaignId").value(2))
                .andExpect(jsonPath("$.description").value("TikTok Ads Abril"))
                .andExpect(jsonPath("$.amount").value(18000.0))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void addExpense_nonExistingCampaign_returns404() throws Exception {
        String body = """
                {
                  "description": "Gasto sin campana",
                  "amount": 5000.0,
                  "category": "tools",
                  "date": "2026-04-10"
                }
                """;

        mockMvc.perform(post("/api/campaigns/999/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBudget_validAmount_returnsUpdatedCampaign() throws Exception {
        mockMvc.perform(put("/api/campaigns/6/budget")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"budget\": 75000.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(6))
                .andExpect(jsonPath("$.budget").value(75000.0));
    }

    @Test
    void updateBudget_nonExistingCampaign_returns404() throws Exception {
        mockMvc.perform(put("/api/campaigns/999/budget")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"budget\": 50000.0}"))
                .andExpect(status().isNotFound());
    }
}
