package br.com.rnplanner.service;

import br.com.rnplanner.model.Entrega;
import br.com.rnplanner.repository.EntregaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Serviço responsável por integrar os dados logísticos do sistema de rastreio de frota.
 * Consome arquivos CSV padronizados para alimentar o status de entrega (Radar) de cada PDV em tempo real.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EntregaService {

    private final EntregaRepository entregaRepository;

    /**
     * Processa as linhas do CSV de roteirização substituindo a base logística atual.
     * O mapeamento utiliza índices fixos de array baseados no layout do sistema exportador.
     *
     * @param file Arquivo CSV contendo as rotas.
     * @return Número de entregas processadas e salvas com sucesso.
     * @throws Exception Caso ocorra erro de leitura no Stream.
     */
    @Transactional
    public int importarCsv(MultipartFile file) throws Exception {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            entregaRepository.deleteAllInBatch();
            List<Entrega> entregas = new ArrayList<>();
            String linha;
            boolean primeiraLinha = true;

            while ((linha = br.readLine()) != null) {
                if (primeiraLinha) { primeiraLinha = false; continue; }

                String[] colunas = linha.split(";");

                if (colunas.length > 21) {
                    try {
                        Entrega entrega = new Entrega();
                        entrega.setMotorista(colunas[3].trim());
                        entrega.setPdvId(Long.parseLong(colunas[5].replaceAll("[^0-9]", ""))); // Garante extração apenas numérica do ID
                        entrega.setNomePdv(colunas[6].trim());
                        entrega.setStatus(colunas[8].trim().toUpperCase());

                        if (!colunas[13].isEmpty()) {
                            entrega.setVisitOrder(Integer.parseInt(colunas[13].trim()));
                        }

                        entrega.setDataRota(parseLocalDate(colunas[1]));
                        entrega.setDriverNotificationTime(parseLocalDateTime(colunas[19]));
                        entrega.setArrivedAt(parseLocalDateTime(colunas[20]));
                        entrega.setFinishedAt(parseLocalDateTime(colunas[21]));

                        entregas.add(entrega);
                    } catch (Exception ignored) {
                        // Ignora de forma silenciosa linhas mal formatadas para não interromper a importação do lote completo.
                    }
                }
            }
            entregaRepository.saveAll(entregas);

            return entregas.size();
        }
    }

    /**
     * Recupera o status logístico do cliente e traduz o status técnico do sistema para uma mensagem amigável ao vendedor.
     *
     * @param pdvId Identificador único do Ponto de Venda.
     * @return Mapa contendo os detalhes traduzidos da entrega.
     */
    public Optional<Map<String, Object>> buscarRastreio(Long pdvId) {
        return entregaRepository.findFirstByPdvId(pdvId).map(entrega -> {

            Map<String, Object> response = new HashMap<>();
            response.put("pdvId", entrega.getPdvId());
            response.put("nomePdv", entrega.getNomePdv());
            response.put("motorista", entrega.getMotorista());
            response.put("status", entrega.getStatus());

            String horarioTexto = "Em rota de entrega";

            if ("CONCLUDED".equals(entrega.getStatus()) && entrega.getArrivedAt() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                horarioTexto = "Entregue às " + entrega.getArrivedAt().format(formatter);
            } else if ("RESCHEDULED".equals(entrega.getStatus())) {
                horarioTexto = "Carga Adiada";
            } else if (entrega.getVisitOrder() != null && entrega.getVisitOrder() > 0) {
                horarioTexto = "Chegada estimada: Parada Nº " + entrega.getVisitOrder() + " da fila";
            }

            response.put("horario", horarioTexto);

            return response;
        });
    }

    private LocalDate parseLocalDate(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        try { return LocalDate.parse(str.trim()); } catch (Exception e) { return null; }
    }

    private LocalDateTime parseLocalDateTime(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        try {
            // Converte timestamps baseados em UTC (Z) nativamente para o fuso horário comercial local (BRT).
            if (str.trim().endsWith("Z")) {
                Instant instanteUtc = Instant.parse(str.trim());
                return LocalDateTime.ofInstant(instanteUtc, ZoneId.of("America/Sao_Paulo"));
            }
            if (str.length() >= 19) {
                return LocalDateTime.parse(str.substring(0, 19));
            }
            return LocalDateTime.parse(str.trim());
        } catch (Exception e) {
            return null;
        }
    }
}