package com.nology.leaddecisions.etl.domain.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Tb_objetivo")
public class ObjectiveEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Descricao")
    private String description;

    @ManyToOne
    @JoinColumn(name = "Id_lead")
    private LeadEntity lead;
}
