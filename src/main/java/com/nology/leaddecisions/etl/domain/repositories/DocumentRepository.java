package com.nology.leaddecisions.etl.domain.repositories;

import com.nology.leaddecisions.etl.domain.models.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
}
