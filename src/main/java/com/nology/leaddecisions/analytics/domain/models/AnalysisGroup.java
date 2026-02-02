package com.nology.leaddecisions.analytics.domain.models;

import com.nology.leaddecisions.analytics.domain.enums.AnalysisStatus;
import lombok.Getter;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
public class AnalysisGroup {

    private final String groupName;
    private final long totalLeads;
    private final long totalSold;
    private final double conversionRate;
    private final AnalysisStatus status;

    public AnalysisGroup(String groupName, long totalLeads, long totalSold, int threshold, double globalAverage) {
        this.groupName = groupName;
        this.totalLeads = totalLeads;
        this.totalSold = totalSold;

        this.conversionRate = calculateConversionRate();

        this.status = calculateStatus(threshold, globalAverage);
    }

    private double calculateConversionRate() {
        if (totalLeads == 0) return 0.0;
        return BigDecimal.valueOf(totalSold)
                .divide(BigDecimal.valueOf(totalLeads), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

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