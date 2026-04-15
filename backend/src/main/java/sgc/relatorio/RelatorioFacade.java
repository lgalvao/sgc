package sgc.relatorio;

import lombok.*;
import org.openpdf.text.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import org.jspecify.annotations.*;
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
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioFacade {
    private static final DateTimeFormatter FORMATADOR_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ProcessoService processoService;
    private final SubprocessoConsultaService consultaService;
    private final ResponsavelUnidadeService responsavelService;
    private final MapaManutencaoService mapaManutencaoService;
    private final PdfFactory pdfFactory;

    @Transactional(readOnly = true)
    public List<RelatorioAndamentoDto> obterRelatorioAndamento(Long codProcesso) {
        List<Subprocesso> subprocessos = consultaService.listarEntidadesPorProcesso(codProcesso);
        Map<Long, UnidadeResponsavelDto> responsaveisPorUnidade = buscarResponsaveisPorUnidade(subprocessos);

        return subprocessos.stream()
                .map(sp -> criarRelatorioAndamentoDto(sp, responsaveisPorUnidade))
                .toList();
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
                RelatorioAndamentoDto relatorio = criarRelatorioAndamentoDto(sp, responsaveisPorUnidade);
                StringBuilder texto = new StringBuilder()
                        .append("Unidade: ").append(relatorio.siglaUnidade()).append(" - ").append(relatorio.nomeUnidade())
                        .append("%nSituação: ").append(relatorio.situacaoAtual())
                        .append("%nResponsável: ").append(relatorio.responsavel())
                        .append("%nData limite: ").append(formatarDataHora(relatorio.dataLimite()));

                if (relatorio.dataFimEtapa1() != null) {
                    texto.append("%nData de finalização da etapa 1: ")
                            .append(formatarDataHora(relatorio.dataFimEtapa1()));
                }
                if (relatorio.dataFimEtapa2() != null) {
                    texto.append("%nData de finalização da etapa 2: ")
                            .append(formatarDataHora(relatorio.dataFimEtapa2()));
                }

                texto.append("%n---------------------------");
                document.add(new Paragraph(texto.toString().formatted()));
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

    private RelatorioAndamentoDto criarRelatorioAndamentoDto(
            Subprocesso sp,
            Map<Long, UnidadeResponsavelDto> responsaveisPorUnidade
    ) {
        Unidade unidade = sp.getUnidade();
        UnidadeResponsavelDto respDto = responsaveisPorUnidade.get(unidade.getCodigo());
        String responsavel = respDto != null ? respDto.titularNome() : "Não designado";

        return RelatorioAndamentoDto.builder()
                .siglaUnidade(unidade.getSigla())
                .nomeUnidade(unidade.getNome())
                .situacaoAtual(sp.getSituacao().name())
                .dataLimite(obterDataLimite(sp))
                .dataFimEtapa1(sp.getDataFimEtapa1())
                .dataFimEtapa2(sp.getDataFimEtapa2())
                .dataUltimaMovimentacao(sp.getDataLimiteEtapa1())
                .responsavel(responsavel)
                .titular(responsavel)
                .build();
    }

    private LocalDateTime obterDataLimite(Subprocesso sp) {
        LocalDateTime dataLimiteEtapa1 = sp.getDataLimiteEtapa1();
        LocalDateTime dataLimiteEtapa2 = sp.getDataLimiteEtapa2();

        if (dataLimiteEtapa1 == null) {
            throw new IllegalStateException("Subprocesso %d com data limite da etapa 2 sem data limite da etapa 1"
                    .formatted(sp.getCodigo()));
        }
        return Objects.requireNonNullElse(dataLimiteEtapa2, dataLimiteEtapa1);
    }

    private String formatarDataHora(@Nullable LocalDateTime dataHora) {
        if (dataHora == null) {
            return "Não informado";
        }
        return dataHora.format(FORMATADOR_DATA_HORA);
    }
}

