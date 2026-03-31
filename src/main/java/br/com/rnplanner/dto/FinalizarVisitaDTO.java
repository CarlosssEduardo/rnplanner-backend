package br.com.rnplanner.dto;

import lombok.Data;

@Data
public class FinalizarVisitaDTO {
    private String anotacao;
    private int qtdTasks;
    private int qtdOfertas;
    private int qtdMissoes;
    private int qtdTasksCompra;
    private int qtdTasksCerveja;
    private int qtdTasksNab;
    private int qtdTasksMkt;
    private boolean virouComprador;
    private int qtdPositivacao;
}