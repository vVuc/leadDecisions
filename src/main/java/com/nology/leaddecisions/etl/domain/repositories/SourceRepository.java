package com.nology.leaddecisions.etl.domain.repositories;

import com.nology.leaddecisions.etl.domain.models.SourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório para a entidade SourceEntity.
 *
 * Gerencia a persistência dos canais de aquisição (origens) dos leads.
 * Interface utilizada para salvar os dados mapeados da aba 'ORIGEM'.
 */
public interface SourceRepository extends JpaRepository<SourceEntity, Long> {
}