package br.com.rnplanner.controller;

import br.com.rnplanner.model.Pdv;
import br.com.rnplanner.repository.PdvRepository;
import br.com.rnplanner.repository.SetorPermitidoRepository;
import br.com.rnplanner.service.ImportacaoPdfService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controlador REST dedicado ao gerenciamento da base de clientes (PDVs).
 * Fornece endpoints para listagem, validação de acesso e extração automatizada via PDF.
 */
@RestController
@RequestMapping("/pdvs")
@CrossOrigin(origins = "*")
public class PdvController {

    private final PdvRepository pdvRepository;
    private final SetorPermitidoRepository setorPermitidoRepository;
    private final ImportacaoPdfService importacaoPdfService;

    public PdvController(PdvRepository pdvRepository,
                         SetorPermitidoRepository setorPermitidoRepository,
                         ImportacaoPdfService importacaoPdfService) {
        this.pdvRepository = pdvRepository;
        this.setorPermitidoRepository = setorPermitidoRepository;
        this.importacaoPdfService = importacaoPdfService;
    }

    /**
     * Recebe e processa o arquivo PDF (Planificador) enviado pelo usuário administrador.
     * O sistema extrai as metas, validações (Score 5) e clientes, repovoando o banco de dados do setor detectado.
     *
     * @param file Arquivo físico binário (multipart/form-data).
     * @return Mensagem de sucesso ou falha na extração dos dados.
     */
    @PostMapping("/upload-pdf")
    public ResponseEntity<String> uploadPdf(@RequestParam("file") MultipartFile file) {
        try {
            String nomeArquivo = file.getOriginalFilename();

            // Blindagem contra NPE: Primeiro verifica se é nulo, depois verifica a extensão.
            if (nomeArquivo == null || !nomeArquivo.toLowerCase().endsWith(".pdf")) {
                return ResponseEntity.badRequest().body("Erro: O arquivo enviado é inválido ou não é um PDF.");
            }

            importacaoPdfService.importarPdf(file);
            return ResponseEntity.ok("Planificador PDF importado e atualizado com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao importar PDF: " + e.getMessage());
        }
    }

    /**
     * Retorna a carteira completa de clientes atribuída a um setor.
     *
     * @param setor Setor logado.
     * @return Lista de entidades PDV.
     */
    @GetMapping("/setor/{setor}")
    public ResponseEntity<List<Pdv>> listarPdvsPorSetor(@PathVariable String setor) {
        return ResponseEntity.ok(pdvRepository.findBySetor(setor));
    }

    /**
     * Regra de segurança/catraca: Valida se o vendedor tem permissão para acessar o aplicativo.
     * O acesso é liberado se o setor possuir carteira atrelada ou estiver na tabela de Setores VIPs.
     *
     * @param setor Setor tentando realizar login.
     * @return Booleano indicando a liberação (true) ou bloqueio (false) do painel.
     */
    @GetMapping("/verificar/{setor}")
    public ResponseEntity<Boolean> verificarSetor(@PathVariable String setor) {
        boolean naListaVip = setorPermitidoRepository.existsBySetor(setor);
        boolean temPdvCadastrado = pdvRepository.existsBySetor(setor);
        boolean acessoLiberado = naListaVip || temPdvCadastrado;

        return ResponseEntity.ok(acessoLiberado);
    }
}