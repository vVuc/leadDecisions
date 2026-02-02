package com.nology.leaddecisions.analytics.domain.enums;

public enum AnalysisStatus {
    SUPERIOR_A_MEDIA,       // Volume OK + Conversão >= Média Global
    INFERIOR_A_MEDIA,       // Volume OK + Conversão < Média Global
    INCONCLUSIVO            // Volume insuficiente (Threshold)
}