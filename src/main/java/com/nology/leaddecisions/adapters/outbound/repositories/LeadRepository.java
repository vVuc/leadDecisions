package com.nology.leaddecisions.adapters.outbound.repositories;

import com.nology.leaddecisions.domain.models.LeadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório para a entidade LeadEntity.
 *
 * Gerencia a persistência do agregado principal do sistema. É utilizado pelo serviço
 * de ETL para salvar os registros centrais após a extração da aba 'BASE' do documento.
 */
public interface LeadRepository extends JpaRepository<LeadEntity, Long> {
}