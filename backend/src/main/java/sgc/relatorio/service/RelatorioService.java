package sgc.relatorio.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.service.CompetenciaService;
import sgc.processo.model.Processo;
import sgc.processo.service.ProcessoService;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.UsuarioService;

import java.io.OutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioService {
    private final ProcessoService processoService;
    private final SubprocessoService subprocessoService;
    private final UsuarioService usuarioService;
    private final CompetenciaService competenciaService;

    @Transactional(readOnly = true)
    public void gerarRelatorioAndamento(Long codProcesso, OutputStream outputStream) {
        Processo processo = processoService.buscarEntidadePorId(codProcesso);
        List<Subprocesso> subprocessos = subprocessoService.listarEntidadesPorProcesso(codProcesso);

        try (Document document = new Document()) {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            document.add(new Paragraph("Relatório de Andamento - " + processo.getDescricao()));
            document.add(new Paragraph(" ")); // Espaço

            for (Subprocesso sp : subprocessos) {
                Unidade unidade = sp.getUnidade();
                String responsavel = "Não definido";

                try {
                     var respDto = usuarioService.buscarResponsavelUnidade(unidade.getCodigo());
                     if (respDto.isPresent()) {
                         responsavel = respDto.get().getTitularNome();
                     }
                } catch (Exception e) {
                    // Ignora erro ao buscar responsável
                }

                String texto = String.format("Unidade: %s - %s\nSituação: %s\nResponsável: %s\n---------------------------",
                        unidade.getSigla(), unidade.getNome(), sp.getSituacao(), responsavel);
                document.add(new Paragraph(texto));
            }
        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }

    @Transactional(readOnly = true)
    public void gerarRelatorioMapas(Long codProcesso, Long codUnidade, OutputStream outputStream) {
        Processo processo = processoService.buscarEntidadePorId(codProcesso);
        List<Subprocesso> subprocessos = subprocessoService.listarEntidadesPorProcesso(codProcesso);

        if (codUnidade != null) {
            subprocessos = subprocessos.stream()
                    .filter(sp -> sp.getUnidade().getCodigo().equals(codUnidade))
                    .toList();
        }

        try (Document document = new Document()) {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            document.add(new Paragraph("Relatório de Mapas - " + processo.getDescricao()));
            document.add(new Paragraph(" "));

            for (Subprocesso sp : subprocessos) {
                if (sp.getMapa() == null) continue;

                Unidade unidade = sp.getUnidade();
                document.add(new Paragraph("Unidade: " + unidade.getSigla() + " - " + unidade.getNome()));
                document.add(new Paragraph(" "));

                List<Competencia> competencias = competenciaService.buscarPorMapa(sp.getMapa().getCodigo());

                for (Competencia c : competencias) {
                    document.add(new Paragraph("Competência: " + c.getDescricao()));

                    if (c.getAtividades() != null) {
                        for (Atividade a : c.getAtividades()) {
                            document.add(new Paragraph("  Atividade: " + a.getDescricao()));
                            if (a.getConhecimentos() != null) {
                                for (Conhecimento k : a.getConhecimentos()) {
                                    document.add(new Paragraph("    Conhecimento: " + k.getDescricao()));
                                }
                            }
                        }
                    }
                }
                document.add(new Paragraph("---------------------------"));
            }
        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }
}
