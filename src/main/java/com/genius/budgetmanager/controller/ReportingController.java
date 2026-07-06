package com.genius.budgetmanager.controller;

import com.genius.budgetmanager.model.Campaign;
import com.genius.budgetmanager.model.GlobalBudgetSummary;
import com.genius.budgetmanager.service.CampaignService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/reporting")
public class ReportingController {

    @Autowired
    private CampaignService campaignService;

    @GetMapping("/download")
    public ResponseEntity<ByteArrayResource> downloadReport(
            @RequestParam(required = false) String client) throws IOException {

        GlobalBudgetSummary summary = campaignService.getGlobalBudgetSummary();

        List<Campaign> campaigns = campaignService.getAllCampaigns();
        if (client != null && !client.isBlank()) {
            campaigns = campaigns.stream()
                    .filter(c -> client.equalsIgnoreCase(c.getClient()))
                    .toList();
        }

        String tituloReporte = (client != null && !client.isBlank())
                ? "Reporte: " + client
                : "Reporte: Todos los clientes";

        String fecha = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        byte[] excelBytes = buildExcel(summary, campaigns, tituloReporte, fecha);

        String clientSlug = (client != null && !client.isBlank())
                ? "-" + client.toLowerCase().replaceAll("\\s+", "")
                : "";
        String filename = "reporte" + clientSlug + "-" + fecha + ".xlsx";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(new ByteArrayResource(excelBytes));
    }

    private byte[] buildExcel(GlobalBudgetSummary summary, List<Campaign> campaigns,
                               String titulo, String fecha) throws IOException {

        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // ---- Estilos reutilizables ----
            Font tituloFont = wb.createFont();
            tituloFont.setBold(true);
            tituloFont.setFontHeightInPoints((short) 14);
            CellStyle tituloStyle = wb.createCellStyle();
            tituloStyle.setFont(tituloFont);

            Font subFont = wb.createFont();
            subFont.setItalic(true);
            subFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            CellStyle subStyle = wb.createCellStyle();
            subStyle.setFont(subFont);

            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            addBorders(headerStyle);

            CellStyle labelBoldStyle = wb.createCellStyle();
            Font boldFont = wb.createFont();
            boldFont.setBold(true);
            labelBoldStyle.setFont(boldFont);
            addBorders(labelBoldStyle);

            DataFormat format = wb.createDataFormat();

            CellStyle currencyStyle = wb.createCellStyle();
            currencyStyle.setDataFormat(format.getFormat("$#,##0.00"));
            addBorders(currencyStyle);

            CellStyle percentStyle = wb.createCellStyle();
            percentStyle.setDataFormat(format.getFormat("0.00\"%\""));
            addBorders(percentStyle);

            CellStyle plainBorderStyle = wb.createCellStyle();
            addBorders(plainBorderStyle);

            CellStyle idStyle = wb.createCellStyle();
            idStyle.setDataFormat(format.getFormat("0"));
            addBorders(idStyle);

            // ==================== Hoja Resumen ====================
            Sheet resumen = wb.createSheet("Resumen");
            int rowIdx = 0;

            Row tituloRow = resumen.createRow(rowIdx++);
            Cell tituloCell = tituloRow.createCell(0);
            tituloCell.setCellValue(titulo);
            tituloCell.setCellStyle(tituloStyle);

            Row fechaRow = resumen.createRow(rowIdx++);
            Cell fechaCell = fechaRow.createCell(0);
            fechaCell.setCellValue("Generado: " + fecha);
            fechaCell.setCellStyle(subStyle);

            rowIdx++; // fila en blanco

            Row kpiHeaderRow = resumen.createRow(rowIdx++);
            String[] kpiCols = {"Métrica", "Valor"};
            for (int i = 0; i < kpiCols.length; i++) {
                Cell c = kpiHeaderRow.createCell(i);
                c.setCellValue(kpiCols[i]);
                c.setCellStyle(headerStyle);
            }

            Object[][] resumenData = {
                {"Campañas activas", summary.getActiveCampaigns(), null},
                {"Presupuesto total", summary.getTotalBudget(), currencyStyle},
                {"Total gastado", summary.getTotalSpent(), currencyStyle},
                {"Total disponible", summary.getTotalAvailable(), currencyStyle},
                {"% de consumo", summary.getConsumptionPercentage(), percentStyle}
            };
            for (Object[] fila : resumenData) {
                Row row = resumen.createRow(rowIdx++);

                Cell labelCell = row.createCell(0);
                labelCell.setCellValue((String) fila[0]);
                labelCell.setCellStyle(labelBoldStyle);

                Cell valueCell = row.createCell(1);
                if (fila[1] instanceof Integer i) {
                    valueCell.setCellValue(i);
                } else {
                    valueCell.setCellValue(((Number) fila[1]).doubleValue());
                }
                valueCell.setCellStyle(fila[2] != null ? (CellStyle) fila[2] : plainBorderStyle);
            }

            resumen.setColumnWidth(0, 26 * 256);
            resumen.setColumnWidth(1, 18 * 256);

            // ==================== Hoja Campañas ====================
            Sheet campaignsSheet = wb.createSheet("Campañas");
            Row campaignHeader = campaignsSheet.createRow(0);
            String[] cols = {"ID", "Nombre", "Cliente", "Estado", "Presupuesto", "Gastado", "Disponible"};
            for (int i = 0; i < cols.length; i++) {
                Cell c = campaignHeader.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(headerStyle);
            }

            int cRow = 1;
            for (Campaign campaign : campaigns) {
                Row row = campaignsSheet.createRow(cRow++);

                Cell idCell = row.createCell(0);
                idCell.setCellValue(campaign.getId());
                idCell.setCellStyle(idStyle);

                Cell nameCell = row.createCell(1);
                nameCell.setCellValue(campaign.getName());
                nameCell.setCellStyle(plainBorderStyle);

                Cell clientCell = row.createCell(2);
                clientCell.setCellValue(campaign.getClient());
                clientCell.setCellStyle(plainBorderStyle);

                Cell statusCell = row.createCell(3);
                statusCell.setCellValue(campaign.getStatus());
                statusCell.setCellStyle(plainBorderStyle);

                Cell budgetCell = row.createCell(4);
                budgetCell.setCellValue(campaign.getBudget());
                budgetCell.setCellStyle(currencyStyle);

                Cell spentCell = row.createCell(5);
                spentCell.setCellValue(campaign.getSpent());
                spentCell.setCellStyle(currencyStyle);

                Cell availableCell = row.createCell(6);
                availableCell.setCellValue(campaign.getBudget() - campaign.getSpent());
                availableCell.setCellStyle(currencyStyle);
            }

            campaignsSheet.setColumnWidth(0, 6 * 256);
            campaignsSheet.setColumnWidth(1, 32 * 256);
            campaignsSheet.setColumnWidth(2, 16 * 256);
            campaignsSheet.setColumnWidth(3, 12 * 256);
            campaignsSheet.setColumnWidth(4, 16 * 256);
            campaignsSheet.setColumnWidth(5, 16 * 256);
            campaignsSheet.setColumnWidth(6, 16 * 256);

            campaignsSheet.createFreezePane(0, 1); // encabezado siempre visible al scrollear

            wb.write(out);
            return out.toByteArray();
        }
    }

    private void addBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}