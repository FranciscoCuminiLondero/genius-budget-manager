package com.genius.budgetmanager.service;

import com.genius.budgetmanager.exception.CampaignNotFoundException;
import com.genius.budgetmanager.model.BudgetSummary;
import com.genius.budgetmanager.model.Campaign;
import com.genius.budgetmanager.model.Expense;
import com.genius.budgetmanager.model.GlobalBudgetSummary;
import com.genius.budgetmanager.repository.CampaignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CampaignService {

    @Autowired
    private CampaignRepository repository;

    public List<Campaign> getAllCampaigns() {
        return repository.findAll();
    }

    public List<Campaign> getCampaignsByStatus(String status) {
        return repository.findAll().stream()
                .filter(c -> c.getType().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }

    public Campaign getCampaignById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new CampaignNotFoundException(id));
    }

    public BudgetSummary getBudgetSummary(Long campaignId) {
        Campaign campaign = getCampaignById(campaignId);

        BudgetSummary summary = new BudgetSummary();
        summary.setCampaignId(campaign.getId());
        summary.setCampaignName(campaign.getName());
        summary.setClient(campaign.getClient());
        summary.setTotalBudget(campaign.getBudget());
        summary.setSpent(campaign.getSpent());
        // FIX BUG-003: era getBudget() - getBudget(), siempre daba 0
        summary.setRemaining(campaign.getBudget() - campaign.getSpent());
        summary.setPercentageUsed(
                Math.round((campaign.getSpent() / campaign.getBudget()) * 10000.0) / 100.0
        );

        return summary;
    }

    public List<Expense> getExpensesByCampaign(Long campaignId) {
        getCampaignById(campaignId);
        return repository.findExpensesByCampaignId(campaignId);
    }

    public Expense addExpense(Long campaignId, Expense expense) {
        Campaign campaign = getCampaignById(campaignId);

        // FIX bug ID cruzado: el campaignId siempre viene de la URL,
        // ignoramos cualquier campaignId que venga en el body del request
        expense.setCampaignId(campaignId);

        // Calcular nuevo spent antes de persistir
        double nuevoSpent = campaign.getSpent() + expense.getAmount();
        campaign.setSpent(nuevoSpent);

        // FIX bug matemático: pasamos la campaña actualizada al repository
        // para que actualice el spent de forma atómica con el gasto
        return repository.saveExpense(expense, campaign);
    }

    public GlobalBudgetSummary getGlobalBudgetSummary() {
        List<Campaign> active = repository.findAll().stream()
                .filter(c -> "active".equalsIgnoreCase(c.getStatus()))
                .collect(Collectors.toList());

        double totalBudget    = active.stream().mapToDouble(Campaign::getBudget).sum();
        double totalSpent     = active.stream().mapToDouble(Campaign::getSpent).sum();
        double totalAvailable = totalBudget - totalSpent;
        double pct = totalBudget > 0
                ? Math.round((totalSpent / totalBudget) * 10000.0) / 100.0
                : 0.0;

        GlobalBudgetSummary summary = new GlobalBudgetSummary();
        summary.setActiveCampaigns(active.size());
        summary.setTotalBudget(totalBudget);
        summary.setTotalSpent(totalSpent);
        summary.setTotalAvailable(totalAvailable);
        summary.setConsumptionPercentage(pct);
        return summary;
    }

    public Campaign updateBudget(Long campaignId, Double newBudget) {
        Campaign campaign = getCampaignById(campaignId);

        // FIX BUG-001: eliminado setSpent(0.0) que reseteaba el gasto acumulado
        // Validación de negocio: no se puede asignar menos de lo ya gastado
        if (newBudget < campaign.getSpent()) {
            throw new IllegalArgumentException(
                "El nuevo presupuesto (" + newBudget +
                ") no puede ser menor al gasto acumulado de la campaña (" + campaign.getSpent() + ")"
            );
        }

        campaign.setBudget(newBudget);
        return campaign;
    }
}