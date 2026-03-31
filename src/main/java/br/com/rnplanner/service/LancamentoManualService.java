package br.com.rnplanner.service;

import br.com.rnplanner.dto.LancamentoManualDTO;
import br.com.rnplanner.model.LancamentoManual;
import br.com.rnplanner.repository.LancamentoManualRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class LancamentoManualService {

    private final LancamentoManualRepository repository;

    public LancamentoManual salvar(LancamentoManualDTO dto) {
        LancamentoManual l = new LancamentoManual();
        l.setData(LocalDate.now(ZoneId.of("America/Sao_Paulo")));
        l.setSetor(dto.getSetor());
        l.setOfertas(dto.getOfertas());
        l.setMissoes(dto.getMissoes());
        l.setQtdPositivacao(dto.getQtdPositivacao());

        l.setTasksCompra(dto.getTasksCompra());
        l.setTasksCerveja(dto.getTasksCerveja());
        l.setTasksNab(dto.getTasksNab());
        l.setTasksMkt(dto.getTasksMkt());

        l.setTasks(dto.getTasksCompra() + dto.getTasksCerveja() + dto.getTasksNab() + dto.getTasksMkt());
        l.setQtdCompradores(dto.isComprador() ? 1 : 0);

        return repository.save(l);
    }
}