package sgc.relatorio;

import lombok.*;
import org.openpdf.text.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.processo.service.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.io.*;
import java.util.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioFacade {
    private final ProcessoService processoService;
    private final SubprocessoConsultaService consultaService;
    private final ResponsavelUnidadeService responsavelService;
    private final MapaManutencaoService mapaManutencaoService;
    private final PdfFactory pdfFactory;

    @Transactional(readOnly = true)
    public List<RelatorioAndamentoDto> obterRelatorioAndamento(Long codProcesso) {
        List<Subprocesso> subprocessos = consultaService.listarEntidadesPorProcesso(codProcesso);
        Map<Long, UnidadeResponsavelDto> responsaveisPorUnidade = buscarResponsaveisPorUnidade(subprocessos);

        return subprocessos.stream().map(sp -> {
            Unidade unidade = sp.getUnidade();
            UnidadeResponsavelDto respDto = responsaveisPorUnidade.get(unidade.getCodigo());
            String responsavel = respDto != null ? respDto.titularNome() : "Não designado";

            java.time.LocalDateTime dataMovimentacao = sp.getDataLimiteEtapa1();

            return RelatorioAndamentoDto.builder()
                    .siglaUnidade(unidade.getSigla())
                    .nomeUnidade(unidade.getNome())
                    .situacaoAtual(sp.getSituacao().name())
                    .dataUltimaMovimentacao(dataMovimentacao)
                    .responsavel(responsavel)
                    .titular(responsavel)
                    .build();
        }).toList();
    }

    @Transactional(readOnly = true)
    public void gerarRelatorioAndamento(Long codProcesso, OutputStream outputStream) {
        Processo processo = processoService.buscarPorCodigo(codProcesso);
        List<Subprocesso> subprocessos = consultaService.listarEntidadesPorProcesso(codProcesso);
        Map<Long, UnidadeResponsavelDto> responsaveisPorUnidade = buscarResponsaveisPorUnidade(subprocessos);

        try (Document document = pdfFactory.createDocument()) {
            pdfFactory.createWriter(document, outputStream);
            document.open();

            document.add(new Paragraph("Relatório de Andamento - %s".formatted(processo.getDescricao())));
            document.add(new Paragraph(" ")); // Espaço

            for (Subprocesso sp : subprocessos) {
                Unidade unidade = sp.getUnidade();
                UnidadeResponsavelDto respDto = responsaveisPorUnidade.get(unidade.getCodigo());
                String responsavel = respDto != null ? respDto.titularNome() : "Não designado";

                String texto = String.format(
                        "Unidade: %s - %s%nSituação: %s%nResponsável: %s%n---------------------------",
                        unidade.getSigla(), unidade.getNome(), sp.getSituacao(), responsavel);
                document.add(new Paragraph(texto));
            }
        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }

    @Transactional(readOnly = true)
    public void gerarRelatorioMapas(Long codProcesso, Long codUnidade, OutputStream outputStream) {
        Processo processo = processoService.buscarPorCodigo(codProcesso);
        List<Subprocesso> subprocessos = consultaService.listarEntidadesPorProcesso(codProcesso);

        if (codUnidade != null) {
            subprocessos = subprocessos.stream()
                    .filter(sp -> sp.getUnidade().getCodigo().equals(codUnidade))
                    .toList();
        }

        try (Document document = pdfFactory.createDocument()) {
            pdfFactory.createWriter(document, outputStream);
            document.open();
            document.add(new Paragraph("Relatório de Mapas Vigentes - %s".formatted(processo.getDescricao())));
            document.add(new Paragraph(" "));

            for (Subprocesso sp : subprocessos) {
                Unidade unidade = sp.getUnidade();
                document.add(new Paragraph("Unidade: %s - %s".formatted(unidade.getSigla(), unidade.getNome())));
                document.add(new Paragraph(" "));

                List<Competencia> competencias = mapaManutencaoService.competenciasCodMapa(sp.getMapa().getCodigo());
                competencias.forEach(c -> {
                    document.add(new Paragraph("Competência: %s".formatted(c.getDescricao())));
                    c.getAtividades().forEach(a -> {
                        document.add(new Paragraph("  Atividade: %s".formatted(a.getDescricao())));
                        for (Conhecimento k : a.getConhecimentos()) {
                            document.add(new Paragraph("    Conhecimento: %s".formatted(k.getDescricao())));
                        }
                    });
                });
                document.add(new Paragraph("---------------------------"));
            }
        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }

    private Map<Long, UnidadeResponsavelDto> buscarResponsaveisPorUnidade(List<Subprocesso> subprocessos) {
        List<Long> codigosUnidade = subprocessos.stream()
                .map(Subprocesso::getUnidade)
                .map(Unidade::getCodigo)
                .distinct()
                .toList();

        if (codigosUnidade.isEmpty()) {
            return Map.of();
        }

        return new HashMap<>(responsavelService.buscarResponsaveisUnidades(codigosUnidade));
    }
}

