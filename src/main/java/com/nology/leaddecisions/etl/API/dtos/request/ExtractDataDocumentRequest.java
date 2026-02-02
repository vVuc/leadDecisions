package com.nology.leaddecisions.etl.API.dtos.request;

import com.nology.leaddecisions.etl.API.validation.XlsxFilename;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Objeto de Transferência de Dados (DTO) para a requisição de upload de documentos.
 *
 * Este DTO encapsula o arquivo recebido via multipart/form-data na API de ETL.
 * Sua principal responsabilidade é garantir que o arquivo seja transportado até a camada de controle
 * e submetido às validações de formato e segurança definidas pelas anotações.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExtractDataDocumentRequest {

    /**
     * O arquivo binário contendo os dados brutos para extração.
     *
     * Este campo é validado pela anotação customizada {@link XlsxFilename}, que assegura:
     *
     * - Que o arquivo não é nulo.
     * - Que a extensão é estritamente '.xlsx' (padrão Excel Moderno).
     * - Que o nome do arquivo não contém tentativas de 'Path Traversal' (ex: '..').
     */
    @XlsxFilename
    private MultipartFile file;

}