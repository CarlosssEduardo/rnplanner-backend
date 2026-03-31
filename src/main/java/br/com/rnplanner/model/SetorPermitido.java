package br.com.rnplanner.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Setor_permitido")
public class SetorPermitido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String setor;
}