"""
Budget Manager — Módulo de reportería
Extrae datos de la API REST y genera un archivo Excel con métricas de campañas.

Uso:
    python extract.py

Requisitos:
    pip install -r requirements.txt

El servidor de Budget Manager debe estar corriendo en http://localhost:8080
"""

import json
import urllib.request
import urllib.error
from datetime import datetime

BUDGET_MANAGER_URL = 'http://localhost:8080'
LANDING_CRM_URL    = 'http://localhost:3000'
OUTPUT_FILE        = 'report.xlsx'


def api_get(url: str) -> list | dict:
    with urllib.request.urlopen(url, timeout=5) as response:
        return json.loads(response.read())


def get_campaigns() -> list:
    """Obtiene todas las campañas del Budget Manager."""
    return api_get(f'{BUDGET_MANAGER_URL}/api/campaigns')


def get_budget_summary() -> dict:
    """Obtiene el resumen global de presupuesto (solo campañas activas)."""
    return api_get(f'{BUDGET_MANAGER_URL}/api/campaigns/summary')


def get_leads_summary() -> list:
    """Obtiene el resumen de leads por landing desde el Landing CRM."""
    return api_get(f'{LANDING_CRM_URL}/api/landings/summary')


def export_to_excel(campaigns: list, summary: dict) -> None:
    """Genera el archivo Excel con métricas de campañas."""
    import openpyxl
    wb = openpyxl.Workbook()

    # Hoja de campañas
    ws = wb.active
    ws.title = 'Campañas'
    ws.append(['ID', 'Nombre', 'Cliente', 'Estado', 'Presupuesto', 'Gastado', 'Disponible'])
    for c in campaigns:
        ws.append([
            c.get('id'),
            c.get('name'),
            c.get('client'),
            c.get('status'),
            c.get('budget', 0),
            c.get('spent', 0),
            c.get('budget', 0) - c.get('spent', 0),
        ])

    # Hoja de resumen
    ws2 = wb.create_sheet('Resumen')
    ws2.append(['Métrica', 'Valor'])
    ws2.append(['Campañas activas',    summary.get('activeCampaigns', 0)])
    ws2.append(['Presupuesto total',   summary.get('totalBudget', 0)])
    ws2.append(['Total gastado',       summary.get('totalSpent', 0)])
    ws2.append(['Total disponible',    summary.get('totalAvailable', 0)])
    ws2.append(['% de consumo',        summary.get('consumptionPercentage', 0)])

    wb.save(OUTPUT_FILE)
    print(f'Reporte guardado en {OUTPUT_FILE}')


if __name__ == '__main__':
    print(f'Extrayendo datos — {datetime.now().strftime("%Y-%m-%d %H:%M")}')
    campaigns = get_campaigns()
    summary   = get_budget_summary()
    print(f'Campañas encontradas: {len(campaigns)}')
    export_to_excel(campaigns, summary)
