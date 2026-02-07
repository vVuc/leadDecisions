package com.nology.leaddecisions.domain.ports;

import org.springframework.web.multipart.MultipartFile;

/**
 * Contrato (Porta de Entrada) para o caso de uso de Extração de Dados.
 * Define O QUE o sistema faz, sem expor COMO ele faz.
 */
public interface ExtractDataDocumentUseCase {

    /**
     * Processa um arquivo de documento (Excel), extrai os dados
     * e os persiste nas tabelas de domínio correspondentes.
     *
     * @param file O arquivo multipart recebido da API.
     * @throws IllegalStateException Se houver erro de leitura ou formato.
     * @throws IllegalArgumentException Se o arquivo for inválido.
     */
    void extract(MultipartFile file);
}