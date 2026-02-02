package com.nology.leaddecisions.etl.domain.models;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidade que representa a dimensão de localização geográfica de um Lead.
 *
 * Mapeia a tabela Tb_local e armazena informações espaciais ou regionais
 * que ajudam a segmentar a performance de vendas por região.
 */
@Data
@Entity
@Table(name = "Tb_local")
public class LocationEntity {

    /**
     * Identificador único do registro de localização.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome da localidade (cidade, estado ou região) extraído do documento.
     */
    @Column(name = "Nome")
    private String name;

    /**
     * Referência ao Lead proprietário desta informação de localização.
     * Define a chave estrangeira na tabela Tb_local para manter a integridade referencial.
     */
    @ManyToOne
    @JoinColumn(name = "Id_lead")
    private LeadEntity lead;
}