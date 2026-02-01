package com.nology.leaddecisions.etl.infraestructure.service;

import com.nology.leaddecisions.etl.domain.models.DocumentEntity;
import com.nology.leaddecisions.etl.domain.models.LeadEntity;
import com.nology.leaddecisions.etl.domain.models.LocationEntity;
import com.nology.leaddecisions.etl.domain.models.MarketEntity;
import com.nology.leaddecisions.etl.domain.models.ObjectiveEntity;
import com.nology.leaddecisions.etl.domain.models.SizeEntity;
import com.nology.leaddecisions.etl.domain.models.SourceEntity;
import com.nology.leaddecisions.etl.domain.repositories.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Transactional
@AllArgsConstructor
public class ExtractDataDocumentService {
    private static final String SHEET_BASE = "BASE";
    private static final String SHEET_ORIGEM = "ORIGEM";
    private static final String SHEET_LOCAL = "LOCAL";
    private static final String SHEET_PORTE = "PORTE";
    private static final String SHEET_OBJETIVO = "OBJETIVO";
    private static final String SHEET_MERCADO = "MERCADO";

    private static final String COL_LEAD_ID = "LEAD_ID";
    private static final String COL_DATA_CADASTRO = "DATA CADASTRO";
    private static final String COL_VENDIDO = "VENDIDO";
    private static final String COL_MERCADO = "MERCADO";
    private static final String COL_ORIGEM = "ORIGEM";
    private static final String COL_SUB_ORIGEM = "SUB-ORIGEM";
    private static final String COL_LOCAL = "LOCAL";
    private static final String COL_PORTE = "PORTE";
    private static final String COL_OBJETIVO = "OBJETIVO";

    private static final List<DateTimeFormatter> DATE_TIME_FORMATS = List.of(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    );

    private final LeadRepository leadRepository;
    private final DocumentRepository documentRepository;
    private final MarketRepository marketRepository;
    private final SourceRepository sourceRepository;
    private final LocationRepository locationRepository;
    private final SizeRepository sizeRepository;
    private final ObjectiveRepository objectiveRepository;

    public void extract(MultipartFile file) {
       DocumentEntity documentEntity = buildDocumentEntity(file);
        documentRepository.save(documentEntity);

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            DataFormatter formatter = new DataFormatter();

            Map<String, LeadEntity> leads = new HashMap<>();
            List<MarketEntity> markets = new ArrayList<>();

            readBaseSheet(workbook, formatter, leads, markets,documentEntity);
            leadRepository.saveAll(leads.values());

            readMarketSheet(workbook, formatter, leads, markets);
            if (!markets.isEmpty()) {
                marketRepository.saveAll(markets);
            }

            List<SourceEntity> sources = readSourceSheet(workbook, formatter, leads);
            if (!sources.isEmpty()) {
                sourceRepository.saveAll(sources);
            }

            List<LocationEntity> locations = readLocationSheet(workbook, formatter, leads);
            if (!locations.isEmpty()) {
                locationRepository.saveAll(locations);
            }

            List<SizeEntity> sizes = readSizeSheet(workbook, formatter, leads);
            if (!sizes.isEmpty()) {
                sizeRepository.saveAll(sizes);
            }

            List<ObjectiveEntity> objectives = readObjectiveSheet(workbook, formatter, leads);
            if (!objectives.isEmpty()) {
                objectiveRepository.saveAll(objectives);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read XLSX file.", e);
        }
    }

    private void readBaseSheet(
            Workbook workbook,
            DataFormatter formatter,
            Map<String, LeadEntity> leads,
            List<MarketEntity> markets,
            DocumentEntity documentEntity
    ) {
        Sheet sheet = requireSheet(workbook, SHEET_BASE);
        Map<String, Integer> headers = buildHeaderMap(sheet, formatter);
        int leadIdIndex = requireHeader(headers, COL_LEAD_ID);
        int createdAtIndex = requireHeader(headers, COL_DATA_CADASTRO);
        int soldIndex = requireHeader(headers, COL_VENDIDO);
        int marketIndex = requireHeader(headers, COL_MERCADO);

        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            String leadId = getCellString(row, leadIdIndex, formatter);
            if (leadId == null || leadId.isBlank()) {
                continue;
            }

            LeadEntity lead = new LeadEntity();
            lead.setDocument(documentEntity);
            lead.setCreatedAt(getCellDateTime(row, createdAtIndex, formatter, rowIndex));
            lead.setSold(parseSold(getCellString(row, soldIndex, formatter)));
            leads.put(leadId.trim(), lead);

            String marketName = getCellString(row, marketIndex, formatter);
            if (marketName != null && !marketName.isBlank()) {
                MarketEntity market = new MarketEntity();
                market.setName(marketName.trim());
                market.setLead(lead);
                markets.add(market);
            }
        }
    }

    private void readMarketSheet(
            Workbook workbook,
            DataFormatter formatter,
            Map<String, LeadEntity> leads,
            List<MarketEntity> markets
    ) {
        Sheet sheet = requireSheet(workbook, SHEET_MERCADO);
        Map<String, Integer> headers = buildHeaderMap(sheet, formatter);
        int leadIdIndex = requireHeader(headers, COL_LEAD_ID);
        int marketIndex = requireHeader(headers, COL_MERCADO);

        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            String leadId = getCellString(row, leadIdIndex, formatter);
            if (leadId == null || leadId.isBlank()) {
                continue;
            }

            LeadEntity lead = findLead(leads, leadId);
            String marketName = getCellString(row, marketIndex, formatter);
            if (marketName != null && !marketName.isBlank()) {
                MarketEntity market = new MarketEntity();
                market.setName(marketName.trim());
                market.setLead(lead);
                markets.add(market);
            }
        }
    }

    private List<SourceEntity> readSourceSheet(
            Workbook workbook,
            DataFormatter formatter,
            Map<String, LeadEntity> leads
    ) {
        Sheet sheet = requireSheet(workbook, SHEET_ORIGEM);
        Map<String, Integer> headers = buildHeaderMap(sheet, formatter);
        int leadIdIndex = requireHeader(headers, COL_LEAD_ID);
        int origemIndex = requireHeader(headers, COL_ORIGEM);
        int subOrigemIndex = requireHeader(headers, COL_SUB_ORIGEM);

        List<SourceEntity> sources = new ArrayList<>();
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            String leadId = getCellString(row, leadIdIndex, formatter);
            if (leadId == null || leadId.isBlank()) {
                continue;
            }

            LeadEntity lead = findLead(leads, leadId);
            String origin = getCellString(row, origemIndex, formatter);
            if (origin == null || origin.isBlank()) {
                continue;
            }

            SourceEntity source = new SourceEntity();
            source.setName(origin.trim());
            source.setSubSource(getCellString(row, subOrigemIndex, formatter));
            source.setLead(lead);
            sources.add(source);
        }
        return sources;
    }

    private List<LocationEntity> readLocationSheet(
            Workbook workbook,
            DataFormatter formatter,
            Map<String, LeadEntity> leads
    ) {
        Sheet sheet = requireSheet(workbook, SHEET_LOCAL);
        Map<String, Integer> headers = buildHeaderMap(sheet, formatter);
        int leadIdIndex = requireHeader(headers, COL_LEAD_ID);
        int localIndex = requireHeader(headers, COL_LOCAL);

        List<LocationEntity> locations = new ArrayList<>();
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            String leadId = getCellString(row, leadIdIndex, formatter);
            if (leadId == null || leadId.isBlank()) {
                continue;
            }

            LeadEntity lead = findLead(leads, leadId);
            String localName = getCellString(row, localIndex, formatter);
            if (localName == null || localName.isBlank()) {
                continue;
            }

            LocationEntity location = new LocationEntity();
            location.setName(localName.trim());
            location.setLead(lead);
            locations.add(location);
        }
        return locations;
    }

    private List<SizeEntity> readSizeSheet(
            Workbook workbook,
            DataFormatter formatter,
            Map<String, LeadEntity> leads
    ) {
        Sheet sheet = requireSheet(workbook, SHEET_PORTE);
        Map<String, Integer> headers = buildHeaderMap(sheet, formatter);
        int leadIdIndex = requireHeader(headers, COL_LEAD_ID);
        int sizeIndex = requireHeader(headers, COL_PORTE);

        List<SizeEntity> sizes = new ArrayList<>();
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            String leadId = getCellString(row, leadIdIndex, formatter);
            if (leadId == null || leadId.isBlank()) {
                continue;
            }

            LeadEntity lead = findLead(leads, leadId);
            String sizeRange = getCellString(row, sizeIndex, formatter);
            if (sizeRange == null || sizeRange.isBlank()) {
                continue;
            }

            SizeEntity size = new SizeEntity();
            size.setSizeRange(sizeRange.trim());
            size.setLead(lead);
            sizes.add(size);
        }
        return sizes;
    }

    private List<ObjectiveEntity> readObjectiveSheet(
            Workbook workbook,
            DataFormatter formatter,
            Map<String, LeadEntity> leads
    ) {
        Sheet sheet = requireSheet(workbook, SHEET_OBJETIVO);
        Map<String, Integer> headers = buildHeaderMap(sheet, formatter);
        int leadIdIndex = requireHeader(headers, COL_LEAD_ID);
        int objectiveIndex = requireHeader(headers, COL_OBJETIVO);

        List<ObjectiveEntity> objectives = new ArrayList<>();
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            String leadId = getCellString(row, leadIdIndex, formatter);
            if (leadId == null || leadId.isBlank()) {
                continue;
            }

            LeadEntity lead = findLead(leads, leadId);
            String description = getCellString(row, objectiveIndex, formatter);
            if (description == null || description.isBlank()) {
                continue;
            }

            ObjectiveEntity objective = new ObjectiveEntity();
            objective.setDescription(description.trim());
            objective.setLead(lead);
            objectives.add(objective);
        }
        return objectives;
    }

    private LeadEntity findLead(Map<String, LeadEntity> leads, String leadId) {
        LeadEntity lead = leads.get(leadId.trim());
        if (lead == null) {
            throw new IllegalArgumentException("Lead ID not found in BASE: " + leadId);
        }
        return lead;
    }

    private Sheet requireSheet(Workbook workbook, String name) {
        Sheet sheet = workbook.getSheet(name);
        if (sheet != null) {
            return sheet;
        }
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet candidate = workbook.getSheetAt(i);
            if (candidate.getSheetName().equalsIgnoreCase(name)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("Missing sheet: " + name);
    }

    private Map<String, Integer> buildHeaderMap(Sheet sheet, DataFormatter formatter) {
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

    private int requireHeader(Map<String, Integer> headers, String headerName) {
        Integer index = headers.get(normalizeHeader(headerName));
        if (index == null) {
            throw new IllegalArgumentException("Missing column: " + headerName);
        }
        return index;
    }

    private String getCellString(Row row, int index, DataFormatter formatter) {
        Cell cell = row.getCell(index);
        if (cell == null) {
            return null;
        }
        String value = formatter.formatCellValue(cell);
        return value == null ? null : value.trim();
    }

    private LocalDateTime getCellDateTime(Row row, int index, DataFormatter formatter, int rowIndex) {
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

    private LocalDateTime parseDateTime(String value) {
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

    private Boolean parseSold(String value) {
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

    private String normalizeHeader(String value) {
        return normalizeValue(value).replaceAll("\\s+", " ");
    }

    private String normalizeValue(String value) {
        String trimmed = value.trim();
        String normalized = Normalizer.normalize(trimmed, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toUpperCase(Locale.ROOT);
    }

    private DocumentEntity buildDocumentEntity(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required.");
        }

        DocumentEntity documentEntity = new DocumentEntity();
        documentEntity.setDocumentContentType(file.getContentType());
        documentEntity.setDocumentName(file.getOriginalFilename());
        try {
            documentEntity.setDocumentContent(file.getBytes());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read document content.", e);
        }
        return documentEntity;
    }
}
