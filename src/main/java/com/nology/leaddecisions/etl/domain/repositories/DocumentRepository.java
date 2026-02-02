package com.nology.leaddecisions.etl.domain.repositories;

import com.nology.leaddecisions.etl.domain.models.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Interface de persistência para a entidade DocumentEntity.
 * * Atua como uma Porta de Saída (Output Port) que permite ao sistema armazenar
 * e recuperar os binários dos documentos processados. Estende JpaRepository para
 * herdar operações padrão de CRUD utilizando o mecanismo do Spring Data JPA.
 */
public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
}