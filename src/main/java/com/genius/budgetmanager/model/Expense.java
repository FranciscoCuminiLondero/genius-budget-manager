package com.genius.budgetmanager.model;

public class Expense {

    private Long id;
    private Long campaignId;
    private String description;
    private Double amount;
    private String category;
    private String date;

    public Expense() {}

    public Expense(Long id, Long campaignId, String description, Double amount, String category, String date) {
        this.id = id;
        this.campaignId = campaignId;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCampaignId() { return campaignId; }
    public void setCampaignId(Long campaignId) { this.campaignId = campaignId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}
