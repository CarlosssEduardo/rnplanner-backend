package br.com.rnplanner.service;

import br.com.rnplanner.model.Pdv;
import br.com.rnplanner.repository.PdvRepository;
import br.com.rnplanner.repository.VisitaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Serviço responsável por realizar o OCR (leitura de texto) no arquivo PDF do planificador diário.
 * Extrai as metas de produtividade da equipe e a lista atualizada de PDVs (Score 5, RKG e Task's).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ImportacaoPdfService {

    private final PdvRepository pdvRepository;
    private final VisitaRepository visitaRepository;

    /**
     * Processa o documento PDF, dividindo-o entre cabeçalho (Metas) e corpo (Tabela de Clientes).
     * Realiza a limpeza do banco para o setor identificado e insere a nova carteira do dia.
     *
     * @param file Arquivo PDF original recebido via upload do Front-End.
     * @throws Exception Caso o arquivo esteja corrompido ou o formato não seja reconhecido.
     */
    @Transactional
    public void importarPdf(MultipartFile file) throws Exception {
        List<Pdv> pdvsParaSalvar = new ArrayList<>();
        String setorEncontrado = null;

        // Fallback: Variáveis iniciam zeradas para impedir que a quebra do cabeçalho corrompa a importação da tabela.
        int desafioMissoes = 0, desafioOfertas = 0;
        int metaCompra = 0, metaCerveja = 0, metaNab = 0, metaMkt = 0, metaComprador = 0, metaPositivacao = 0;

        try (InputStream is = file.getInputStream();
             PDDocument document = PDDocument.load(is)) {

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String textoPdf = stripper.getText(document);

            // Isola o cabeçalho da tabela de clientes utilizando palavras-chave conhecidas do relatório original.
            int inicioTabela = textoPdf.toUpperCase().indexOf("RKG");
            if (inicioTabela == -1) inicioTabela = textoPdf.toUpperCase().indexOf("COD PDV");
            if (inicioTabela == -1) inicioTabela = textoPdf.length();

            String cabecalho = textoPdf.substring(0, inicioTabela);
            String tabelaTexto = textoPdf.substring(inicioTabela);

            // Remove datas do cabeçalho para evitar falsos positivos na captura do código do Setor (3 ou 4 dígitos).
            String cabecalhoSemDatas = cabecalho.replaceAll("(?i)\\d{2}/\\d{2}/\\d{4}", "");
            Matcher mSetor = Pattern.compile("\\b(\\d{3,4})\\b").matcher(cabecalhoSemDatas);
            if (mSetor.find()) {
                setorEncontrado = mSetor.group(1);
            }

            if (setorEncontrado == null) {
                throw new RuntimeException("ERRO: Não foi possível identificar o número do Setor no PDF.");
            }

            String cabecalhoLimpo = cabecalhoSemDatas.replaceAll("(?i)\\d{2,3}\\s*/\\s*\\d{1,2}\\s*/\\s*\\d{1,2}", "");

            List<Integer> numsCabecalho = new ArrayList<>();
            Matcher mNums = Pattern.compile("\\b\\d+\\b").matcher(cabecalhoLimpo);
            while (mNums.find()) {
                numsCabecalho.add(Integer.parseInt(mNums.group()));
            }

            // Mapeia dinamicamente os índices das metas dependendo da presença da coluna de Positivação.
            boolean temPositivacao = cabecalho.toLowerCase().contains("positiva");
            int expectedSize = temPositivacao ? 9 : 8;

            if (numsCabecalho.size() >= expectedSize) {
                int offset = numsCabecalho.size() - expectedSize;

                metaCompra     = numsCabecalho.get(offset);
                desafioMissoes = numsCabecalho.get(offset + 1);
                desafioOfertas = numsCabecalho.get(offset + 2);

                if (temPositivacao) {
                    metaPositivacao = numsCabecalho.get(offset + 3);
                    metaCerveja     = numsCabecalho.get(offset + 5);
                    metaNab         = numsCabecalho.get(offset + 6);
                    metaMkt         = numsCabecalho.get(offset + 7);
                    metaComprador   = numsCabecalho.get(offset + 8);
                } else {
                    metaCerveja     = numsCabecalho.get(offset + 4);
                    metaNab         = numsCabecalho.get(offset + 5);
                    metaMkt         = numsCabecalho.get(offset + 6);
                    metaComprador   = numsCabecalho.get(offset + 7);
                }
            }

            int desafioTasks = metaCompra + metaCerveja + metaNab + metaMkt;

            // Remove quebras de linha para processar a tabela de PDVs de forma contínua através de Regex.
            String tabelaLimpa = tabelaTexto.replaceAll("\\r", " ").replaceAll("\\n", " ");

            Pattern patternPdv = Pattern.compile("(?i)\\b(\\d{1,3})\\s+(\\d{3,8})\\s+(.+?)\\s+(\\d{1,3})\\s+(\\d{1,3})\\s+(\\d{1,3})\\s+(SIM|NÃO|N[ÃãA]O|NAO)\\s+(SIM|NÃO|N[ÃãA]O|NAO)\\b");
            Matcher matcher = patternPdv.matcher(tabelaLimpa);

            while (matcher.find()) {
                Pdv pdv = new Pdv();
                pdv.setRkg(Integer.parseInt(matcher.group(1).trim()));
                pdv.setCodigo(Integer.parseInt(matcher.group(2).trim()));
                pdv.setId(Long.parseLong(matcher.group(2).trim()));
                pdv.setNome(matcher.group(3).trim());

                pdv.setMetaTasks(Integer.parseInt(matcher.group(4).trim()));
                pdv.setMetaMissoes(Integer.parseInt(matcher.group(5).trim()));
                pdv.setMetaOfertas(Integer.parseInt(matcher.group(6).trim()));

                // Padroniza os retornos acentuados do OCR para evitar divergências na base de dados.
                pdv.setScore5(matcher.group(7).trim().toUpperCase().replace("Ã", "A"));
                pdv.setComprador(matcher.group(8).trim().toUpperCase().replace("Ã", "A"));
                pdv.setSetor(setorEncontrado);

                pdv.setDesafioTasks(desafioTasks);
                pdv.setDesafioMissoes(desafioMissoes);
                pdv.setDesafioOfertas(desafioOfertas);
                pdv.setMetaTasksCompra(metaCompra);
                pdv.setMetaTasksCerveja(metaCerveja);
                pdv.setMetaTasksNab(metaNab);
                pdv.setMetaTasksMkt(metaMkt);
                pdv.setMetaComprador(metaComprador);
                pdv.setMetaPositivacao(metaPositivacao);

                pdvsParaSalvar.add(pdv);
            }
        }

        if (!pdvsParaSalvar.isEmpty()) {
            List<String> setores = List.of(setorEncontrado);
            visitaRepository.deleteByPdvSetorIn(setores);
            pdvRepository.deleteBySetorIn(setores);
            pdvRepository.saveAll(pdvsParaSalvar);
            log.info("Sucesso! {} PDVs importados na base para o SETOR {}.", pdvsParaSalvar.size(), setorEncontrado);
        } else {
            throw new RuntimeException("Nenhum cliente válido encontrado na tabela do PDF.");
        }
    }
}