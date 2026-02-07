package com.nology.leaddecisions.etl.domain.repositories;

import com.nology.leaddecisions.etl.domain.models.MarketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório para a entidade MarketEntity.
 *
 * Gerencia o armazenamento dos segmentos de mercado. Essencial para a
 * rastreabilidade de nichos de atuação extraídos da aba 'MERCADO'.
 */
public interface MarketRepository extends JpaRepository<MarketEntity, Long> {
}