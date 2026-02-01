package com.nology.leaddecisions.etl.domain.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Tb_porte")
public class SizeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Porte")
    private String sizeRange;

    @ManyToOne
    @JoinColumn(name = "Id_lead")
    private LeadEntity lead;
}
