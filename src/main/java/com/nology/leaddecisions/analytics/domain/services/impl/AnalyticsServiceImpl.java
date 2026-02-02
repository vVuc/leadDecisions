package com.nology.leaddecisions.analytics.domain.services.impl;

import com.nology.leaddecisions.analytics.domain.enums.AnalysisStatus;
import com.nology.leaddecisions.analytics.domain.models.*;
import com.nology.leaddecisions.analytics.domain.ports.AnalyticsRepositoryPort;
import com.nology.leaddecisions.analytics.domain.services.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsRepositoryPort repository;
    private static final int STATISTICAL_THRESHOLD = 10;

    @Override
    public MarketingReport generateFullReport() {
        // 1. PRIMEIRO: Calcular a Média Global (A Régua)
        long totalLeads = repository.countTotalLeads();
        long totalSales = repository.countTotalSales();
        double globalConversion = calculateSafeConversion(totalLeads, totalSales);

        // 2. Processar dimensões passando a "Régua"
        List<MarketingReport.DimensionAnalysis> analyses = new ArrayList<>();

        analyses.add(processDimension(
                "MERCADO",
                "Performance por segmento",
                repository.getStatsByMarket(),
                globalConversion // Injeção de Contexto
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

    private double calculateSafeConversion(long total, long sold) {
        if (total == 0) return 0.0;
        return (double) sold / total * 100;
    }

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