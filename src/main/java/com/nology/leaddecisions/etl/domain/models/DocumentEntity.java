package com.nology.leaddecisions.etl.domain.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Tb_documento")
public class DocumentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "Documento")
    private byte[] documentContent;

    @Column(name = "Documento_nome")
    private String documentName;

    @Column(name = "Documento_tipo")
    private String documentContentType;
}
