package com.nology.leaddecisions.etl.domain.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Tb_origem")
public class SourceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Nome")
    private String name;

    @Column(name = "Sub_origem")
    private String subSource;

    @ManyToOne
    @JoinColumn(name = "Id_lead")
    private LeadEntity lead;
}
