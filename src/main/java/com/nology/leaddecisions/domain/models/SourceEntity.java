package com.nology.leaddecisions.domain.models;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidade que representa a dimensão de Origem (Canal de Aquisição) de um Lead.
 * * Mapeia a tabela Tb_origem e detalha de onde o lead veio (ex: Google, Instagram)
 * e, opcionalmente, o detalhamento dessa origem (ex: Ads, Orgânico).
 * É a base para a análise de ROI por canal no módulo de Analytics.
 */
@Data
@Entity
@Table(name = "Tb_origem")
public class SourceEntity {

    /**
     * Identificador único do registro de origem.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome do canal principal de origem (ex: Google, Facebook, Indicação).
     */
    @Column(name = "Nome")
    private String name;

    /**
     * Detalhamento opcional da origem (ex: Campanha específica, sub-rede).
     */
    @Column(name = "Sub_origem")
    private String subSource;

    /**
     * Referência ao Lead proprietário desta informação de origem.
     * Define a integridade referencial entre o canal de aquisição e o lead.
     */
    @ManyToOne
    @JoinColumn(name = "Id_lead")
    private LeadEntity lead;
}