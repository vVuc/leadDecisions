package com.nology.leaddecisions.etl.infraestructure.service;

import com.nology.leaddecisions.etl.domain.models.DocumentEntity;
import com.nology.leaddecisions.etl.domain.repositories.*;
import com.nology.leaddecisions.etl.infraestructure.excel.ExcelHelper;
import lombok.AllArgsConstructor;
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

@ExtendWith(MockitoExtension.class) // Inicializa os Mocks sem carregar o Spring Context (Rápido)
class ExtractDataDocumentServiceTest {

    // Mocks dos repositórios (Dependências do Service)
    @Mock private LeadRepository leadRepository;
    @Mock private DocumentRepository documentRepository;
    @Mock private MarketRepository marketRepository;
    @Mock private SourceRepository sourceRepository;
    @Spy private ExcelHelper excelHelper;
    @Mock private LocationRepository locationRepository;
    @Mock private SizeRepository sizeRepository;
    @Mock private ObjectiveRepository objectiveRepository;

    @InjectMocks // Injeta os mocks acima dentro do Service real
    private ExtractDataDocumentService service;

    // --- Cenario 1: Validação de Entrada (Guard Clauses) ---

    @Test
    @DisplayName("Deve lançar exceção quando o arquivo for nulo")
    void shouldThrowExceptionWhenFileIsNull() {
        // Ação & Verificação
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.extract(null)
        );

        // Assertiva da mensagem (opcional, mas boa prática)
        // Ajuste conforme a mensagem exata no seu código ("File is required" no seu buildDocumentEntity)
        assertEquals("File is required.", exception.getMessage());

        // Verificação de Segurança: Garante que NENHUM repository foi chamado
        verifyNoInteractions(documentRepository, leadRepository);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o arquivo estiver vazio (0 bytes)")
    void shouldThrowExceptionWhenFileIsEmpty() {
        // Cenário
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "vazio.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[0]
        );

        // Ação & Verificação
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.extract(emptyFile)
        );

        assertEquals("File is required.", exception.getMessage());
        verifyNoInteractions(documentRepository);
    }

    // --- Cenario 2: Integridade do Arquivo ---

    @Test
    @DisplayName("Deve falhar graciosamente ao receber um arquivo que não é Excel (ex: .txt)")
    void shouldFailGracefullyWhenFileIsNotExcel() {
        // Cenário: Um arquivo de texto fingindo ser upload
        MockMultipartFile textFile = new MockMultipartFile(
                "file",
                "teste.txt",
                "text/plain",
                "Conteúdo inválido que quebraria o Apache POI".getBytes()
        );

        // Ação & Verificação
        // O seu código captura IOException e relança como IllegalStateException
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.extract(textFile)
        );

        assertEquals("Unable to read XLSX file.", exception.getMessage());

        // Importante: Neste caso, o documento CHEGA a ser salvo (pois o erro ocorre ao abrir o workbook)
        // Então verificamos se o save foi chamado 1 vez
        verify(documentRepository, times(1)).save(any());
        // Mas garantimos que nenhum LEAD foi salvo
        verifyNoInteractions(leadRepository);
    }

    // --- Cenario 3: Performance Básica (SLA de Validação) ---

    @Test
    @DisplayName("Deve validar rejeição de arquivo inválido em menos de 50ms")
    void shouldRejectInvalidFileFast() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);

        // assertTimeout garante que a lógica de validação não está fazendo nada pesado antes de falhar
        assertTimeout(Duration.ofMillis(50), () -> {
            assertThrows(IllegalArgumentException.class, () -> service.extract(emptyFile));
        });
    }

    @Test
    @DisplayName("Deve processar um arquivo Excel válido e persistir as entidades corretamente")
    void shouldExtractAndPersistDataWhenFileIsValid() throws IOException {
        // 1. PREPARAÇÃO (Arrange)
        // Criamos um Excel real em memória com todas as abas obrigatórias
        byte[] excelContent = createValidExcelFile();

        MockMultipartFile validFile = new MockMultipartFile(
                "file",
                "leads_validos.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                excelContent
        );

        // 2. AÇÃO (Act)
        service.extract(validFile);

        // 3. VERIFICAÇÃO (Assert)

        // Verifica se o documento (blob) foi salvo
        verify(documentRepository, times(1)).save(any(DocumentEntity.class));

        // Verifica se os leads foram salvos (esperamos 1 lead no mapa)
        // O argumento é um Collection, então usamos any()
        verify(leadRepository, times(1)).saveAll(any());

        // Verifica se as entidades filhas foram salvas
//        excelHelper.getCellString()
        verify(marketRepository, times(1)).saveAll(any());
        verify(sourceRepository, times(1)).saveAll(any());

        // Dica: Para validar o CONTEÚDO exato, usaríamos ArgumentCaptor,
        // mas para este teste de fumaça, garantir que os métodos foram chamados já valida o fluxo.
    }

    // --- Helper para criar o Excel Fake ---
    private byte[] createValidExcelFile() throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            // --- Aba BASE ---
            Sheet baseSheet = workbook.createSheet("BASE");
            Row baseHeader = baseSheet.createRow(0);
            baseHeader.createCell(0).setCellValue("LEAD_ID");
            baseHeader.createCell(1).setCellValue("DATA CADASTRO");
            baseHeader.createCell(2).setCellValue("VENDIDO");

            Row baseData = baseSheet.createRow(1);
            baseData.createCell(0).setCellValue("12345"); // ID do Lead
            baseData.createCell(1).setCellValue("01/01/2026 10:00");
            baseData.createCell(2).setCellValue("SIM");

            // --- Aba MERCADO ---
            Sheet marketSheet = workbook.createSheet("MERCADO");
            Row marketHeader = marketSheet.createRow(0);
            marketHeader.createCell(0).setCellValue("LEAD_ID");
            marketHeader.createCell(1).setCellValue("MERCADO");

            Row marketData = marketSheet.createRow(1);
            marketData.createCell(0).setCellValue("12345");
            marketData.createCell(1).setCellValue("Tecnologia");

            // --- Aba ORIGEM ---
            Sheet sourceSheet = workbook.createSheet("ORIGEM");
            Row sourceHeader = sourceSheet.createRow(0);
            sourceHeader.createCell(0).setCellValue("LEAD_ID");
            sourceHeader.createCell(1).setCellValue("ORIGEM");
            sourceHeader.createCell(2).setCellValue("SUB-ORIGEM");

            Row sourceData = sourceSheet.createRow(1);
            sourceData.createCell(0).setCellValue("12345");
            sourceData.createCell(1).setCellValue("Google");
            sourceData.createCell(2).setCellValue("Ads");

            // --- Abas Opcionais (Criamos vazias ou com header para não quebrar requireSheet) ---
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