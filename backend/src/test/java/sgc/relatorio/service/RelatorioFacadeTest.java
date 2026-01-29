package sgc.relatorio.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.dto.UnidadeResponsavelDto;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.service.ProcessoFacade;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("RelatorioFacade Test")
class RelatorioFacadeTest {

    @Mock
    private ProcessoFacade processoFacade;
    @Mock
    private SubprocessoFacade subprocessoFacade;
    @Mock
    private UnidadeFacade unidadeService;
    @Mock
    private MapaManutencaoService mapaManutencaoService;

    @Mock
    private PdfFactory pdfFactory;

    @Mock
    private Document document;

    @InjectMocks
    private RelatorioFacade relatorioService;

    @Test
    @DisplayName("Deve gerar relatório de andamento")
    void deveGerarRelatorioAndamento() {
        when(pdfFactory.createDocument()).thenReturn(document);
        Processo p = new Processo();
        p.setDescricao("Proc Teste");

        Unidade u = new Unidade();
        u.setSigla("U1");
        u.setNome("Unidade 1");
        u.setCodigo(1L);

        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(p);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
        when(unidadeService.buscarResponsavelUnidade(1L)).thenReturn(UnidadeResponsavelDto.builder().titularNome("Resp").build());

        OutputStream out = new ByteArrayOutputStream();
        relatorioService.gerarRelatorioAndamento(1L, out);
        verify(document, atLeastOnce()).add(any());
    }


    @Test
    @DisplayName("Deve gerar relatório de mapas completo")
    void deveGerarRelatorioMapasCompleto() {
        when(pdfFactory.createDocument()).thenReturn(document);
        Processo p = new Processo();
        p.setDescricao("Proc Teste");

        Unidade u = new Unidade();
        u.setSigla("U1");
        u.setNome("Unidade 1");
        u.setCodigo(1L);

        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);

        Competencia c = new Competencia();
        c.setDescricao("Comp 1");

        Atividade a = new Atividade();
        a.setDescricao("Ativ 1");

        Conhecimento k = new Conhecimento();
        k.setDescricao("Conh 1");
        a.setConhecimentos(List.of(k));
        c.setAtividades(java.util.Set.of(a));

        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(p);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
        when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(c));

        OutputStream out = new ByteArrayOutputStream();
        relatorioService.gerarRelatorioMapas(1L, 1L, out);
        verify(document, atLeastOnce()).add(any());
    }

    @Test
    @DisplayName("Deve filtrar por unidade no relatório de mapas")
    void deveFiltrarPorUnidadeNoRelatorioMapas() {
        when(pdfFactory.createDocument()).thenReturn(document);
        Processo p = new Processo();
        p.setDescricao("Proc Teste");

        Unidade u1 = new Unidade();
        u1.setCodigo(1L);
        u1.setSigla("U1");
        Unidade u2 = new Unidade();
        u2.setCodigo(2L);
        u2.setSigla("U2");

        Subprocesso sp1 = new Subprocesso();
        sp1.setUnidade(u1);
        sp1.setMapa(new Mapa());
        sp1.getMapa().setCodigo(10L);
        Subprocesso sp2 = new Subprocesso();
        sp2.setUnidade(u2);
        sp2.setMapa(new Mapa());
        sp2.getMapa().setCodigo(20L);

        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(p);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp1, sp2));
        when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of());

        OutputStream out = new ByteArrayOutputStream();
        // Filtra pela unidade 1
        relatorioService.gerarRelatorioMapas(1L, 1L, out);
        verify(document, atLeastOnce()).add(any());
    }


    @Test
    @DisplayName("Deve processar competência sem atividades")
    void deveProcessarCompetenciaSemAtividades() {
        when(pdfFactory.createDocument()).thenReturn(document);
        Processo p = new Processo();
        Unidade u = new Unidade();
        u.setSigla("U1");
        u.setNome("U1");
        u.setCodigo(1L);
        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);

        Competencia c = new Competencia();
        c.setDescricao("Comp 1");
        c.setAtividades(java.util.Set.of()); // Atividades vazias

        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(p);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
        when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(c));

        OutputStream out = new ByteArrayOutputStream();
        relatorioService.gerarRelatorioMapas(1L, 1L, out);
        verify(document, atLeastOnce()).add(any());
    }

    @Test
    @DisplayName("Deve processar atividade sem conhecimentos")
    void deveProcessarAtividadeSemConhecimentos() {
        when(pdfFactory.createDocument()).thenReturn(document);
        Processo p = new Processo();
        Unidade u = new Unidade();
        u.setSigla("U1");
        u.setNome("U1");
        u.setCodigo(1L);
        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);

        Competencia c = new Competencia();
        c.setDescricao("Comp 1");
        Atividade a = new Atividade();
        a.setDescricao("Ativ 1");
        a.setConhecimentos(List.of()); // Conhecimentos vazios
        c.setAtividades(java.util.Set.of(a));

        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(p);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
        when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(c));

        OutputStream out = new ByteArrayOutputStream();
        relatorioService.gerarRelatorioMapas(1L, 1L, out);
        verify(document, atLeastOnce()).add(any());
    }


    @Test
    @DisplayName("Deve cobrir erro ao gerar PDF")
    void deveCobrirErroGerarPdf() throws DocumentException {
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(new Processo());
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of());
        when(pdfFactory.createDocument()).thenReturn(document);
        doThrow(new DocumentException("Simulado")).when(pdfFactory).createWriter(any(), any());

        OutputStream out = new ByteArrayOutputStream();
        assertThatThrownBy(() -> relatorioService.gerarRelatorioAndamento(1L, out))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro ao gerar PDF");
    }

    @Test
    @DisplayName("Deve cobrir erro ao gerar PDF de mapas")
    void deveCobrirErroGerarPdfMapas() throws DocumentException {
        when(processoFacade.buscarEntidadePorId(1L)).thenReturn(new Processo());
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of());
        when(pdfFactory.createDocument()).thenReturn(document);
        doThrow(new DocumentException("Simulado")).when(pdfFactory).createWriter(any(), any());

        OutputStream out = new ByteArrayOutputStream();
        assertThatThrownBy(() -> relatorioService.gerarRelatorioMapas(1L, 1L, out))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro ao gerar PDF");
    }


}