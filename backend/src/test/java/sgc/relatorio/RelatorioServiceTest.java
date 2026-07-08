package sgc.relatorio;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.openpdf.text.*;
import sgc.comum.erros.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RelatorioService Test")
@SuppressWarnings("NullAway.Init")
class RelatorioServiceTest {
    @Mock
    private ProcessoService processoService;
    @Mock
    private SubprocessoConsultaService consultaService;
    @Mock
    private ResponsavelUnidadeService responsavelService;
    @Mock
    private MapaManutencaoService mapaManutencaoService;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private UnidadeHierarquiaService unidadeHierarquiaService;
    @Mock
    private LocalizacaoSubprocessoService localizacaoSubprocessoService;
    @Mock
    private UsuarioAplicacaoService usuarioAplicacaoService;
    @Mock
    private PdfFactory pdfFactory;
    @Mock
    private Document document;

    @InjectMocks
    private RelatorioService relatorioService;

    private void mockContextoAdmin() {
        when(usuarioAplicacaoService.contextoAutenticado()).thenReturn(new ContextoUsuarioAutenticado(
                "111111111111",
                999L,
                Perfil.ADMIN
        ));
    }

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
        sp.setCodigo(100L);
        sp.setUnidade(u);
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setDataLimiteEtapa1(java.time.LocalDateTime.of(2023, 10, 10, 10, 0));
        sp.setDataFimEtapa1(java.time.LocalDateTime.of(2023, 10, 11, 15, 30));
        sp.setDataLimiteEtapa2(java.time.LocalDateTime.of(2023, 10, 20, 18, 0));
        sp.setDataFimEtapa2(java.time.LocalDateTime.of(2023, 10, 21, 9, 45));

        when(consultaService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
        when(responsavelService.buscarResponsaveisUnidades(List.of(1L)))
                .thenReturn(Map.of(1L, UnidadeResponsavelDto.builder().titularNome("Resp").build()));
        when(localizacaoSubprocessoService.obterLocalizacoesAtuais(any())).thenReturn(Map.of(100L, u));

        List<RelatorioAndamentoDto> resultado = relatorioService.obterRelatorioAndamento(1L);

        assertThat(resultado).hasSize(1);
        RelatorioAndamentoDto dto = resultado.getFirst();
        assertThat(dto.siglaUnidade()).isEqualTo("U1");
        assertThat(dto.nomeUnidade()).isEqualTo("Unidade 1");
        assertThat(dto.situacaoAtual()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO.getDescricao());
        assertThat(dto.dataLimiteEtapa1()).isEqualTo(java.time.LocalDateTime.of(2023, 10, 10, 10, 0));
        assertThat(dto.dataLimiteEtapa2()).isEqualTo(java.time.LocalDateTime.of(2023, 10, 20, 18, 0));
        assertThat(dto.dataFimEtapa1()).isEqualTo(java.time.LocalDateTime.of(2023, 10, 11, 15, 30));
        assertThat(dto.dataFimEtapa2()).isEqualTo(java.time.LocalDateTime.of(2023, 10, 21, 9, 45));
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
        sp.setCodigo(100L);
        sp.setUnidade(unidade);
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setDataLimiteEtapa1(java.time.LocalDateTime.of(2023, 9, 10, 8, 0));

        when(consultaService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
        when(responsavelService.buscarResponsaveisUnidades(List.of(1L)))
                .thenReturn(Map.of(1L, UnidadeResponsavelDto.builder().titularNome("Resp").build()));
        when(localizacaoSubprocessoService.obterLocalizacoesAtuais(any())).thenReturn(Map.of(100L, unidade));

        List<RelatorioAndamentoDto> resultado = relatorioService.obterRelatorioAndamento(1L);

        assertThat(resultado.getFirst().dataLimiteEtapa1()).isEqualTo(java.time.LocalDateTime.of(2023, 9, 10, 8, 0));
        assertThat(resultado.getFirst().dataFimEtapa1()).isNull();
        assertThat(resultado.getFirst().dataFimEtapa2()).isNull();
    }

    @Test
    @DisplayName("Deve gerar relatório de andamento")
    void deveGerarRelatorioAndamento() throws DocumentException {
        when(pdfFactory.createDocument()).thenReturn(document);
        Processo p = new Processo();
        p.setDescricao("Proc teste");
        p.setTipo(TipoProcesso.MAPEAMENTO);
        p.setDataLimite(java.time.LocalDateTime.now().plusDays(30));

        Unidade u = new Unidade();
        u.setSigla("U1");
        u.setNome("Unidade 1");
        u.setCodigo(1L);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
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
        when(localizacaoSubprocessoService.obterLocalizacoesAtuais(any())).thenReturn(Map.of(100L, u));

        OutputStream out = new ByteArrayOutputStream();
        relatorioService.gerarRelatorioAndamento(1L, out);
        verify(document, atLeastOnce()).add(any());
        // No novo layout não usamos mais tabela simples de 6 colunas, mas sim cartões
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
        sp1.setCodigo(1L);
        sp1.setUnidade(u1);
        sp1.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp1.setDataLimiteEtapa1(java.time.LocalDateTime.of(2023, 9, 10, 8, 0));

        Subprocesso sp2 = new Subprocesso();
        sp2.setCodigo(2L);
        sp2.setUnidade(u2);
        sp2.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp2.setDataLimiteEtapa1(java.time.LocalDateTime.of(2023, 9, 11, 8, 0));

        when(consultaService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp1, sp2));
        when(responsavelService.buscarResponsaveisUnidades(List.of(1L, 2L))).thenReturn(Map.of(
                1L, UnidadeResponsavelDto.builder().titularNome("Resp 1").build(),
                2L, UnidadeResponsavelDto.builder().titularNome("Resp 2").build()
        ));
        when(localizacaoSubprocessoService.obterLocalizacoesAtuais(any())).thenReturn(Map.of(
                1L, u1,
                2L, u2
        ));

        relatorioService.obterRelatorioAndamento(1L);

        verify(responsavelService).buscarResponsaveisUnidades(List.of(1L, 2L));
        verify(responsavelService, never()).buscarResponsavelUnidade(anyLong());
    }

    @Test
    @DisplayName("Deve gerar relatorio cobrindo todas as variacoes de nomes de responsaveis e localizacao")
    void deveCobrirVariacoesNomesResponsaveisELocalizacao() {
        Unidade u0 = new Unidade();
        u0.setSigla("U0");
        u0.setNome("Unidade Raiz");
        u0.setCodigo(0L);

        Unidade u1 = new Unidade();
        u1.setSigla("U1");
        u1.setNome("Unidade 1");
        u1.setCodigo(1L);
        // Sem unidade superior, titular nulo

        Unidade u2 = new Unidade();
        u2.setSigla("U2");
        u2.setNome("Unidade 2");
        u2.setCodigo(2L);
        u2.setUnidadeSuperior(u0);
        // titular null, sem respDto (fallback)

        Unidade u3 = new Unidade();
        u3.setSigla("U3");
        u3.setNome("Unidade 3");
        u3.setCodigo(3L);
        u3.setUnidadeSuperior(u0);
        // titular ok, substituto ok

        Subprocesso sp1 = new Subprocesso();
        sp1.setCodigo(1L);
        sp1.setUnidade(u1);
        sp1.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
        sp1.setDataLimiteEtapa1(java.time.LocalDateTime.of(2023, 9, 10, 8, 0));
        sp1.setDataLimiteEtapa2(java.time.LocalDateTime.of(2023, 10, 10, 8, 0));

        Subprocesso sp2 = new Subprocesso();
        sp2.setCodigo(2L);
        sp2.setUnidade(u2);
        sp2.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp2.setDataLimiteEtapa1(java.time.LocalDateTime.of(2023, 9, 10, 8, 0));
        sp2.setDataFimEtapa1(java.time.LocalDateTime.of(2023, 9, 11, 8, 0));
        sp2.setDataLimiteEtapa2(java.time.LocalDateTime.of(2023, 9, 10, 8, 0));

        Subprocesso sp3 = new Subprocesso();
        sp3.setCodigo(3L);
        sp3.setUnidade(u3);
        sp3.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp3.setDataLimiteEtapa1(java.time.LocalDateTime.of(2023, 9, 10, 8, 0));
        sp3.setDataFimEtapa1(java.time.LocalDateTime.of(2023, 9, 11, 8, 0));
        sp3.setDataLimiteEtapa2(java.time.LocalDateTime.of(2023, 10, 10, 8, 0));
        sp3.setDataFimEtapa2(java.time.LocalDateTime.of(2023, 10, 12, 8, 0));

        when(consultaService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp1, sp2, sp3));

        // resp1: dto != null mas titularNome = null e substituto = null
        UnidadeResponsavelDto resp1 = UnidadeResponsavelDto.builder().titularNome(null).substitutoNome(null).build();
        // resp3: titular ok, substituto ok
        UnidadeResponsavelDto resp3 = UnidadeResponsavelDto.builder().titularNome("Titular 3").substitutoNome("Substituto 3").build();

        when(responsavelService.buscarResponsaveisUnidades(List.of(1L, 2L, 3L))).thenReturn(Map.of(
                1L, resp1,
                // sem resp para u2 -> map nao tem entry 2L
                3L, resp3
        ));
        when(localizacaoSubprocessoService.obterLocalizacoesAtuais(any())).thenReturn(Map.of(
                1L, u1,
                2L, u2,
                3L, u3
        ));

        List<RelatorioAndamentoDto> resultado = relatorioService.obterRelatorioAndamento(1L);

        assertThat(resultado).hasSize(3);

        // Valida sp1
        assertThat(resultado.getFirst().titular()).isEqualTo("Não designado");
        assertThat(resultado.getFirst().responsavel()).isEqualTo("Não designado");
        assertThat(resultado.get(0).localizacao()).isEqualTo("U1");
        assertThat(resultado.get(0).dataUltimaMovimentacao()).isEqualTo(java.time.LocalDateTime.of(2023, 9, 10, 8, 0));

        // Valida sp2
        assertThat(resultado.get(1).titular()).isEqualTo("Não designado");
        assertThat(resultado.get(1).responsavel()).isEqualTo("Não designado");
        assertThat(resultado.get(1).localizacao()).isEqualTo("U2");
        assertThat(resultado.get(1).dataUltimaMovimentacao()).isEqualTo(java.time.LocalDateTime.of(2023, 9, 11, 8, 0));

        // Valida sp3
        assertThat(resultado.get(2).titular()).isEqualTo("Titular 3");
        assertThat(resultado.get(2).responsavel()).isEqualTo("Substituto 3 (Substituição)");
        assertThat(resultado.get(2).localizacao()).isEqualTo("U3");
        assertThat(resultado.get(2).dataUltimaMovimentacao()).isEqualTo(java.time.LocalDateTime.of(2023, 10, 12, 8, 0));
    }

    @Test
    @DisplayName("Deve gerar relatório de mapas completo")
    void deveGerarRelatorioMapasCompleto() throws DocumentException {
        mockContextoAdmin();
        when(pdfFactory.createDocument()).thenReturn(document);
        Unidade u = new Unidade();
        u.setSigla("U1");
        u.setNome("Unidade 1");
        u.setCodigo(1L);

        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);
        sp.getMapa().setSubprocesso(sp);

        Competencia c = new Competencia();
        c.setDescricao("Comp 1");

        Atividade a = new Atividade();
        a.setDescricao("Ativ 1");

        Conhecimento k = new Conhecimento();
        k.setDescricao("Conh 1");
        a.setConhecimentos(Set.of(k));
        c.setAtividades(Set.of(a));

        UnidadeMapa unidadeMapa = UnidadeMapa.builder()
                .unidadeCodigo(1L)
                .mapaVigente(sp.getMapa())
                .build();

        when(unidadeService.buscarMapasPorUnidades(List.of(1L))).thenReturn(List.of(unidadeMapa));
        when(mapaManutencaoService.competenciasCodMapa(10L)).thenReturn(List.of(c));

        OutputStream out = new ByteArrayOutputStream();
        relatorioService.gerarRelatorioMapas(List.of(1L), out);
        verify(document, atLeastOnce()).add(any());
    }

    @Test
    @DisplayName("Deve obter relatório de mapas somente para unidades com mapa vigente")
    void deveObterRelatorioMapasSomenteParaUnidadesComMapaVigente() {
        mockContextoAdmin();
        Unidade u1 = new Unidade();
        u1.setCodigo(1L);
        u1.setSigla("U1");
        u1.setNome("Unidade 1");

        Unidade u2 = new Unidade();
        u2.setCodigo(2L);
        u2.setSigla("U2");
        u2.setNome("Unidade 2");

        Subprocesso sp1 = new Subprocesso();
        sp1.setUnidade(u1);
        sp1.setMapa(new Mapa());
        sp1.getMapa().setCodigo(10L);
        sp1.getMapa().setSubprocesso(sp1);

        Subprocesso sp2 = new Subprocesso();
        sp2.setUnidade(u2);
        sp2.setMapa(new Mapa());
        sp2.getMapa().setCodigo(20L);
        sp2.getMapa().setSubprocesso(sp2);

        UnidadeMapa mapa1 = UnidadeMapa.builder()
                .unidadeCodigo(1L)
                .mapaVigente(sp1.getMapa())
                .build();
        UnidadeMapa mapa2 = UnidadeMapa.builder()
                .unidadeCodigo(2L)
                .mapaVigente(sp2.getMapa())
                .build();

        when(unidadeService.buscarMapasPorUnidades(List.of(1L, 2L))).thenReturn(List.of(mapa1, mapa2));
        when(mapaManutencaoService.competenciasCodMapa(10L)).thenReturn(List.of());
        when(mapaManutencaoService.competenciasCodMapa(20L)).thenReturn(List.of());

        List<RelatorioMapaDto> resultado = relatorioService.obterRelatorioMapas(List.of(1L, 2L));

        assertThat(resultado).hasSize(2);
        assertThat(resultado.getFirst().codigoUnidade()).isEqualTo(1L);
        assertThat(resultado.get(1).codigoUnidade()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Deve normalizar códigos duplicados antes de buscar mapas")
    void deveNormalizarCodigosDuplicadosAntesDeBuscarMapas() {
        mockContextoAdmin();
        when(unidadeService.buscarMapasPorUnidades(List.of(1L, 2L))).thenReturn(List.of());

        relatorioService.obterRelatorioMapas(List.of(1L, 1L, 2L));

        verify(unidadeService).buscarMapasPorUnidades(List.of(1L, 2L));
    }

    @Test
    @DisplayName("Deve ignorar unidades sem mapa vigente ao montar relatório de mapas")
    void deveIgnorarUnidadesSemMapaVigenteAoMontarRelatorioDeMapas() {
        mockContextoAdmin();
        Unidade unidade = new Unidade();
        unidade.setCodigo(2L);
        unidade.setSigla("U2");
        unidade.setNome("Unidade 2");

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setUnidade(unidade);
        subprocesso.setMapa(new Mapa());
        subprocesso.getMapa().setCodigo(20L);
        subprocesso.getMapa().setSubprocesso(subprocesso);

        UnidadeMapa mapaValido = UnidadeMapa.builder()
                .unidadeCodigo(2L)
                .mapaVigente(subprocesso.getMapa())
                .build();

        when(unidadeService.buscarMapasPorUnidades(List.of(1L, 2L))).thenReturn(List.of(mapaValido));
        when(mapaManutencaoService.competenciasCodMapa(20L)).thenReturn(List.of());

        List<RelatorioMapaDto> resultado = relatorioService.obterRelatorioMapas(List.of(1L, 2L));

        assertThat(resultado).hasSize(1);
        assertThat(resultado)
                .singleElement()
                .extracting(RelatorioMapaDto::codigoUnidade, RelatorioMapaDto::siglaUnidade, RelatorioMapaDto::totalCompetencias)
                .containsExactly(2L, "U2", 0);
    }

    @Test
    @DisplayName("Deve ignorar seleção vazia no relatório de mapas")
    void deveIgnorarSelecaoVaziaNoRelatorioMapas() throws DocumentException {
        when(pdfFactory.createDocument()).thenReturn(document);

        OutputStream out = new ByteArrayOutputStream();
        relatorioService.gerarRelatorioMapas(List.of(), out);
        verify(document, atLeastOnce()).add(any());
        verify(unidadeService, never()).buscarMapasPorUnidades(anyList());
    }

    @Test
    @DisplayName("Deve processar competência sem atividades")
    void deveProcessarCompetenciaSemAtividades() throws DocumentException {
        mockContextoAdmin();
        when(pdfFactory.createDocument()).thenReturn(document);
        Unidade u = new Unidade();
        u.setSigla("U1");
        u.setNome("U1");
        u.setCodigo(1L);
        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);
        sp.getMapa().setSubprocesso(sp);

        Competencia c = new Competencia();
        c.setDescricao("Comp 1");
        c.setAtividades(Set.of()); // Atividades vazias

        UnidadeMapa unidadeMapa = UnidadeMapa.builder()
                .unidadeCodigo(1L)
                .mapaVigente(sp.getMapa())
                .build();

        when(unidadeService.buscarMapasPorUnidades(List.of(1L))).thenReturn(List.of(unidadeMapa));
        when(mapaManutencaoService.competenciasCodMapa(10L)).thenReturn(List.of(c));

        OutputStream out = new ByteArrayOutputStream();
        relatorioService.gerarRelatorioMapas(List.of(1L), out);
        verify(document, atLeastOnce()).add(any());
    }

    @Test
    @DisplayName("Deve processar atividade sem conhecimentos")
    void deveProcessarAtividadeSemConhecimentos() throws DocumentException {
        mockContextoAdmin();
        when(pdfFactory.createDocument()).thenReturn(document);
        Unidade u = new Unidade();
        u.setSigla("U1");
        u.setNome("U1");
        u.setCodigo(1L);
        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);
        sp.getMapa().setSubprocesso(sp);

        Competencia c = new Competencia();
        c.setDescricao("Comp 1");
        Atividade a = new Atividade();
        a.setDescricao("Ativ 1");
        a.setConhecimentos(Set.of()); // Conhecimentos vazios
        c.setAtividades(Set.of(a));

        UnidadeMapa unidadeMapa = UnidadeMapa.builder()
                .unidadeCodigo(1L)
                .mapaVigente(sp.getMapa())
                .build();

        when(unidadeService.buscarMapasPorUnidades(List.of(1L))).thenReturn(List.of(unidadeMapa));
        when(mapaManutencaoService.competenciasCodMapa(10L)).thenReturn(List.of(c));

        OutputStream out = new ByteArrayOutputStream();
        relatorioService.gerarRelatorioMapas(List.of(1L), out);
        verify(document, atLeastOnce()).add(any());
    }

    @Test
    @DisplayName("Deve cobrir erro ao gerar PDF")
    void deveCobrirErroGerarPdf() throws DocumentException {
        when(processoService.buscarPorCodigo(1L)).thenReturn(new Processo());
        when(consultaService.listarEntidadesPorProcesso(1L)).thenReturn(List.of());
        when(localizacaoSubprocessoService.obterLocalizacoesAtuais(any())).thenReturn(Map.of());
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
        mockContextoAdmin();
        when(pdfFactory.createDocument()).thenReturn(document);
        when(unidadeService.buscarMapasPorUnidades(List.of(1L))).thenReturn(List.of());
        doThrow(new DocumentException("Simulado")).when(pdfFactory).createWriter(any(), any());

        OutputStream out = new ByteArrayOutputStream();
        assertThatThrownBy(() -> relatorioService.gerarRelatorioMapas(List.of(1L), out))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro ao gerar PDF");
    }

    @Test
    @DisplayName("Deve cobrir erro ao gerar relatório de unidades sem mapas vigentes")
    void deveCobrirErroAoGerarRelatorioDeUnidadesSemMapasVigentes() throws Exception {
        when(pdfFactory.createDocument()).thenReturn(document);
        when(unidadeService.buscarCodigosUnidadesSemMapaVigente()).thenReturn(List.of());
        doThrow(new DocumentException("Simulado")).when(pdfFactory).createWriter(any(), any());

        OutputStream out = new ByteArrayOutputStream();
        assertThatThrownBy(() -> relatorioService.gerarRelatorioUnidadesSemMapasVigentes(out))
                .isInstanceOf(sgc.comum.erros.ErroInconsistenciaInterna.class)
                .hasMessageContaining("Erro ao gerar PDF");
    }

    @Test
    @DisplayName("Deve obter relatório do mapa vigente da unidade a partir do vínculo vigente")
    void deveObterRelatorioMapaVigenteDaUnidade() {
        mockContextoAdmin();
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSigla("U1");
        unidade.setNome("Unidade 1");

        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);

        Competencia competencia = new Competencia();
        competencia.setCodigo(100L);
        competencia.setDescricao("Comp 1");
        competencia.setAtividades(Set.of());

        when(unidadeService.buscarPorCodigo(1L)).thenReturn(unidade);
        when(unidadeService.buscarMapaVigente(1L)).thenReturn(Optional.of(mapa));
        when(mapaManutencaoService.competenciasCodMapa(10L)).thenReturn(List.of(competencia));

        RelatorioMapaDto resultado = relatorioService.obterRelatorioMapaVigenteUnidade(1L);

        assertThat(resultado.codigoUnidade()).isEqualTo(1L);
        assertThat(resultado.siglaUnidade()).isEqualTo("U1");
        assertThat(resultado.totalCompetencias()).isEqualTo(1);
        verify(unidadeService).buscarMapaVigente(1L);
        verify(mapaManutencaoService, never()).mapaVigenteUnidade(anyLong());
    }

    private void mockContextoUsuario(Perfil perfil, Long unidadeAtivaCodigo) {
        when(usuarioAplicacaoService.contextoAutenticado()).thenReturn(new ContextoUsuarioAutenticado(
                "111111111111",
                unidadeAtivaCodigo,
                perfil
        ));
    }

    @Test
    @DisplayName("Deve permitir obter relatório de mapas para gestor dentro de sua hierarquia")
    void devePermitirObterRelatorioMapasParaGestorDentroDeSuaHierarquia() {
        mockContextoUsuario(Perfil.GESTOR, 1L);
        when(unidadeHierarquiaService.buscarIdsDescendentes(1L)).thenReturn(List.of(2L));

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
        sp1.getMapa().setSubprocesso(sp1);

        Subprocesso sp2 = new Subprocesso();
        sp2.setUnidade(u2);
        sp2.setMapa(new Mapa());
        sp2.getMapa().setCodigo(20L);
        sp2.getMapa().setSubprocesso(sp2);

        UnidadeMapa mapa1 = UnidadeMapa.builder().unidadeCodigo(1L).mapaVigente(sp1.getMapa()).build();
        UnidadeMapa mapa2 = UnidadeMapa.builder().unidadeCodigo(2L).mapaVigente(sp2.getMapa()).build();

        when(unidadeService.buscarMapasPorUnidades(List.of(1L, 2L))).thenReturn(List.of(mapa1, mapa2));

        List<RelatorioMapaDto> resultado = relatorioService.obterRelatorioMapas(List.of(1L, 2L));

        assertThat(resultado).hasSize(2);
        verify(unidadeHierarquiaService).buscarIdsDescendentes(1L);
    }

    @Test
    @DisplayName("Deve lançar ErroAcessoNegado para gestor ao consultar unidade fora de sua hierarquia")
    void deveLancarErroAcessoNegadoParaGestorAoConsultarUnidadeForaDeSuaHierarquia() {
        mockContextoUsuario(Perfil.GESTOR, 1L);
        when(unidadeHierarquiaService.buscarIdsDescendentes(1L)).thenReturn(List.of(2L));

        assertThatThrownBy(() -> relatorioService.obterRelatorioMapas(List.of(1L, 3L)))
                .isInstanceOf(ErroAcessoNegado.class)
                .hasMessageContaining("Usuário não possui permissão para gerar relatório para uma ou mais unidades selecionadas.");
    }

    @Test
    @DisplayName("Deve lançar ErroAcessoNegado para outro perfil que não gestor ou admin ao obter relatório de mapas")
    void deveLancarErroAcessoNegadoParaOutroPerfilQueNaoGestorOuAdminAoObterRelatorioMapas() {
        mockContextoUsuario(Perfil.CHEFE, 1L);

        assertThatThrownBy(() -> relatorioService.obterRelatorioMapas(List.of(1L)))
                .isInstanceOf(ErroAcessoNegado.class)
                .hasMessageContaining("Usuário não possui permissão para gerar relatório de mapas vigentes.");
    }

    @Test
    @DisplayName("Deve permitir obter relatório de mapa vigente para gestor dentro de sua hierarquia")
    void devePermitirObterRelatorioMapaVigenteParaGestorDentroDeSuaHierarquia() {
        mockContextoUsuario(Perfil.GESTOR, 1L);
        when(unidadeHierarquiaService.buscarIdsDescendentes(1L)).thenReturn(List.of(2L));

        Unidade u2 = new Unidade();
        u2.setCodigo(2L);
        u2.setSigla("U2");
        Mapa mapa = new Mapa();
        mapa.setCodigo(20L);

        when(unidadeService.buscarPorCodigo(2L)).thenReturn(u2);
        when(unidadeService.buscarMapaVigente(2L)).thenReturn(Optional.of(mapa));

        RelatorioMapaDto resultado = relatorioService.obterRelatorioMapaVigenteUnidade(2L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.codigoUnidade()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Deve lançar ErroAcessoNegado para gestor ao obter relatório de mapa de unidade fora de sua hierarquia")
    void deveLancarErroAcessoNegadoParaGestorAoObterRelatorioMapaDeUnidadeForaDeSuaHierarquia() {
        mockContextoUsuario(Perfil.GESTOR, 1L);
        when(unidadeHierarquiaService.buscarIdsDescendentes(1L)).thenReturn(List.of(2L));

        assertThatThrownBy(() -> relatorioService.obterRelatorioMapaVigenteUnidade(3L))
                .isInstanceOf(ErroAcessoNegado.class)
                .hasMessageContaining("Usuário não possui permissão para gerar relatório de mapa vigente desta unidade.");
    }

    @Test
    @DisplayName("Deve permitir obter relatório de mapa vigente para chefe de sua própria unidade")
    void devePermitirObterRelatorioMapaVigenteParaChefeDeSuaPropriaUnidade() {
        mockContextoUsuario(Perfil.CHEFE, 1L);

        Unidade u1 = new Unidade();
        u1.setCodigo(1L);
        u1.setSigla("U1");
        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);

        when(unidadeService.buscarPorCodigo(1L)).thenReturn(u1);
        when(unidadeService.buscarMapaVigente(1L)).thenReturn(Optional.of(mapa));

        RelatorioMapaDto resultado = relatorioService.obterRelatorioMapaVigenteUnidade(1L);

        assertThat(resultado).isNotNull();
    }

    @Test
    @DisplayName("Deve lançar ErroAcessoNegado para chefe ao obter relatório de mapa vigente de outra unidade")
    void deveLancarErroAcessoNegadoParaChefeAoObterRelatorioMapaVigenteDeOutraUnidade() {
        mockContextoUsuario(Perfil.CHEFE, 1L);

        assertThatThrownBy(() -> relatorioService.obterRelatorioMapaVigenteUnidade(2L))
                .isInstanceOf(ErroAcessoNegado.class)
                .hasMessageContaining("Usuário não possui permissão para gerar relatório de mapa vigente desta unidade.");
    }

    @Test
    @DisplayName("Deve lançar ErroAcessoNegado para servidor ao obter relatório de mapa vigente")
    void deveLancarErroAcessoNegadoParaServidorAoObterRelatorioMapaVigente() {
        mockContextoUsuario(Perfil.SERVIDOR, 1L);

        assertThatThrownBy(() -> relatorioService.obterRelatorioMapaVigenteUnidade(1L))
                .isInstanceOf(ErroAcessoNegado.class)
                .hasMessageContaining("Usuário não possui permissão para gerar relatório de mapa vigente desta unidade.");
    }

    @Test
    @DisplayName("Deve gerar relatório de unidades sem mapas vigentes quando não há unidades pendentes")
    void deveGerarRelatorioUnidadesSemMapasVigentesQuandoNaoHaUnidadesPendentes() throws DocumentException {
        when(pdfFactory.createDocument()).thenReturn(document);
        when(unidadeService.buscarCodigosUnidadesSemMapaVigente()).thenReturn(List.of());
        when(unidadeHierarquiaService.buscarArvoreHierarquica()).thenReturn(List.of());

        OutputStream out = new ByteArrayOutputStream();
        relatorioService.gerarRelatorioUnidadesSemMapasVigentes(out);

        verify(document, atLeastOnce()).add(any());
    }

    @Test
    @DisplayName("Deve obter relatório em JSON de unidades sem mapas vigentes")
    void deveObterRelatorioUnidadesSemMapasVigentes() {
        UnidadeDto raiz = new UnidadeDto();
        raiz.setCodigo(1L);
        raiz.setSigla("RAIZ");
        raiz.setNome("Unidade Raiz");

        UnidadeDto secretaria = new UnidadeDto();
        secretaria.setCodigo(10L);
        secretaria.setSigla("SEC");
        secretaria.setNome("SECRETARIA X");
        secretaria.setTipo("SECRETARIA");

        UnidadeDto zonaEleitoral = new UnidadeDto();
        zonaEleitoral.setCodigo(11L);
        zonaEleitoral.setSigla("ZE 1");
        zonaEleitoral.setNome("ZONA ELEITORAL 1");
        zonaEleitoral.setTipo("ZONA ELEITORAL");
        secretaria.setSubunidades(List.of(zonaEleitoral));
        raiz.setSubunidades(List.of(secretaria));

        when(unidadeService.buscarCodigosUnidadesSemMapaVigente()).thenReturn(List.of(11L));
        when(unidadeHierarquiaService.buscarArvoreHierarquica()).thenReturn(List.of(raiz));

        List<RelatorioUnidadeSemMapaVigenteDto> resultado = relatorioService.obterRelatorioUnidadesSemMapasVigentes();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().codigo()).isEqualTo(10L);
        assertThat(resultado.getFirst().sigla()).isEqualTo("SEC");
        assertThat(resultado.getFirst().filhas()).hasSize(1);
        assertThat(resultado.getFirst().filhas().getFirst().sigla()).isEqualTo("ZONAS ELEITORAIS");
        assertThat(resultado.getFirst().filhas().getFirst().filhas()).hasSize(1);
        assertThat(resultado.getFirst().filhas().getFirst().filhas().getFirst().codigo()).isEqualTo(11L);
    }

    @Test
    @DisplayName("Deve gerar relatório de unidades sem mapas vigentes com ordenações e agrupamentos complexos")
    void deveGerarRelatorioUnidadesSemMapasVigentesComOrdenacoesEAgrupamentosComplexos() throws DocumentException {
        when(pdfFactory.createDocument()).thenReturn(document);

        // 1. Configurar códigos das unidades sem mapa vigente
        when(unidadeService.buscarCodigosUnidadesSemMapaVigente()).thenReturn(List.of(11L, 12L, 13L, 14L, 16L));

        // 2. Montar árvore hierárquica contendo unidades, subunidades, secretarias e zonas eleitorais
        UnidadeDto raiz = new UnidadeDto();
        raiz.setCodigo(1L);
        raiz.setSigla("RAIZ");
        raiz.setNome("Unidade Raiz");

        // Secretaria B (para testar ordenação alfabética com Secretaria A)
        UnidadeDto secretariaB = new UnidadeDto();
        secretariaB.setCodigo(10L);
        secretariaB.setSigla("SEC-B");
        secretariaB.setNome("SECRETARIA B");
        secretariaB.setTipo("SECRETARIA");

        // Subunidades do tipo ZONA ELEITORAL para testar agrupamento visual
        // ZE 02 (número) e ZE 10 (número maior) e ZE 99999999999999999999 (estouro de inteiro para NumberFormatException)
        UnidadeDto subSecB1 = new UnidadeDto();
        subSecB1.setCodigo(11L);
        subSecB1.setSigla("ZE 02");
        subSecB1.setNome("ZONA ELEITORAL 02");
        subSecB1.setTipo("ZONA ELEITORAL");

        UnidadeDto subSecB2 = new UnidadeDto();
        subSecB2.setCodigo(12L);
        subSecB2.setSigla("ZE 10");
        subSecB2.setNome("ZONA ELEITORAL 10");
        subSecB2.setTipo("ZONA ELEITORAL");

        UnidadeDto subSecB3 = new UnidadeDto();
        subSecB3.setCodigo(16L);
        subSecB3.setSigla("ZE 99999999999999999999");
        subSecB3.setNome("ZONA ELEITORAL LIMITE");
        subSecB3.setTipo("ZONA ELEITORAL");

        secretariaB.setSubunidades(List.of(subSecB1, subSecB2, subSecB3));

        // Secretaria A
        UnidadeDto secretariaA = new UnidadeDto();
        secretariaA.setCodigo(20L);
        secretariaA.setSigla("SEC-A");
        secretariaA.setNome("SECRETARIA A");
        secretariaA.setTipo("SECRETARIA");

        // Subunidade com sigla vazia/em branco
        UnidadeDto subSecA1 = new UnidadeDto();
        subSecA1.setCodigo(13L);
        subSecA1.setSigla("SUB-A1-SIGLA");
        subSecA1.setNome("SUB-A1");
        subSecA1.setTipo("COMUM");

        // Subunidade com sigla e nome válidos
        UnidadeDto subSecA2 = new UnidadeDto();
        subSecA2.setCodigo(14L);
        subSecA2.setSigla("SUB-A2-SIGLA");
        subSecA2.setNome("Sub-A2");
        subSecA2.setTipo("COMUM");

        // Subunidade que não está sem mapa e não deve aparecer
        UnidadeDto subSecA3 = new UnidadeDto();
        subSecA3.setCodigo(15L);
        subSecA3.setSigla("OK");
        subSecA3.setNome("SUB-OK");
        subSecA3.setSubunidades(List.of());

        secretariaA.setSubunidades(List.of(subSecA1, subSecA2, subSecA3));

        raiz.setSubunidades(List.of(secretariaB, secretariaA));

        when(unidadeHierarquiaService.buscarArvoreHierarquica()).thenReturn(List.of(raiz));

        OutputStream out = new ByteArrayOutputStream();
        relatorioService.gerarRelatorioUnidadesSemMapasVigentes(out);

        verify(document, atLeastOnce()).add(any());
    }


    @Test
    @DisplayName("Deve gerar relatório de andamento com localização nula")
    void deveGerarRelatorioAndamentoComLocalizacaoNula() throws DocumentException {
        when(pdfFactory.createDocument()).thenReturn(document);
        Processo p = new Processo();
        p.setDescricao("Processo Teste");
        p.setTipo(TipoProcesso.MAPEAMENTO);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setUnidade(new Unidade());
        sp.getUnidade().setSigla("U1");
        sp.getUnidade().setNome("Unidade 1");
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setDataLimiteEtapa1(java.time.LocalDateTime.now());

        when(processoService.buscarPorCodigo(1L)).thenReturn(p);
        when(consultaService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
        when(responsavelService.buscarResponsaveisUnidades(any())).thenReturn(Map.of());
        when(localizacaoSubprocessoService.obterLocalizacoesAtuais(any())).thenReturn(new HashMap<>());

        OutputStream out = new ByteArrayOutputStream();
        relatorioService.gerarRelatorioAndamento(1L, out);

        verify(document, atLeastOnce()).add(any());
    }

    @Test
    @DisplayName("Deve formatar datas nulas como traço no relatório de andamento")
    void deveFormatarDataFimNulaComoTracoNoRelatorioAndamento() {
        Unidade u = new Unidade();
        u.setSigla("U1");
        u.setCodigo(1L);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setUnidade(u);
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setDataLimiteEtapa1(null);
        sp.setDataFimEtapa1(null);
        sp.setDataLimiteEtapa2(null);
        sp.setDataFimEtapa2(null);

        when(consultaService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
        when(responsavelService.buscarResponsaveisUnidades(any())).thenReturn(Map.of());
        when(localizacaoSubprocessoService.obterLocalizacoesAtuais(any())).thenReturn(Map.of(100L, u));

        List<RelatorioAndamentoDto> resultado = relatorioService.obterRelatorioAndamento(1L);
        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().dataLimiteEtapa1()).isNull();
    }

    @Test
    @DisplayName("Deve gerar relatório de andamento com titular igual ao responsável")
    void deveGerarRelatorioAndamentoComTitularIgualAoResponsavel() throws DocumentException {
        when(pdfFactory.createDocument()).thenReturn(document);
        Processo p = new Processo();
        p.setDescricao("Processo Teste");
        p.setTipo(TipoProcesso.MAPEAMENTO);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setUnidade(new Unidade());
        sp.getUnidade().setSigla("U1");
        sp.getUnidade().setNome("Unidade 1");
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setDataLimiteEtapa1(java.time.LocalDateTime.now());
        sp.setDataLimiteEtapa2(java.time.LocalDateTime.now());

        when(processoService.buscarPorCodigo(1L)).thenReturn(p);
        when(consultaService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
        when(responsavelService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(
                1L, UnidadeResponsavelDto.builder().titularNome("Carlos").substitutoNome(null).build()
        ));
        when(localizacaoSubprocessoService.obterLocalizacoesAtuais(any())).thenReturn(Map.of(100L, sp.getUnidade()));

        OutputStream out = new ByteArrayOutputStream();
        relatorioService.gerarRelatorioAndamento(1L, out);

        verify(document, atLeastOnce()).add(any());
    }

    @Test
    @DisplayName("Deve gerar relatório de unidades sem mapas vigentes cobrindo variações extremas")
    void deveGerarRelatorioUnidadesSemMapasVigentesComVariacoesDeNomesESiglas() throws DocumentException {
        when(pdfFactory.createDocument()).thenReturn(document);
        when(unidadeService.buscarCodigosUnidadesSemMapaVigente()).thenReturn(List.of(10L, 11L, 12L));

        UnidadeDto raiz = new UnidadeDto();
        raiz.setCodigo(1L);
        raiz.setSigla("RAIZ");
        raiz.setNome("Unidade Raiz");

        // Unidade com sigla e nome válidos
        UnidadeDto u1 = new UnidadeDto();
        u1.setCodigo(10L);
        u1.setSigla("ZE-10");
        u1.setNome("Zona Eleitoral Dez");
        u1.setTipo("ZONA ELEITORAL");

        // Unidade com sigla e nome válidos
        UnidadeDto u2 = new UnidadeDto();
        u2.setCodigo(11L);
        u2.setSigla("ZE-11");
        u2.setNome("ZONA ELEITORAL TESTE");
        u2.setTipo("ZONA ELEITORAL");

        // Unidade com sigla igual ao nome
        UnidadeDto u3 = new UnidadeDto();
        u3.setCodigo(12L);
        u3.setSigla("SEC");
        u3.setNome("SEC");
        u3.setTipo("SECRETARIA");

        raiz.setSubunidades(List.of(u1, u2, u3));

        when(unidadeHierarquiaService.buscarArvoreHierarquica()).thenReturn(List.of(raiz));

        OutputStream out = new ByteArrayOutputStream();
        relatorioService.gerarRelatorioUnidadesSemMapasVigentes(out);

        verify(document, atLeastOnce()).add(any());
    }

    @Test
    @DisplayName("Deve formatar situação com múltiplos underscores de forma robusta")
    void deveFormatarSituacaoComMultiplosUnderscores() throws DocumentException {
        when(pdfFactory.createDocument()).thenReturn(document);
        Processo p = new Processo();
        p.setDescricao("Processo Teste");
        p.setTipo(TipoProcesso.MAPEAMENTO);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setUnidade(new Unidade());
        sp.getUnidade().setSigla("U1");
        sp.getUnidade().setNome("Unidade 1");
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        when(processoService.buscarPorCodigo(1L)).thenReturn(p);
        when(consultaService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
        when(responsavelService.buscarResponsaveisUnidades(any())).thenReturn(Map.of());
        when(localizacaoSubprocessoService.obterLocalizacoesAtuais(any())).thenReturn(Map.of(100L, sp.getUnidade()));

        OutputStream out = new ByteArrayOutputStream();
        relatorioService.gerarRelatorioAndamento(1L, out);

        verify(document, atLeastOnce()).add(any());
    }

    @Test
    @DisplayName("Deve gerar relatório de andamento com responsável substituto")
    void deveGerarRelatorioAndamentoComResponsavelSubstituto() throws DocumentException {
        when(pdfFactory.createDocument()).thenReturn(document);
        Processo processo = new Processo();
        processo.setDescricao("Processo Teste");
        processo.setTipo(TipoProcesso.MAPEAMENTO);

        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSigla("U1");
        unidade.setNome("Unidade 1");

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(unidade);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        subprocesso.setDataLimiteEtapa1(java.time.LocalDateTime.now());

        when(processoService.buscarPorCodigo(1L)).thenReturn(processo);
        when(consultaService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(subprocesso));
        when(responsavelService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(
                1L, UnidadeResponsavelDto.builder().titularNome("Carlos").substitutoNome("Ana").build()
        ));
        when(localizacaoSubprocessoService.obterLocalizacoesAtuais(any())).thenReturn(Map.of(100L, unidade));

        List<RelatorioAndamentoDto> relatorio = relatorioService.obterRelatorioAndamento(1L);
        relatorioService.gerarRelatorioAndamento(1L, new ByteArrayOutputStream());

        assertThat(relatorio.getFirst().titular()).isEqualTo("Carlos");
        assertThat(relatorio.getFirst().responsavel()).isEqualTo("Ana (Substituição)");
        verify(document, atLeastOnce()).add(any());
    }

    @Test
    @DisplayName("Deve lançar erro quando a unidade não possui mapa vigente")
    void deveLancarErroQuandoUnidadeNaoPossuiMapaVigente() {
        mockContextoAdmin();
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        when(unidadeService.buscarPorCodigo(1L)).thenReturn(unidade);
        when(unidadeService.buscarMapaVigente(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> relatorioService.obterRelatorioMapaVigenteUnidade(1L))
                .isInstanceOf(sgc.comum.erros.ErroEntidadeNaoEncontrada.class)
                .hasMessageContaining("MapaVigente");
    }

    @Test
    @DisplayName("Deve agrupar unidades com sigla ZE como zonas eleitorais")
    void deveAgruparUnidadesComSiglaZeComoZonasEleitorais() {
        UnidadeDto raiz = new UnidadeDto();
        raiz.setCodigo(1L);
        raiz.setSigla("RAIZ");

        UnidadeDto secretaria = new UnidadeDto();
        secretaria.setCodigo(10L);
        secretaria.setSigla("SEC");
        secretaria.setNome("SECRETARIA X");
        secretaria.setTipo("SECRETARIA");

        UnidadeDto zonaPorSigla = new UnidadeDto();
        zonaPorSigla.setCodigo(11L);
        zonaPorSigla.setSigla("ZE");
        zonaPorSigla.setNome("Cartório 11");
        zonaPorSigla.setTipo(null);
        secretaria.setSubunidades(List.of(zonaPorSigla));
        raiz.setSubunidades(List.of(secretaria));

        when(unidadeService.buscarCodigosUnidadesSemMapaVigente()).thenReturn(List.of(11L));
        when(unidadeHierarquiaService.buscarArvoreHierarquica()).thenReturn(List.of(raiz));

        List<RelatorioUnidadeSemMapaVigenteDto> resultado = relatorioService.obterRelatorioUnidadesSemMapasVigentes();

        assertThat(resultado.getFirst().filhas()).hasSize(1);
        assertThat(resultado.getFirst().filhas().getFirst().sigla()).isEqualTo("ZONAS ELEITORAIS");
        assertThat(resultado.getFirst().filhas().getFirst().filhas()).extracting(RelatorioUnidadeSemMapaVigenteDto::codigo)
                .containsExactly(11L);
    }

    @Test
    @DisplayName("Deve filtrar unidades sem mapa quando subunidades sao nulas ou vazias")
    void deveFiltrarUnidadesQuandoSubunidadesSaoNulasOuVazias() {
        UnidadeDto raiz = new UnidadeDto();
        raiz.setCodigo(1L);
        raiz.setSigla("RAIZ");

        UnidadeDto secretariaComSubunidades = new UnidadeDto();
        secretariaComSubunidades.setCodigo(10L);
        secretariaComSubunidades.setSigla("SEC");
        secretariaComSubunidades.setNome("SECRETARIA X");
        secretariaComSubunidades.setTipo("SECRETARIA");

        // secretaria com subunidades == null (nao deve aparecer no resultado)
        UnidadeDto secretariaSemFilhas = new UnidadeDto();
        secretariaSemFilhas.setCodigo(20L);
        secretariaSemFilhas.setSigla("SEC-B");
        secretariaSemFilhas.setNome("SECRETARIA B");
        secretariaSemFilhas.setSubunidades(null);

        UnidadeDto zona = new UnidadeDto();
        zona.setCodigo(11L);
        zona.setSigla("ZE 01");
        zona.setNome("ZONA ELEITORAL 1");
        zona.setTipo("ZONA ELEITORAL");
        secretariaComSubunidades.setSubunidades(List.of(zona));
        raiz.setSubunidades(List.of(secretariaComSubunidades, secretariaSemFilhas));

        when(unidadeService.buscarCodigosUnidadesSemMapaVigente()).thenReturn(List.of(11L));
        when(unidadeHierarquiaService.buscarArvoreHierarquica()).thenReturn(List.of(raiz));

        List<RelatorioUnidadeSemMapaVigenteDto> resultado = relatorioService.obterRelatorioUnidadesSemMapasVigentes();

        // Apenas a secretaria com subunidades pendentes deve aparecer
        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().sigla()).isEqualTo("SEC");
    }

    @Test
    @DisplayName("Deve gerar relatório de andamento sem marcar prazo ajustado quando datas são iguais")
    void deveGerarRelatorioAndamentoSemMarcarPrazoAjustadoQuandoDatasSaoIguais() throws DocumentException {
        when(pdfFactory.createDocument()).thenReturn(document);
        Processo processo = new Processo();
        processo.setDescricao("Processo Teste");
        processo.setTipo(TipoProcesso.MAPEAMENTO);

        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSigla("U1");
        unidade.setNome("Unidade 1");

        java.time.LocalDateTime data = java.time.LocalDateTime.now();
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(unidade);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        subprocesso.setDataLimiteEtapa1(data);
        subprocesso.setDataLimiteEtapa2(data);

        when(processoService.buscarPorCodigo(1L)).thenReturn(processo);
        when(consultaService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(subprocesso));
        when(responsavelService.buscarResponsaveisUnidades(any())).thenReturn(Map.of());
        when(localizacaoSubprocessoService.obterLocalizacoesAtuais(any())).thenReturn(Map.of(100L, unidade));

        relatorioService.gerarRelatorioAndamento(1L, new ByteArrayOutputStream());

        verify(document, atLeastOnce()).add(any());
    }

    @Test
    @DisplayName("Deve cobrir casos de ordenacao natural complexa e identificacoes vazias em relatorio de unidades sem mapas vigentes")
    void deveCobrirOrdenacaoNaturalComplexaEIdentificacoesVazias() throws DocumentException {
        when(pdfFactory.createDocument()).thenReturn(document);

        UnidadeDto u1 = new UnidadeDto();
        u1.setCodigo(10L);
        u1.setSigla("U999999999999");
        u1.setNome("Unidade Grande 1");
        u1.setTipo("SECRETARIA");

        UnidadeDto u2 = new UnidadeDto();
        u2.setCodigo(11L);
        u2.setSigla("U888888888888");
        u2.setNome("Unidade Grande 2");
        u2.setTipo("SECRETARIA");

        UnidadeDto u3 = new UnidadeDto();
        u3.setCodigo(12L);
        u3.setSigla("Z.E.");
        u3.setNome("Zona Eleitoral 10");
        u3.setTipo("OUTRO");

        UnidadeDto u4 = new UnidadeDto();
        u4.setCodigo(13L);
        u4.setSigla("   ");
        u4.setNome("");
        u4.setTipo("OUTRO");

        UnidadeDto raiz = new UnidadeDto();
        raiz.setCodigo(1L);
        raiz.setSigla("RAIZ");
        raiz.setNome("Raiz");
        raiz.setSubunidades(List.of(u1, u2, u3, u4));

        when(unidadeService.buscarCodigosUnidadesSemMapaVigente()).thenReturn(List.of(10L, 11L, 12L, 13L));
        when(unidadeHierarquiaService.buscarArvoreHierarquica()).thenReturn(List.of(raiz));

        OutputStream out = new ByteArrayOutputStream();
        relatorioService.gerarRelatorioUnidadesSemMapasVigentes(out);

        verify(document, atLeastOnce()).add(any());
    }

    @Test
    @DisplayName("Deve lancar ErroInconsistenciaInterna quando falha escrita de PDF")
    void deveLancarErroInconsistenciaInternaQuandoFalhaEscritaDePdf() throws Exception {
        RelatorioService spyRelatorioService = spy(relatorioService);
        when(pdfFactory.createDocument()).thenReturn(document);

        // Raiz com filho para garantir que tente adicionar algo ao documento
        UnidadeDto u = new UnidadeDto();
        u.setCodigo(10L);
        u.setSigla("U1");
        u.setNome("Unidade Um");
        UnidadeDto raiz = new UnidadeDto();
        raiz.setCodigo(1L);
        raiz.setSigla("RAIZ");
        raiz.setNome("Raiz");
        raiz.setSubunidades(List.of(u));

        when(unidadeService.buscarCodigosUnidadesSemMapaVigente()).thenReturn(List.of(10L));
        when(unidadeHierarquiaService.buscarArvoreHierarquica()).thenReturn(List.of(raiz));

        // Força processarUnidadesNoPdf a lançar exceção usando o SPY
        doThrow(new DocumentException("Falha simulada no processamento")).when(spyRelatorioService).processarUnidadesNoPdf(any(), any());

        OutputStream out = new ByteArrayOutputStream();

        assertThatThrownBy(() -> spyRelatorioService.gerarRelatorioUnidadesSemMapasVigentes(out))
                .isInstanceOf(sgc.comum.erros.ErroInconsistenciaInterna.class)
                .hasMessageContaining("Erro ao gerar PDF")
                .hasCauseInstanceOf(DocumentException.class);
    }

    @Test
    @DisplayName("Deve cobrir gaps de ordenacao natural, sigla sem nome, subunidades nulas e situacao com underscores")
    void deveCobrirGapsOrdenacaoSiglaSemNomeESubunidadesNulas() throws DocumentException {
        when(pdfFactory.createDocument()).thenReturn(document);

        // 1. Unidade com sigla e nome vazios (gera "-" na ordenação e tituloTexto)
        UnidadeDto u1 = new UnidadeDto();
        u1.setCodigo(10L);
        u1.setSigla("");
        u1.setNome("");
        u1.setTipo("COMUM");

        // 1b. Unidade com sigla vazia e nome vazio (gera "-")
        UnidadeDto u1b = new UnidadeDto();
        u1b.setCodigo(101L);
        u1b.setSigla(" ");
        u1b.setNome("");

        // 2. Unidade com sigla preenchida (ZE), nome vazio
        // Nota: tipo COMUM para não curto-circuitar ehZonaEleitoralPorMetadados e exercitar ehSiglaZonaEleitoral
        UnidadeDto u2 = new UnidadeDto();
        u2.setCodigo(11L);
        u2.setSigla("z.e."); // Lacuna linha 777 - regex complexa
        u2.setNome("");
        u2.setTipo("COMUM");

        UnidadeDto u2b = new UnidadeDto();
        u2b.setCodigo(111L);
        u2b.setSigla("Z.E."); // Outra variação regex
        u2b.setNome("");
        u2b.setTipo("COMUM");

        UnidadeDto u2c = new UnidadeDto();
        u2c.setCodigo(112L);
        u2c.setSigla("ZE "); // Outra variação regex
        u2c.setNome("");
        u2c.setTipo("COMUM");

        // 3. Unidade com nome SECRETARIA 10 e outra SECRETARIA 2 (para testar ordenação numérica)
        UnidadeDto u3 = new UnidadeDto();
        u3.setCodigo(12L);
        u3.setSigla("SEC 10 A"); // Lacuna linha 810 - empate numerico para continuar loop
        u3.setNome("SECRETARIA 10");
        u3.setTipo("SECRETARIA");

        UnidadeDto u4 = new UnidadeDto();
        u4.setCodigo(13L);
        u4.setSigla("SEC 10 B");
        u4.setNome("SECRETARIA 10");
        u4.setTipo("SECRETARIA");

        UnidadeDto u4b = new UnidadeDto();
        u4b.setCodigo(131L);
        u4b.setSigla("SEC 10"); // Lacuna linha 822 - tamanhos diferentes
        u4b.setNome("SECRETARIA 10");
        u4b.setTipo("SECRETARIA");

        // Unidade com subunidades vazias explicitamente (cobre linha 644 - !isEmpty)
        UnidadeDto u5 = new UnidadeDto();
        u5.setCodigo(14L);
        u5.setSigla("SUB-VAZIA");
        u5.setNome("Subunidades Vazias");
        u5.setSubunidades(Collections.emptyList());
        u5.setTipo("COMUM");

        // Unidade com subunidades nulas (cobre linha 644 - null)
        UnidadeDto u9 = new UnidadeDto();
        u9.setCodigo(18L);
        u9.setSigla("SUB-NULA");
        u9.setNome("Subunidades Nulas");
        u9.setSubunidades(null);
        u9.setTipo("COMUM");

        // Unidade com sigla misturando letras e números complexos (ZE-01 vs ZE-02)
        UnidadeDto u6 = new UnidadeDto();
        u6.setCodigo(15L);
        u6.setSigla("ZE-01");
        u6.setNome("Zona 01");
        u6.setTipo("ZONA ELEITORAL");

        UnidadeDto u7 = new UnidadeDto();
        u7.setCodigo(16L);
        u7.setSigla("ZE-02");
        u7.setNome("Zona 02");
        u7.setTipo("ZONA ELEITORAL");

        // Unidade com sigla preenchida (sigla nunca é nula)
        UnidadeDto u8 = new UnidadeDto();
        u8.setCodigo(17L);
        u8.setSigla("U8");
        u8.setNome("Unidade Oito");
        u8.setTipo("COMUM");

        // Unidade com sigla preenchida (sigla nunca é nula)
        UnidadeDto u10 = new UnidadeDto();
        u10.setCodigo(19L);
        u10.setSigla("U10");
        u10.setNome("Sigla em branco");

        // Unidade com sigla que NÃO é zona eleitoral (Lacuna linha 777 - false)
        UnidadeDto u11 = new UnidadeDto();
        u11.setCodigo(20L);
        u11.setSigla("COMUM");
        u11.setNome("Comum");

        UnidadeDto u2d = new UnidadeDto();
        u2d.setCodigo(113L);
        u2d.setSigla("ZE."); // Lacuna linha 777 - regex dot end
        u2d.setNome("Zona Eleitoral D");
        u2d.setTipo("COMUM");

        UnidadeDto u2e = new UnidadeDto();
        u2e.setCodigo(114L);
        u2e.setSigla("ZE"); // Lacuna linha 777 - regex simple match
        u2e.setNome("Zona Eleitoral E");
        u2e.setTipo("COMUM");

        // 4. Unidade com sigla "SECRETARIA" (Lacuna linha 777 - false match com string não nula)
        UnidadeDto u12 = new UnidadeDto();
        u12.setCodigo(21L);
        u12.setSigla("SECRETARIA");
        u12.setNome("Secretaria de Teste");

        // 5. Unidade com sigla "" (Lacuna linha 777 - false match com string vazia)
        UnidadeDto u13 = new UnidadeDto();
        u13.setCodigo(22L);
        u13.setSigla("");
        u13.setNome("Sigla Vazia");

        UnidadeDto raiz = new UnidadeDto();
        raiz.setCodigo(1L);
        raiz.setSigla("RAIZ");
        raiz.setNome("Raiz");
        raiz.setSubunidades(List.of(u1, u1b, u2, u2b, u2c, u2d, u2e, u3, u4, u4b, u5, u6, u7, u8, u9, u10, u11, u12, u13));

        // Raiz com subunidades nulas (Lacuna linha 644 - null)
        UnidadeDto raizNula = new UnidadeDto();
        raizNula.setCodigo(2L);
        raizNula.setSigla("RN");
        raizNula.setNome("Raiz Nula");
        raizNula.setSubunidades(null);

        // Raiz com subunidades vazias (Lacuna linha 644 - empty)
        UnidadeDto raizVazia = new UnidadeDto();
        raizVazia.setCodigo(3L);
        raizVazia.setSigla("RV");
        raizVazia.setNome("Raiz Vazia");
        raizVazia.setSubunidades(Collections.emptyList());

        when(unidadeService.buscarCodigosUnidadesSemMapaVigente()).thenReturn(List.of(10L, 101L, 11L, 111L, 112L, 113L, 114L, 12L, 13L, 131L, 14L, 15L, 16L, 17L, 18L, 19L, 20L, 21L, 22L));
        when(unidadeHierarquiaService.buscarArvoreHierarquica()).thenReturn(List.of(raiz, raizNula, raizVazia));

        OutputStream out = new ByteArrayOutputStream();
        relatorioService.gerarRelatorioUnidadesSemMapasVigentes(out);

        verify(document, atLeastOnce()).add(any());
    }

    @Test
    @DisplayName("Deve formatar situacao com underscores consecutivos no relatorio de andamento")
    void deveFormatarSituacaoComUnderscoresConsecutivos() throws DocumentException {
        when(pdfFactory.createDocument()).thenReturn(document);
        Processo p = new Processo();
        p.setDescricao("Processo Teste");
        p.setTipo(TipoProcesso.MAPEAMENTO);

        // Subprocesso 1: Descrição com underscores consecutivos (Lacuna linha 880)
        Subprocesso sp1 = mock(Subprocesso.class);
        Unidade u1 = new Unidade();
        u1.setCodigo(101L);
        u1.setSigla("U1");
        u1.setNome("Unidade 1");
        when(sp1.getUnidade()).thenReturn(u1);
        when(sp1.getCodigo()).thenReturn(101L);
        SituacaoSubprocesso sit1 = mock(SituacaoSubprocesso.class);
        when(sit1.getDescricao()).thenReturn("MAPEAMENTO__MAPA__COM__SUGESTOES");
        when(sp1.getSituacao()).thenReturn(sit1);

        // Subprocesso 2: Descrição vazia (Lacuna linha 873 - isBlank)
        Subprocesso sp2 = mock(Subprocesso.class);
        Unidade u2 = new Unidade();
        u2.setCodigo(102L);
        u2.setSigla("U2");
        u2.setNome("Unidade 2");
        when(sp2.getUnidade()).thenReturn(u2);
        when(sp2.getCodigo()).thenReturn(102L);
        SituacaoSubprocesso sit2 = mock(SituacaoSubprocesso.class);
        when(sit2.getDescricao()).thenReturn("   ");
        when(sp2.getSituacao()).thenReturn(sit2);

        // Subprocesso 3: Descrição nula (Lacuna linha 873 - null)
        Subprocesso sp3 = mock(Subprocesso.class);
        Unidade u3 = new Unidade();
        u3.setCodigo(103L);
        u3.setSigla("U3");
        u3.setNome("Unidade 3");
        when(sp3.getUnidade()).thenReturn(u3);
        when(sp3.getCodigo()).thenReturn(103L);
        SituacaoSubprocesso sit3 = mock(SituacaoSubprocesso.class);
        when(sit3.getDescricao()).thenReturn(null);
        when(sp3.getSituacao()).thenReturn(sit3);

        when(processoService.buscarPorCodigo(1L)).thenReturn(p);
        when(consultaService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp1, sp2, sp3));
        when(responsavelService.buscarResponsaveisUnidades(any())).thenReturn(Map.of());
        when(localizacaoSubprocessoService.obterLocalizacoesAtuais(any())).thenReturn(Map.of(
                101L, u1, 102L, u2, 103L, u3
        ));

        OutputStream out = new ByteArrayOutputStream();
        relatorioService.gerarRelatorioAndamento(1L, out);

        verify(document, atLeastOnce()).add(any());
    }

    @Nested
    @DisplayName("Testes de Métodos de Utilidade (Aumentar Testabilidade)")
    class TestesUtilidade {

        @ParameterizedTest
        @CsvSource({
                ", false",
                "'', false",
                "'   ', false",
                "ZE, true",
                "Z.E., true",
                "ze, true",
                "z.e., true",
                "Z E, true",
                "Z. E., true",
                "COMUM, false",
                "SECRETARIA, false",
                "ZERO, false"
        })
        @DisplayName("Deve validar siglas de zona eleitoral corretamente")
        void deveValidarSiglasZonaEleitoral(String sigla, boolean esperado) {
            assertThat(relatorioService.ehSiglaZonaEleitoral(sigla)).isEqualTo(esperado);
        }

        @Test
        @DisplayName("Deve separar segmentos de texto corretamente")
        void deveSepararSegmentos() {
            assertThat(relatorioService.separarSegmentos("")).containsExactly("");
            assertThat(relatorioService.separarSegmentos("SEC 10")).containsExactly("SEC ", "10");
            assertThat(relatorioService.separarSegmentos("SEC 10 A")).containsExactly("SEC ", "10", " A");
        }

        @ParameterizedTest
        @CsvSource({
                ", '-'",
                "'', '-'",
                "'   ', '-'",
                "MAPEAMENTO__MAPA, Mapeamento Mapa",
                "REVISAO_MAPA, Revisao Mapa",
                "SITUACAO_SIMPLES, Situacao Simples"
        })
        @DisplayName("Deve formatar situação para PDF corretamente")
        void deveFormatarSituacaoPdf(String situacao, String esperado) {
            assertThat(relatorioService.formatarSituacaoPdf(situacao)).isEqualTo(esperado);
        }

        @Test
        @DisplayName("Deve validar siglas de zona eleitoral - gaps de cobertura")
        void deveValidarSiglasZonaEleitoralGaps() {
            assertThat(relatorioService.ehSiglaZonaEleitoral("")).isFalse();
            assertThat(relatorioService.ehSiglaZonaEleitoral("SECRETARIA")).isFalse();
            assertThat(relatorioService.ehSiglaZonaEleitoral(null)).isFalse();
            assertThat(relatorioService.ehTextoSecretaria(null)).isFalse();
            assertThat(relatorioService.ehTextoSecretaria("")).isFalse();
            assertThat(relatorioService.ehTextoSecretaria("   ")).isFalse();
        }

        @Test
        @DisplayName("Deve cobrir IOException no relatorio")
        void deveCobrirIOExceptionNoRelatorio() throws Exception {
            RelatorioService spyService = spy(relatorioService);
            when(pdfFactory.createDocument()).thenReturn(document);

            // Força IOException no cabeçalho para atingir o catch de IOException em gerarRelatorioUnidadesSemMapasVigentes
            when(unidadeService.buscarCodigosUnidadesSemMapaVigente()).thenReturn(List.of(1L));
            doThrow(new IOException("Erro de IO")).when(spyService).adicionarCabecalhoRelatorio(any(), any());
            assertThatThrownBy(() -> spyService.gerarRelatorioUnidadesSemMapasVigentes(new ByteArrayOutputStream()))
                    .isInstanceOf(sgc.comum.erros.ErroInconsistenciaInterna.class)
                    .hasMessageContaining("Erro ao gerar PDF")
                    .hasCauseInstanceOf(IOException.class);

            // Força DocumentException em gerarRelatorioAndamento
            Processo p = new Processo();
            p.setDescricao("P1");
            p.setTipo(TipoProcesso.MAPEAMENTO);
            when(processoService.buscarPorCodigo(1L)).thenReturn(p);
            doThrow(new DocumentException("Erro PDF")).when(spyService).adicionarCabecalhoRelatorio(any(), any());
            assertThatThrownBy(() -> spyService.gerarRelatorioAndamento(1L, new ByteArrayOutputStream()))
                    .isInstanceOf(sgc.comum.erros.ErroInconsistenciaInterna.class)
                    .hasCauseInstanceOf(DocumentException.class);
        }

        @Test
        @DisplayName("Deve cobrir IOException no relatorio de andamento")
        void deveCobrirIOExceptionNoRelatorioAndamento() throws Exception {
            RelatorioService spyService = spy(relatorioService);
            when(pdfFactory.createDocument()).thenReturn(document);
            Processo p = new Processo();
            p.setDescricao("P1");
            p.setTipo(TipoProcesso.MAPEAMENTO);
            when(processoService.buscarPorCodigo(1L)).thenReturn(p);
            doThrow(new IOException("Erro de IO")).when(spyService).adicionarCabecalhoRelatorio(any(), any());

            assertThatThrownBy(() -> spyService.gerarRelatorioAndamento(1L, new ByteArrayOutputStream()))
                    .isInstanceOf(sgc.comum.erros.ErroInconsistenciaInterna.class)
                    .hasMessageContaining("Erro ao gerar PDF")
                    .hasCauseInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("Deve cobrir IOException no relatorio de mapas")
        void deveCobrirIOExceptionNoRelatorioMapas() throws Exception {
            RelatorioService spyService = spy(relatorioService);
            when(pdfFactory.createDocument()).thenReturn(document);
            mockContextoAdmin();
            when(unidadeService.buscarMapasPorUnidades(any())).thenReturn(List.of());
            doThrow(new IOException("Erro de IO")).when(spyService).adicionarCabecalhoRelatorio(any(), any());

            assertThatThrownBy(() -> spyService.gerarRelatorioMapas(List.of(1L), new ByteArrayOutputStream()))
                    .isInstanceOf(sgc.comum.erros.ErroInconsistenciaInterna.class)
                    .hasMessageContaining("Erro ao gerar PDF")
                    .hasCauseInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("Deve tratar caso de documento nulo na geracao de relatorio de unidades sem mapas vigentes")
        void deveTratarCasoDeDocumentoNulo() {
            when(pdfFactory.createDocument()).thenReturn(null);
            when(unidadeService.buscarCodigosUnidadesSemMapaVigente()).thenReturn(List.of());

            assertThatThrownBy(() -> relatorioService.gerarRelatorioUnidadesSemMapasVigentes(new ByteArrayOutputStream()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Deve tratar erro ao fechar documento no relatorio de unidades sem mapas vigentes")
        void deveTratarErroAoFecharDocumento() {
            when(pdfFactory.createDocument()).thenReturn(document);
            when(unidadeService.buscarCodigosUnidadesSemMapaVigente()).thenReturn(List.of(10L));
            doThrow(new RuntimeException("Erro ao fechar")).when(document).close();

            assertThatThrownBy(() -> relatorioService.gerarRelatorioUnidadesSemMapasVigentes(new ByteArrayOutputStream()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Erro ao fechar");
        }

        @ParameterizedTest
        @CsvSource({
                ", false",
                "'', false",
                "'   ', false",
                "SECRETARIA, true",
                "Secretaria da Saúde, true",
                "COMUM, false"
        })
        @DisplayName("Deve validar texto de secretaria corretamente")
        void deveValidarTextoSecretaria(String texto, boolean esperado) {
            assertThat(relatorioService.ehTextoSecretaria(texto)).isEqualTo(esperado);
        }
    }
}
