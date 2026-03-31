package br.com.rnplanner.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class LancamentoManual {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate data;
    private String setor;

    private int tasks;
    private int ofertas;
    private int missoes;

    private int tasksCompra;
    private int tasksCerveja;
    private int tasksNab;
    private int tasksMkt;

    private int qtdPositivacao;

    private int qtdCompradores;
}