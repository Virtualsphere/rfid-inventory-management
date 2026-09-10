package com.virtualsphere.rfidbackend.service;

import com.virtualsphere.rfidbackend.model.ScanCategory;
import com.virtualsphere.rfidbackend.model.ScanReport;
import com.virtualsphere.rfidbackend.model.ScanReportItem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Builds the .xlsx files behind the mobile app's "Export" button - one
 * workbook per bulk scan-verify session, or one combined workbook over
 * however many individual scan-log reports the frontend collected during a
 * one-by-one scanning session.
 */
@Service
public class ExcelReportService {

    private static final String[] ITEM_HEADERS = {"EPC", "Category", "Product Name", "Item Location", "Previous Status"};
    private static final String[] SCAN_HEADERS = {"Time", "User", "Location", "EPC", "Category", "Product Name"};

    public byte[] bulkReportWorkbook(ScanReport report, List<ScanReportItem> items) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = headerStyle(workbook);
            CellStyle dateStyle = dateStyle(workbook);

            Sheet summary = workbook.createSheet("Summary");
            int r = 0;
            r = labelValue(summary, r, headerStyle, "Report ID", String.valueOf(report.getId()));
            r = labelValue(summary, r, headerStyle, "Type", report.getType().name());
            r = labelValue(summary, r, headerStyle, "Location", report.getLocation());
            r = labelValue(summary, r, headerStyle, "Performed By", report.getPerformedBy());
            r = labelDate(summary, r, headerStyle, dateStyle, "Scanned At", report.getScannedAt());
            r = labelValue(summary, r, headerStyle, "Duration (seconds)",
                    report.getDurationSeconds() != null ? String.valueOf(report.getDurationSeconds()) : "");
            r = labelValue(summary, r, headerStyle, "Expected",
                    report.getExpectedCount() != null ? String.valueOf(report.getExpectedCount()) : "");
            r = labelValue(summary, r, headerStyle, "Found", String.valueOf(report.getFoundCount()));
            r = labelValue(summary, r, headerStyle, "Missing", String.valueOf(report.getMissingCount()));
            r = labelValue(summary, r, headerStyle, "Unexpected", String.valueOf(report.getUnexpectedCount()));
            r = labelValue(summary, r, headerStyle, "Unknown", String.valueOf(report.getUnknownCount()));
            r = labelValue(summary, r, headerStyle, "Unavailable", String.valueOf(report.getUnavailableCount()));
            r = labelValue(summary, r, headerStyle, "Total Detected", String.valueOf(report.getTotalDetected()));
            labelDate(summary, r, headerStyle, dateStyle, "Generated At", LocalDateTime.now());
            summary.setColumnWidth(0, 22 * 256);
            summary.setColumnWidth(1, 32 * 256);

            Sheet itemsSheet = workbook.createSheet("Items");
            writeHeaderRow(itemsSheet, headerStyle, ITEM_HEADERS);
            int row = 1;
            for (ScanReportItem item : items) {
                Row xlRow = itemsSheet.createRow(row++);
                xlRow.createCell(0).setCellValue(item.getEpc());
                xlRow.createCell(1).setCellValue(item.getCategory().name());
                xlRow.createCell(2).setCellValue(blank(item.getProductName()));
                xlRow.createCell(3).setCellValue(blank(item.getItemLocation()));
                xlRow.createCell(4).setCellValue(item.getPreviousStatus() != null ? item.getPreviousStatus().name() : "");
            }
            setWidths(itemsSheet, 26, 16, 28, 22, 16);

            return toBytes(workbook);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to build Excel report", e);
        }
    }

    public byte[] singleReportsWorkbook(List<ScanReport> reports, List<ScanReportItem> items) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = headerStyle(workbook);
            CellStyle dateStyle = dateStyle(workbook);

            Map<Long, ScanReportItem> itemByReportId = items.stream()
                    .collect(Collectors.toMap(ScanReportItem::getReportId, i -> i, (a, b) -> a));

            long found = countCategory(items, ScanCategory.FOUND);
            long unexpected = countCategory(items, ScanCategory.UNEXPECTED);
            long unknown = countCategory(items, ScanCategory.UNKNOWN);
            long unavailable = countCategory(items, ScanCategory.UNAVAILABLE);

            LocalDateTime earliest = reports.stream().map(ScanReport::getScannedAt)
                    .filter(Objects::nonNull).min(Comparator.naturalOrder()).orElse(null);
            LocalDateTime latest = reports.stream().map(ScanReport::getScannedAt)
                    .filter(Objects::nonNull).max(Comparator.naturalOrder()).orElse(null);
            Set<String> users = reports.stream().map(ScanReport::getPerformedBy).collect(Collectors.toSet());
            Set<String> locations = reports.stream().map(ScanReport::getLocation).collect(Collectors.toSet());

            Sheet summary = workbook.createSheet("Summary");
            int r = 0;
            r = labelValue(summary, r, headerStyle, "Total Scans", String.valueOf(reports.size()));
            r = labelValue(summary, r, headerStyle, "Found", String.valueOf(found));
            r = labelValue(summary, r, headerStyle, "Unexpected", String.valueOf(unexpected));
            r = labelValue(summary, r, headerStyle, "Unknown", String.valueOf(unknown));
            r = labelValue(summary, r, headerStyle, "Unavailable", String.valueOf(unavailable));
            r = labelValue(summary, r, headerStyle, "Performed By",
                    users.size() == 1 ? users.iterator().next() : "Multiple");
            r = labelValue(summary, r, headerStyle, "Location",
                    locations.size() == 1 ? locations.iterator().next() : "Multiple");
            r = labelDate(summary, r, headerStyle, dateStyle, "Earliest Scan", earliest);
            r = labelDate(summary, r, headerStyle, dateStyle, "Latest Scan", latest);
            labelDate(summary, r, headerStyle, dateStyle, "Generated At", LocalDateTime.now());
            summary.setColumnWidth(0, 22 * 256);
            summary.setColumnWidth(1, 32 * 256);

            Sheet scansSheet = workbook.createSheet("Scans");
            writeHeaderRow(scansSheet, headerStyle, SCAN_HEADERS);
            List<ScanReport> sorted = reports.stream()
                    .sorted(Comparator.comparing(ScanReport::getScannedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();
            int row = 1;
            for (ScanReport rep : sorted) {
                ScanReportItem item = itemByReportId.get(rep.getId());
                Row xlRow = scansSheet.createRow(row++);
                Cell timeCell = xlRow.createCell(0);
                if (rep.getScannedAt() != null) {
                    timeCell.setCellValue(rep.getScannedAt());
                    timeCell.setCellStyle(dateStyle);
                }
                xlRow.createCell(1).setCellValue(blank(rep.getPerformedBy()));
                xlRow.createCell(2).setCellValue(blank(rep.getLocation()));
                xlRow.createCell(3).setCellValue(item != null ? item.getEpc() : "");
                xlRow.createCell(4).setCellValue(item != null ? item.getCategory().name() : "");
                xlRow.createCell(5).setCellValue(item != null ? blank(item.getProductName()) : "");
            }
            setWidths(scansSheet, 20, 16, 22, 26, 16, 28);

            return toBytes(workbook);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to build Excel report", e);
        }
    }

    private long countCategory(List<ScanReportItem> items, ScanCategory category) {
        return items.stream().filter(i -> i.getCategory() == category).count();
    }

    private int labelValue(Sheet sheet, int rowIndex, CellStyle headerStyle, String label, String value) {
        Row row = sheet.createRow(rowIndex);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(headerStyle);
        row.createCell(1).setCellValue(blank(value));
        return rowIndex + 1;
    }

    private int labelDate(Sheet sheet, int rowIndex, CellStyle headerStyle, CellStyle dateStyle, String label,
                           LocalDateTime value) {
        Row row = sheet.createRow(rowIndex);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(headerStyle);
        Cell valueCell = row.createCell(1);
        if (value != null) {
            valueCell.setCellValue(value);
            valueCell.setCellStyle(dateStyle);
        }
        return rowIndex + 1;
    }

    private void writeHeaderRow(Sheet sheet, CellStyle headerStyle, String[] headers) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        sheet.createFreezePane(0, 1);
    }

    private void setWidths(Sheet sheet, int... charWidths) {
        for (int i = 0; i < charWidths.length; i++) {
            sheet.setColumnWidth(i, charWidths[i] * 256);
        }
    }

    private String blank(String value) {
        return value != null ? value : "";
    }

    private CellStyle headerStyle(Workbook workbook) {
        Font bold = workbook.createFont();
        bold.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(bold);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle dateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));
        return style;
    }

    private byte[] toBytes(XSSFWorkbook workbook) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
