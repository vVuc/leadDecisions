package com.nology.leaddecisions.adapters.outbound.repositories;

import com.nology.leaddecisions.domain.models.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório para a entidade LocationEntity.
 *
 * Responsável pela persistência dos dados geográficos vinculados aos leads.
 * Utilizado durante o processamento da aba 'LOCAL' para associar regiões aos leads existentes.
 */
public interface LocationRepository extends JpaRepository<LocationEntity, Long> {
}