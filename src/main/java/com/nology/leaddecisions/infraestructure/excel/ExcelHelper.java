package com.nology.leaddecisions.infraestructure.excel;

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

/**
 * Componente utilitário para suporte a operações de leitura e processamento de arquivos Microsoft Excel.
 *
 * Atua como uma camada de abstração sobre a biblioteca Apache POI, fornecendo métodos resilientes para extração,
 * normalização e conversão de dados provenientes de planilhas. Este componente garante que os dados brutos
 * sejam transformados em tipos Java seguros antes de serem processados pelo domínio.
 */
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

    /**
     * Extrai o valor de uma célula como uma String tratada.
     * * Utiliza o DataFormatter para converter o conteúdo da célula em texto e o FormulaEvaluator para
     * resolver possíveis fórmulas antes da captura do valor. O resultado é retornado sem espaços
     * em branco nas extremidades.
     *
     * @param row A linha da planilha a ser processada.
     * @param index O índice da coluna desejada.
     * @param formatter O formatador de dados do Apache POI.
     * @param evaluator O avaliador de fórmulas do Apache POI.
     * @return O valor textual da célula ou null caso a célula não exista.
     */
    public String getCellString(Row row, int index, DataFormatter formatter, FormulaEvaluator evaluator) {
        Cell cell = row.getCell(index);
        if (cell == null) {
            return null;
        }
        String value = formatter.formatCellValue(cell, evaluator);
        return value == null ? null : value.trim();
    }
    /**
     * Extrai e converte o valor de uma célula para LocalDateTime.
     * * O método suporta tanto células formatadas nativamente como data no Excel (tipo numérico)
     * quanto células de texto que contenham representações de data suportadas pelo sistema.
     *
     * @param row A linha da planilha.
     * @param index O índice da coluna.
     * @param formatter O formatador de dados.
     * @param rowIndex O índice da linha atual (utilizado para mensagens de erro).
     * @return Objeto LocalDateTime correspondente à célula.
     * @throws IllegalArgumentException Caso a data no formato texto não corresponda a nenhum padrão suportado.
     */
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
    /**
     * Converte uma String em LocalDateTime testando sequencialmente os padrões definidos em DATE_TIME_FORMATS.
     * * O fluxo de execução segue a ordem:
     * 1. Tenta o parsing para LocalDateTime completo (Data + Hora).
     * 2. Caso falhe, tenta o parsing para LocalDate (Apenas Data).
     * 3. Em caso de sucesso como LocalDate, define o horário para 00:00:00 via atStartOfDay().
     * @param value Representação textual da data ou data/hora.
     * @return Objeto LocalDateTime correspondente ou null se nenhum padrão for compatível.
     */
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
    /**
     * Converte representações textuais de estados afirmativos ou negativos em Booleano.
     * * Normaliza a entrada para ignorar acentos e maiúsculas. Mapeia variações como "SIM", "S",
     * "TRUE" ou "1" para true, e "NAO", "N", "FALSE" ou "0" para false.
     *
     * @param value O texto a ser analisado.
     * @return true, false ou null caso o valor não corresponda aos padrões.
     */
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
    /**
     * Converte representações textuais de estados afirmativos ou negativos em Booleano.
     * * Normaliza a entrada para ignorar acentos e maiúsculas. Mapeia variações como "SIM", "S",
     * "TRUE" ou "1" para true, e "NAO", "N", "FALSE" ou "0" para false.
     *
     * @param value O texto a ser analisado.
     * @return true, false ou null caso o valor não corresponda aos padrões.
     */
    public String normalizeHeader(String value) {
        return normalizeValue(value).replaceAll("\\s+", " ");
    }
    /**
     * Normaliza uma String removendo acentos, espaços extras e convertendo para maiúsculas.
     * * Utiliza a normalização Unicode (NFD) para decompor caracteres acentuados e remove
     * marcas de acentuação via expressão regular.
     *
     * @param value O texto original.
     * @return O texto normalizado e em caixa alta.
     */
    public String normalizeValue(String value) {
        String trimmed = value.trim();
        String normalized = Normalizer.normalize(trimmed, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toUpperCase(Locale.ROOT);
    }
    /**
     * Localiza e retorna uma aba (Sheet) obrigatória do arquivo Excel.
     * * Realiza uma busca inicial pelo nome exato e, caso não encontre, percorre todas as abas
     * realizando uma comparação insensível a maiúsculas e minúsculas.
     *
     * @param workbook O arquivo Excel aberto.
     * @param name O nome da aba desejada.
     * @return A aba correspondente.
     * @throws IllegalArgumentException Caso a aba solicitada não exista no arquivo.
     */
    public Sheet requireSheet(Workbook workbook, String name) {
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
    /**
     * Mapeia os cabeçalhos presentes na primeira linha de uma aba.
     * * Cria um dicionário onde a chave é o nome da coluna normalizado e o valor é o seu
     * índice físico na planilha. Isso permite que a extração seja resiliente a mudanças
     * na ordem das colunas.
     *
     * @param sheet A aba a ser analisada.
     * @param formatter O formatador de dados.
     * @return Mapa vinculando nomes de colunas aos seus respectivos índices.
     */
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
    /**
     * Recupera o índice de uma coluna obrigatória a partir de um mapa de cabeçalhos.
     *
     * @param headers O mapa gerado por buildHeaderMap.
     * @param headerName O nome da coluna esperado.
     * @return O índice da coluna.
     * @throws IllegalArgumentException Caso a coluna solicitada não esteja presente no mapa.
     */
    public int requireHeader(Map<String, Integer> headers, String headerName) {
        Integer index = headers.get(normalizeHeader(headerName));
        if (index == null) {
            throw new IllegalArgumentException("Missing column: " + headerName);
        }
        return index;
    }

}

