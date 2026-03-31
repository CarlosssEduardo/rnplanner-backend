package br.com.rnplanner.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class Visita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pdv_id")
    private Pdv pdv;

    private LocalDate data;
    private String setor;
    private String pendenciaStatus;
    private String observacao;


    private int qtdTasks;
    private int qtdOfertas;
    private int qtdMissoes;


    private int qtdTasksCompra;
    private int qtdTasksCerveja;
    private int qtdTasksNab;
    private int qtdTasksMkt;

    private int qtdPositivacao;

    private boolean virouComprador;

    private boolean finalizada;
}