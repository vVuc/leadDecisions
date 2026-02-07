package com.nology.leaddecisions.domain.ports;

import com.nology.leaddecisions.domain.models.DimensionStatsDto;
import java.util.List;

/**
 * Define o contrato (Porta de Saída / Secondary Port) para acesso a dados analíticos.
 *
 * Na Arquitetura Hexagonal, esta interface permite que o Domínio solicite dados
 * agregados sem conhecer a tecnologia de persistência subjacente (JPA, SQL Nativo, Mongo, etc).
 * O objetivo principal é fornecer métricas brutas para que o Domain Service aplique a inteligência.
 */
public interface AnalyticsRepositoryPort {

    /**
     * Recupera o volume total absoluto de leads registrados na base.
     * Utilizado como denominador no cálculo da taxa de conversão global.
     *
     * @return Quantidade total de leads.
     */
    long countTotalLeads();

    /**
     * Recupera o volume total absoluto de vendas (conversões) confirmadas.
     * Utilizado como numerador no cálculo da taxa de conversão global.
     *
     * @return Quantidade total de vendas (onde vendido = true).
     */
    long countTotalSales();

    /**
     * Recupera as estatísticas de leads e vendas agrupadas por Segmento de Mercado.
     *
     * Os dados retornados neste DTO são agnósticos de regra de negócio (apenas contagem pura).
     * A classificação (Bom/Ruim) será feita posteriormente pelo Domain Service.
     *
     * @return Lista de DTOs contendo Categoria (Mercado), Total Leads e Total Vendas.
     */
    List<DimensionStatsDto> getStatsByMarket();

    /**
     * Recupera as estatísticas de leads e vendas agrupadas por Canal de Origem (Source).
     *
     * Permite analisar a eficiência de cada canal de aquisição (ex: Google, Facebook, Orgânico).
     *
     * @return Lista de DTOs contendo Categoria (Origem), Total Leads e Total Vendas.
     */
    List<DimensionStatsDto> getStatsBySource();

}