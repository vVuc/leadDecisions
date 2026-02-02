package com.nology.leaddecisions.analytics.domain.models;

import com.nology.leaddecisions.analytics.domain.enums.AnalysisStatus;
import lombok.Getter;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Representa um recorte analítico específico (um "Cluster" ou "Grupo"), contendo
 * suas métricas brutas e sua classificação de performance relativa.
 *
 * Esta classe atua como um 'Rich Domain Model' (Modelo Rico), pois não apenas transporta dados,
 * mas encapsula a regra de negócio de cálculo de conversão e decisão de status
 * no momento de sua construção.
 */
@Getter
public class AnalysisGroup {

    /**
     * O identificador do grupo (ex: nome do Mercado "Tecnologia" ou da Origem "Google Ads").
     */
    private final String groupName;

    /**
     * Volume total de leads processados neste grupo.
     */
    private final long totalLeads;

    /**
     * Volume total de vendas confirmadas neste grupo.
     */
    private final long totalSold;

    /**
     * Taxa de conversão percentual calculada (0.00 a 100.00).
     */
    private final double conversionRate;

    /**
     * Classificação da performance deste grupo em relação à média global,
     * considerando a relevância estatística.
     */
    private final AnalysisStatus status;

    /**
     * Constrói o grupo e executa imediatamente os cálculos de métricas e classificação.
     *
     * Ao instanciar este objeto, as regras de conversão e definição de status são aplicadas,
     * garantindo que o objeto nasça em um estado consistente e imutável.
     *
     * @param groupName Nome da categoria analisada.
     * @param totalLeads Quantidade total de entradas.
     * @param totalSold Quantidade total de conversões.
     * @param threshold O volume mínimo de leads necessário para que a análise seja considerada conclusiva.
     * @param globalAverage A taxa de conversão média global para servir de régua de comparação.
     */
    public AnalysisGroup(String groupName, long totalLeads, long totalSold, int threshold, double globalAverage) {
        this.groupName = groupName;
        this.totalLeads = totalLeads;
        this.totalSold = totalSold;

        this.conversionRate = calculateConversionRate();

        this.status = calculateStatus(threshold, globalAverage);
    }

    /**
     * Calcula a taxa de conversão com precisão financeira (2 casas decimais).
     *
     * Utiliza BigDecimal para evitar erros de ponto flutuante comuns em operações de divisão.
     * Retorna 0.0 caso não existam leads, evitando ArithmeticException (divisão por zero).
     *
     * @return Taxa percentual arredondada (Half-Up).
     */
    private double calculateConversionRate() {
        if (totalLeads == 0) return 0.0;
        return BigDecimal.valueOf(totalSold)
                .divide(BigDecimal.valueOf(totalLeads), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Determina o status de performance do grupo aplicando a "Régua de Negócio".
     *
     * A ordem de avaliação é crítica:
     * 1. Verifica se há volume suficiente (Threshold). Se não, é INCONCLUSIVO.
     * 2. Se houver volume, compara a taxa do grupo com a média global.
     *
     * @param threshold Limite mínimo de leads para relevância estatística.
     * @param globalAverage Média global para comparação.
     * @return O enum AnalysisStatus correspondente.
     */
    private AnalysisStatus calculateStatus(int threshold, double globalAverage) {
        if (totalLeads < threshold) {
            return AnalysisStatus.INCONCLUSIVO;
        }

        if (conversionRate >= globalAverage) {
            return AnalysisStatus.SUPERIOR_A_MEDIA;
        } else {
            return AnalysisStatus.INFERIOR_A_MEDIA;
        }
    }
}