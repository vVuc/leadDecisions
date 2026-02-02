package com.nology.leaddecisions.etl.infraestructure.excel;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class ExcelHelper {

    private static final List<DateTimeFormatter> DATE_TIME_FORMATS = List.of(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    );


    public String getCellString(Row row, int index, DataFormatter formatter, FormulaEvaluator evaluator) {
        Cell cell = row.getCell(index);
        if (cell == null) {
            return null;
        }
        String value = formatter.formatCellValue(cell, evaluator);
        return value == null ? null : value.trim();
    }

    public LocalDateTime getCellDateTime(Row row, int index, DataFormatter formatter, int rowIndex) {
        Cell cell = row.getCell(index);
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue();
        }

        String value = formatter.formatCellValue(cell);
        if (value == null || value.isBlank()) {
            return null;
        }

        LocalDateTime parsed = parseDateTime(value.trim());
        if (parsed == null) {
            throw new IllegalArgumentException("Invalid date in BASE at row " + (rowIndex + 1) + ": " + value);
        }
        return parsed;
    }

    public LocalDateTime parseDateTime(String value) {
        for (DateTimeFormatter formatter : DATE_TIME_FORMATS) {
            try {
                return LocalDateTime.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
                try {
                    LocalDate date = LocalDate.parse(value, formatter);
                    return date.atStartOfDay();
                } catch (DateTimeParseException ignoredDate) {
                    // Try next pattern.
                }
            }
        }
        return null;
    }

    public Boolean parseSold(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = normalizeValue(value);
        if (normalized.equals("SIM") || normalized.equals("S") || normalized.equals("TRUE") || normalized.equals("1")) {
            return true;
        }
        if (normalized.equals("NAO") || normalized.equals("N") || normalized.equals("FALSE") || normalized.equals("0")) {
            return false;
        }
        return null;
    }

    public String normalizeHeader(String value) {
        return normalizeValue(value).replaceAll("\\s+", " ");
    }

    public String normalizeValue(String value) {
        String trimmed = value.trim();
        String normalized = Normalizer.normalize(trimmed, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toUpperCase(Locale.ROOT);
    }
    public Sheet requireSheet(Workbook workbook, String name) {
        Sheet sheet = workbook.getSheet(name);
        if (sheet != null) {
            return sheet;
        }
        //PARA TODA TABELA EU ENTRO NOVAMENTE NESTE LOOP
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet candidate = workbook.getSheetAt(i);
            if (candidate.getSheetName().equalsIgnoreCase(name)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("Missing sheet: " + name);
    }

    public Map<String, Integer> buildHeaderMap(Sheet sheet, DataFormatter formatter) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IllegalArgumentException("Missing header row in sheet: " + sheet.getSheetName());
        }

        Map<String, Integer> headers = new HashMap<>();
        for (Cell cell : headerRow) {
            String header = formatter.formatCellValue(cell);
            if (header != null && !header.isBlank()) {
                headers.put(normalizeHeader(header), cell.getColumnIndex());
            }
        }
        return headers;
    }

    public int requireHeader(Map<String, Integer> headers, String headerName) {
        Integer index = headers.get(normalizeHeader(headerName));
        if (index == null) {
            throw new IllegalArgumentException("Missing column: " + headerName);
        }
        return index;
    }

}

