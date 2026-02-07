package com.nology.leaddecisions.application.service;

import com.nology.leaddecisions.domain.enums.AnalysisStatus;
import com.nology.leaddecisions.domain.ports.AnalyticsRepositoryPort;
import com.nology.leaddecisions.application.usecase.AnalyticsService;
import com.nology.leaddecisions.domain.models.AnalysisGroup;
import com.nology.leaddecisions.domain.models.DimensionStatsDto;
import com.nology.leaddecisions.domain.models.MarketingReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementação do serviço de domínio responsável pela geração de inteligência de dados.
 *
 * Esta classe orquestra o cálculo de métricas de marketing, aplicando regras estatísticas
 * para transformar contagens brutas (leads/vendas) em insights comparativos.
 * A estratégia central é o estabelecimento de uma "Régua Global" (Média de Conversão)
 * contra a qual todos os subgrupos (Mercados, Origens) são avaliados.
 */
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsRepositoryPort repository;

    /**
     * Limite mínimo de amostra (Leads) para que um grupo seja considerado estatisticamente relevante.
     * Grupos com volume inferior a este valor serão classificados como INCONCLUSIVO,
     * evitando distorções em análises de "cauda longa".
     */
    private static final int STATISTICAL_THRESHOLD = 10;

    /**
     * Orquestra a geração do relatório consolidado de marketing.
     *
     * O fluxo de execução segue uma abordagem "Top-Down":
     * 1. Calcula-se a performance macro (Global) para estabelecer a linha de base.
     * 2. Injeta-se essa linha de base no processamento das dimensões (Mercado, Origem).
     * 3. Sintetiza os resultados em um relatório hierárquico contendo estatísticas, rankings e insights.
     *
     * @return Relatório completo contendo o snapshot do momento da geração.
     */
    @Override
    public MarketingReport generateFullReport() {
        long totalLeads = repository.countTotalLeads();
        long totalSales = repository.countTotalSales();
        double globalConversion = calculateSafeConversion(totalLeads, totalSales);

        List<MarketingReport.DimensionAnalysis> analyses = new ArrayList<>();

        analyses.add(processDimension(
                "MERCADO",
                "Performance por segmento",
                repository.getStatsByMarket(),
                globalConversion
        ));

        analyses.add(processDimension(
                "ORIGEM",
                "Performance por canal",
                repository.getStatsBySource(),
                globalConversion
        ));

        return MarketingReport.builder()
                .reportId(UUID.randomUUID().toString())
                .generatedAt(LocalDateTime.now())
                .globalStats(MarketingReport.GlobalStats.builder()
                        .totalLeads(totalLeads)
                        .totalSales(totalSales)
                        .overallConversionRate(globalConversion)
                        .build())
                .analyses(analyses)
                .topInsights(generateInsights(analyses))
                .build();
    }

    /**
     * Processa uma dimensão específica transformando dados brutos em insights classificados.
     *
     * Responsabilidades:
     * 1. Converter DTOs de banco em Modelos Ricos (AnalysisGroup), aplicando o Threshold e a Média Global.
     * 2. Ordenar a lista resultante com base em performance.
     *
     * Critério de Ordenação (Ranking):
     * - Primário: Taxa de Conversão (Maior para Menor).
     * - Secundário: Volume de Vendas (Maior para Menor) em caso de empate na taxa.
     *
     * @param dimensionName O identificador da dimensão (ex: "MERCADO").
     * @param description Descrição legível para o relatório.
     * @param rawData Lista de dados brutos vindos do repositório.
     * @param globalAverage A média global calculada anteriormente para servir de comparativo.
     * @return Objeto contendo o ranking processado da dimensão.
     */
    private MarketingReport.DimensionAnalysis processDimension(
            String dimensionName,
            String description,
            List<DimensionStatsDto> rawData,
            double globalAverage) {

        List<AnalysisGroup> richGroups = rawData.stream()
                .map(dto -> new AnalysisGroup(
                        dto.getCategoryName(),
                        dto.getTotalLeads(),
                        dto.getTotalSold(),
                        STATISTICAL_THRESHOLD,
                        globalAverage
                ))
                .sorted((g1, g2) -> {
                    int rateComparison = Double.compare(g2.getConversionRate(), g1.getConversionRate());

                    if (rateComparison != 0) {
                        return rateComparison;
                    }

                    return Long.compare(g2.getTotalSold(), g1.getTotalSold());
                })
                .toList();

        return MarketingReport.DimensionAnalysis.builder()
                .dimension(dimensionName)
                .description(description)
                .ranking(richGroups)
                .build();
    }

    /**
     * Realiza o cálculo da taxa de conversão protegendo contra divisão por zero.
     *
     * @param total Denominador (Total de Leads).
     * @param sold Numerador (Total de Vendas).
     * @return Percentual de conversão (0.0 a 100.0). Retorna 0.0 se total for zero.
     */
    private double calculateSafeConversion(long total, long sold) {
        if (total == 0) return 0.0;
        return (double) sold / total * 100;
    }

    /**
     * Gera destaques textuais (Key Highlights) baseados nos líderes de cada ranking.
     *
     * Filtra apenas os grupos que possuem performance classificada como SUPERIOR_A_MEDIA
     * e seleciona o campeão de conversão de cada dimensão para compor o resumo executivo.
     *
     * @param analyses Lista de análises dimensionais já processadas.
     * @return Mapa de insights chave-valor (ex: "Melhor MERCADO" -> "Tecnologia (15%)").
     */
    private Map<String, String> generateInsights(List<MarketingReport.DimensionAnalysis> analyses) {
        Map<String, String> insights = new HashMap<>();

        analyses.forEach(analysis -> analysis.getRanking().stream()
                .filter(g -> g.getStatus() == AnalysisStatus.SUPERIOR_A_MEDIA)
                .max(Comparator.comparingDouble(AnalysisGroup::getConversionRate)) // Busca o maior valor
                .ifPresent(winner -> insights.put(
                        "Melhor " + analysis.getDimension(),
                        winner.getGroupName() + " (" + winner.getConversionRate() + "%)"
                )));
        return insights;
    }
}