package com.nology.leaddecisions.application.usecase;

import com.nology.leaddecisions.domain.models.MarketingReport;

public interface AnalyticsService {

    /**
     * Gera o relatório completo de inteligência de marketing.
     * Orquestra a coleta de dados de múltiplas dimensões (Mercado, Origem),
     * aplica regras de threshold (RN04) e ordenação por score (RN07).
     *
     * @return O objeto rico contendo estatísticas globais e rankings detalhados.
     */
    MarketingReport generateFullReport();
}