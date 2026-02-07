package com.nology.leaddecisions.analytics.infraestructure.persistence;

import com.nology.leaddecisions.analytics.domain.models.DimensionStatsDto;
import com.nology.leaddecisions.analytics.domain.ports.AnalyticsRepositoryPort;
import com.nology.leaddecisions.etl.domain.models.LeadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Implementação do adaptador de persistência para o módulo de Analytics.
 *
 * Atua como a implementação concreta da porta de saída (AnalyticsRepositoryPort),
 * utilizando o Spring Data JPA para realizar consultas agregadas diretamente no banco de dados.
 * O foco desta classe é a eficiência na extração de dados, delegando o processamento
 * de entidades JPA para projeções DTO sempre que possível.
 */
@Repository
public interface JpaAnalyticsAdapter extends JpaRepository<LeadEntity, Long>, AnalyticsRepositoryPort {

    /**
     * Realiza a contagem total de registros de leads presentes na base de dados.
     * Esta operação é otimizada para retornar apenas o número escalar.
     *
     * @return Total de leads registrados.
     */
    @Override
    @Query("SELECT COUNT(l) FROM LeadEntity l")
    long countTotalLeads();

    /**
     * Realiza a contagem de leads que possuem o indicador de venda positivo.
     * Filtra diretamente na query para evitar carregamento de objetos em memória.
     *
     * @return Total de vendas confirmadas.
     */
    @Override
    @Query("SELECT COUNT(l) FROM LeadEntity l WHERE l.sold = true")
    long countTotalSales();


    /**
     * Consulta as estatísticas agrupadas por Mercado.
     *
     * Utiliza uma expressão de construtor (Constructor Expression) na consulta JPQL
     * para instanciar e preencher os DTOs diretamente com os resultados da agregação
     * (contagem total e soma de vendas) feita pelo banco de dados.
     *
     * @return Lista de estatísticas por mercado.
     */
    @Override
    @Query("""
       SELECT new com.nology.leaddecisions.analytics.domain.models.DimensionStatsDto(
           m.name,
           COUNT(l),
           SUM(CASE WHEN l.sold = true THEN 1 ELSE 0 END)
       )
       FROM LeadEntity l
       JOIN l.markets m
       GROUP BY m.name
       """)
    List<DimensionStatsDto> getStatsByMarket();

    /**
     * Consulta as estatísticas agrupadas por Origem (Source).
     *
     * Executa a junção com a entidade de Origem e agrupa os resultados para
     * fornecer os volumes de leads e vendas por canal de aquisição.
     *
     * @return Lista de estatísticas por origem.
     */
    @Override
    @Query("""
       SELECT new com.nology.leaddecisions.analytics.domain.models.DimensionStatsDto(
           s.name,
           COUNT(l),
           SUM(CASE WHEN l.sold = true THEN 1 ELSE 0 END)
       )
       FROM LeadEntity l
       JOIN l.sources s
       GROUP BY s.name
       """)
    List<DimensionStatsDto> getStatsBySource();
}