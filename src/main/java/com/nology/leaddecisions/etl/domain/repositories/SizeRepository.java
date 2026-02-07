package com.nology.leaddecisions.etl.domain.repositories;

import com.nology.leaddecisions.etl.domain.models.SizeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório para a entidade SizeEntity.
 *
 * Responsável por persistir as faixas de porte empresarial (tamanho da empresa)
 * associadas a cada lead processado na aba 'PORTE'.
 */
public interface SizeRepository extends JpaRepository<SizeEntity, Long> {
}