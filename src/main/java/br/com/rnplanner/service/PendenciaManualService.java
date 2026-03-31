package br.com.rnplanner.service;

import br.com.rnplanner.model.PendenciaManual;
import br.com.rnplanner.repository.PendenciaManualRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PendenciaManualService {


    private final PendenciaManualRepository repository;

    public PendenciaManual salvar(PendenciaManual pendencia) {

        pendencia.setData(LocalDate.now());
        pendencia.setStatus("PENDENTE");
        return repository.save(pendencia);
    }

    public void resolver(Long id) {

        repository.findById(id).ifPresent(p -> {
            p.setStatus("RESOLVIDO");
            repository.save(p);
        });
    }

    public void deletar(Long id) {

        repository.deleteById(id);
    }
}