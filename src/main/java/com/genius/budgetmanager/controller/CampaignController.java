package com.genius.budgetmanager.controller;

import com.genius.budgetmanager.model.BudgetSummary;
import com.genius.budgetmanager.model.BudgetUpdateRequest;
import com.genius.budgetmanager.model.Campaign;
import com.genius.budgetmanager.model.Expense;
import com.genius.budgetmanager.model.GlobalBudgetSummary;
import com.genius.budgetmanager.service.CampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
@Tag(name = "Campaigns", description = "Gestion de campanas y presupuestos")
public class CampaignController {

    @Autowired
    private CampaignService campaignService;

    @GetMapping
    @Operation(summary = "Listar campanas", description = "Retorna todas las campanas. Acepta filtro opcional por status.")
    public ResponseEntity<List<Campaign>> getCampaigns(@RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            return ResponseEntity.ok(campaignService.getCampaignsByStatus(status));
        }
        return ResponseEntity.ok(campaignService.getAllCampaigns());
    }

    @GetMapping("/summary")
    @Operation(summary = "Resumen global de presupuesto", description = "Agrega KPIs de todas las campanas activas: total asignado, gastado, disponible y porcentaje de consumo.")
    public ResponseEntity<GlobalBudgetSummary> getGlobalBudgetSummary() {
        return ResponseEntity.ok(campaignService.getGlobalBudgetSummary());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener campana por ID")
    public ResponseEntity<Campaign> getCampaignById(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.getCampaignById(id));
    }

    @GetMapping("/{id}/summary")
    @Operation(summary = "Resumen de presupuesto", description = "Retorna el resumen de uso de presupuesto de la campana.")
    public ResponseEntity<BudgetSummary> getBudgetSummary(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.getBudgetSummary(id));
    }

    @GetMapping("/{id}/expenses")
    @Operation(summary = "Listar gastos de una campana")
    public ResponseEntity<List<Expense>> getExpenses(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.getExpensesByCampaign(id));
    }

    @PostMapping("/{id}/expenses")
    @Operation(summary = "Registrar un gasto en la campana")
    // FIX BUG-002: se documenta el 201 que ya devolvía el endpoint pero
    // aparecía como "Undocumented" en Swagger
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Gasto registrado correctamente"),
        @ApiResponse(responseCode = "404", description = "Campaña no encontrada")
    })
    public ResponseEntity<Expense> addExpense(@PathVariable Long id, @RequestBody Expense expense) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campaignService.addExpense(id, expense));
    }

    @PutMapping("/{id}/budget")
    @Operation(summary = "Actualizar presupuesto de la campana")
    public ResponseEntity<Campaign> updateBudget(@PathVariable Long id, @RequestBody BudgetUpdateRequest request) {
        return ResponseEntity.ok(campaignService.updateBudget(id, request.getBudget()));
    }
}