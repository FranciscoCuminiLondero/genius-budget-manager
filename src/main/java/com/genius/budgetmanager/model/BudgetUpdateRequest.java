package com.genius.budgetmanager.model;
 
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
 
public class BudgetUpdateRequest {
 
    @NotNull(message = "El presupuesto no puede ser nulo")
    @PositiveOrZero(message = "El presupuesto debe ser cero o un valor positivo")
    private Double budget;
 
    public Double getBudget() { return budget; }
    public void setBudget(Double budget) { this.budget = budget; }
}
 