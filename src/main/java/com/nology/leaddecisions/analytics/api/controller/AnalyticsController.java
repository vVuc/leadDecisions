package com.nology.leaddecisions.analytics.api.controller;

import com.nology.leaddecisions.analytics.domain.models.MarketingReport;
import com.nology.leaddecisions.analytics.domain.services.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Endpoint Mestre de Analytics.
     * Retorna o dashboard consolidado com KPIs globais, rankings por dimensão (Mercado, Origem)
     * e insights de negócio gerados pelo domínio.
     */
    @GetMapping("/report")
    public ResponseEntity<MarketingReport> getFullAnalyticsReport() {
        // O Controller delega a orquestração para o Service, que aplica as regras (RN03, RN04, RN07)
        MarketingReport report = analyticsService.generateFullReport();
        return ResponseEntity.ok(report);
    }
}