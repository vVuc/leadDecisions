package com.nology.leaddecisions.analytics.domain.enums;

/**
 * Define a classificação de performance de um agrupamento (ex: um Mercado específico ou uma Origem)
 * em relação à métrica global do relatório.
 *
 * Este enum é fundamental para a geração de insights, pois separa o que é performance real
 * (Positiva/Negativa) do que é ruído estatístico (Inconclusivo).
 */
public enum AnalysisStatus {

    /**
     * O grupo possui volume de dados estatisticamente relevante (acima do threshold)
     * e sua taxa de conversão é igual ou superior à média global.
     * Interpretação: É um destaque positivo ou "Carro-chefe".
     */
    SUPERIOR_A_MEDIA,

    /**
     * O grupo possui volume de dados estatisticamente relevante,
     * mas sua taxa de conversão está abaixo da média global.
     * Interpretação: É um ponto de atenção que está puxando a média para baixo.
     */
    INFERIOR_A_MEDIA,

    /**
     * O grupo não atingiu o volume mínimo (threshold) de leads para uma análise segura.
     * Interpretação: Não há dados suficientes para afirmar se é bom ou ruim.
     * Geralmente é ignorado na geração de "Top Insights".
     */
    INCONCLUSIVO
}