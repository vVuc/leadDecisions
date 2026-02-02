package com.nology.leaddecisions.analytics.domain.ports;

import com.nology.leaddecisions.analytics.domain.models.DimensionStatsDto;
import java.util.List;

public interface AnalyticsRepositoryPort {

    // Métricas Globais (Header do Relatório)
    long countTotalLeads();
    long countTotalSales();

    // Métricas Dimensionais (Gráficos/Tabelas)
    // O retorno é genérico (DimensionStatsDto), o que muda é a query
    List<DimensionStatsDto> getStatsByMarket();
    List<DimensionStatsDto> getStatsBySource();

    // Expansível para futuro:
    // List<DimensionStatsDto> getStatsBySize();
    // List<DimensionStatsDto> getStatsByLocation();
}