package com.nology.leaddecisions.etl.infraestructure.service;

import com.nology.leaddecisions.adapters.outbound.repositories.*;
import com.nology.leaddecisions.application.service.ExtractDataDocumentService;
import com.nology.leaddecisions.domain.models.DocumentEntity;
import com.nology.leaddecisions.infraestructure.excel.ExcelHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class ExtractDataDocumentServiceTest {

    @Mock private LeadRepository leadRepository;
    @Mock private DocumentRepository documentRepository;
    @Mock private MarketRepository marketRepository;
    @Mock private SourceRepository sourceRepository;
    @Spy private ExcelHelper excelHelper;
    @Mock private LocationRepository locationRepository;
    @Mock private SizeRepository sizeRepository;
    @Mock private ObjectiveRepository objectiveRepository;

    @InjectMocks
    private ExtractDataDocumentService service;

    @Test
    @DisplayName("Deve lançar exceção quando o arquivo for nulo")
    void shouldThrowExceptionWhenFileIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.extract(null)
        );

        assertEquals("File is required.", exception.getMessage());

        verifyNoInteractions(documentRepository, leadRepository);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o arquivo estiver vazio (0 bytes)")
    void shouldThrowExceptionWhenFileIsEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "vazio.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[0]
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.extract(emptyFile)
        );

        assertEquals("File is required.", exception.getMessage());
        verifyNoInteractions(documentRepository);
    }


    @Test
    @DisplayName("Deve falhar graciosamente ao receber um arquivo que não é Excel (ex: .txt)")
    void shouldFailGracefullyWhenFileIsNotExcel() {
        MockMultipartFile textFile = new MockMultipartFile(
                "file",
                "teste.txt",
                "text/plain",
                "Conteúdo inválido que quebraria o Apache POI".getBytes()
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.extract(textFile)
        );

        assertEquals("Unable to read XLSX file.", exception.getMessage());

        verify(documentRepository, times(1)).save(any());
        verifyNoInteractions(leadRepository);
    }

    @Test
    @DisplayName("Deve validar rejeição de arquivo inválido em menos de 50ms")
    void shouldRejectInvalidFileFast() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);

        assertTimeout(Duration.ofMillis(50), () -> {
            assertThrows(IllegalArgumentException.class, () -> service.extract(emptyFile));
        });
    }

    @Test
    @DisplayName("Deve processar um arquivo Excel válido e persistir as entidades corretamente")
    void shouldExtractAndPersistDataWhenFileIsValid() throws IOException {
        byte[] excelContent = createValidExcelFile();

        MockMultipartFile validFile = new MockMultipartFile(
                "file",
                "leads_validos.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                excelContent
        );

        service.extract(validFile);

        verify(documentRepository, times(1)).save(any(DocumentEntity.class));

        verify(leadRepository, times(1)).saveAll(any());

        verify(marketRepository, times(1)).saveAll(any());
        verify(sourceRepository, times(1)).saveAll(any());

    }

    private byte[] createValidExcelFile() throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet baseSheet = workbook.createSheet("BASE");
            Row baseHeader = baseSheet.createRow(0);
            baseHeader.createCell(0).setCellValue("LEAD_ID");
            baseHeader.createCell(1).setCellValue("DATA CADASTRO");
            baseHeader.createCell(2).setCellValue("VENDIDO");

            Row baseData = baseSheet.createRow(1);
            baseData.createCell(0).setCellValue("12345");
            baseData.createCell(1).setCellValue("01/01/2026 10:00");
            baseData.createCell(2).setCellValue("SIM");

            Sheet marketSheet = workbook.createSheet("MERCADO");
            Row marketHeader = marketSheet.createRow(0);
            marketHeader.createCell(0).setCellValue("LEAD_ID");
            marketHeader.createCell(1).setCellValue("MERCADO");

            Row marketData = marketSheet.createRow(1);
            marketData.createCell(0).setCellValue("12345");
            marketData.createCell(1).setCellValue("Tecnologia");

            Sheet sourceSheet = workbook.createSheet("ORIGEM");
            Row sourceHeader = sourceSheet.createRow(0);
            sourceHeader.createCell(0).setCellValue("LEAD_ID");
            sourceHeader.createCell(1).setCellValue("ORIGEM");
            sourceHeader.createCell(2).setCellValue("SUB-ORIGEM");

            Row sourceData = sourceSheet.createRow(1);
            sourceData.createCell(0).setCellValue("12345");
            sourceData.createCell(1).setCellValue("Google");
            sourceData.createCell(2).setCellValue("Ads");

            createSimpleSheet(workbook, "LOCAL", "LOCAL");
            createSimpleSheet(workbook, "PORTE", "PORTE");
            createSimpleSheet(workbook, "OBJETIVO", "OBJETIVO");

            workbook.write(bos);
            return bos.toByteArray();
        }
    }

    private void createSimpleSheet(Workbook workbook, String sheetName, String colName) {
        Sheet sheet = workbook.createSheet(sheetName);
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("LEAD_ID");
        header.createCell(1).setCellValue(colName);
    }
}