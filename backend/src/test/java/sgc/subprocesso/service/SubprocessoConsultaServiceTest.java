package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.test.util.*;
import sgc.comum.erros.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoConsultaService")
@SuppressWarnings("NullAway.Init")
class SubprocessoConsultaServiceTest {
    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private AnaliseRepo analiseRepo;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private sgc.organizacao.UsuarioFacade usuarioFacade;
    @Mock
    private sgc.organizacao.service.UsuarioService usuarioService;
    @Mock
    private LocalizacaoSubprocessoService localizacaoSubprocessoService;
    @Mock
    private HierarquiaService hierarquiaService;
    @Mock
    private sgc.mapa.service.MapaManutencaoService mapaManutencaoService;
    @Mock
    private MapaVisualizacaoService mapaVisualizacaoService;
    @Mock
    private ImpactoMapaService impactoMapaService;
    @Mock
    private SubprocessoValidacaoService validacaoService;
    @Mock
    private MovimentacaoRepo movimentacaoRepo;

    private SubprocessoConsultaService service;

    @BeforeEach
    void configurarDependenciasAdicionais() {
        AnaliseHistoricoService analiseHistoricoService = new AnaliseHistoricoService(unidadeService, usuarioService);
        SubprocessoContextoConsultaService contextoConsultaService = new SubprocessoContextoConsultaService(unidadeService, usuarioFacade, hierarquiaService, localizacaoSubprocessoService);
        SubprocessoAcessoService acessoService = new SubprocessoAcessoService(impactoMapaService);
        SubprocessoVisualizacaoService visualizacaoService = new SubprocessoVisualizacaoService(
                usuarioFacade, mapaManutencaoService, mapaVisualizacaoService, impactoMapaService, acessoService, analiseRepo, analiseHistoricoService);

        service = new SubprocessoConsultaService(
                subprocessoRepo,
                mapaManutencaoService,
                movimentacaoRepo,
                validacaoService,
                contextoConsultaService,
                acessoService,
                visualizacaoService
        );
    }

    @Test
    @DisplayName("buscarSubprocesso deve falhar quando codigo nao existir")
    void buscarSubprocessoDeveFalharQuandoCodigoNaoExistir() {
        when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarSubprocesso(99L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                .hasMessageContaining("Subprocesso")
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("listarPorProcessoEUnidadeCodigosESituacoes deve retornar vazio quando lista de unidades ou situacoes estiver vazia")
    void listarPorProcessoEUnidadeCodigosESituacoesDeveRetornarVazio() {
        assertThat(service.listarPorProcessoEUnidadeCodigosESituacoes(1L, List.of(), List.of(SituacaoSubprocesso.NAO_INICIADO))).isEmpty();
        assertThat(service.listarPorProcessoEUnidadeCodigosESituacoes(1L, List.of(10L), List.of())).isEmpty();
        verify(subprocessoRepo, never()).listarPorProcessoEUnidadesComUnidade(anyLong(), anyList());
    }

    @Test
    @DisplayName("listarEntidadesPorProcessoEUnidades deve retornar vazio quando lista de unidades estiver vazia")
    void listarEntidadesPorProcessoEUnidadesDeveRetornarVazioQuandoListaDeUnidadesEstiverVazia() {
        assertThat(service.listarEntidadesPorProcessoEUnidades(1L, List.of())).isEmpty();
        verify(subprocessoRepo, never()).listarPorProcessoEUnidadesComUnidade(anyLong(), anyList());
    }

    @Test
    @DisplayName("obterContextoCadastroAtividades deve carregar dados com sucesso")
    void obterContextoCadastroAtividadesSucesso() {
        Long codSubprocesso = 1L;
        Unidade unidade = new Unidade();
        unidade.setCodigo(100L);
        unidade.setSigla("U100");
        unidade.setNome("Unidade 100");
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        Mapa mapa = new Mapa();
        mapa.setCodigo(500L);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codSubprocesso);
        sp.setUnidade(unidade);
        sp.setMapa(mapa);
        mapa.setSubprocesso(sp);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setProcesso(new sgc.processo.model.Processo());
        sp.getProcesso().setSituacao(sgc.processo.model.SituacaoProcesso.EM_ANDAMENTO);

        when(subprocessoRepo.buscarPorCodigoComMapa(codSubprocesso)).thenReturn(Optional.of(sp));
        when(usuarioFacade.contextoAutenticado()).thenReturn(new sgc.organizacao.ContextoUsuarioAutenticado("123", 100L, sgc.organizacao.model.Perfil.ADMIN));
        when(unidadeService.buscarPorCodigoComSuperior(100L)).thenReturn(unidade);
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(unidade);
        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(500L)).thenReturn(List.of());

        var response = service.obterContextoCadastroAtividades(codSubprocesso);

        assertThat(response).isNotNull();
        assertThat(response.unidade()).isEqualTo(unidade);
        assertThat(response.assinaturaCadastroReferencia()).isEmpty();
    }

    @Test
    @DisplayName("verificarAcessoCadastroHabilitado deve permitir ADMIN em qualquer unidade se disponibilizado")
    void verificarAcessoCadastroHabilitadoAdmin() {
        Long codSubprocesso = 1L;
        Unidade unidadeAlvo = new Unidade();
        unidadeAlvo.setCodigo(100L);
        unidadeAlvo.setSigla("U100");

        Unidade unidadeAdmin = new Unidade();
        unidadeAdmin.setCodigo(999L);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codSubprocesso);
        sp.setUnidade(unidadeAlvo);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        sp.setProcesso(new sgc.processo.model.Processo());
        sp.getProcesso().setSituacao(sgc.processo.model.SituacaoProcesso.EM_ANDAMENTO);

        when(usuarioFacade.contextoAutenticado()).thenReturn(new sgc.organizacao.ContextoUsuarioAutenticado("admin", 999L, sgc.organizacao.model.Perfil.ADMIN));
        when(unidadeService.buscarPorCodigoComSuperior(999L)).thenReturn(unidadeAdmin);
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(unidadeAlvo);
        when(hierarquiaService.ehMesmaOuSubordinada(any(), any())).thenReturn(true);

        var permissoes = service.obterPermissoesUI(sp);
        assertThat(permissoes.habilitarAcessoCadastro()).isTrue();
    }

    @Test
    @DisplayName("listarHistoricoCadastro deve carregar unidades em lote")
    void listarHistoricoCadastroDeveCarregarUnidadesEmLote() {
        Analise analise1 = new Analise();
        analise1.setUnidadeCodigo(10L);
        analise1.setTipo(TipoAnalise.CADASTRO);
        analise1.setUsuarioTitulo("analista1");
        analise1.setAcao(TipoAcaoAnalise.ACEITE_MAPEAMENTO);

        Analise analise2 = new Analise();
        analise2.setUnidadeCodigo(20L);
        analise2.setTipo(TipoAnalise.CADASTRO);
        analise2.setUsuarioTitulo("analista2");
        analise2.setAcao(TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO);

        when(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(1L)).thenReturn(List.of(analise1, analise2));
        when(unidadeService.buscarResumosPorCodigos(List.of(10L, 20L))).thenReturn(List.of(
                new UnidadeResumoLeitura(10L, "Unidade 10", "U10", TipoUnidade.OPERACIONAL),
                new UnidadeResumoLeitura(20L, "Unidade 20", "U20", TipoUnidade.OPERACIONAL)
        ));
        when(usuarioService.buscarConsultasPorTitulos(anyCollection())).thenReturn(List.of(
                new UsuarioConsultaLeitura("analista1", "mat1", "Analista 1", "email", "ramal", 10L, "U10", "U10", TipoUnidade.OPERACIONAL, "tit1", 10L),
                new UsuarioConsultaLeitura("analista2", "mat2", "Analista 2", "email", "ramal", 20L, "U20", "U20", TipoUnidade.OPERACIONAL, "tit2", 20L)
        ));

        assertThat(service.listarHistoricoCadastro(1L)).hasSize(2);
        verify(unidadeService).buscarResumosPorCodigos(List.of(10L, 20L));
        verify(unidadeService, never()).buscarPorCodigo(anyLong());
    }

    private Usuario criarUsuario(String titulo, String nome) {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(titulo);
        usuario.setNome(nome);
        return usuario;
    }

    @Nested
    @DisplayName("Cobertura extra - visualização e contexto")
    class CoberturaConsultaTest {

        @Mock
        private AnaliseHistoricoService analiseHistoricoServiceMock;

        private SubprocessoConsultaService target;

        @BeforeEach
        void configurarServicos() {
            SubprocessoAcessoService acessoService = new SubprocessoAcessoService(impactoMapaService);
            SubprocessoVisualizacaoService visualizacaoService = new SubprocessoVisualizacaoService(
                    usuarioFacade, mapaManutencaoService, mapaVisualizacaoService, impactoMapaService,
                    acessoService, analiseRepo, analiseHistoricoServiceMock);

            target = service;
            ReflectionTestUtils.setField(target, "acessoService", acessoService);
            ReflectionTestUtils.setField(target, "visualizacaoService", visualizacaoService);
            ReflectionTestUtils.setField(
                    target,
                    "contextoConsultaService",
                    new SubprocessoContextoConsultaService(unidadeService, usuarioFacade, hierarquiaService, localizacaoSubprocessoService)
            );
        }

        private void stubContextoAutenticado(Usuario usuario) {
            when(usuarioFacade.contextoAutenticado()).thenReturn(new ContextoUsuarioAutenticado(
                    usuario.getTituloEleitoral(),
                    usuario.getUnidadeAtivaCodigo(),
                    usuario.getPerfilAtivo()
            ));
            Unidade u = new Unidade();
            u.setCodigo(usuario.getUnidadeAtivaCodigo());
            u.setNome("Unidade Usuario");
            u.setSigla("UU");
            when(unidadeService.buscarPorCodigoComSuperior(usuario.getUnidadeAtivaCodigo())).thenReturn(u);
        }

        @Test
        @DisplayName("listarHistoricoValidacao deve delegar conversão para AnaliseHistoricoService")
        void deveDelegarConversaoHistoricoValidacao() {
            Long codSubprocesso = 100L;
            Analise analise = Analise.builder()
                    .codigo(10L)
                    .unidadeCodigo(1L)
                    .dataHora(LocalDateTime.now())
                    .acao(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                    .tipo(TipoAnalise.VALIDACAO)
                    .usuarioTitulo("123456789012")
                    .build();
            when(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(codSubprocesso))
                    .thenReturn(List.of(analise));
            when(analiseHistoricoServiceMock.converterLista(List.of(analise))).thenReturn(List.of());

            target.listarHistoricoValidacao(codSubprocesso);

            verify(analiseHistoricoServiceMock).converterLista(List.of(analise));
        }

        @Test
        @DisplayName("obterContextoEdicao deve separar carga de mapa e atividades")
        void obterContextoEdicaoDeveSepararCargaDeMapaEAtividades() {
            Unidade unidade = new Unidade();
            unidade.setCodigo(10L);
            unidade.setSigla("U10");
            unidade.setNome("Unidade 10");
            unidade.setTipo(TipoUnidade.OPERACIONAL);

            Processo processo = new Processo();
            processo.setCodigo(1L);
            processo.setSituacao(sgc.processo.model.SituacaoProcesso.EM_ANDAMENTO);

            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setCodigo(100L);
            subprocesso.setUnidade(unidade);
            subprocesso.setProcesso(processo);
            subprocesso.setSituacao(SituacaoSubprocesso.NAO_INICIADO);

            Usuario usuario = Usuario.builder()
                    .tituloEleitoral("123456789012")
                    .perfilAtivo(Perfil.ADMIN)
                    .unidadeAtivaCodigo(10L)
                    .build();

            Atividade atividade = new Atividade();
            atividade.setCodigo(200L);
            atividade.setDescricao("Atividade");
            atividade.setConhecimentos(Set.of());

            Mapa mapa = new Mapa();
            mapa.setCodigo(300L);
            mapa.setSubprocesso(subprocesso);
            mapa.setCompetencias(Set.of());
            mapa.setAtividades(Set.of(atividade));
            atividade.setMapa(mapa);
            subprocesso.setMapa(mapa);

            stubContextoAutenticado(usuario);
            when(subprocessoRepo.buscarPorCodigoComMapa(100L)).thenReturn(Optional.of(subprocesso));
            when(unidadeService.temMapaVigente(10L)).thenReturn(false);
            when(localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso)).thenReturn(unidade);
            when(mapaManutencaoService.mapaCompletoSubprocesso(100L)).thenReturn(mapa);

            ContextoEdicaoResponse contexto = target.obterContextoEdicao(100L);

            assertThat(contexto.mapa().atividades()).hasSize(1);
            assertThat(contexto.mapa().atividades().getFirst().descricao()).isEqualTo("Atividade");
            verify(mapaManutencaoService).mapaCompletoSubprocesso(100L);
        }

        @Test
        @DisplayName("obterContextoEdicao deve reaproveitar subprocesso ja carregado")
        void obterContextoEdicaoDeveReaproveitarSubprocessoCarregado() {
            Unidade unidade = new Unidade();
            unidade.setCodigo(10L);
            unidade.setSigla("U10");
            unidade.setNome("Unidade 10");
            unidade.setTipo(TipoUnidade.OPERACIONAL);

            Processo processo = new Processo();
            processo.setCodigo(1L);
            processo.setSituacao(sgc.processo.model.SituacaoProcesso.EM_ANDAMENTO);

            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setCodigo(100L);
            subprocesso.setUnidade(unidade);
            subprocesso.setProcesso(processo);
            subprocesso.setSituacao(SituacaoSubprocesso.NAO_INICIADO);

            Mapa mapa = new Mapa();
            mapa.setCodigo(300L);
            mapa.setSubprocesso(subprocesso);
            mapa.setCompetencias(Set.of());
            mapa.setAtividades(Set.of());
            subprocesso.setMapa(mapa);

            Usuario usuario = Usuario.builder()
                    .tituloEleitoral("123456789012")
                    .perfilAtivo(Perfil.ADMIN)
                    .unidadeAtivaCodigo(10L)
                    .build();

            stubContextoAutenticado(usuario);
            when(unidadeService.temMapaVigente(10L)).thenReturn(false);
            when(localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso)).thenReturn(unidade);
            when(mapaManutencaoService.mapaCompletoSubprocesso(100L)).thenReturn(mapa);

            ContextoEdicaoResponse contexto = target.obterContextoEdicao(subprocesso);

            assertThat(contexto.detalhes().subprocesso().codigo()).isEqualTo(100L);
            verify(subprocessoRepo, never()).buscarPorCodigoComMapa(anyLong());
            verify(subprocessoRepo, never()).buscarPorCodigoComMapaEAtividades(anyLong());
        }

        @Test
        @DisplayName("obterContextoCadastroAtividades para REVISAO deve gerar assinatura baseada no mapa vigente")
        void obterContextoCadastroAtividadesRevisao() {
            Processo processo = new Processo();
            processo.setCodigo(1L);
            processo.setTipo(TipoProcesso.REVISAO);
            processo.setSituacao(sgc.processo.model.SituacaoProcesso.EM_ANDAMENTO);

            Unidade unidade = new Unidade();
            unidade.setCodigo(10L);
            unidade.setSigla("U10");
            unidade.setNome("Unidade 10");

            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setCodigo(100L);
            subprocesso.setUnidade(unidade);
            subprocesso.setProcesso(processo);
            subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);

            Mapa mapa = new Mapa();
            mapa.setCodigo(300L);
            mapa.setSubprocesso(subprocesso);
            subprocesso.setMapa(mapa);

            Mapa mapaVigente = new Mapa();
            mapaVigente.setCodigo(400L);

            Conhecimento conhecimento = new Conhecimento();
            conhecimento.setDescricao("Conhecimento Teste");
            Atividade atividadeVigente = new Atividade();
            atividadeVigente.setCodigo(200L);
            atividadeVigente.setDescricao("Atividade Vigente");
            atividadeVigente.setConhecimentos(Set.of(conhecimento));

            when(subprocessoRepo.buscarPorCodigoComMapa(100L)).thenReturn(Optional.of(subprocesso));
            when(mapaManutencaoService.mapaVigenteUnidade(10L)).thenReturn(Optional.of(mapaVigente));
            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(400L)).thenReturn(List.of(atividadeVigente));
            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(300L)).thenReturn(List.of());

            Usuario usuario = Usuario.builder()
                    .tituloEleitoral("123456789012")
                    .unidadeAtivaCodigo(10L)
                    .perfilAtivo(Perfil.ADMIN)
                    .build();
            stubContextoAutenticado(usuario);
            when(localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso)).thenReturn(unidade);

            ContextoCadastroAtividadesResponse res = target.obterContextoCadastroAtividades(100L);

            assertThat(res.atividadesDisponiveis()).isEmpty();
            assertThat(res.assinaturaCadastroReferencia()).isNotBlank();

            verify(mapaManutencaoService).mapaVigenteUnidade(10L);
            verify(mapaManutencaoService).atividadesMapaCodigoComConhecimentos(400L);
        }

        @Test
        @DisplayName("obterPermissoesUI deve bloquear acesso ao cadastro para CHEFE de outra unidade")
        void obterPermissoesUI_BloqueadoChefeOutraUnidade() {
            Subprocesso sp = new Subprocesso();
            Unidade uni = new Unidade();
            uni.setCodigo(10L);
            uni.setSigla("U10");
            uni.setNome("Unidade 10");
            sp.setUnidade(uni);
            sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

            when(usuarioFacade.contextoAutenticado()).thenReturn(new ContextoUsuarioAutenticado("tit", 20L, Perfil.CHEFE));
            Unidade u20 = new Unidade();
            u20.setCodigo(20L);
            u20.setNome("Unidade 20");
            when(unidadeService.buscarPorCodigoComSuperior(20L)).thenReturn(u20);
            when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(uni);

            PermissoesSubprocessoDto result = target.obterPermissoesUI(sp);

            assertThat(result.habilitarAcessoCadastro()).isFalse();
        }
    }

    @Nested
    @DisplayName("Cobertura extra - localização, permissões e histórico")
    class CoberturaConsultaExtraTest {

        @Mock
        private MovimentacaoRepo movimentacaoRepo;

        private LocalizacaoSubprocessoService localizacaoSubprocessoService;

        @BeforeEach
        void setUp() {
            localizacaoSubprocessoService = new LocalizacaoSubprocessoService(movimentacaoRepo);
            AnaliseHistoricoService analiseHistoricoService = new AnaliseHistoricoService(unidadeService, usuarioService);

            SubprocessoContextoConsultaService contextoConsultaService = new SubprocessoContextoConsultaService(unidadeService, usuarioFacade, hierarquiaService, localizacaoSubprocessoService);
            ReflectionTestUtils.setField(service, "contextoConsultaService", contextoConsultaService);

            SubprocessoAcessoService acessoService = new SubprocessoAcessoService(impactoMapaService);
            ReflectionTestUtils.setField(service, "acessoService", acessoService);

            SubprocessoVisualizacaoService visualizacaoService = new SubprocessoVisualizacaoService(
                    usuarioFacade, mapaManutencaoService, mapaVisualizacaoService, impactoMapaService, acessoService, analiseRepo, analiseHistoricoService);
            ReflectionTestUtils.setField(service, "visualizacaoService", visualizacaoService);
        }

        private Subprocesso criarSubprocessoComMapa(Long codigo) {
            return criarSubprocessoComMapa(codigo, TipoProcesso.MAPEAMENTO);
        }

        private Subprocesso criarSubprocessoComMapa(Long codigo, TipoProcesso tipo) {
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(codigo);
            sp.setMapa(new Mapa());
            sp.setSituacaoForcada(NAO_INICIADO);
            sp.setProcesso(Processo.builder()
                    .tipo(tipo)
                    .descricao("Processo teste")
                    .dataCriacao(LocalDateTime.of(2025, 1, 1, 10, 0))
                    .situacao(SituacaoProcesso.EM_ANDAMENTO)
                    .build());
            return sp;
        }

        private Usuario criarUsuarioMock() {
            Usuario user = new Usuario();
            user.setTituloEleitoral("12345678");
            user.setNome("Usuario Teste");
            return user;
        }

        private void stubContextoAutenticado(Usuario usuario) {
            when(usuarioFacade.contextoAutenticado()).thenReturn(new ContextoUsuarioAutenticado(
                    usuario.getTituloEleitoral(),
                    usuario.getUnidadeAtivaCodigo(),
                    usuario.getPerfilAtivo()
            ));
        }

        private void stubUltimaMovimentacaoNaUnidade(Subprocesso sp) {
            if (sp.getCodigo() == null) {
                return;
            }
            when(movimentacaoRepo.listarUltimasUnidadesDestinoPorSubprocesso(eq(sp.getCodigo()), any()))
                    .thenReturn(List.of(sp.getUnidade()));
        }

        @Nested
        @DisplayName("obterUnidadeLocalizacao")
        class ObterUnidadeLocalizacao {
            @Test
            @DisplayName("deve retornar a unidade destino da ultima movimentacao se localizacaoAtual for null")
            void deveRetornarDestinoMovimentacao() {
                Unidade u1 = new Unidade();
                u1.setCodigo(1L);
                Unidade u2 = new Unidade();
                u2.setCodigo(2L);
                Subprocesso sp = criarSubprocessoComMapa(100L);
                sp.setUnidade(u1);
                when(movimentacaoRepo.listarUltimasUnidadesDestinoPorSubprocesso(eq(100L), any()))
                        .thenReturn(List.of(u2));

                assertThat(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).isEqualTo(u2);
            }

            @Test
            @DisplayName("deve retornar unidade do subprocesso se codigo for null")
            void codNull() {
                Unidade u = new Unidade();
                Subprocesso sp = criarSubprocessoComMapa(null);
                sp.setUnidade(u);
                assertThat(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).isEqualTo(u);
            }

            @Test
            @DisplayName("deve retornar unidade do subprocesso se movimentacoes vazio")
            void movVazio() {
                Unidade u = new Unidade();
                Subprocesso sp = criarSubprocessoComMapa(1L);
                sp.setUnidade(u);
                when(movimentacaoRepo.listarUltimasUnidadesDestinoPorSubprocesso(eq(1L), any()))
                        .thenReturn(List.of());
                assertThat(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).isEqualTo(u);
            }

            @Test
            @DisplayName("deve retornar unidade quando consulta nao retorna destino")
            void destinoAusenteNaConsulta() {
                Unidade u = new Unidade();
                Subprocesso sp = criarSubprocessoComMapa(2L);
                sp.setUnidade(u);
                when(movimentacaoRepo.listarUltimasUnidadesDestinoPorSubprocesso(eq(2L), any()))
                        .thenReturn(List.of());
                assertThat(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).isEqualTo(u);
            }
        }

        @Nested
        @DisplayName("Permissões e Detalhes")
        class PermissoesEDetalhes {
            @Test
            @DisplayName("deve obter detalhes e permissoes para CHEFE em processo finalizado")
            void obterDetalhes_Chefe_ProcessoFinalizado() {
                Subprocesso sp = criarSubprocessoComMapa(1L);
                sp.setSituacaoForcada(MAPEAMENTO_MAPA_HOMOLOGADO);

                Unidade u = new Unidade();
                u.setTipo(TipoUnidade.OPERACIONAL);
                u.setCodigo(10L);
                u.setNome("Unidade 1");
                u.setSigla("U1");
                u.setTituloTitular("titular");
                sp.setUnidade(u);

                Processo p = sp.getProcesso();
                p.setSituacao(SituacaoProcesso.FINALIZADO);

                Usuario user = criarUsuarioMock();
                user.setPerfilAtivo(Perfil.CHEFE);
                user.setUnidadeAtivaCodigo(10L);

                when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(sp));
                stubContextoAutenticado(user);
                when(usuarioFacade.buscarUsuarioSemAtribuicoes("titular")).thenReturn(user);
                when(movimentacaoRepo.listarUltimasUnidadesDestinoPorSubprocesso(eq(1L), any())).thenReturn(List.of(u));
                when(unidadeService.buscarPorCodigoComSuperior(10L)).thenReturn(u);

                SubprocessoDetalheResponse res = service.obterDetalhes(1L);

                assertThat(res.permissoes().habilitarAcessoCadastro()).isTrue();
                assertThat(res.permissoes().habilitarAcessoMapa()).isTrue();
            }

            @Test
            @DisplayName("obterDetalhes com movimentacao completa")
            void obterDetalhes_MovimentacaoCompleta() {
                Subprocesso sp = criarSubprocessoComMapa(1L);
                sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
                Unidade u = new Unidade();
                u.setTipo(TipoUnidade.OPERACIONAL);
                u.setSigla("U1");
                u.setCodigo(10L);
                u.setNome("Unidade 1");
                u.setTituloTitular("titular");
                sp.setUnidade(u);

                Usuario user = criarUsuarioMock();
                user.setPerfilAtivo(Perfil.ADMIN);
                user.setUnidadeAtivaCodigo(10L);

                Unidade dest = new Unidade();
                dest.setCodigo(20L);
                dest.setSigla("U2");
                dest.setNome("Dest");
                Movimentacao mov = new Movimentacao();
                mov.setUnidadeOrigem(u);
                mov.setUnidadeDestino(dest);
                mov.setUsuario(user);

                when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(sp));
                stubContextoAutenticado(user);
                when(movimentacaoRepo.listarUltimasUnidadesDestinoPorSubprocesso(eq(1L), any())).thenReturn(List.of(dest));
                when(unidadeService.buscarPorCodigoComSuperior(10L)).thenReturn(u);
                when(usuarioFacade.buscarUsuarioSemAtribuicoes("titular")).thenReturn(user);

                SubprocessoDetalheResponse res = service.obterDetalhes(1L);
                assertThat(res.localizacaoAtual()).isEqualTo("U2");
            }

            @Test
            @DisplayName("obterDetalhes com destino null na movimentacao")
            void obterDetalhes_MovimentacaoDestinoNull() {
                Subprocesso sp = criarSubprocessoComMapa(1L);
                sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
                Unidade u = new Unidade();
                u.setTipo(TipoUnidade.OPERACIONAL);
                u.setSigla("U1");
                u.setCodigo(10L);
                u.setNome("Unidade 1");
                u.setTituloTitular("titular");
                sp.setUnidade(u);

                Usuario user = criarUsuarioMock();
                user.setPerfilAtivo(Perfil.ADMIN);
                user.setUnidadeAtivaCodigo(10L);

                Movimentacao mov = new Movimentacao();
                mov.setUnidadeOrigem(u);
                mov.setUnidadeDestino(u);
                mov.setUsuario(user);

                when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(sp));
                stubContextoAutenticado(user);
                when(movimentacaoRepo.listarUltimasUnidadesDestinoPorSubprocesso(eq(1L), any())).thenReturn(List.of(u));
                when(unidadeService.buscarPorCodigoComSuperior(10L)).thenReturn(u);
                when(usuarioFacade.buscarUsuarioSemAtribuicoes("titular")).thenReturn(user);

                SubprocessoDetalheResponse res = service.obterDetalhes(1L);
                assertThat(res.localizacaoAtual()).isEqualTo("U1");
            }

            @Test
            @DisplayName("obterDetalhes não deve buscar titular quando título for em branco")
            void obterDetalhesSemTitularQuandoTituloEmBranco() {
                Subprocesso sp = criarSubprocessoComMapa(11L);
                sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

                Unidade unidade = new Unidade();
                unidade.setCodigo(11L);
                unidade.setSigla("U11");
                unidade.setTipo(TipoUnidade.OPERACIONAL);
                unidade.setNome("Unidade 11");
                unidade.setTituloTitular("   ");
                sp.setUnidade(unidade);

                Usuario usuario = criarUsuarioMock();
                usuario.setPerfilAtivo(Perfil.ADMIN);
                usuario.setUnidadeAtivaCodigo(11L);

                ResponsavelDto responsavel = ResponsavelDto.builder().build();

                when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(11L)).thenReturn(Optional.of(sp));
                stubContextoAutenticado(usuario);
                when(movimentacaoRepo.listarUltimasUnidadesDestinoPorSubprocesso(eq(11L), any())).thenReturn(List.of(unidade));
                when(usuarioFacade.buscarResponsabilidadeDetalhadaAtual(11L)).thenReturn(responsavel);
                when(unidadeService.temMapaVigente(11L)).thenReturn(true);

                SubprocessoDetalheResponse response = service.obterDetalhes(11L);

                assertThat(response.titular()).isNull();
                verify(usuarioFacade, never()).buscarPorLogin(anyString());
            }

            @Test
            @DisplayName("obterPermissoesUI para GESTOR")
            void obterPermissoesUI_Gestor() {
                Subprocesso sp = criarSubprocessoComMapa(1L);
                sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
                Unidade u = new Unidade();
                u.setCodigo(10L);
                sp.setUnidade(u);

                Usuario user = criarUsuarioMock();
                user.setPerfilAtivo(Perfil.GESTOR);
                user.setUnidadeAtivaCodigo(20L);

                when(unidadeService.buscarPorCodigoComSuperior(20L)).thenReturn(new Unidade());
                when(unidadeService.temMapaVigente(10L)).thenReturn(true);
                when(hierarquiaService.ehMesmaOuSubordinada(any(), any())).thenReturn(true);
                stubUltimaMovimentacaoNaUnidade(sp);

                stubContextoAutenticado(user);
                PermissoesSubprocessoDto res = service.obterPermissoesUI(sp);
                assertThat(res.habilitarAcessoCadastro()).isTrue();
            }

            @Test
            @DisplayName("obterPermissoesUI para SERVIDOR mesma unidade")
            void obterPermissoesUI_Servidor() {
                Subprocesso sp = criarSubprocessoComMapa(1L);
                sp.setSituacaoForcada(MAPEAMENTO_MAPA_DISPONIBILIZADO);
                Unidade u = new Unidade();
                u.setCodigo(10L);
                sp.setUnidade(u);

                Usuario user = criarUsuarioMock();
                user.setPerfilAtivo(Perfil.SERVIDOR);
                user.setUnidadeAtivaCodigo(10L);

                when(unidadeService.buscarPorCodigoComSuperior(10L)).thenReturn(u);
                when(unidadeService.temMapaVigente(10L)).thenReturn(false);
                stubUltimaMovimentacaoNaUnidade(sp);

                stubContextoAutenticado(user);
                PermissoesSubprocessoDto res = service.obterPermissoesUI(sp);
                assertThat(res.habilitarAcessoMapa()).isTrue();
            }

            @Test
            @DisplayName("obterPermissoesUI para ADMIN sem processo e com movimentacao de destino")
            void obterPermissoesUI_Admin() {
                Subprocesso sp = criarSubprocessoComMapa(1L);
                sp.setSituacaoForcada(REVISAO_MAPA_COM_SUGESTOES);
                Unidade u = new Unidade();
                u.setCodigo(10L);
                sp.setUnidade(u);

                Unidade dest = new Unidade();
                dest.setCodigo(30L);
                when(movimentacaoRepo.listarUltimasUnidadesDestinoPorSubprocesso(eq(1L), any()))
                        .thenReturn(List.of(dest));

                Usuario user = criarUsuarioMock();
                user.setPerfilAtivo(Perfil.ADMIN);
                user.setUnidadeAtivaCodigo(20L);

                when(unidadeService.buscarPorCodigoComSuperior(20L)).thenReturn(new Unidade());
                when(unidadeService.temMapaVigente(10L)).thenReturn(true);

                stubContextoAutenticado(user);
                PermissoesSubprocessoDto res = service.obterPermissoesUI(sp);
                assertThat(res.habilitarAcessoMapa()).isTrue();
            }

            @Test
            @DisplayName("obterPermissoesUI situacao DIAGNOSTICO")
            void obterPermissoesUI_Diagnostico() {
                Subprocesso sp = criarSubprocessoComMapa(1L);
                sp.setSituacaoForcada(DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO);
                Unidade u = new Unidade();
                u.setCodigo(10L);
                sp.setUnidade(u);

                Usuario user = criarUsuarioMock();
                user.setPerfilAtivo(Perfil.ADMIN);
                user.setUnidadeAtivaCodigo(10L);

                when(unidadeService.buscarPorCodigoComSuperior(10L)).thenReturn(new Unidade());
                when(unidadeService.temMapaVigente(10L)).thenReturn(true);
                stubUltimaMovimentacaoNaUnidade(sp);

                stubContextoAutenticado(user);
                PermissoesSubprocessoDto res = service.obterPermissoesUI(sp);
                assertThat(res.habilitarAcessoCadastro()).isFalse();
                assertThat(res.habilitarAcessoMapa()).isFalse();
                assertThat(res.podeReabrirCadastro()).isTrue();
                assertThat(res.habilitarReabrirCadastro()).isFalse();
                assertThat(res.podeReabrirRevisao()).isTrue();
                assertThat(res.habilitarReabrirRevisao()).isFalse();
            }

            @Test
            @DisplayName("obterPermissoesUI para ADMIN em REVISAO")
            void obterPermissoesUI_Admin_Revisao() {
                Subprocesso sp = criarSubprocessoComMapa(1L);
                sp.setSituacaoForcada(REVISAO_MAPA_HOMOLOGADO);
                Unidade u = new Unidade();
                u.setCodigo(10L);
                sp.setUnidade(u);

                Usuario user = criarUsuarioMock();
                user.setPerfilAtivo(Perfil.ADMIN);
                user.setUnidadeAtivaCodigo(10L);

                when(unidadeService.buscarPorCodigoComSuperior(10L)).thenReturn(new Unidade());
                when(unidadeService.temMapaVigente(10L)).thenReturn(true);
                stubUltimaMovimentacaoNaUnidade(sp);

                stubContextoAutenticado(user);
                PermissoesSubprocessoDto res = service.obterPermissoesUI(sp);
                assertThat(res.habilitarAcessoCadastro()).isTrue();
                assertThat(res.habilitarAcessoMapa()).isTrue();
                assertThat(res.podeReabrirRevisao()).isTrue();
            }

            @Test
            @DisplayName("obterPermissoesUI para GESTOR falha hierarquia")
            void obterPermissoesUI_Gestor_FalhaHierarquia() {
                Subprocesso sp = criarSubprocessoComMapa(1L);
                sp.setSituacaoForcada(REVISAO_MAPA_DISPONIBILIZADO);
                Unidade u = new Unidade();
                u.setCodigo(10L);
                sp.setUnidade(u);

                Usuario user = criarUsuarioMock();
                user.setPerfilAtivo(Perfil.GESTOR);
                user.setUnidadeAtivaCodigo(20L);

                when(unidadeService.buscarPorCodigoComSuperior(20L)).thenReturn(new Unidade());
                when(unidadeService.temMapaVigente(10L)).thenReturn(true);
                when(hierarquiaService.ehMesmaOuSubordinada(any(), any())).thenReturn(false);
                stubUltimaMovimentacaoNaUnidade(sp);

                stubContextoAutenticado(user);
                PermissoesSubprocessoDto res = service.obterPermissoesUI(sp);
                assertThat(res.habilitarAcessoCadastro()).isFalse();
                assertThat(res.habilitarAcessoMapa()).isFalse();
            }

            @Test
            @DisplayName("obterPermissoesUI isChefe false branch e isAdmin false branch em situacao especifica")
            void obterPermissoesUI_FalseBranches() {
                Subprocesso sp = criarSubprocessoComMapa(1L);
                sp.setSituacaoForcada(NAO_INICIADO);
                Unidade u = new Unidade();
                u.setCodigo(10L);
                sp.setUnidade(u);

                Usuario user = criarUsuarioMock();
                user.setPerfilAtivo(Perfil.SERVIDOR);
                user.setUnidadeAtivaCodigo(10L);

                when(unidadeService.buscarPorCodigoComSuperior(10L)).thenReturn(u);
                when(unidadeService.temMapaVigente(10L)).thenReturn(true);

                stubContextoAutenticado(user);
                PermissoesSubprocessoDto res = service.obterPermissoesUI(sp);
                assertThat(res.podeValidarMapa()).isFalse();
                assertThat(res.podeDisponibilizarMapa()).isFalse();
            }

            @Test
            @DisplayName("obterPermissoesUI processo null")
            void obterPermissoesUI_ProcessoNull() {
                Subprocesso sp = new Subprocesso();
                sp.setProcesso(null);
                sp.setSituacaoForcada(NAO_INICIADO);
                sp.setUnidade(new Unidade());
                sp.setCodigo(1L);

                Usuario user = criarUsuarioMock();
                user.setUnidadeAtivaCodigo(10L);
                user.setPerfilAtivo(Perfil.ADMIN);

                when(unidadeService.buscarPorCodigoComSuperior(10L)).thenReturn(new Unidade());

                stubContextoAutenticado(user);
                PermissoesSubprocessoDto res = service.obterPermissoesUI(sp);
                assertThat(res).isNotNull();
            }

            @Test
            @DisplayName("obterPermissoesUI Gestor em unidade subordinada")
            void obterPermissoesUI_GestorSubordinada() {
                Subprocesso sp = criarSubprocessoComMapa(1L);
                sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
                Unidade uAlvo = new Unidade();
                uAlvo.setCodigo(10L);
                sp.setUnidade(uAlvo);

                Usuario user = criarUsuarioMock();
                user.setUnidadeAtivaCodigo(20L);
                user.setPerfilAtivo(Perfil.GESTOR);
                Unidade uGestor = new Unidade();
                uGestor.setCodigo(20L);

                when(unidadeService.buscarPorCodigoComSuperior(20L)).thenReturn(uGestor);
                when(hierarquiaService.ehMesmaOuSubordinada(uAlvo, uGestor)).thenReturn(true);
                stubUltimaMovimentacaoNaUnidade(sp);

                stubContextoAutenticado(user);
                PermissoesSubprocessoDto res = service.obterPermissoesUI(sp);
                assertTrue(res.habilitarAcessoCadastro());
            }

            @Test
            @DisplayName("obterPermissoesUI Servidor em unidade diferente")
            void obterPermissoesUI_ServidorDiferente() {
                Subprocesso sp = criarSubprocessoComMapa(1L);
                sp.setSituacaoForcada(MAPEAMENTO_MAPA_DISPONIBILIZADO);
                Unidade uAlvo = new Unidade();
                uAlvo.setCodigo(10L);
                sp.setUnidade(uAlvo);

                Usuario user = criarUsuarioMock();
                user.setUnidadeAtivaCodigo(20L);
                user.setPerfilAtivo(Perfil.SERVIDOR);
                Unidade uUser = new Unidade();
                uUser.setCodigo(20L);

                when(unidadeService.buscarPorCodigoComSuperior(20L)).thenReturn(uUser);
                stubUltimaMovimentacaoNaUnidade(sp);

                stubContextoAutenticado(user);
                PermissoesSubprocessoDto res = service.obterPermissoesUI(sp);
                assertFalse(res.habilitarAcessoMapa());
            }

            @Test
            @DisplayName("obterPermissoesUI situações de revisão para visualização")
            void obterPermissoesUI_SituacoesRevisaoVisualizacao() {
                Subprocesso sp = criarSubprocessoComMapa(1L, TipoProcesso.REVISAO);
                sp.setSituacaoForcada(REVISAO_CADASTRO_DISPONIBILIZADA);
                Unidade u = new Unidade();
                u.setCodigo(10L);
                sp.setUnidade(u);

                Usuario user = criarUsuarioMock();
                user.setPerfilAtivo(Perfil.ADMIN);
                user.setUnidadeAtivaCodigo(10L);
                when(unidadeService.buscarPorCodigoComSuperior(10L)).thenReturn(u);
                stubUltimaMovimentacaoNaUnidade(sp);

                stubContextoAutenticado(user);
                PermissoesSubprocessoDto res = service.obterPermissoesUI(sp);
                assertTrue(res.habilitarAcessoCadastro());

                sp.setSituacaoForcada(REVISAO_MAPA_DISPONIBILIZADO);
                res = service.obterPermissoesUI(sp);
                assertTrue(res.habilitarAcessoMapa());
            }

            @Test
            @DisplayName("obterPermissoesUI podeDisponibilizarMapa varied situations")
            void obterPermissoesUI_PodeDisponibilizarMapa() {
                Subprocesso sp = criarSubprocessoComMapa(1L);
                Unidade u = new Unidade();
                u.setCodigo(10L);
                sp.setUnidade(u);
                Usuario user = criarUsuarioMock();
                user.setPerfilAtivo(Perfil.ADMIN);
                user.setUnidadeAtivaCodigo(10L);
                when(unidadeService.buscarPorCodigoComSuperior(10L)).thenReturn(u);
                stubUltimaMovimentacaoNaUnidade(sp);
                stubContextoAutenticado(user);

                for (SituacaoSubprocesso s : List.of(MAPEAMENTO_CADASTRO_HOMOLOGADO, MAPEAMENTO_MAPA_COM_SUGESTOES, REVISAO_MAPA_AJUSTADO)) {
                    sp.setSituacaoForcada(s);
                    assertTrue(service.obterPermissoesUI(sp).podeDisponibilizarMapa());
                }
            }

            @Test
            @DisplayName("obterPermissoesUI podeValidarMapa para CHEFE em situação de análise")
            void obterPermissoesUI_ChefeAnalise() {
                Subprocesso sp = criarSubprocessoComMapa(1L);
                sp.setSituacaoForcada(MAPEAMENTO_MAPA_DISPONIBILIZADO);
                Unidade u = new Unidade();
                u.setCodigo(10L);
                sp.setUnidade(u);

                Usuario user = criarUsuarioMock();
                user.setPerfilAtivo(Perfil.CHEFE);
                user.setUnidadeAtivaCodigo(10L);

                when(unidadeService.buscarPorCodigoComSuperior(10L)).thenReturn(u);
                stubUltimaMovimentacaoNaUnidade(sp);

                stubContextoAutenticado(user);
                PermissoesSubprocessoDto res = service.obterPermissoesUI(sp);
                assertThat(res.podeValidarMapa()).isTrue();
                assertThat(res.podeApresentarSugestoes()).isTrue();
            }
        }

        @Nested
        @DisplayName("listarPorProcessoEUnidadeCodigosESituacoes")
        class ListarPorProcessoEUnidadeCodigosESituacoes {
            @Test
            @DisplayName("deve filtrar por situacoes")
            void deveFiltrar() {
                Subprocesso sp1 = criarSubprocessoComMapa(null);
                sp1.setSituacaoForcada(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
                Subprocesso sp2 = criarSubprocessoComMapa(null);
                sp2.setSituacaoForcada(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

                when(subprocessoRepo.listarPorProcessoEUnidadesComUnidade(1L, List.of(10L, 11L)))
                        .thenReturn(List.of(sp1, sp2));

                List<Subprocesso> res = service.listarPorProcessoEUnidadeCodigosESituacoes(1L, List.of(10L, 11L), List.of(MAPEAMENTO_CADASTRO_EM_ANDAMENTO));

                assertThat(res).containsExactly(sp1);
            }
        }

        @Nested
        @DisplayName("listarAtividadesParaImportacao")
        class ListarAtividadesParaImportacao {
            @Test
            @DisplayName("deve lancar erro se processo nao finalizado")
            void processoNaoFinalizado() {
                Subprocesso sp = criarSubprocessoComMapa(null);
                Processo p = sp.getProcesso();
                p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
                when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(sp));
                assertThrows(ErroValidacao.class, () -> service.listarAtividadesParaImportacao(1L));
            }

            @Test
            @DisplayName("deve lancar erro se processo for null")
            void processoNull() {
                Subprocesso sp = new Subprocesso();
                sp.setProcesso(null);
                when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(sp));
                assertThrows(ErroValidacao.class, () -> service.listarAtividadesParaImportacao(1L));
            }

            @Test
            @DisplayName("deve retornar lista de atividades")
            void deveRetornarLista() {
                Subprocesso sp = criarSubprocessoComMapa(1L);
                sp.getProcesso().setSituacao(SituacaoProcesso.FINALIZADO);
                Mapa mapa = sp.getMapa();
                mapa.setCodigo(10L);

                when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(sp));
                when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(10L)).thenReturn(List.of());

                service.listarAtividadesParaImportacao(1L);
                verify(mapaManutencaoService).atividadesMapaCodigoComConhecimentos(10L);
            }
        }

        @Nested
        @DisplayName("Análises e Histórico")
        class AnalisesEHistorico {
            @Test
            @DisplayName("listarHistoricoCadastro filtra analises de cadastro")
            void listarHistoricoCadastroFiltraAnalises() {
                Analise a1 = new Analise();
                a1.setTipo(TipoAnalise.CADASTRO);
                a1.setUnidadeCodigo(10L);
                a1.setUsuarioTitulo("analista1");
                a1.setAcao(TipoAcaoAnalise.ACEITE_MAPEAMENTO);
                Analise a2 = new Analise();
                a2.setTipo(TipoAnalise.VALIDACAO);
                a2.setUnidadeCodigo(10L);
                a2.setUsuarioTitulo("analista2");
                a2.setAcao(TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO);
                when(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(1L)).thenReturn(List.of(a1, a2));
                when(unidadeService.buscarResumosPorCodigos(List.of(10L))).thenReturn(List.of(
                        new UnidadeResumoLeitura(10L, "Unidade 10", "U10", TipoUnidade.OPERACIONAL)
                ));
                when(usuarioService.buscarConsultasPorTitulos(any())).thenReturn(List.of(
                        new UsuarioConsultaLeitura("analista1", "mat1", "Analista 1", "email", "ramal", 10L, "U10", "U10", TipoUnidade.OPERACIONAL, "tit1", 10L),
                        new UsuarioConsultaLeitura("analista2", "mat2", "Analista 2", "email", "ramal", 10L, "U10", "U10", TipoUnidade.OPERACIONAL, "tit1", 10L)
                ));

                assertThat(service.listarHistoricoCadastro(1L)).hasSize(1);
            }

            @Test
            @DisplayName("listarHistoricoValidacao filtra analises de validacao")
            void listarHistoricoValidacaoFiltraAnalises() {
                Analise a1 = new Analise();
                a1.setTipo(TipoAnalise.CADASTRO);
                a1.setUnidadeCodigo(10L);
                a1.setUsuarioTitulo("analista1");
                a1.setAcao(TipoAcaoAnalise.ACEITE_MAPEAMENTO);
                Analise a2 = new Analise();
                a2.setTipo(TipoAnalise.VALIDACAO);
                a2.setUnidadeCodigo(10L);
                a2.setUsuarioTitulo("analista2");
                a2.setAcao(TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO);
                when(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(1L)).thenReturn(List.of(a1, a2));
                when(unidadeService.buscarResumosPorCodigos(List.of(10L))).thenReturn(List.of(
                        new UnidadeResumoLeitura(10L, "Unidade 10", "U10", TipoUnidade.OPERACIONAL)
                ));
                when(usuarioService.buscarConsultasPorTitulos(any())).thenReturn(List.of(
                        new UsuarioConsultaLeitura("analista1", "mat1", "Analista 1", "email", "ramal", 10L, "U10", "U10", TipoUnidade.OPERACIONAL, "tit1", 10L),
                        new UsuarioConsultaLeitura("analista2", "mat2", "Analista 2", "email", "ramal", 10L, "U10", "U10", TipoUnidade.OPERACIONAL, "tit1", 10L)
                ));

                assertThat(service.listarHistoricoValidacao(1L)).hasSize(1);
            }

            @Test
            @DisplayName("listarHistoricoCadastro e Validacao")
            void listarHistoricos() {
                Analise a1 = new Analise();
                a1.setTipo(TipoAnalise.CADASTRO);
                a1.setUnidadeCodigo(10L);
                a1.setUsuarioTitulo("analista1");
                a1.setAcao(TipoAcaoAnalise.ACEITE_MAPEAMENTO);
                Analise a2 = new Analise();
                a2.setTipo(TipoAnalise.VALIDACAO);
                a2.setUnidadeCodigo(10L);
                a2.setUsuarioTitulo("analista2");
                a2.setAcao(TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO);
                when(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(1L)).thenReturn(List.of(a1, a2));
                when(unidadeService.buscarResumosPorCodigos(List.of(10L))).thenReturn(List.of(
                        new UnidadeResumoLeitura(10L, "Unidade 10", "U10", TipoUnidade.OPERACIONAL)
                ));
                when(usuarioService.buscarConsultasPorTitulos(anyCollection())).thenReturn(List.of(
                        new UsuarioConsultaLeitura("analista1", "mat1", "Analista 1", "email", "ramal", 10L, "U10", "U10", TipoUnidade.OPERACIONAL, "tit1", 10L),
                        new UsuarioConsultaLeitura("analista2", "mat2", "Analista 2", "email", "ramal", 10L, "U10", "U10", TipoUnidade.OPERACIONAL, "tit1", 10L)
                ));

                assertThat(service.listarHistoricoCadastro(1L)).hasSize(1);
                assertThat(service.listarHistoricoValidacao(1L)).hasSize(1);
            }
        }

        @Nested
        @DisplayName("Localização Atual")
        class LocalizacaoAtual {
            @Test
            @DisplayName("obterLocalizacaoAtual - varios branches")
            void obterLocalizacaoAtual_Branches() {
                Subprocesso sp = new Subprocesso();
                Unidade u = new Unidade();
                sp.setUnidade(u);
                sp.setSituacaoForcada(NAO_INICIADO);

                assertThat(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).isEqualTo(u);

                sp.setCodigo(null);
                assertThat(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).isEqualTo(u);

                sp.setCodigo(1L);
                Unidade dest = new Unidade();
                when(movimentacaoRepo.listarUltimasUnidadesDestinoPorSubprocesso(eq(1L), any()))
                        .thenReturn(List.of(dest));
                assertThat(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).isEqualTo(dest);

                when(movimentacaoRepo.listarUltimasUnidadesDestinoPorSubprocesso(eq(1L), any()))
                        .thenReturn(List.of());
                assertThat(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).isEqualTo(u);
            }

            @Test
            @DisplayName("obterLocalizacaoAtual deve falhar para subprocesso persistido sem movimentação fora de NAO_INICIADO")
            void obterLocalizacaoAtual_DeveFalharSemMovimentacaoForaEstadoInicial() {
                Subprocesso sp = new Subprocesso();
                Unidade u = new Unidade();
                sp.setUnidade(u);
                sp.setCodigo(1L);
                sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

                when(movimentacaoRepo.listarUltimasUnidadesDestinoPorSubprocesso(eq(1L), any()))
                        .thenReturn(List.of());

                assertThatThrownBy(() -> localizacaoSubprocessoService.obterLocalizacaoAtual(sp))
                        .isInstanceOf(ErroValidacao.class)
                        .hasMessageContaining("Subprocesso persistido sem movimentação");
            }
        }
    }
}
