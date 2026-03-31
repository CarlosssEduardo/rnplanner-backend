package br.com.rnplanner.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Pdv {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUnico;

    private Long id;
    private Integer codigo;
    private String nome;
    private String setor;
    private String rota;


    private int metaTasks;
    private int metaMissoes;
    private int metaOfertas;


    private int desafioTasks;
    private int desafioMissoes;
    private int desafioOfertas;


    private int metaTasksCompra;
    private int metaTasksCerveja;
    private int metaTasksNab;
    private int metaTasksMkt;
    private int metaComprador;
    private int metaPositivacao;

    private String score5;
    private String comprador;
    private int rkg;
}