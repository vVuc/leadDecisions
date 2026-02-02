package com.nology.leaddecisions.etl.domain.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidade central do domínio que representa um Lead (potencial cliente).
 *
 * Atua como o nó agregador de todas as informações extraídas do processo de ETL.
 * Esta classe centraliza as dimensões de análise (Mercado, Localização, Origem, etc.)
 * e mantém a rastreabilidade com o documento de origem.
 */
@Data
@Entity
@Table(name = "Tb_lead")
public class LeadEntity {

    /**
     * Identificador único do lead.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Referência ao documento binário que originou este registro.
     * Estabelece a relação de muitos-para-um com Tb_documento.
     */
    @ManyToOne
    @JoinColumn(name = "Id_documento")
    private DocumentEntity document;

    /**
     * Data e hora em que o lead foi registrado originalmente na planilha.
     */
    @Column(name = "Data_cadastro")
    private LocalDateTime createdAt;

    /**
     * Indicador de conversão.
     * Define se o lead foi convertido em venda (true) ou não (false).
     */
    @Column(name = "Vendido")
    private Boolean sold;

    /**
     * Lista de objetivos associados ao lead.
     * O uso de orphanRemoval garante que objetivos órfãos sejam removidos do banco.
     */
    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ObjectiveEntity> objectives;

    /**
     * Lista de portes (tamanhos de empresa) associados ao lead.
     */
    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL)
    private List<SizeEntity> sizes;

    /**
     * Lista de segmentos de mercado associados ao lead.
     */
    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL)
    private List<MarketEntity> markets;

    /**
     * Lista de localizações geográficas associadas ao lead.
     */
    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL)
    private List<LocationEntity> locations;

    /**
     * Lista de origens (canais de aquisição) associadas ao lead.
     */
    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL)
    private List<SourceEntity> sources;
}