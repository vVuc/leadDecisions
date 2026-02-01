package com.nology.leaddecisions.etl.domain.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "Tb_lead")
public class LeadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "Id_documento")
    private DocumentEntity document;

    @Column(name = "Data_cadastro")
    private LocalDateTime createdAt;

    @Column(name = "Vendido")
    private Boolean sold;

    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ObjectiveEntity> objectives;

    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL)
    private List<SizeEntity> sizes;

    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL)
    private List<MarketEntity> markets;

    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL)
    private List<LocationEntity> locations;

    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL)
    private List<SourceEntity> sources;
}
