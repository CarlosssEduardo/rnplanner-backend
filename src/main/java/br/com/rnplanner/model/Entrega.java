package br.com.rnplanner.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
public class Entrega {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long pdvId;
    private String nomePdv;
    private String motorista;
    private String status;

    private Integer visitOrder;
    private LocalDate dataRota;

    private LocalDateTime driverNotificationTime;
    private LocalDateTime arrivedAt;
    private LocalDateTime finishedAt;
}