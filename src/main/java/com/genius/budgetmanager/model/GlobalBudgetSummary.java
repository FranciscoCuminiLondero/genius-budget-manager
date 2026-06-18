package com.genius.budgetmanager.model;

public class GlobalBudgetSummary {

    private int activeCampaigns;
    private Double totalBudget;
    private Double totalSpent;
    private Double totalAvailable;
    private Double consumptionPercentage;

    public GlobalBudgetSummary() {}

    public int getActiveCampaigns() { return activeCampaigns; }
    public void setActiveCampaigns(int activeCampaigns) { this.activeCampaigns = activeCampaigns; }

    public Double getTotalBudget() { return totalBudget; }
    public void setTotalBudget(Double totalBudget) { this.totalBudget = totalBudget; }

    public Double getTotalSpent() { return totalSpent; }
    public void setTotalSpent(Double totalSpent) { this.totalSpent = totalSpent; }

    public Double getTotalAvailable() { return totalAvailable; }
    public void setTotalAvailable(Double totalAvailable) { this.totalAvailable = totalAvailable; }

    public Double getConsumptionPercentage() { return consumptionPercentage; }
    public void setConsumptionPercentage(Double consumptionPercentage) { this.consumptionPercentage = consumptionPercentage; }
}
