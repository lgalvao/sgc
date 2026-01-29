package sgc.relatorio.service;

import lombok.RequiredArgsConstructor;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Paragraph;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.dto.ResponsavelDto;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.service.ProcessoFacade;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.io.OutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioFacade {
    private final ProcessoFacade processoFacade;
    private final SubprocessoFacade subprocessoFacade;
    private final UnidadeFacade unidadeService;
    private final MapaManutencaoService mapaManutencaoService;
    private final PdfFactory pdfFactory;

    @Transactional(readOnly = true)
    public void gerarRelatorioAndamento(Long codProcesso, OutputStream outputStream) {
        Processo processo = processoFacade.buscarEntidadePorId(codProcesso);
        List<Subprocesso> subprocessos = subprocessoFacade.listarEntidadesPorProcesso(codProcesso);

        try (Document document = pdfFactory.createDocument()) {
            pdfFactory.createWriter(document, outputStream);
            document.open();

            document.add(new Paragraph("Relatório de Andamento - %s".formatted(processo.getDescricao())));
            document.add(new Paragraph(" ")); // Espaço

            for (Subprocesso sp : subprocessos) {
                Unidade unidade = sp.getUnidade();
                ResponsavelDto respDto = unidadeService.buscarResponsavelUnidade(unidade.getCodigo());
                String responsavel = respDto.titularNome();

                String texto = String.format(
                        "Unidade: %s - %s%nSituação: %s%nResponsável: %s%n---------------------------",
                        unidade.getSigla(), unidade.getNome(), sp.getSituacao(), responsavel);
                document.add(new Paragraph(texto));
            }
        } catch (DocumentException e) {
            throw new ErroRelatorio("Erro ao gerar PDF", e);
        }
    }

    @Transactional(readOnly = true)
    public void gerarRelatorioMapas(Long codProcesso, Long codUnidade, OutputStream outputStream) {
        Processo processo = processoFacade.buscarEntidadePorId(codProcesso);
        List<Subprocesso> subprocessos = subprocessoFacade.listarEntidadesPorProcesso(codProcesso);

        subprocessos = subprocessos.stream()
                .filter(sp -> {
                    Long codigoUnidade = sp.getUnidade().getCodigo();
                    return codigoUnidade.equals(codUnidade);
                })
                .toList();

        try (Document document = pdfFactory.createDocument()) {
            pdfFactory.createWriter(document, outputStream);
            document.open();
            document.add(new Paragraph("Relatório de Mapas - %s".formatted(processo.getDescricao())));
            document.add(new Paragraph(" "));

            for (Subprocesso sp : subprocessos) {
                Unidade unidade = sp.getUnidade();
                document.add(new Paragraph("Unidade: %s - %s".formatted(unidade.getSigla(), unidade.getNome())));
                document.add(new Paragraph(" "));

                List<Competencia> competencias = mapaManutencaoService.buscarCompetenciasPorCodMapa(sp.getMapa().getCodigo());
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
            throw new ErroRelatorio("Erro ao gerar PDF", e);
        }
    }
}