package com.nology.leaddecisions.etl.domain.models;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidade que representa a dimensão de Segmento de Mercado associada a um Lead.
 * * Mapeia a tabela Tb_mercado e armazena os diferentes setores econômicos nos quais
 * o lead atua ou possui interesse. Esta informação é fundamental para o módulo
 * de Analytics realizar o agrupamento de performance por nicho.
 */
@Data
@Entity
@Table(name = "Tb_mercado")
public class MarketEntity {

    /**
     * Identificador único do registro de mercado.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome do segmento de mercado (ex: Tecnologia, Saúde, Educação) extraído do documento.
     */
    @Column(name = "Nome")
    private String name;

    /**
     * Referência ao Lead proprietário desta classificação de mercado.
     * Estabelece a chave estrangeira na tabela Tb_mercado para manter o vínculo com o registro principal.
     */
    @ManyToOne
    @JoinColumn(name = "Id_lead")
    private LeadEntity lead;
}