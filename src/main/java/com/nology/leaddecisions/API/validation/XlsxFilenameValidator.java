package com.nology.leaddecisions.API.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

/**
 * Lógica de validação responsável por aplicar as regras definidas na anotação XlsxFilename.
 *
 * Implementa a interface ConstraintValidator para integrar-se ao ciclo de vida de validação do Jakarta EE.
 * Esta classe atua como uma barreira de segurança e integridade para uploads de arquivos.
 *
 * Principais verificações:
 * - Nulidade: Garante que o arquivo foi efetivamente enviado.
 * - Segurança (Path Traversal): Detecta e bloqueia tentativas de manipulação de caminhos de diretório
 * (ex: uso de ".." ou separadores de pasta no nome do arquivo).
 * - Formato: Valida estritamente a extensão ".xlsx" (Excel Moderno).
 */
public class XlsxFilenameValidator implements ConstraintValidator<XlsxFilename, MultipartFile> {

    /**
     * Executa a verificação de validade sobre o arquivo recebido.
     *
     * O processo segue uma abordagem "Fail Fast" (falha rápida), retornando falso imediatamente
     * ao encontrar qualquer violação de segurança ou formato.
     *
     * @param value O objeto MultipartFile contendo o arquivo enviado pelo cliente.
     * @param context Contexto da validação, permite customizar a construção de violações (não utilizado aqui).
     * @return true se o arquivo for seguro e estiver no formato correto; false caso contrário.
     */
    @Override
    public boolean isValid(MultipartFile value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        String filename = value.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            return false;
        }

        String normalized = filename.replace("\\", "/");

        if (normalized.contains("..") || normalized.contains("/")) {
            return false;
        }

        return normalized.toLowerCase().endsWith(".xlsx");
    }
}