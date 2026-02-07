package com.nology.leaddecisions.etl.adapters.inbound.controller;

import com.nology.leaddecisions.etl.API.dtos.request.ExtractDataDocumentRequest;
import com.nology.leaddecisions.etl.domain.ports.ExtractDataDocumentUseCase;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Controlador REST responsável por expor os pontos de entrada para operações de ETL (Extract, Transform, Load).
 *
 * Atua como um Adaptador Primário (Driving Adapter) na Arquitetura Hexagonal,
 * recebendo requisições HTTP, validando o payload de entrada e delegando o processamento
 * para a Porta de Entrada do Domínio (UseCase).
 */
@RestController
@RequestMapping("/etl")
@AllArgsConstructor
public class ExtractDataDocumentController {

    private final ExtractDataDocumentUseCase extractDataDocumentUseCase;

    /**
     * Endpoint para upload e processamento de documentos de carga de dados.
     *
     * Este método consome requisições do tipo multipart/form-data, permitindo o envio de arquivos binários.
     * A validação do arquivo (extensão, segurança e integridade) é realizada automaticamente
     * através das anotações presentes no DTO de requisição.
     *
     * O processamento é delegado de forma síncrona para o caso de uso.
     *
     * @param request O DTO contendo o arquivo (MultipartFile) e metadados da requisição.
     * @return ResponseEntity com status 204 (No Content) indicando sucesso no processamento, sem corpo de resposta.
     */
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> upload(@Valid @ModelAttribute ExtractDataDocumentRequest request) {
        extractDataDocumentUseCase.extract(request.getFile());
        return ResponseEntity.noContent().build();
    }
}