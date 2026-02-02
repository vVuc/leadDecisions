package com.nology.leaddecisions.analytics.domain.models;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Representa o Relatório Consolidado de Inteligência de Marketing.
 * É um objeto de Domínio, pois agrupa resultados de regras de negócio.
 */
@Getter
@Builder
public class MarketingReport {
    private String reportId;
    private LocalDateTime generatedAt;
    private GlobalStats globalStats;
    private List<DimensionAnalysis> analyses;
    private Map<String, String> topInsights;

    @Getter
    @Builder
    public static class GlobalStats {
        private long totalLeads;
        private long totalSales;
        private double overallConversionRate;
    }

    @Getter
    @Builder
    public static class DimensionAnalysis {
        private String dimension;
        private String description;
        private List<AnalysisGroup> ranking;
    }
}