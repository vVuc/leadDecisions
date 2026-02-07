package com.nology.leaddecisions.domain.models;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidade responsável pela persistência do arquivo físico (blob) importado no sistema.
 *
 * Mapeia a tabela Tb_documento e armazena o binário original do Excel processado,
 * garantindo a rastreabilidade (auditoria) entre os dados extraídos e sua fonte original.
 */
@Data
@Entity
@Table(name = "Tb_documento")
public class DocumentEntity {

    /**
     * Chave primária autoincremental do documento.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Conteúdo binário (bytes) do arquivo Excel armazenado.
     *
     * A anotação @Lob (Large Object) instrui o banco a tratar este campo como um BLOB.
     * A estratégia FetchType.LAZY é utilizada para otimização de performance:
     * o conteúdo pesado do arquivo NÃO é carregado em memória quando se busca
     * apenas os metadados da entidade (como nome ou ID), sendo baixado apenas sob demanda.
     */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "Documento")
    private byte[] documentContent;

    /**
     * Nome original do arquivo recebido no upload (ex: leads-q1.xlsx).
     * Preservado para fins de identificação e logs.
     */
    @Column(name = "Documento_nome")
    private String documentName;

    /**
     * Tipo de conteúdo MIME (ex: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet).
     * Armazenado para garantir que o sistema saiba como manipular ou devolver o arquivo no futuro.
     */
    @Column(name = "Documento_tipo")
    private String documentContentType;
}