package com.nology.leaddecisions.etl.domain.models;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidade que representa a dimensão de Porte (tamanho da empresa) de um Lead.
 * * Mapeia a tabela Tb_porte e armazena faixas de tamanho (ex: Micro, Pequena, Grande)
 * extraídas do documento. Esta informação auxilia na análise de perfil de cliente
 * ideal e ticket médio.
 */
@Data
@Entity
@Table(name = "Tb_porte")
public class SizeEntity {

    /**
     * Identificador único do registro de porte.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Descrição da faixa de tamanho ou faturamento da empresa do lead.
     */
    @Column(name = "Porte")
    private String sizeRange;

    /**
     * Referência ao Lead proprietário desta classificação de porte.
     * Estabelece o vínculo de Muitos-para-Um necessário para a agregação de dados.
     */
    @ManyToOne
    @JoinColumn(name = "Id_lead")
    private LeadEntity lead;
}