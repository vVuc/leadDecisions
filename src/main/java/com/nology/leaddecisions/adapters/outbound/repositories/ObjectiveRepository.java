package com.nology.leaddecisions.adapters.outbound.repositories;

import com.nology.leaddecisions.domain.models.ObjectiveEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório para a entidade ObjectiveEntity.
 *
 * Permite a persistência das intenções e dores dos leads. Atua na carga de dados
 * provenientes da aba 'OBJETIVO' do arquivo Excel.
 */
public interface ObjectiveRepository extends JpaRepository<ObjectiveEntity, Long> {
}