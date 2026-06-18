package com.genius.budgetmanager.model;

public class Campaign {

    private Long id;
    private String name;
    private String client;
    private String type;
    private String status;
    private Double budget;
    private Double spent;
    private String currency;
    private String startDate;
    private String endDate;

    public Campaign() {}

    public Campaign(Long id, String name, String client, String type, String status,
                    Double budget, Double spent, String currency, String startDate, String endDate) {
        this.id = id;
        this.name = name;
        this.client = client;
        this.type = type;
        this.status = status;
        this.budget = budget;
        this.spent = spent;
        this.currency = currency;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getClient() { return client; }
    public void setClient(String client) { this.client = client; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getBudget() { return budget; }
    public void setBudget(Double budget) { this.budget = budget; }

    public Double getSpent() { return spent; }
    public void setSpent(Double spent) { this.spent = spent; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}
