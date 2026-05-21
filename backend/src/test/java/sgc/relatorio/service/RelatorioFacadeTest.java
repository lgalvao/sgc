package sgc.relatorio.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.openpdf.text.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.processo.service.*;
import sgc.relatorio.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;
import sgc.comum.erros.ErroAcessoNegado;

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
    private UnidadeService unidadeService;
    @Mock
    private UnidadeHierarquiaService unidadeHierarquiaService;
    @Mock
    private LocalizacaoSubprocessoService localizacaoSubprocessoService;
    @Mock
    private UsuarioFacade usuarioFacade;
    @Mock
    private PdfFactory pdfFactory;
    @Mock
    private Document document;

    @InjectMocks
    private RelatorioFacade relatorioService;

    private void mockContextoAdmin() {
        when(usuarioFacade.contextoAutenticado()).thenReturn(new ContextoUsuarioAutenticado(
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

        UnidadeMapa mapaNulo = UnidadeMapa.builder()
                .unidadeCodigo(1L)
                .mapaVigente(null)
                .build();
        UnidadeMapa mapaValido = UnidadeMapa.builder()
                .unidadeCodigo(2L)
                .mapaVigente(subprocesso.getMapa())
                .build();

        when(unidadeService.buscarMapasPorUnidades(List.of(1L, 2L))).thenReturn(List.of(mapaNulo, mapaValido));
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
        when(usuarioFacade.contextoAutenticado()).thenReturn(new ContextoUsuarioAutenticado(
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
        subSecA1.setSigla("  "); // em branco
        subSecA1.setNome("SUB-A1");
        subSecA1.setTipo("COMUM");

        // Subunidade com sigla nula e nome vazio para testar fallback
        UnidadeDto subSecA2 = new UnidadeDto();
        subSecA2.setCodigo(14L);
        subSecA2.setSigla(null);
        subSecA2.setNome("");
        subSecA2.setTipo(null);

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
}
