package sgc.relatorio.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.openpdf.text.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.processo.service.*;
import sgc.relatorio.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.io.*;
import java.util.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RelatorioFacade Test")
@SuppressWarnings("NullAway.Init")
class RelatorioFacadeTest {
    @Mock
    private ProcessoService processoService;
    @Mock
    private SubprocessoConsultaService consultaService;
    @Mock
    private ResponsavelUnidadeService responsavelService;
    @Mock
    private MapaManutencaoService mapaManutencaoService;
    @Mock
    private PdfFactory pdfFactory;
    @Mock
    private Document document;

    @InjectMocks
    private RelatorioFacade relatorioService;

    @Test
    @DisplayName("Deve obter relatório de andamento com dados corretos")
    void deveObterRelatorioAndamento() {
        Processo p = new Processo();
        p.setDescricao("Proc teste");

        Unidade u = new Unidade();
        u.setSigla("U1");
        u.setNome("Unidade 1");
        u.setCodigo(1L);

        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setDataLimiteEtapa1(java.time.LocalDateTime.of(2023, 10, 10, 10, 0));
        sp.setDataFimEtapa1(java.time.LocalDateTime.of(2023, 10, 11, 15, 30));
        sp.setDataLimiteEtapa2(java.time.LocalDateTime.of(2023, 10, 20, 18, 0));
        sp.setDataFimEtapa2(java.time.LocalDateTime.of(2023, 10, 21, 9, 45));

        when(consultaService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
        when(responsavelService.buscarResponsaveisUnidades(List.of(1L)))
                .thenReturn(Map.of(1L, UnidadeResponsavelDto.builder().titularNome("Resp").build()));

        List<RelatorioAndamentoDto> resultado = relatorioService.obterRelatorioAndamento(1L);

        assertThat(resultado).hasSize(1);
        RelatorioAndamentoDto dto = resultado.getFirst();
        assertThat(dto.siglaUnidade()).isEqualTo("U1");
        assertThat(dto.nomeUnidade()).isEqualTo("Unidade 1");
        assertThat(dto.situacaoAtual()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO.name());
        assertThat(dto.dataLimite()).isEqualTo(java.time.LocalDateTime.of(2023, 10, 20, 18, 0));
        assertThat(dto.dataFimEtapa1()).isEqualTo(java.time.LocalDateTime.of(2023, 10, 11, 15, 30));
        assertThat(dto.dataFimEtapa2()).isEqualTo(java.time.LocalDateTime.of(2023, 10, 21, 9, 45));
        assertThat(dto.dataUltimaMovimentacao()).isEqualTo(java.time.LocalDateTime.of(2023, 10, 10, 10, 0));
        assertThat(dto.responsavel()).isEqualTo("Resp");
        assertThat(dto.titular()).isEqualTo("Resp");
    }

    @Test
    @DisplayName("Deve usar data limite da etapa 1 quando nao houver etapa 2")
    void deveUsarDataLimiteEtapa1QuandoNaoHouverEtapa2() {
        Unidade unidade = new Unidade();
        unidade.setSigla("U1");
        unidade.setNome("Unidade 1");
        unidade.setCodigo(1L);

        Subprocesso sp = new Subprocesso();
        sp.setUnidade(unidade);
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setDataLimiteEtapa1(java.time.LocalDateTime.of(2023, 9, 10, 8, 0));

        when(consultaService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
        when(responsavelService.buscarResponsaveisUnidades(List.of(1L)))
                .thenReturn(Map.of(1L, UnidadeResponsavelDto.builder().titularNome("Resp").build()));

        List<RelatorioAndamentoDto> resultado = relatorioService.obterRelatorioAndamento(1L);

        assertThat(resultado.getFirst().dataLimite()).isEqualTo(java.time.LocalDateTime.of(2023, 9, 10, 8, 0));
        assertThat(resultado.getFirst().dataFimEtapa1()).isNull();
        assertThat(resultado.getFirst().dataFimEtapa2()).isNull();
    }

    @Test
    @DisplayName("Deve gerar relatório de andamento")
    void deveGerarRelatorioAndamento() throws DocumentException {
        when(pdfFactory.createDocument()).thenReturn(document);
        Processo p = new Processo();
        p.setDescricao("Proc teste");

        Unidade u = new Unidade();
        u.setSigla("U1");
        u.setNome("Unidade 1");
        u.setCodigo(1L);

        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setDataLimiteEtapa1(java.time.LocalDateTime.of(2023, 10, 10, 10, 0));
        sp.setDataFimEtapa1(java.time.LocalDateTime.of(2023, 10, 11, 15, 30));
        sp.setDataLimiteEtapa2(java.time.LocalDateTime.of(2023, 10, 20, 18, 0));
        sp.setDataFimEtapa2(java.time.LocalDateTime.of(2023, 10, 21, 9, 45));

        when(processoService.buscarPorCodigo(1L)).thenReturn(p);
        when(consultaService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
        when(responsavelService.buscarResponsaveisUnidades(List.of(1L)))
                .thenReturn(Map.of(1L, UnidadeResponsavelDto.builder().titularNome("Resp").build()));

        OutputStream out = new ByteArrayOutputStream();
        relatorioService.gerarRelatorioAndamento(1L, out);
        verify(document, atLeastOnce()).add(any());
        verify(document).add(argThat(element -> element instanceof Paragraph paragraph
                && paragraph.getContent().contains("Data limite: 20/10/2023 18:00")
                && paragraph.getContent().contains("Data de finalização da etapa 1: 11/10/2023 15:30")
                && paragraph.getContent().contains("Data de finalização da etapa 2: 21/10/2023 09:45")));
    }

    @Test
    @DisplayName("Deve buscar responsáveis em lote no relatório de andamento")
    void deveBuscarResponsaveisEmLoteNoRelatorioDeAndamento() {
        Unidade u1 = new Unidade();
        u1.setSigla("U1");
        u1.setNome("Unidade 1");
        u1.setCodigo(1L);

        Unidade u2 = new Unidade();
        u2.setSigla("U2");
        u2.setNome("Unidade 2");
        u2.setCodigo(2L);

        Subprocesso sp1 = new Subprocesso();
        sp1.setUnidade(u1);
        sp1.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        Subprocesso sp2 = new Subprocesso();
        sp2.setUnidade(u2);
        sp2.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        when(consultaService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp1, sp2));
        when(responsavelService.buscarResponsaveisUnidades(List.of(1L, 2L))).thenReturn(Map.of(
                1L, UnidadeResponsavelDto.builder().titularNome("Resp 1").build(),
                2L, UnidadeResponsavelDto.builder().titularNome("Resp 2").build()
        ));

        relatorioService.obterRelatorioAndamento(1L);

        verify(responsavelService).buscarResponsaveisUnidades(List.of(1L, 2L));
        verify(responsavelService, never()).buscarResponsavelUnidade(anyLong());
    }

    @Test
    @DisplayName("Deve gerar relatório de mapas completo")
    void deveGerarRelatorioMapasCompleto() throws DocumentException {
        when(pdfFactory.createDocument()).thenReturn(document);
        Processo p = new Processo();
        p.setDescricao("Proc teste");

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
        a.setConhecimentos(Set.of(k));
        c.setAtividades(Set.of(a));

        when(processoService.buscarPorCodigo(1L)).thenReturn(p);
        when(consultaService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
        when(mapaManutencaoService.competenciasCodMapa(10L)).thenReturn(List.of(c));

        OutputStream out = new ByteArrayOutputStream();
        relatorioService.gerarRelatorioMapas(1L, 1L, out);
        verify(document, atLeastOnce()).add(any());
    }

    @Test
    @DisplayName("Deve filtrar por unidade no relatório de mapas")
    void deveFiltrarPorUnidadeNoRelatorioMapas() throws DocumentException {
        when(pdfFactory.createDocument()).thenReturn(document);
        Processo p = new Processo();
        p.setDescricao("Proc teste");

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

        when(processoService.buscarPorCodigo(1L)).thenReturn(p);
        when(consultaService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp1, sp2));
        when(mapaManutencaoService.competenciasCodMapa(10L)).thenReturn(List.of());

        OutputStream out = new ByteArrayOutputStream();
        // Filtra pela unidade 1
        relatorioService.gerarRelatorioMapas(1L, 1L, out);
        verify(document, atLeastOnce()).add(any());
    }

    @Test
    @DisplayName("Deve processar competência sem atividades")
    void deveProcessarCompetenciaSemAtividades() throws DocumentException {
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
        c.setAtividades(Set.of()); // Atividades vazias

        when(processoService.buscarPorCodigo(1L)).thenReturn(p);
        when(consultaService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
        when(mapaManutencaoService.competenciasCodMapa(10L)).thenReturn(List.of(c));

        OutputStream out = new ByteArrayOutputStream();
        relatorioService.gerarRelatorioMapas(1L, 1L, out);
        verify(document, atLeastOnce()).add(any());
    }

    @Test
    @DisplayName("Deve processar atividade sem conhecimentos")
    void deveProcessarAtividadeSemConhecimentos() throws DocumentException {
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
        a.setConhecimentos(Set.of()); // Conhecimentos vazios
        c.setAtividades(Set.of(a));

        when(processoService.buscarPorCodigo(1L)).thenReturn(p);
        when(consultaService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
        when(mapaManutencaoService.competenciasCodMapa(10L)).thenReturn(List.of(c));

        OutputStream out = new ByteArrayOutputStream();
        relatorioService.gerarRelatorioMapas(1L, 1L, out);
        verify(document, atLeastOnce()).add(any());
    }

    @Test
    @DisplayName("Deve cobrir erro ao gerar PDF")
    void deveCobrirErroGerarPdf() throws DocumentException {
        when(processoService.buscarPorCodigo(1L)).thenReturn(new Processo());
        when(consultaService.listarEntidadesPorProcesso(1L)).thenReturn(List.of());
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
        when(processoService.buscarPorCodigo(1L)).thenReturn(new Processo());
        when(consultaService.listarEntidadesPorProcesso(1L)).thenReturn(List.of());
        when(pdfFactory.createDocument()).thenReturn(document);
        doThrow(new DocumentException("Simulado")).when(pdfFactory).createWriter(any(), any());

        OutputStream out = new ByteArrayOutputStream();
        assertThatThrownBy(() -> relatorioService.gerarRelatorioMapas(1L, 1L, out))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro ao gerar PDF");
    }

}
