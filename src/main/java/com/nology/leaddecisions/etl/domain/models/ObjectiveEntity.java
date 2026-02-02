package com.nology.leaddecisions.etl.domain.models;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidade que representa os objetivos ou intenções declaradas de um Lead.
 * * Mapeia a tabela Tb_objetivo e registra as metas ou dores que o lead busca resolver.
 * É uma dimensão qualitativa utilizada para entender o propósito da conversão.
 */
@Data
@Entity
@Table(name = "Tb_objetivo")
public class ObjectiveEntity {

    /**
     * Identificador único do registro de objetivo.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Texto descritivo do objetivo extraído diretamente da planilha processada.
     */
    @Column(name = "Descricao")
    private String description;

    /**
     * Referência ao Lead proprietário deste objetivo.
     * Define a integridade referencial entre o objetivo específico e a entidade central LeadEntity.
     */
    @ManyToOne
    @JoinColumn(name = "Id_lead")
    private LeadEntity lead;
}