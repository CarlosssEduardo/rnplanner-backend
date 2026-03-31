package br.com.rnplanner.dto;

import lombok.Data;

@Data
public class LancamentoManualDTO {
    private String setor;
    private int ofertas;
    private int missoes;

    private int tasksCompra;
    private int tasksCerveja;
    private int tasksNab;
    private int tasksMkt;
    private boolean comprador;
    private int qtdPositivacao;
}