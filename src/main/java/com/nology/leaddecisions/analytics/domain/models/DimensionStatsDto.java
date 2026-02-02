package com.nology.leaddecisions.analytics.domain.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO de Transferência Pura (Banco -> Java).
 * Representa os dados brutos antes da inteligência ser aplicada.
 */
@Getter
@AllArgsConstructor
public class DimensionStatsDto {
    private String categoryName;
    private Long totalLeads;
    private Long totalSold;
}