package com.nology.leaddecisions.etl.infraestructure.service;

import com.nology.leaddecisions.etl.domain.models.DocumentEntity;
import com.nology.leaddecisions.etl.domain.models.LeadEntity;
import com.nology.leaddecisions.etl.domain.models.LocationEntity;
import com.nology.leaddecisions.etl.domain.models.MarketEntity;
import com.nology.leaddecisions.etl.domain.models.ObjectiveEntity;
import com.nology.leaddecisions.etl.domain.models.SizeEntity;
import com.nology.leaddecisions.etl.domain.models.SourceEntity;
import com.nology.leaddecisions.etl.domain.ports.ExtractDataDocumentUseCase;
import com.nology.leaddecisions.etl.domain.repositories.*;
import com.nology.leaddecisions.etl.infraestructure.excel.ExcelHelper;
import com.nology.leaddecisions.etl.infraestructure.excel.LeadExcelSchema;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@AllArgsConstructor
public class ExtractDataDocumentService implements ExtractDataDocumentUseCase {
    private final LeadRepository leadRepository;
    private final DocumentRepository documentRepository;
    private final MarketRepository marketRepository;
    private final SourceRepository sourceRepository;
    private final LocationRepository locationRepository;
    private final SizeRepository sizeRepository;
    private final ObjectiveRepository objectiveRepository;
    private final ExcelHelper excelHelper;

    public void extract(MultipartFile file) {
       DocumentEntity documentEntity = buildDocumentEntity(file);
        documentRepository.save(documentEntity);

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            Map<String, LeadEntity> leads = new HashMap<>();
            List<MarketEntity> markets = new ArrayList<>();

            readBaseSheet(workbook, formatter, evaluator, leads,documentEntity);
            leadRepository.saveAll(leads.values());

            readMarketSheet(workbook, formatter, evaluator, leads, markets);
            if (!markets.isEmpty()) {
                marketRepository.saveAll(markets);
            }

            List<SourceEntity> sources = readSourceSheet(workbook, formatter, evaluator, leads);
            if (!sources.isEmpty()) {
                sourceRepository.saveAll(sources);
            }

            List<LocationEntity> locations = readLocationSheet(workbook, formatter, evaluator, leads);
            if (!locations.isEmpty()) {
                locationRepository.saveAll(locations);
            }

            List<SizeEntity> sizes = readSizeSheet(workbook, formatter, evaluator, leads);
            if (!sizes.isEmpty()) {
                sizeRepository.saveAll(sizes);
            }

            List<ObjectiveEntity> objectives = readObjectiveSheet(workbook, formatter, evaluator, leads);
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
            FormulaEvaluator evaluator,
            Map<String, LeadEntity> leads,
            DocumentEntity documentEntity
    ) {
        Sheet sheet = excelHelper.requireSheet(workbook, LeadExcelSchema.Sheets.BASE);
        Map<String, Integer> headers = excelHelper.buildHeaderMap(sheet, formatter);
        int leadIdIndex = excelHelper.requireHeader(headers, LeadExcelSchema.Columns.LEAD_ID);
        int createdAtIndex = excelHelper.requireHeader(headers, LeadExcelSchema.Columns.DATA_CADASTRO);
        int soldIndex = excelHelper.requireHeader(headers, LeadExcelSchema.Columns.VENDIDO);

        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            String leadId = excelHelper.getCellString(row, leadIdIndex, formatter,evaluator);
            if (leadId == null || leadId.isBlank()) {
                continue;
            }

            LeadEntity lead = new LeadEntity();
            lead.setDocument(documentEntity);
            lead.setCreatedAt(excelHelper.getCellDateTime(row, createdAtIndex, formatter, rowIndex));
            lead.setSold(excelHelper.parseSold(excelHelper.getCellString(row, soldIndex, formatter,evaluator)));
            leads.put(leadId.trim(), lead);
        }
    }

    private void readMarketSheet(
            Workbook workbook,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            Map<String, LeadEntity> leads,
            List<MarketEntity> markets
    ) {
        Sheet sheet = excelHelper.requireSheet(workbook, LeadExcelSchema.Sheets.MERCADO);
        Map<String, Integer> headers = excelHelper.buildHeaderMap(sheet, formatter);
        int leadIdIndex = excelHelper.requireHeader(headers, LeadExcelSchema.Columns.LEAD_ID);
        int marketIndex = excelHelper.requireHeader(headers, LeadExcelSchema.Columns.MERCADO);

        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {

                continue;
            }

            String leadId = excelHelper.getCellString(row, leadIdIndex, formatter,evaluator);
            if (leadId == null || leadId.isBlank()) {
                continue;
            }

            LeadEntity lead = findLead(leads, leadId);
            String marketName = excelHelper.getCellString(row, marketIndex, formatter,evaluator);
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
            FormulaEvaluator evaluator,
            Map<String, LeadEntity> leads
    ) {
        Sheet sheet = excelHelper.requireSheet(workbook, LeadExcelSchema.Sheets.ORIGEM);
        Map<String, Integer> headers = excelHelper.buildHeaderMap(sheet, formatter);
        int leadIdIndex = excelHelper.requireHeader(headers, LeadExcelSchema.Columns.LEAD_ID);
        int origemIndex = excelHelper.requireHeader(headers, LeadExcelSchema.Columns.ORIGEM);
        int subOrigemIndex = excelHelper.requireHeader(headers, LeadExcelSchema.Columns.SUB_ORIGEM);

        List<SourceEntity> sources = new ArrayList<>();
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            String leadId = excelHelper.getCellString(row, leadIdIndex, formatter,evaluator);
            if (leadId == null || leadId.isBlank()) {
                continue;
            }

            LeadEntity lead = findLead(leads, leadId);
            String origin = excelHelper.getCellString(row, origemIndex, formatter,evaluator);
            if (origin == null || origin.isBlank()) {
                continue;
            }

            SourceEntity source = new SourceEntity();
            source.setName(origin.trim());
            source.setSubSource(excelHelper.getCellString(row, subOrigemIndex, formatter,evaluator));
            source.setLead(lead);
            sources.add(source);
        }
        return sources;
    }

    private List<LocationEntity> readLocationSheet(
            Workbook workbook,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            Map<String, LeadEntity> leads
    ) {
        Sheet sheet = excelHelper.requireSheet(workbook, LeadExcelSchema.Sheets.LOCAL);
        Map<String, Integer> headers = excelHelper.buildHeaderMap(sheet, formatter);
        int leadIdIndex = excelHelper.requireHeader(headers, LeadExcelSchema.Columns.LEAD_ID);
        int localIndex = excelHelper.requireHeader(headers, LeadExcelSchema.Columns.LOCAL);

        List<LocationEntity> locations = new ArrayList<>();
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            String leadId = excelHelper.getCellString(row, leadIdIndex, formatter,evaluator);
            if (leadId == null || leadId.isBlank()) {
                continue;
            }

            LeadEntity lead = findLead(leads, leadId);
            String localName = excelHelper.getCellString(row, localIndex, formatter,evaluator);
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
            FormulaEvaluator evaluator,
            Map<String, LeadEntity> leads
    ) {
        Sheet sheet = excelHelper.requireSheet(workbook, LeadExcelSchema.Sheets.PORTE);
        Map<String, Integer> headers = excelHelper.buildHeaderMap(sheet, formatter);
        int leadIdIndex = excelHelper.requireHeader(headers, LeadExcelSchema.Columns.LEAD_ID);
        int sizeIndex = excelHelper.requireHeader(headers, LeadExcelSchema.Columns.PORTE);

        List<SizeEntity> sizes = new ArrayList<>();
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            String leadId = excelHelper.getCellString(row, leadIdIndex, formatter,evaluator);
            if (leadId == null || leadId.isBlank()) {
                continue;
            }

            LeadEntity lead = findLead(leads, leadId);
            String sizeRange = excelHelper.getCellString(row, sizeIndex, formatter,evaluator);
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
            FormulaEvaluator evaluator,
            Map<String, LeadEntity> leads
    ) {
        Sheet sheet = excelHelper.requireSheet(workbook, LeadExcelSchema.Sheets.OBJETIVO);
        Map<String, Integer> headers = excelHelper.buildHeaderMap(sheet, formatter);
        int leadIdIndex = excelHelper.requireHeader(headers, LeadExcelSchema.Columns.LEAD_ID);
        int objectiveIndex = excelHelper.requireHeader(headers, LeadExcelSchema.Columns.OBJETIVO);

        List<ObjectiveEntity> objectives = new ArrayList<>();
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            String leadId = excelHelper.getCellString(row, leadIdIndex, formatter,evaluator);
            if (leadId == null || leadId.isBlank()) {
                continue;
            }

            LeadEntity lead = findLead(leads, leadId);
            String description = excelHelper.getCellString(row, objectiveIndex, formatter,evaluator);
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
