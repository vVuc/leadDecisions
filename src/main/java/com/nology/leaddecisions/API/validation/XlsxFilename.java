package com.nology.leaddecisions.API.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Anotação de validação customizada para garantir que um arquivo (MultipartFile)
 * seja do formato Excel Moderno (.xlsx).
 *
 * Esta anotação integra-se ao ciclo de vida do Bean Validation (Jakarta Validation)
 * e delega a lógica de verificação para a classe XlsxFilenameValidator.
 *
 * Critérios verificados:
 * - A extensão do arquivo deve ser estritamente ".xlsx" (case insensitive).
 * - Verificações de segurança no nome do arquivo (como prevenção de Path Traversal)
 * são aplicadas conforme a implementação do validador associado.
 */
@Documented
@Constraint(validatedBy = XlsxFilenameValidator.class)
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface XlsxFilename {

    /**
     * Define a mensagem padrão retornada ao cliente caso o arquivo seja rejeitado.
     * Pode ser sobrescrita no momento do uso da anotação para maior especificidade.
     */
    String message() default "File name must end with .xlsx";

    /**
     * Permite a definição de grupos de validação, possibilitando aplicar esta restrição
     * apenas em contextos específicos (ex: Criação vs Atualização).
     * Obrigatório pelo contrato da especificação Bean Validation.
     */
    Class<?>[] groups() default {};

    /**
     * Permite associar metadados adicionais (payload) à restrição de validação,
     * útil para clientes da API ou frameworks de processamento de erro.
     * Obrigatório pelo contrato da especificação Bean Validation.
     */
    Class<? extends Payload>[] payload() default {};
}