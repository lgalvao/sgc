package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.data.domain.*;
import org.springframework.security.core.*;
import sgc.alerta.*;
import sgc.alerta.model.*;
import sgc.comum.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.configuracoes.*;
import sgc.fixture.*;
import sgc.mapa.model.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;
import sgc.seguranca.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;
import sgc.subprocesso.service.SubprocessoValidacaoService.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sgc.processo.model.AcaoProcesso.*;
import static sgc.processo.model.SituacaoProcesso.*;
import static sgc.processo.model.TipoProcesso.*;
import static sgc.seguranca.AcaoPermissao.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoService Test suite")
@SuppressWarnings("NullAway.Init")
class ProcessoServiceTest {
    @InjectMocks
    private ProcessoService processoService;
    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private ComumRepo repo;
    @Mock
    private UnidadeHierarquiaService unidadeHierarquiaService;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private ResponsavelUnidadeService responsavelUnidadeService;
    @Mock
    private SubprocessoService subprocessoService;
    @Mock
    private SubprocessoConsultaService consultaService;
    @Mock
    private LocalizacaoSubprocessoService localizacaoSubprocessoService;
    @Mock
    private SubprocessoValidacaoService validacaoService;
    @Mock
    private UsuarioFacade usuarioService;
    @Mock
    private AlertaFacade servicoAlertas;
    @Mock
    private NotificacaoService notificacaoService;
    @Mock
    private EmailModelosService emailModelosService;
    @Mock
    private SgcPermissionEvaluator permissionEvaluator;
    @Mock
    private SubprocessoTransicaoService transicaoService;
    @Mock
    private CadastroFluxoService cadastroFluxoService;
    @Mock
    private ConfiguracaoService configuracaoService;
    @Mock
    private MapaRepo mapaRepo;

    private void mockarResponsaveisEfetivos() {
        when(responsavelUnidadeService.todasPossuemResponsavelEfetivo(anyList())).thenReturn(true);
    }

    private Unidade criarUnidadeValida(Long codigo) {
        Unidade unidade = new Unidade();
        unidade.setCodigo(codigo);
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        unidade.setSigla("U" + codigo);
        unidade.setNome("Unidade " + codigo);
        return unidade;
    }

    @Test
    @DisplayName("executarAcaoEmBloco nao executa transicoes quando subprocesso nao eh elegivel")
    void executarAcaoEmBloco_NaoExecutaTransicoesQuandoNaoElegivel() {
        Long codProcesso = 1L;
        ProcessarAnaliseEmBlocoCommand req = new ProcessarAnaliseEmBlocoCommand(
                List.of(10L),
                ACEITAR
        );

        Usuario usuario = new Usuario();
        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

        Subprocesso sub = Subprocesso.builder()
                .codigo(100L)
                .unidade(Unidade.builder().codigo(10L).build())
                .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                .build();

        when(consultaService.listarEntidadesPorProcessoEUnidades(codProcesso, req.unidadeCodigos()))
                .thenReturn(List.of(sub));

        Subprocesso subNaoElegivel = Subprocesso.builder()
                .codigo(101L)
                .unidade(Unidade.builder().codigo(10L).build())
                .situacao(SituacaoSubprocesso.NAO_INICIADO)
                .build();
        when(consultaService.listarEntidadesPorProcessoEUnidades(codProcesso, req.unidadeCodigos()))
                .thenReturn(List.of(subNaoElegivel));

        when(permissionEvaluator.verificarPermissao(eq(usuario), anyList(), any())).thenReturn(true);

        processoService.executarAcaoEmBloco(codProcesso, req);

        verify(cadastroFluxoService, never()).aceitarCadastroEmBloco(any());
        verify(cadastroFluxoService, never()).homologarCadastroEmBloco(any());
    }

    @Nested
    @DisplayName("Cobertura e Casos de Borda")
    class CoverageTests {

        @Test
        @DisplayName("buscarIdsUnidadesComProcessosAtivos deve delegar para repo")
        void buscarIdsUnidadesComProcessosAtivos_DeveDelegar() {
            Long codigoIgnorar = 1L;
            when(processoRepo.listarUnidadesEmSituacoesExcetoProcesso(anyList(), eq(codigoIgnorar)))
                    .thenReturn(List.of(10L, 20L));

            Set<Long> resultado = processoService.buscarIdsUnidadesComProcessosAtivos(codigoIgnorar);

            assertThat(resultado).containsExactlyInAnyOrder(10L, 20L);
        }

    @Nested
    @DisplayName("Segurança e Controle de Acesso")
    class SecurityTests {
        @Test
        @DisplayName("Deve negar acesso quando usuário não autenticado")
        void deveNegarAcessoQuandoNaoAutenticado() {
            Authentication auth = mock(Authentication.class);
            // Assume permissionEvaluator handles this
            assertThat(processoService.checarAcesso(auth, 1L)).isFalse();
        }
    }

    @Nested
    @DisplayName("Workflow e Inicialização")
    class Workflow {
        @Test
        @DisplayName("Deve iniciar mapeamento com sucesso e salvar")
        void deveIniciarMapeamentoComSucesso() {
            Long id = 100L;

            Processo p = new Processo();
            p.setCodigo(id);
            p.setSituacao(SituacaoProcesso.CRIADO);
            p.setTipo(TipoProcesso.MAPEAMENTO);
            p.setDataLimite(LocalDateTime.now().plusDays(30));
            Unidade uni = criarUnidadeValida(10L);
            p.adicionarParticipantes(Set.of(uni));

            when(repo.buscar(Processo.class, id)).thenReturn(p);
            Unidade uniAdmin = new Unidade();
            uniAdmin.setSituacao(SituacaoUnidade.ATIVA);
            when(unidadeService.buscarAdmin()).thenReturn(uniAdmin);
            mockarResponsaveisEfetivos();

            processoService.iniciar(id, List.of());

            verify(processoRepo).save(any(Processo.class));
        }

        @Test
        @DisplayName("Deve iniciar revisao com sucesso e salvar")
        void deveIniciarRevisaoComSucesso() {
            Long id = 100L;

            Processo p = new Processo();
            p.setCodigo(id);
            p.setSituacao(SituacaoProcesso.CRIADO);
            p.setTipo(TipoProcesso.REVISAO);
            p.setDataLimite(LocalDateTime.now().plusDays(30));
            Unidade uni = criarUnidadeValida(1L);
            p.adicionarParticipantes(Set.of(uni));

            when(repo.buscar(Processo.class, id)).thenReturn(p);
            when(unidadeService.buscarPorCodigos(anyList())).thenReturn(List.of(uni));
            when(unidadeService.buscarTodosCodigosUnidadesComMapa()).thenReturn(List.of(1L));

            UnidadeMapa um = new UnidadeMapa();
            um.setUnidadeCodigo(1L);
            when(unidadeService.buscarMapasPorUnidades(anyList())).thenReturn(List.of(um));

            Unidade uniAdmin = new Unidade();
            uniAdmin.setSituacao(SituacaoUnidade.ATIVA);
            when(unidadeService.buscarAdmin()).thenReturn(uniAdmin);
            mockarResponsaveisEfetivos();

            processoService.iniciar(id, List.of(1L));

            verify(processoRepo).save(any(Processo.class));
            verify(subprocessoService).criarParaRevisao(argThat(command ->
                    command.processo() == p
                            && command.unidade() == uni
                            && command.unidadeMapa() == um
                            && command.unidadeOrigem() == uniAdmin));
        }

        @Test
        @DisplayName("Deve iniciar revisao ignorando unidade ancestral selecionada junto com a descendente")
        void deveIniciarRevisaoIgnorandoAncestralRedundante() {
            Long id = 101L;
            Processo processo = new Processo();
            processo.setCodigo(id);
            processo.setSituacao(SituacaoProcesso.CRIADO);
            processo.setTipo(TipoProcesso.REVISAO);
            processo.setDataLimite(LocalDateTime.now().plusDays(30));

            Unidade unidadePai = criarUnidadeValida(10L);
            Unidade unidadeFilha = criarUnidadeValida(20L);
            processo.adicionarParticipantes(Set.of(unidadePai, unidadeFilha));

            when(repo.buscar(Processo.class, id)).thenReturn(processo);
            when(unidadeHierarquiaService.buscarMapaFilhoPai()).thenReturn(Map.of(20L, 10L));
            when(unidadeHierarquiaService.buscarCodigosSuperiores(20L)).thenReturn(List.of(10L));
            when(unidadeService.buscarPorCodigos(List.of(10L, 20L))).thenReturn(List.of(unidadePai, unidadeFilha));
            when(unidadeService.buscarPorCodigos(List.of(20L))).thenReturn(List.of(unidadeFilha));
            when(unidadeService.buscarTodosCodigosUnidadesComMapa()).thenReturn(List.of(20L));
            when(unidadeService.buscarMapasPorUnidades(List.of(20L))).thenReturn(List.of(
                    UnidadeMapa.builder().unidadeCodigo(20L).build()
            ));
            when(unidadeService.buscarPorCodigos(List.of(10L))).thenReturn(List.of(unidadePai));
            Unidade admin = criarUnidadeValida(999L);
            when(unidadeService.buscarAdmin()).thenReturn(admin);
            mockarResponsaveisEfetivos();

            processoService.iniciar(id, List.of(10L, 20L));

            verify(subprocessoService, times(1)).criarParaRevisao(any());
            verify(subprocessoService).criarParaRevisao(argThat(command ->
                    command.processo() == processo
                            && command.unidade().getCodigo().equals(20L)
            ));
        }

        @Test
        @DisplayName("Deve iniciar revisao mantendo unidade interoperacional selecionada junto com a descendente")
        void deveIniciarRevisaoMantendoInteroperacionalSelecionada() {
            Long id = 102L;
            Processo processo = new Processo();
            processo.setCodigo(id);
            processo.setSituacao(SituacaoProcesso.CRIADO);
            processo.setTipo(TipoProcesso.REVISAO);
            processo.setDataLimite(LocalDateTime.now().plusDays(30));

            Unidade unidadePai = criarUnidadeValida(10L);
            unidadePai.setTipo(TipoUnidade.INTEROPERACIONAL);
            unidadePai.setSigla("STIC");

            Unidade unidadeFilha = criarUnidadeValida(20L);
            unidadeFilha.setSigla("SEDIA");
            processo.adicionarParticipantes(Set.of(unidadePai, unidadeFilha));

            UnidadeMapa mapaPai = UnidadeMapa.builder().unidadeCodigo(10L).build();
            UnidadeMapa mapaFilha = UnidadeMapa.builder().unidadeCodigo(20L).build();

            when(repo.buscar(Processo.class, id)).thenReturn(processo);
            when(unidadeHierarquiaService.buscarMapaFilhoPai()).thenReturn(Map.of(20L, 10L));
            when(unidadeHierarquiaService.buscarCodigosSuperiores(10L)).thenReturn(List.of());
            when(unidadeHierarquiaService.buscarCodigosSuperiores(20L)).thenReturn(List.of(10L));
            when(unidadeService.buscarPorCodigos(List.of(10L, 20L))).thenReturn(List.of(unidadePai, unidadeFilha));
            when(unidadeService.buscarPorCodigos(List.of(10L))).thenReturn(List.of(unidadePai));
            when(unidadeService.buscarMapasPorUnidades(argThat(codigos ->
                    codigos.size() == 2
                            && codigos.contains(10L)
                            && codigos.contains(20L)
            ))).thenReturn(List.of(mapaPai, mapaFilha));
            Unidade admin = criarUnidadeValida(999L);
            when(unidadeService.buscarAdmin()).thenReturn(admin);
            mockarResponsaveisEfetivos();

            processoService.iniciar(id, List.of(10L, 20L));

            verify(subprocessoService, times(2)).criarParaRevisao(any());
            verify(subprocessoService).criarParaRevisao(argThat(command ->
                    command.processo() == processo
                            && command.unidade().getCodigo().equals(10L)
                            && command.unidadeMapa() == mapaPai
            ));
            verify(subprocessoService).criarParaRevisao(argThat(command ->
                    command.processo() == processo
                            && command.unidade().getCodigo().equals(20L)
                            && command.unidadeMapa() == mapaFilha
            ));
        }

        @Test
        @DisplayName("Deve vincular subprocesso nas notificacoes de inicio para exibir no painel de notificacoes")
        void deveVincularSubprocessoNasNotificacoesDeInicio() {
            Long id = 103L;
            Processo processo = new Processo();
            processo.setCodigo(id);
            processo.setSituacao(SituacaoProcesso.CRIADO);
            processo.setTipo(TipoProcesso.REVISAO);
            processo.setDescricao("Processo notificacoes");
            processo.setDataLimite(LocalDateTime.now().plusDays(30));

            Unidade unidade = criarUnidadeValida(10L);
            processo.adicionarParticipantes(Set.of(unidade));

            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setCodigo(501L);
            subprocesso.setProcesso(processo);
            subprocesso.setUnidade(unidade);

            when(repo.buscar(Processo.class, id)).thenReturn(processo);
            when(unidadeService.buscarAdmin()).thenReturn(criarUnidadeValida(999L));
            when(unidadeService.buscarPorCodigos(List.of(10L))).thenReturn(List.of(unidade));
            when(unidadeService.buscarTodosCodigosUnidadesComMapa()).thenReturn(List.of(10L));
            when(unidadeService.buscarMapasPorUnidades(List.of(10L))).thenReturn(List.of(
                    UnidadeMapa.builder().unidadeCodigo(10L).build()
            ));
            when(consultaService.listarEntidadesPorProcesso(id)).thenReturn(List.of(subprocesso));
            when(emailModelosService.criarEmailInicioProcessoConsolidado(anyString(), anyString(), any(), anyString(), anyBoolean(), anyList()))
                    .thenReturn("<html>inicio</html>");
            mockarResponsaveisEfetivos();

            processoService.iniciar(id, List.of(10L));

            verify(notificacaoService).enfileirar(argThat(cmd ->
                    cmd.tipoNotificacao() == TipoNotificacao.PROCESSO_INICIADO
                            && cmd.subprocesso() != null
                            && Objects.equals(cmd.subprocesso().getCodigo(), 501L)
            ));
        }

        @Test
        @DisplayName("Deve falhar ao iniciar processo se houver unidades em processo ativo")
        void deveFalharAoIniciarSeHouverUnidadesEmProcessoAtivo() {
            Long id = 100L;

            Processo p = new Processo();
            p.setCodigo(id);
            p.setSituacao(SituacaoProcesso.CRIADO);
            p.setTipo(TipoProcesso.MAPEAMENTO);
            Unidade uni = criarUnidadeValida(1L);
            p.adicionarParticipantes(Set.of(uni));

            when(repo.buscar(Processo.class, id)).thenReturn(p);
            // Simular que a unidade já está em outro processo
            when(processoRepo.listarUnidadesEmProcessoAtivo(eq(SituacaoProcesso.EM_ANDAMENTO), anyList()))
                    .thenReturn(List.of(1L));
            mockarResponsaveisEfetivos();

            List<Long> unidades = List.of();
            assertThatThrownBy(() -> processoService.iniciar(id, unidades))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining(Mensagens.UNIDADES_EM_PROCESSO_ATIVO);
        }

        @Test
        @DisplayName("Deve falhar ao iniciar processo se houver unidades sem mapa em REVISAO")
        void deveFalharAoIniciarSeHouverUnidadesSemMapaEmRevisao() {
            Long id = 100L;

            Processo p = new Processo();
            p.setCodigo(id);
            p.setSituacao(SituacaoProcesso.CRIADO);
            p.setTipo(TipoProcesso.REVISAO);
            p.setDataLimite(LocalDateTime.now().plusDays(30));
            Unidade uni = criarUnidadeValida(1L);
            p.adicionarParticipantes(Set.of(uni));

            when(repo.buscar(Processo.class, id)).thenReturn(p);
            when(unidadeService.buscarPorCodigos(anyList())).thenReturn(List.of(uni));

            // Simular que a unidade não tem mapa vigente
            when(unidadeService.buscarTodosCodigosUnidadesComMapa()).thenReturn(List.of());
            when(unidadeService.buscarSiglasPorCodigos(anyList())).thenReturn(List.of("U1"));
            mockarResponsaveisEfetivos();

            List<Long> unidades = List.of(1L);
            assertThatThrownBy(() -> processoService.iniciar(id, unidades))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining(Mensagens.UNIDADES_SEM_MAPA);
        }

        @Test
        @DisplayName("Deve finalizar processo criando notificacao por email")
        void deveFinalizarProcessoQuandoTudoHomologado() {
            Long id = 100L;
            Processo p = new Processo();
            p.setCodigo(id);
            p.setDescricao("Mapeamento 2026");
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            p.setTipo(TipoProcesso.DIAGNOSTICO);
            Unidade unidade = criarUnidadeValida(10L);
            unidade.setSigla("SECAO");
            p.adicionarParticipantes(Set.of(unidade));

            when(repo.buscar(Processo.class, id)).thenReturn(p);
            when(unidadeService.buscarPorCodigos(anyList())).thenReturn(List.of(unidade));
            when(unidadeHierarquiaService.buscarCodigosSuperiores(10L)).thenReturn(List.of());
            when(emailModelosService.criarEmailProcessoFinalizadoPorUnidade("SECAO", "Mapeamento 2026"))
                    .thenReturn("<html>finalizado</html>");
            when(validacaoService.validarSubprocessosParaFinalizacao(id))
                    .thenReturn(ResultadoValidacao.ofValido());

            processoService.finalizar(id);

            verify(processoRepo).save(p);
            verify(notificacaoService).enfileirar(argThat(command ->
                    command.tipoNotificacao() == TipoNotificacao.PROCESSO_FINALIZADO
                            && command.destinatario().equals("secao@tre-pe.jus.br")
                            && command.assunto().equals("SGC: Finalização do processo Mapeamento 2026")
                            && command.corpoHtml().equals("<html>finalizado</html>")
                            && command.chaveIdempotencia().equals("processo:100:finalizacao:unidade:10:direto")
            ));
            assertThat(p.getSituacao()).isEqualTo(SituacaoProcesso.FINALIZADO);
        }

        @Test
        @DisplayName("Deve falhar ao iniciar processo se houver unidades sem responsavel efetivo")
        void deveFalharAoIniciarSeHouverUnidadesSemResponsavelEfetivo() {
            Long id = 100L;
            Processo p = new Processo();
            p.setCodigo(id);
            p.setSituacao(SituacaoProcesso.CRIADO);
            p.setTipo(TipoProcesso.MAPEAMENTO);
            Unidade uni = criarUnidadeValida(1L);
            p.adicionarParticipantes(Set.of(uni));

            when(repo.buscar(Processo.class, id)).thenReturn(p);
            when(unidadeService.buscarPorCodigos(anyList())).thenReturn(List.of(uni));
            when(responsavelUnidadeService.todasPossuemResponsavelEfetivo(anyList())).thenReturn(false);

            List<Long> unidades = List.of();
            assertThatThrownBy(() -> processoService.iniciar(id, unidades))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining(Mensagens.OPERACAO_NAO_PERMITIDA);
        }
    }

    @Nested
    @DisplayName("Detalhes e Elegibilidade")
    class DetalhesEElegibilidade {
        @Test
        @DisplayName("Deve obter detalhes completos do processo")
        void deveObterDetalhesCompletos() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setPerfilAtivo(Perfil.ADMIN);

            Processo p = new Processo();
            p.setCodigo(codProcesso);
            p.setDescricao("Processo");
            p.setTipo(TipoProcesso.MAPEAMENTO);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

            Unidade u = criarUnidadeValida(10L);
            p.adicionarParticipantes(Set.of(u));

            when(repo.buscar(Processo.class, codProcesso)).thenReturn(p);
            when(permissionEvaluator.verificarPermissao(usuario, p, AcaoPermissao.FINALIZAR_PROCESSO)).thenReturn(true);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setUnidade(u);
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), u));
            when(validacaoService.validarSubprocessosParaFinalizacao(codProcesso)).thenReturn(ResultadoValidacao.ofValido());

            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
            ProcessoDetalheDto result = processoService.obterDetalhesCompleto(codProcesso, false);

            assertThat(result).isNotNull();
            assertThat(result.getCodigo()).isEqualTo(codProcesso);
            assertThat(result.getUnidades()).isNotEmpty();
        }

        @Test
        @DisplayName("Deve listar subprocessos elegíveis para ação em bloco")
        void deveListarSubprocessosElegiveis() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setUnidadeAtivaCodigo(10L);
            usuario.setPerfilAtivo(Perfil.CHEFE);
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Subprocesso s1 = new Subprocesso();
            s1.setCodigo(101L);
            s1.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            Unidade u1 = new Unidade();
            u1.setCodigo(10L);
            s1.setUnidade(u1);

            Subprocesso s2 = new Subprocesso();
            s2.setCodigo(102L);
            s2.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            Unidade u2 = new Unidade();
            u2.setCodigo(20L);
            s2.setUnidade(u2);

            Subprocesso s3 = new Subprocesso();
            s3.setCodigo(103L);
            s3.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO); // Não elegível
            Unidade u3 = new Unidade();
            u3.setCodigo(30L);
            s3.setUnidade(u3);

            when(consultaService.listarEntidadesPorProcessoEUnidades(eq(codProcesso), anyList())).thenReturn(List.of(s1, s2, s3));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(
                    s1.getCodigo(), u1,
                    s2.getCodigo(), u1
            ));
            when(permissionEvaluator.verificarPermissaoSilenciosa(usuario, s1, AcaoPermissao.ACEITAR_CADASTRO)).thenReturn(true);
            when(permissionEvaluator.verificarPermissaoSilenciosa(usuario, s1, AcaoPermissao.HOMOLOGAR_CADASTRO)).thenReturn(false);
            when(permissionEvaluator.verificarPermissaoSilenciosa(usuario, s2, AcaoPermissao.ACEITAR_MAPA)).thenReturn(true);
            when(permissionEvaluator.verificarPermissaoSilenciosa(usuario, s2, AcaoPermissao.HOMOLOGAR_MAPA)).thenReturn(false);

            List<SubprocessoElegivelDto> result = processoService.listarSubprocessosElegiveis(codProcesso);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Deve montar hierarquia no DTO corretamente para GESTOR")
        void deveMontarHierarquiaDtoGestor() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setUnidadeAtivaCodigo(10L); // Pai
            usuario.setPerfilAtivo(Perfil.GESTOR);

            Processo p = new Processo();
            p.setCodigo(codProcesso);
            p.setDescricao("Processo");
            p.setTipo(TipoProcesso.MAPEAMENTO);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

            Unidade uPai = criarUnidadeValida(10L);
            uPai.setSigla("PAI");
            p.adicionarParticipantes(Set.of(uPai));

            Unidade uFilho = criarUnidadeValida(20L);
            uFilho.setSigla("FILHO");
            uFilho.setUnidadeSuperior(uPai);
            p.adicionarParticipantes(Set.of(uFilho));

            Unidade uSemSub = criarUnidadeValida(30L);
            uSemSub.setSigla("SEMSUB");
            p.adicionarParticipantes(Set.of(uSemSub));

            when(repo.buscar(Processo.class, codProcesso)).thenReturn(p);
            when(unidadeHierarquiaService.buscarIdsDescendentes(10L)).thenReturn(List.of(20L));

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setUnidade(uPai);
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            // Filho não tem subprocesso para cobrir branch sp != null

            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), uPai));

            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
            ProcessoDetalheDto result = processoService.obterDetalhesCompleto(codProcesso, false);

            assertThat(result.getUnidades()).isNotEmpty();
            assertThat(result.getUnidades().getFirst().getFilhos()).isNotEmpty();
        }

        @Test
        @DisplayName("isElegivelParaAcaoEmBloco deve retornar false quando elegivelMapa mas sem permissao ACEITAR ou HOMOLOGAR")
        void isElegivelParaAcaoEmBloco_DeveRetornarFalseQuandoElegivelMapaSemPermissoes() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setPerfilAtivo(Perfil.ADMIN);
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            sp.setUnidade(new Unidade());

            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
            when(permissionEvaluator.verificarPermissaoSilenciosa(usuario, sp, ACEITAR_MAPA)).thenReturn(false);
            when(permissionEvaluator.verificarPermissaoSilenciosa(usuario, sp, HOMOLOGAR_MAPA)).thenReturn(false);

            List<SubprocessoElegivelDto> result = processoService.listarSubprocessosElegiveis(codProcesso);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("isElegivelParaAcaoEmBloco deve retornar false quando elegivelDisponibilizacao mas sem permissao DISPONIBILIZAR_MAPA")
        void isElegivelParaAcaoEmBloco_DeveRetornarFalseQuandoElegivelDisponibilizacaoSemPermissao() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setPerfilAtivo(Perfil.ADMIN);
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            sp.setUnidade(new Unidade());

            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
            when(permissionEvaluator.verificarPermissaoSilenciosa(usuario, sp, DISPONIBILIZAR_MAPA)).thenReturn(false);

            List<SubprocessoElegivelDto> result = processoService.listarSubprocessosElegiveis(codProcesso);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Ações em Bloco")
    class AcoesEmBloco {
        @Test
        @DisplayName("Deve falhar ao executar ação em bloco sem unidades")
        void deveFalharAoExecutarAcaoEmBlocoSemUnidades() {
            ProcessarAnaliseEmBlocoCommand req = new ProcessarAnaliseEmBlocoCommand(List.of(), ACEITAR);
            assertThatThrownBy(() -> processoService.executarAcaoEmBloco(1L, req))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining(Mensagens.SELECIONE_AO_MENOS_UMA_UNIDADE);
        }

        @Test
        @DisplayName("Deve executar ação de HOMOLOGAR e ACEITAR separando cadastro e validacao")
        void deveExecutarAcaoBlocoHomologarEAceitar() {
            Usuario usuario = new Usuario();
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Subprocesso sCad = new Subprocesso();
            sCad.setCodigo(10L);
            sCad.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

            Subprocesso sVal = new Subprocesso();
            sVal.setCodigo(20L);
            sVal.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);

            when(consultaService.listarEntidadesPorProcessoEUnidades(eq(1L), anyList()))
                    .thenReturn(List.of(sCad, sVal));
            
            when(permissionEvaluator.verificarPermissao(eq(usuario), anyList(), any())).thenReturn(true);

            // Teste ACEITAR
            ProcessarAnaliseEmBlocoCommand reqAceitar = new ProcessarAnaliseEmBlocoCommand(List.of(10L, 20L), ACEITAR);
            processoService.executarAcaoEmBloco(1L, reqAceitar);
            verify(cadastroFluxoService).aceitarCadastroEmBloco(List.of(10L));
            verify(transicaoService).aceitarValidacaoEmBloco(List.of(20L));

            // Teste HOMOLOGAR
            ProcessarAnaliseEmBlocoCommand reqHomologar = new ProcessarAnaliseEmBlocoCommand(List.of(10L, 20L), HOMOLOGAR);
            processoService.executarAcaoEmBloco(1L, reqHomologar);
            verify(cadastroFluxoService).homologarCadastroEmBloco(List.of(10L));
            verify(transicaoService).homologarValidacaoEmBloco(List.of(20L));
        }
    }

    @Nested
    @DisplayName("Criação de Processo")
    class Criacao {
        @Test
        @DisplayName("Deve criar processo quando dados válidos")
        void deveCriarProcessoQuandoDadosValidos() {
            CriarProcessoRequest req = new CriarProcessoRequest(
                    "Teste", TipoProcesso.MAPEAMENTO, LocalDateTime.now(), List.of(1L));

            Unidade uni = criarUnidadeValida(1L);
            when(unidadeService.buscarPorCodigos(List.of(1L))).thenReturn(List.of(uni));
            when(processoRepo.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
            mockarResponsaveisEfetivos();

            Processo resultado = processoService.criar(req);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getDescricao()).isEqualTo("Teste");
            verify(processoRepo).saveAndFlush(any());
        }

        @Test
        @DisplayName("Deve falhar ao criar processo com unidade sem responsável efetivo")
        void deveFalharCriacaoSemResponsavelEfetivo() {
            CriarProcessoRequest req = new CriarProcessoRequest(
                    "Teste", TipoProcesso.MAPEAMENTO, LocalDateTime.now(), List.of(1L));
            Unidade unidade = new Unidade();
            unidade.setCodigo(1L);
            unidade.setSigla("U1");
            unidade.setSituacao(SituacaoUnidade.ATIVA);
            when(unidadeService.buscarPorCodigos(List.of(1L))).thenReturn(List.of(unidade));
            when(responsavelUnidadeService.todasPossuemResponsavelEfetivo(anyList())).thenReturn(false);

            assertThatThrownBy(() -> processoService.criar(req))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining(Mensagens.OPERACAO_NAO_PERMITIDA);
        }
    }

    @Nested
    @DisplayName("Checagem de Acesso")
    class ChecagemAcesso {
        @Test
        @DisplayName("Deve retornar false se auth for nulo ou invalido")
        void deveRetornarFalseSeAuthInvalido() {
            assertThat(processoService.checarAcesso(null, 1L)).isFalse();

            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(false);
            assertThat(processoService.checarAcesso(auth, 1L)).isFalse();

            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getPrincipal()).thenReturn(new Object());
            assertThat(processoService.checarAcesso(auth, 1L)).isFalse();
        }

        @Test
        @DisplayName("Deve retornar true se ADMIN")
        void deveRetornarTrueSeAdmin() {
            Authentication auth = mock(Authentication.class);
            Usuario user = new Usuario();
            user.setPerfilAtivo(Perfil.ADMIN);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getPrincipal()).thenReturn(user);

            assertThat(processoService.checarAcesso(auth, 1L)).isTrue();
        }

        @Test
        @DisplayName("Deve retornar true se unidade esta no processo para GESTOR/CHEFE")
        void deveRetornarTrueSeUnidadeNoProcesso() {
            Authentication auth = mock(Authentication.class);
            Usuario user = new Usuario();
            user.setPerfilAtivo(Perfil.CHEFE);
            user.setUnidadeAtivaCodigo(10L);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getPrincipal()).thenReturn(user);
            when(consultaService.listarEntidadesPorProcessoEUnidades(1L, List.of(10L))).thenReturn(List.of(new Subprocesso()));

            assertThat(processoService.checarAcesso(auth, 1L)).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false se unidade nao possui subprocesso acessivel")
        void deveRetornarFalseSemSubprocessoAcessivel() {
            Authentication auth = mock(Authentication.class);
            Usuario user = new Usuario();
            user.setPerfilAtivo(Perfil.CHEFE);
            user.setUnidadeAtivaCodigo(10L);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getPrincipal()).thenReturn(user);
            when(consultaService.listarEntidadesPorProcessoEUnidades(1L, List.of(10L))).thenReturn(List.of());

            assertThat(processoService.checarAcesso(auth, 1L)).isFalse();
        }
    }

    @Nested
    @DisplayName("Consultas e Detalhes")
    class Consultas {
        @Test
        @DisplayName("Deve listar para importacao")
        void deveListarParaImportacao() {
            Processo p = new Processo();
            when(processoRepo.listarPorSituacaoComParticipantes(SituacaoProcesso.FINALIZADO)).thenReturn(List.of(p));

            List<Processo> res = processoService.listarParaImportacao();
            assertThat(res).containsExactly(p);
            verify(processoRepo).listarPorSituacaoComParticipantes(SituacaoProcesso.FINALIZADO);
        }

        @Test
        @DisplayName("Deve listar ativos para ADMIN")
        void deveListarAtivosParaAdmin() {
            Usuario admin = new Usuario();
            admin.setPerfilAtivo(Perfil.ADMIN);
            when(usuarioService.usuarioAutenticado()).thenReturn(admin);

            Processo p = new Processo();
            when(processoRepo.listarPorSituacao(SituacaoProcesso.EM_ANDAMENTO)).thenReturn(List.of(p));

            List<Processo> res = processoService.listarAtivos();
            assertThat(res).containsExactly(p);
            verify(processoRepo).listarPorSituacao(SituacaoProcesso.EM_ANDAMENTO);
            verify(processoRepo, never()).listarPorSituacaoEUnidadeCodigos(any(), any());
        }

        @Test
        @DisplayName("Deve listar ativos para usuario normal")
        void deveListarAtivosParaUsuarioNormal() {
            Usuario gestor = new Usuario();
            gestor.setPerfilAtivo(Perfil.GESTOR);
            Unidade u = new Unidade();
            u.setCodigo(1L);
            gestor.setUnidadeAtivaCodigo(1L);
            when(usuarioService.usuarioAutenticado()).thenReturn(gestor);
            when(unidadeHierarquiaService.buscarIdsDescendentes(1L)).thenReturn(List.of());

            Processo p = new Processo();
            when(processoRepo.listarPorSituacaoEUnidadeCodigos(eq(SituacaoProcesso.EM_ANDAMENTO), anyList())).thenReturn(List.of(p));

            List<Processo> res = processoService.listarAtivos();
            assertThat(res).containsExactly(p);
            verify(processoRepo).listarPorSituacaoEUnidadeCodigos(eq(SituacaoProcesso.EM_ANDAMENTO), anyList());
            verify(processoRepo, never()).listarPorSituacao(any());
        }

        @Test
        @DisplayName("Deve listar iniciados por participantes")
        void deveListarIniciadosPorParticipantes() {
            Pageable pageable = Pageable.unpaged();
            Processo p = new Processo();
            p.setCodigo(5L);
            when(processoRepo.listarCodigosPorParticipantesESituacaoDiferente(anyList(), eq(SituacaoProcesso.CRIADO), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(5L), pageable, 1));
            when(processoRepo.listarPorCodigosComParticipantes(List.of(5L))).thenReturn(List.of(p));

            Page<Processo> res = processoService.listarIniciadosPorParticipantes(List.of(1L), pageable);
            assertThat(res.getContent()).containsExactly(p);
            verify(processoRepo).listarCodigosPorParticipantesESituacaoDiferente(List.of(1L), SituacaoProcesso.CRIADO, pageable);
        }

        @Test
        @DisplayName("Deve listar iniciados por subprocessos")
        void deveListarIniciadosPorSubprocessos() {
            Pageable pageable = Pageable.unpaged();
            Processo p = new Processo();
            p.setCodigo(10L);
            when(configuracaoService.buscarDiasInativacaoProcesso()).thenReturn(10);
            when(processoRepo.listarCodigosAtivosPorSubprocessos(eq(List.of(1L)), eq(SituacaoProcesso.CRIADO), eq(SituacaoProcesso.FINALIZADO), any(), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(10L), pageable, 1));
            when(processoRepo.listarPorCodigosComParticipantes(List.of(10L))).thenReturn(List.of(p));

            Page<Processo> res = processoService.listarIniciadosPorSubprocessos(List.of(1L), pageable);

            assertThat(res.getContent()).containsExactly(p);
            verify(processoRepo).listarCodigosAtivosPorSubprocessos(eq(List.of(1L)), eq(SituacaoProcesso.CRIADO), eq(SituacaoProcesso.FINALIZADO), any(), eq(pageable));
        }

        @Test
        @DisplayName("Deve listar unidades bloqueadas por tipo")
        void deveListarUnidadesBloqueadasPorTipo() {
            when(processoRepo.listarUnidadesBloqueadasPorSituacaoETipo(SituacaoProcesso.EM_ANDAMENTO, TipoProcesso.MAPEAMENTO))
                    .thenReturn(List.of(1L, 2L));

            List<Long> res = processoService.listarUnidadesBloqueadasPorTipo(TipoProcesso.MAPEAMENTO);
            assertThat(res).containsExactly(1L, 2L);
            verify(processoRepo).listarUnidadesBloqueadasPorSituacaoETipo(SituacaoProcesso.EM_ANDAMENTO, TipoProcesso.MAPEAMENTO);
        }

        @Test
        @DisplayName("Deve buscar entidade por ID")
        void deveBuscarEntidadePorId() {
            Long id = 100L;
            Processo processo = ProcessoFixture.processoPadrao();
            when(repo.buscar(Processo.class, id)).thenReturn(processo);

            Processo res = processoService.buscarPorCodigo(id);
            assertThat(res).isEqualTo(processo);
        }

        @Test
        @DisplayName("Deve obter processo por ID (Optional)")
        void deveobterPorCodigoOptional() {
            Long id = 100L;
            Processo processo = ProcessoFixture.processoPadrao();
            when(processoRepo.buscarPorCodigoComParticipantes(id)).thenReturn(Optional.of(processo));

            Optional<Processo> res = processoService.buscarOpt(id);
            assertThat(res).isPresent();
        }

        @Test
        @DisplayName("Deve listar todos com paginação")
        void deveListarTodosPaginado() {
            Pageable pageable = Pageable.unpaged();
            when(configuracaoService.buscarDiasInativacaoProcesso()).thenReturn(10);
            when(processoRepo.listarCodigosAtivos(eq(SituacaoProcesso.FINALIZADO), any(), eq(pageable))).thenReturn(Page.empty());

            var res = processoService.listarTodos(pageable);
            assertThat(res).isEmpty();
        }
    }

    @Nested
    @DisplayName("Operações em Bloco")
    class OperacoesEmBloco {
        @Nested
        @DisplayName("Executar ação em Bloco - DISPONIBILIZAR")
        class AcaoDisponibilizar {
            @Test
            @DisplayName("Deve disponibilizar mapas em bloco quando ação é DISPONIBILIZAR")
            void deveDisponibilizarMapasEmBloco() {

                Usuario usuario = new Usuario();
                usuario.setTituloEleitoral("12345678901");
                when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

                LocalDate dataLimite = LocalDate.now().plusDays(30);
                DisponibilizarMapaEmBlocoCommand req = new DisponibilizarMapaEmBlocoCommand(
                        List.of(1L, 2L, 3L),
                        dataLimite
                );

                Subprocesso sp1 = Subprocesso.builder().codigo(1001L).unidade(Unidade.builder().codigo(1L).build()).build();
                Subprocesso sp2 = Subprocesso.builder().codigo(1002L).unidade(Unidade.builder().codigo(2L).build()).build();
                Subprocesso sp3 = Subprocesso.builder().codigo(1003L).unidade(Unidade.builder().codigo(3L).build()).build();
                when(consultaService.listarEntidadesPorProcessoEUnidades(100L, List.of(1L, 2L, 3L))).thenReturn(List.of(sp1, sp2, sp3));
                when(permissionEvaluator.verificarPermissao(eq(usuario), anyList(), eq(DISPONIBILIZAR_MAPA))).thenReturn(true);

                processoService.executarAcaoEmBloco(100L, req);

                ArgumentCaptor<DisponibilizarMapaRequest> captor =
                        ArgumentCaptor.forClass(DisponibilizarMapaRequest.class);
                verify(transicaoService).disponibilizarMapaEmBloco(
                        argThat(subprocessos -> subprocessos.stream()
                                .map(Subprocesso::getCodigo)
                                .toList()
                                .equals(List.of(1001L, 1002L, 1003L))),
                        captor.capture(),
                        eq(usuario)
                );

                DisponibilizarMapaRequest captured = captor.getValue();
                assertThat(captured.dataLimite()).isEqualTo(dataLimite);
                assertThat(captured.observacoes()).isEqualTo("Disponibilização em bloco");
            }
        }

        @Nested
        @DisplayName("Executar ação em Bloco - ACEITAR")
        class AcaoAceitar {
            @Test
            @DisplayName("Deve aceitar cadastro quando subprocessos estão em MAPEAMENTO_CADASTRO_DISPONIBILIZADO")
            void deveAceitarCadastroQuandoMapeamentoCadastro() {

                Usuario usuario = new Usuario();
                when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

                Subprocesso sp1 = Subprocesso.builder()
                        .codigo(1L)
                        .unidade(Unidade.builder().codigo(10L).build())
                        .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                        .build();
                Subprocesso sp2 = Subprocesso.builder()
                        .codigo(2L)
                        .unidade(Unidade.builder().codigo(20L).build())
                        .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                        .build();

                when(consultaService.listarEntidadesPorProcessoEUnidades(100L, List.of(10L, 20L))).thenReturn(List.of(sp1, sp2));
                when(permissionEvaluator.verificarPermissao(eq(usuario), anyList(), any())).thenReturn(true);

                ProcessarAnaliseEmBlocoCommand req = new ProcessarAnaliseEmBlocoCommand(
                        List.of(10L, 20L),
                        ACEITAR
                );

                processoService.executarAcaoEmBloco(100L, req);

                verify(cadastroFluxoService).aceitarCadastroEmBloco(List.of(1L, 2L));
            }

            @Test
            @DisplayName("Deve aceitar validação quando subprocessos estão em situação de mapa disponibilizado")
            void deveAceitarValidacaoQuandoMapaDisponibilizado() {

                Usuario usuario = new Usuario();
                when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

                Subprocesso sp1 = Subprocesso.builder()
                        .codigo(1L)
                        .unidade(Unidade.builder().codigo(10L).build())
                        .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO)
                        .build();

                when(consultaService.listarEntidadesPorProcessoEUnidades(100L, List.of(10L))).thenReturn(List.of(sp1));
                when(permissionEvaluator.verificarPermissao(eq(usuario), anyList(), any())).thenReturn(true);

                ProcessarAnaliseEmBlocoCommand req = new ProcessarAnaliseEmBlocoCommand(
                        List.of(10L),
                        ACEITAR
                );

                processoService.executarAcaoEmBloco(100L, req);

                verify(transicaoService).aceitarValidacaoEmBloco(List.of(1L));
            }
        }

        @Nested
        @DisplayName("Executar ação em Bloco - HOMOLOGAR")
        class AcaoHomologar {
            @Test
            @DisplayName("Deve homologar cadastro quando subprocessos estão em situação de cadastro")
            void deveHomologarCadastroQuandoCadastro() {

                Usuario usuario = new Usuario();
                when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

                Subprocesso sp1 = Subprocesso.builder()
                        .codigo(1L)
                        .unidade(Unidade.builder().codigo(10L).build())
                        .situacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA)
                        .build();

                when(consultaService.listarEntidadesPorProcessoEUnidades(100L, List.of(10L))).thenReturn(List.of(sp1));
                when(permissionEvaluator.verificarPermissao(eq(usuario), anyList(), any())).thenReturn(true);

                ProcessarAnaliseEmBlocoCommand req = new ProcessarAnaliseEmBlocoCommand(
                        List.of(10L),
                        HOMOLOGAR
                );

                processoService.executarAcaoEmBloco(100L, req);

                verify(cadastroFluxoService).homologarCadastroEmBloco(List.of(1L));
            }

            @Test
            @DisplayName("Deve homologar validação quando subprocessos estão em validação")
            void deveHomologarValidacaoQuandoValidacao() {

                Usuario usuario = new Usuario();
                when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

                Subprocesso sp1 = Subprocesso.builder()
                        .codigo(1L)
                        .unidade(Unidade.builder().codigo(10L).build())
                        .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO)
                        .build();

                when(consultaService.listarEntidadesPorProcessoEUnidades(100L, List.of(10L))).thenReturn(List.of(sp1));
                when(permissionEvaluator.verificarPermissao(eq(usuario), anyList(), any())).thenReturn(true);

                ProcessarAnaliseEmBlocoCommand req = new ProcessarAnaliseEmBlocoCommand(
                        List.of(10L),
                        HOMOLOGAR
                );

                processoService.executarAcaoEmBloco(100L, req);

                verify(transicaoService).homologarValidacaoEmBloco(List.of(1L));
            }
        }
    }

    @Nested
    @DisplayName("Cobertura Adicional de Branches")
    class CoberturaAdicional {

        @Test
        @DisplayName("Deve iniciar diagnostico com sucesso e salvar")
        void deveIniciarDiagnosticoComSucesso() {
            Long id = 100L;
            Processo p = new Processo();
            p.setCodigo(id);
            p.setSituacao(SituacaoProcesso.CRIADO);
            p.setTipo(TipoProcesso.DIAGNOSTICO);
            p.setDataLimite(LocalDateTime.now().plusDays(30));
            Unidade uni = criarUnidadeValida(1L);
            p.adicionarParticipantes(Set.of(uni));

            when(repo.buscar(Processo.class, id)).thenReturn(p);
            when(unidadeService.buscarPorCodigos(anyList())).thenReturn(List.of(uni));
            when(unidadeService.buscarTodosCodigosUnidadesComMapa()).thenReturn(List.of(1L));

            UnidadeMapa um = new UnidadeMapa();
            um.setUnidadeCodigo(1L);
            when(unidadeService.buscarMapasPorUnidades(anyList())).thenReturn(List.of(um));

            Unidade uniAdmin = new Unidade();
            uniAdmin.setSituacao(SituacaoUnidade.ATIVA);
            when(unidadeService.buscarAdmin()).thenReturn(uniAdmin);
            mockarResponsaveisEfetivos();

            processoService.iniciar(id, List.of());

            verify(processoRepo).save(any(Processo.class));
            verify(subprocessoService).criarParaDiagnostico(argThat(command ->
                    command.processo() == p
                            && command.unidade() == uni
                            && command.unidadeMapa() == um
                            && command.unidadeOrigem() == uniAdmin));
        }

        @Test
        @DisplayName("Deve omitir mapaCodigo no DTO quando subprocesso nao possuir mapa")
        void deveOmitirMapaCodigoNoDtoQuandoSubprocessoSemMapa() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setPerfilAtivo(Perfil.ADMIN);

            Processo p = new Processo();
            p.setCodigo(codProcesso);
            p.setTipo(TipoProcesso.MAPEAMENTO);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

            Unidade u = criarUnidadeValida(10L);
            p.adicionarParticipantes(Set.of(u));

            when(repo.buscar(Processo.class, codProcesso)).thenReturn(p);
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setUnidade(u);
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            sp.setMapa(null); // Explicitly null

            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), u));

            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
            ProcessoDetalheDto result = processoService.obterDetalhesCompleto(codProcesso, false);

            assertThat(result.getUnidades().getFirst().getMapaCodigo()).isNull();
        }

        @Test
        @DisplayName("Deve incluir mapaCodigo no DTO quando subprocesso possuir mapa")
        void deveIncluirMapaCodigoNoDtoQuandoSubprocessoComMapa() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setPerfilAtivo(Perfil.ADMIN);

            Processo p = new Processo();
            p.setCodigo(codProcesso);
            p.setTipo(TipoProcesso.MAPEAMENTO);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

            Unidade u = criarUnidadeValida(10L);
            p.adicionarParticipantes(Set.of(u));

            when(repo.buscar(Processo.class, codProcesso)).thenReturn(p);
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setUnidade(u);
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            Mapa mapa = new Mapa();
            mapa.setCodigo(500L);
            sp.setMapa(mapa);

            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), u));

            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
            ProcessoDetalheDto result = processoService.obterDetalhesCompleto(codProcesso, false);

            assertThat(result.getUnidades().getFirst().getMapaCodigo()).isEqualTo(500L);
        }

        @ParameterizedTest
        @EnumSource(value = SituacaoSubprocesso.class, names = {
                "REVISAO_CADASTRO_DISPONIBILIZADA",
                "REVISAO_MAPA_COM_SUGESTOES",
                "REVISAO_MAPA_VALIDADO",
                "MAPEAMENTO_MAPA_CRIADO",
                "REVISAO_MAPA_AJUSTADO"
        })

        @DisplayName("Deve verificar elegibilidade para acao em bloco para diversas situacoes")
        void deveVerificarElegibilidadeParaDiversasSituacoes(SituacaoSubprocesso situacao) {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setPerfilAtivo(Perfil.ADMIN);
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setUnidade(new Unidade());
            sp.setSituacao(situacao);

            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
            when(permissionEvaluator.verificarPermissaoSilenciosa(any(), any(), any())).thenReturn(true);
            when(localizacaoSubprocessoService.obterLocalizacaoAtual(any())).thenReturn(new Unidade());

            List<SubprocessoElegivelDto> result = processoService.listarSubprocessosElegiveis(codProcesso);
            assertThat(result).hasSize(1);
            if (situacao == SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES) {
                assertThat(result.getFirst().isHabilitarDisponibilizarMapaBloco()).isTrue();
            }
        }

        @Test
        @DisplayName("Deve habilitar disponibilização em bloco para mapa com sugestões")
        void deveHabilitarDisponibilizacaoMapaBlocoQuandoMapaComSugestoes() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setPerfilAtivo(Perfil.ADMIN);
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            Unidade unidade = new Unidade();
            unidade.setCodigo(10L);
            sp.setUnidade(unidade);
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES);

            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
            when(permissionEvaluator.verificarPermissaoSilenciosa(any(), any(), any())).thenReturn(true);
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(List.of(sp))).thenReturn(Map.of(100L, unidade));

            List<SubprocessoElegivelDto> result = processoService.listarSubprocessosElegiveis(codProcesso);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().isHabilitarDisponibilizarMapaBloco()).isTrue();
        }

        @Test
        @DisplayName("Deve verificar elegibilidade quando permissao eh HOMOLOGAR_MAPA")
        void deveVerificarElegibilidadeComPermissaoHomologarMapa() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setPerfilAtivo(Perfil.ADMIN);
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            sp.setUnidade(new Unidade());

            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
            when(permissionEvaluator.verificarPermissaoSilenciosa(usuario, sp, ACEITAR_MAPA)).thenReturn(false);
            when(permissionEvaluator.verificarPermissaoSilenciosa(usuario, sp, HOMOLOGAR_MAPA)).thenReturn(true);
            when(localizacaoSubprocessoService.obterLocalizacaoAtual(any())).thenReturn(new Unidade());

            List<SubprocessoElegivelDto> result = processoService.listarSubprocessosElegiveis(codProcesso);
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Deve falhar ao iniciar revisao com unidades vazias")
        void deveFalharAoIniciarRevisaoSemUnidades() {
            Long id = 100L;
            Processo p = new Processo();
            p.setCodigo(id);
            p.setSituacao(SituacaoProcesso.CRIADO);
            p.setTipo(TipoProcesso.REVISAO);

            when(repo.buscar(Processo.class, id)).thenReturn(p);

            List<Long> unidades = List.of();
            assertThatThrownBy(() -> processoService.iniciar(id, unidades))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining(Mensagens.LISTA_UNIDADES_OBRIGATORIA_REVISAO);
        }

        @Test
        @DisplayName("Deve falhar ao enviar lembrete quando processo sem data limite")
        void deveFalharAoEnviarLembreteSemDataLimite() {
            Long codProcesso = 1L;
            Long codUnidade = 10L;

            Processo p = new Processo();
            p.setCodigo(codProcesso);
            Unidade u = criarUnidadeValida(codUnidade);
            u.setTituloTitular("TITULAR");
            p.adicionarParticipantes(Set.of(u));
            p.setDataLimite(null);

            when(processoRepo.buscarPorCodigoComParticipantes(codProcesso)).thenReturn(Optional.of(p));
            when(unidadeService.buscarPorCodigo(codUnidade)).thenReturn(u);

            assertThatThrownBy(() -> processoService.enviarLembrete(codProcesso, codUnidade))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("sem data limite");
        }

        @Test
        @DisplayName("Deve enfileirar email e criar alerta ao enviar lembrete com sucesso")
        void deveEnfileirarEmailECriarAlertaAoEnviarLembrete() {
            Long codProcesso = 1L;
            Long codUnidade = 10L;

            Processo p = new Processo();
            p.setCodigo(codProcesso);
            p.setDescricao("Processo teste");
            Unidade u = criarUnidadeValida(codUnidade);
            u.setTituloTitular("TITULAR");
            p.adicionarParticipantes(Set.of(u));
            p.setDataLimite(LocalDateTime.of(2026, 6, 30, 0, 0));

            Usuario titular = new Usuario();
            titular.setTituloEleitoral("TITULAR");
            titular.setEmail("titular@tre-pe.jus.br");

            sgc.alerta.model.Alerta alerta = new sgc.alerta.model.Alerta();
            alerta.setCodigo(55L);

            when(processoRepo.buscarPorCodigoComParticipantes(codProcesso)).thenReturn(Optional.of(p));
            when(unidadeService.buscarPorCodigo(codUnidade)).thenReturn(u);
            when(emailModelosService.criarEmailLembretePrazo(anyString(), anyString(), any())).thenReturn("<html>lembrete</html>");
            when(servicoAlertas.criarAlertaAdmin(eq(p), eq(u), anyString())).thenReturn(alerta);

            processoService.enviarLembrete(codProcesso, codUnidade);

            verify(servicoAlertas).criarAlertaAdmin(eq(p), eq(u), contains("Lembrete"));
            verify(notificacaoService).enfileirar(argThat(cmd ->
                    "u10@tre-pe.jus.br".equals(cmd.destinatario())
                            && "U10".equals(cmd.unidadeDestinoSigla())
                            && cmd.usuarioDestinoTitulo() == null
                            && cmd.assunto().contains("Processo teste")
                            && cmd.tipoNotificacao() == TipoNotificacao.LEMBRETE_PRAZO
                            && cmd.subprocesso() == null
                            && cmd.chaveIdempotencia().contains("processo:1:lembrete:unidade:10")
            ));
        }

        @Test
        @DisplayName("executarAcaoEmBloco não deve acionar fluxos de aceite/homologação quando ação for disponibilizar")
        void deveNaoAcionarFluxosDeAceiteOuHomologacaoQuandoAcaoForDisponibilizar() {
            Long codProcesso = 1L;
            DisponibilizarMapaEmBlocoCommand req = new DisponibilizarMapaEmBlocoCommand(List.of(10L), LocalDate.now().plusDays(1));

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            Unidade unidade = new Unidade();
            unidade.setCodigo(10L);
            sp.setUnidade(unidade);

            Usuario usuario = new Usuario();
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
            when(consultaService.listarEntidadesPorProcessoEUnidades(eq(codProcesso), anyList())).thenReturn(List.of(sp));
            when(permissionEvaluator.verificarPermissao(eq(usuario), anyList(), any())).thenReturn(true);

            assertThatCode(() -> processoService.executarAcaoEmBloco(codProcesso, req)).doesNotThrowAnyException();

            verify(cadastroFluxoService, never()).aceitarCadastroEmBloco(anyList());
            verify(transicaoService, never()).aceitarValidacaoEmBloco(anyList());
        }
    }
    @Test
    @DisplayName("executarAcaoEmBloco deve lançar ErroAcessoNegado quando não houver permissão")
    void deveLancarErroAcessoNegado() {
        Long codProcesso = 100L;
        DisponibilizarMapaEmBlocoCommand req = new DisponibilizarMapaEmBlocoCommand(List.of(1L), LocalDate.now().plusDays(1));

        Subprocesso sp = mock(Subprocesso.class);
        Usuario usuario = new Usuario();
        when(consultaService.listarEntidadesPorProcessoEUnidades(eq(codProcesso), anyList()))
                .thenReturn(List.of(sp));

        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

        when(permissionEvaluator.verificarPermissao(any(Usuario.class), any(List.class), any(AcaoPermissao.class))).thenReturn(false);

        assertThatThrownBy(() -> processoService.executarAcaoEmBloco(codProcesso, req))
                .isInstanceOf(ErroAcessoNegado.class);
    }

    @Test
    @DisplayName("AcaoEmBlocoRequest deve exigir data limite ao converter disponibilizacao")
    void deveExigirDataLimiteAoConverterDisponibilizacao() {
        AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(List.of(1L), DISPONIBILIZAR, null);

        assertThatThrownBy(req::paraCommand)
                .isInstanceOf(ErroValidacao.class)
                .hasMessage(Mensagens.DATA_LIMITE_OBRIGATORIA);
    }

    @Test
    @DisplayName("obterDetalhesCompleto deve reutilizar subprocessos ja carregados ao listar elegiveis")
    void deveReutilizarSubprocessosAoListarElegiveisNoContextoCompleto() {
        Long cod = 1L;
        Processo processo = new Processo();
        processo.setCodigo(cod);
        processo.setTipo(MAPEAMENTO);
        processo.setSituacao(EM_ANDAMENTO);

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setSigla("U10");
        unidade.setNome("Unidade 10");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        processo.adicionarParticipantes(Set.of(unidade));

        Usuario usuario = new Usuario();
        usuario.setPerfilAtivo(Perfil.ADMIN);
        usuario.setUnidadeAtivaCodigo(10L);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(unidade);
        subprocesso.setSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(1));

        when(repo.buscar(Processo.class, cod)).thenReturn(processo);
        when(consultaService.listarEntidadesPorProcesso(cod)).thenReturn(List.of(subprocesso));
        when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(subprocesso.getCodigo(), unidade));
        when(permissionEvaluator.verificarPermissao(usuario, processo, FINALIZAR_PROCESSO)).thenReturn(false);

        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
        ProcessoDetalheDto resultado = processoService.obterDetalhesCompleto(cod, true);

        assertThat(resultado.getElegiveis()).hasSize(1);
        verify(consultaService, times(1)).listarEntidadesPorProcesso(cod);
        verify(localizacaoSubprocessoService, times(1)).obterLocalizacoesAtuais(anyCollection());
        verify(permissionEvaluator, never()).verificarPermissaoSilenciosa(eq(usuario), any(Subprocesso.class), any(AcaoPermissao.class));
    }

    @Test
    @DisplayName("finalizar deve notificar participantes")
    void deveNotificarParticipantesAoFinalizar() {
        Long codigo = 1L;
        Processo p = mock(Processo.class);
        when(p.getCodigo()).thenReturn(codigo);
        when(p.getDescricao()).thenReturn("Desc");
        when(p.getTipo()).thenReturn(DIAGNOSTICO);
        when(p.getSituacao()).thenReturn(EM_ANDAMENTO);

        UnidadeProcesso up = new UnidadeProcesso();
        up.setUnidadeCodigo(10L);
        when(p.getParticipantes()).thenReturn(List.of(up));

        Unidade uni = new Unidade();
        uni.setCodigo(10L);
        when(unidadeService.buscarPorCodigos(anyList())).thenReturn(List.of(uni));

        when(repo.buscar(Processo.class, codigo)).thenReturn(p);

        SubprocessoValidacaoService.ResultadoValidacao v = SubprocessoValidacaoService.ResultadoValidacao.ofValido();
        when(validacaoService.validarSubprocessosParaFinalizacao(codigo)).thenReturn(v);

        processoService.finalizar(codigo);

        verify(p).setSituacao(FINALIZADO);
        verify(servicoAlertas).criarAlertaAdmin(p, uni, "Processo finalizado: Desc");
        verify(processoRepo).save(p);
    }

    @Test
    @DisplayName("validarSelecaoBloco deve lançar ErroValidacao quando tamanhos diferirem")
    void deveLancarErroValidacaoEmBloco() {
        Long codProcesso = 100L;
        DisponibilizarMapaEmBlocoCommand req = new DisponibilizarMapaEmBlocoCommand(List.of(1L, 2L), LocalDate.now().plusDays(1));

        Subprocesso sp = mock(Subprocesso.class);
        Unidade u = new Unidade();
        Usuario usuario = new Usuario();
        u.setCodigo(1L);
        when(sp.getUnidade()).thenReturn(u);

        // Retorna apenas 1 subprocesso para 2 códigos solicitados
        when(consultaService.listarEntidadesPorProcessoEUnidades(eq(codProcesso), anyList()))
                .thenReturn(List.of(sp));

        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

        assertThatThrownBy(() -> processoService.executarAcaoEmBloco(codProcesso, req))
                .isInstanceOf(ErroValidacao.class);
    }

    @Test
    @DisplayName("enviarLembrete deve lançar IllegalStateException quando processo não tem data limite")
    void enviarLembreteSemDataLimite() {
        Long codProcesso = 1L;
        Long unidadeCodigo = 10L;
        Processo p = new Processo();
        UnidadeProcesso up = new UnidadeProcesso();
        up.setUnidadeCodigo(unidadeCodigo);
        p.setParticipantes(new ArrayList<>(List.of(up)));

        when(processoRepo.buscarPorCodigoComParticipantes(codProcesso)).thenReturn(Optional.of(p));
        when(unidadeService.buscarPorCodigo(unidadeCodigo)).thenReturn(new Unidade());

        assertThatThrownBy(() -> processoService.enviarLembrete(codProcesso, unidadeCodigo))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sem data limite");
    }

    @Test
    @DisplayName("iniciar deve lançar IllegalStateException quando unidade não tem mapa no modo Revisão")
    void iniciarSemMapaRevisao() {
        Long cod = 1L;
        Processo p = new Processo();
        p.setCodigo(cod);
        p.setTipo(REVISAO);
        p.setSituacao(CRIADO);

        when(repo.buscar(Processo.class, cod)).thenReturn(p);

        Unidade uni = new Unidade();
        uni.setCodigo(10L);
        uni.setSigla("U10");
        uni.setNome("Unidade 10");
        uni.setTipo(sgc.organizacao.model.TipoUnidade.OPERACIONAL);
        uni.setSituacao(sgc.organizacao.model.SituacaoUnidade.ATIVA);

        when(unidadeService.buscarPorCodigos(anyList())).thenReturn(List.of(uni));
        when(unidadeService.buscarMapasPorUnidades(anyList())).thenReturn(new ArrayList<>());
        mockarResponsaveisEfetivos();

        Unidade admin = new Unidade();
        admin.setCodigo(99L);
        admin.setSigla("ADMIN");
        admin.setNome("Administrador");
        admin.setTipo(sgc.organizacao.model.TipoUnidade.RAIZ);
        admin.setSituacao(sgc.organizacao.model.SituacaoUnidade.ATIVA);
        when(unidadeService.buscarAdmin()).thenReturn(admin);

        List<Long> unidadeCods = List.of(10L);
        // when(usuarioService.usuarioAutenticado()).thenReturn(usuario); - desnecessário nesta versão
        assertThatThrownBy(() -> processoService.iniciar(cod, unidadeCods))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sem mapa vigente");
    }

    @Test
    @DisplayName("iniciar deve suportar tipo DIAGNOSTICO")
    void iniciarDiagnostico() {
        Long cod = 1L;
        Processo p = new Processo();
        p.setCodigo(cod);
        p.setTipo(DIAGNOSTICO);
        p.setSituacao(CRIADO);
        p.setDataLimite(LocalDateTime.now().plusDays(30));
        Unidade u = new Unidade();
        u.setCodigo(10L);
        u.setTipo(sgc.organizacao.model.TipoUnidade.OPERACIONAL);
        u.setSigla("U10");
        u.setSituacao(SituacaoUnidade.ATIVA);
        p.adicionarParticipantes(Set.of(u));

        Unidade admin = new Unidade();
        admin.setCodigo(99L);
        admin.setSigla("ADMIN");
        admin.setSituacao(SituacaoUnidade.ATIVA);

        when(repo.buscar(Processo.class, cod)).thenReturn(p);
        when(unidadeService.buscarPorCodigos(any())).thenReturn(List.of(u));
        UnidadeMapa um = new UnidadeMapa();
        um.setUnidadeCodigo(10L);
        when(unidadeService.buscarMapasPorUnidades(any())).thenReturn(List.of(um));
        when(unidadeService.buscarAdmin()).thenReturn(admin);
        mockarResponsaveisEfetivos();

        // when(usuarioService.usuarioAutenticado()).thenReturn(new Usuario()); - desnecessário
        processoService.iniciar(cod, List.of(10L)); // branch 419 (DIAGNOSTICO)
        verify(subprocessoService).criarParaDiagnostico(any());
    }

    @Test
    @DisplayName("iniciar deve lançar IllegalStateException quando unidade obrigatória está ausente")
    void iniciarUnidadeObrigatoriaAusente() {
        Long cod = 1L;
        Processo p = new Processo();
        p.setCodigo(cod);
        p.setTipo(REVISAO);
        p.setSituacao(CRIADO);

        when(repo.buscar(Processo.class, cod)).thenReturn(p);

        Unidade u10 = new Unidade();
        u10.setCodigo(10L);
        u10.setSigla("U10");
        u10.setTipo(sgc.organizacao.model.TipoUnidade.OPERACIONAL);
        u10.setSituacao(SituacaoUnidade.ATIVA);

        // Retorna apenas U10, mas vamos pedir 10 e 11
        when(unidadeService.buscarPorCodigos(anyList())).thenReturn(List.of(u10));

        UnidadeMapa um10 = new UnidadeMapa();
        um10.setUnidadeCodigo(10L);
        UnidadeMapa um11 = new UnidadeMapa();
        um11.setUnidadeCodigo(11L);
        when(unidadeService.buscarMapasPorUnidades(anyList())).thenReturn(List.of(um10, um11));

        mockarResponsaveisEfetivos();
        when(unidadeService.buscarAdmin()).thenReturn(new Unidade());

        List<Long> codigos = List.of(10L, 11L);
        // when(usuarioService.usuarioAutenticado()).thenReturn(usuario); - desnecessário
        assertThatThrownBy(() -> processoService.iniciar(cod, codigos))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unidade 11 ausente para iniciar subprocesso");
    }

    @Test
    @DisplayName("executarAcaoEmBloco - acao HOMOLOGAR")
    void executarAcaoEmBloco_Homologar() {
        Long codProc = 1L;
        ProcessarAnaliseEmBlocoCommand req = new ProcessarAnaliseEmBlocoCommand(List.of(10L), AcaoProcesso.HOMOLOGAR);
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        Unidade u = new Unidade();
        u.setCodigo(10L);
        sp.setUnidade(u);

        when(consultaService.listarEntidadesPorProcessoEUnidades(eq(codProc), anyList())).thenReturn(List.of(sp));
        Usuario usuario = new Usuario();
        usuario.setUnidadeAtivaCodigo(10L);
        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
        
        when(permissionEvaluator.verificarPermissao(eq(usuario), anyList(), any())).thenReturn(true);

        processoService.executarAcaoEmBloco(codProc, req); // branch 570 (HOMOLOGAR)
        verify(cadastroFluxoService).homologarCadastroEmBloco(anyList());
    }

    @Test
    @DisplayName("enviarLembrete - sucesso")
    void enviarLembreteSucesso() {
        Processo p = new Processo();
        p.setCodigo(1L);
        p.setDescricao("Processo Teste");
        p.setDataLimite(java.time.LocalDateTime.now().plusDays(1));

        Unidade u = new Unidade();
        u.setCodigo(10L);
        u.setSigla("U1");
        u.setSituacao(sgc.organizacao.model.SituacaoUnidade.ATIVA);
        u.setTipo(sgc.organizacao.model.TipoUnidade.OPERACIONAL);

        p.adicionarParticipantes(Set.of(u));

        when(processoRepo.buscarPorCodigoComParticipantes(1L)).thenReturn(Optional.of(p));
        when(unidadeService.buscarPorCodigo(10L)).thenReturn(u);
        when(emailModelosService.criarEmailLembretePrazo(any(), any(), any())).thenReturn("<html></html>");

        processoService.enviarLembrete(1L, 10L);

        verify(servicoAlertas).criarAlertaAdmin(eq(p), eq(u), anyString());
        verify(notificacaoService).enfileirar(argThat(cmd ->
                "u1@tre-pe.jus.br".equals(cmd.destinatario())
                        && cmd.tipoNotificacao() == TipoNotificacao.LEMBRETE_PRAZO
                        && "U1".equals(cmd.unidadeDestinoSigla())
                        && cmd.chaveIdempotencia().startsWith("processo:1:lembrete:unidade:10:dia:")
            ));
        }

    @Test
    @DisplayName("enviarLembrete para unidade ADMIN deve enviar para SEDOC quando ADMIN estiver lotado na SEDOC")
    void enviarLembreteAdminLotadoNaSedoc() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("Processo Teste");
        processo.setDataLimite(LocalDateTime.of(2026, 6, 30, 0, 0));

        Unidade admin = new Unidade();
        admin.setCodigo(1L);
        admin.setSigla("ADMIN");
        admin.setNome("Administrador");
        UnidadeProcesso participanteAdmin = new UnidadeProcesso();
        participanteAdmin.setUnidadeCodigo(1L);
        participanteAdmin.setSigla("ADMIN");
        processo.setParticipantes(new ArrayList<>(List.of(participanteAdmin)));

        Unidade sedoc = new Unidade();
        sedoc.setCodigo(2L);
        sedoc.setSigla("SEDOC");

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("TITULO_SEDOC");
        usuario.setPerfilAtivo(Perfil.ADMIN);
        usuario.setEmail("admin.sedoc@tre-pe.jus.br");
        usuario.setUnidadeLotacao(sedoc);

        Alerta alerta = new Alerta();
        alerta.setCodigo(10L);

        when(processoRepo.buscarPorCodigoComParticipantes(1L)).thenReturn(Optional.of(processo));
        when(unidadeService.buscarPorCodigo(1L)).thenReturn(admin);
        when(emailModelosService.criarEmailLembretePrazo(anyString(), anyString(), any())).thenReturn("<html>lembrete</html>");
        when(servicoAlertas.criarAlertaAdmin(eq(processo), eq(admin), anyString())).thenReturn(alerta);
        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
        when(usuarioService.buscarUsuarioComUnidadeLotacao("TITULO_SEDOC")).thenReturn(usuario);

        processoService.enviarLembrete(1L, 1L);

        verify(notificacaoService, times(1)).enfileirar(argThat(cmd ->
                "sedoc@tre-pe.jus.br".equals(cmd.destinatario())
                        && "ADMIN".equals(cmd.unidadeDestinoSigla())
                        && cmd.chaveIdempotencia().equals("processo:1:lembrete:unidade:1:dia:" + LocalDate.now())
        ));
    }

    @Test
    @DisplayName("enviarLembrete para unidade ADMIN deve enviar cópia ao e-mail pessoal quando ADMIN estiver lotado em outra unidade")
    void enviarLembreteAdminLotadoForaDaSedoc() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("Processo Teste");
        processo.setDataLimite(LocalDateTime.of(2026, 6, 30, 0, 0));

        Unidade admin = new Unidade();
        admin.setCodigo(1L);
        admin.setSigla("ADMIN");
        admin.setNome("Administrador");
        UnidadeProcesso participanteAdmin = new UnidadeProcesso();
        participanteAdmin.setUnidadeCodigo(1L);
        participanteAdmin.setSigla("ADMIN");
        processo.setParticipantes(new ArrayList<>(List.of(participanteAdmin)));

        Unidade outra = new Unidade();
        outra.setCodigo(3L);
        outra.setSigla("COAUD");

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("TITULO_COAUD");
        usuario.setPerfilAtivo(Perfil.ADMIN);
        usuario.setEmail("admin.teste@tre-pe.jus.br");
        usuario.setUnidadeLotacao(outra);

        Alerta alerta = new Alerta();
        alerta.setCodigo(10L);

        when(processoRepo.buscarPorCodigoComParticipantes(1L)).thenReturn(Optional.of(processo));
        when(unidadeService.buscarPorCodigo(1L)).thenReturn(admin);
        when(emailModelosService.criarEmailLembretePrazo(anyString(), anyString(), any())).thenReturn("<html>lembrete</html>");
        when(servicoAlertas.criarAlertaAdmin(eq(processo), eq(admin), anyString())).thenReturn(alerta);
        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
        when(usuarioService.buscarUsuarioComUnidadeLotacao("TITULO_COAUD")).thenReturn(usuario);

        processoService.enviarLembrete(1L, 1L);

        verify(notificacaoService, times(2)).enfileirar(any());
        verify(notificacaoService).enfileirar(argThat(cmd ->
                "sedoc@tre-pe.jus.br".equals(cmd.destinatario())
                        && cmd.chaveIdempotencia().equals("processo:1:lembrete:unidade:1:dia:" + LocalDate.now())
        ));
        verify(notificacaoService).enfileirar(argThat(cmd ->
                "admin.teste@tre-pe.jus.br".equals(cmd.destinatario())
                        && cmd.chaveIdempotencia().equals("processo:1:lembrete:unidade:1:dia:" + LocalDate.now() + ":copia-admin")
        ));
    }

    @Test
    @DisplayName("executarAcaoEmBloco - deve executar ACEITAR")
    void executarAcaoEmBloco_DeveExecutarAceitar() {
        Long codProc = 1L;
        ProcessarAnaliseEmBlocoCommand req = new ProcessarAnaliseEmBlocoCommand(List.of(10L, 20L), AcaoProcesso.ACEITAR);

        Subprocesso spCadastro = new Subprocesso();
        spCadastro.setCodigo(100L);
        spCadastro.setSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        Unidade u1 = new Unidade();
        u1.setCodigo(10L);
        spCadastro.setUnidade(u1);

        Subprocesso spValidacao = new Subprocesso();
        spValidacao.setCodigo(200L);
        spValidacao.setSituacao(MAPEAMENTO_MAPA_DISPONIBILIZADO);
        Unidade u2 = new Unidade();
        u2.setCodigo(20L);
        spValidacao.setUnidade(u2);

        when(consultaService.listarEntidadesPorProcessoEUnidades(eq(codProc), anyList())).thenReturn(List.of(spCadastro, spValidacao));
        Usuario usuario = new Usuario();
        usuario.setUnidadeAtivaCodigo(10L);
        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
        
        when(permissionEvaluator.verificarPermissao(eq(usuario), anyList(), any())).thenReturn(true);

        processoService.executarAcaoEmBloco(codProc, req);

        verify(cadastroFluxoService).aceitarCadastroEmBloco(List.of(100L));
        verify(transicaoService).aceitarValidacaoEmBloco(List.of(200L));
    }

    @Test
    @DisplayName("executarAcaoEmBloco - não deve acionar transições para ação DISPONIBILIZAR em comando de análise")
    void executarAcaoEmBloco_ProcessarAnaliseDisponibilizarSemTransicao() {
        Long codProc = 1L;
        ProcessarAnaliseEmBlocoCommand req = new ProcessarAnaliseEmBlocoCommand(List.of(10L), AcaoProcesso.DISPONIBILIZAR);
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setSituacao(MAPEAMENTO_MAPA_DISPONIBILIZADO);
        Unidade u = new Unidade();
        u.setCodigo(10L);
        sp.setUnidade(u);
        Usuario usuario = new Usuario();

        when(consultaService.listarEntidadesPorProcessoEUnidades(codProc, List.of(10L))).thenReturn(List.of(sp));
        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

        assertThatCode(() -> processoService.executarAcaoEmBloco(codProc, req)).doesNotThrowAnyException();
        verifyNoInteractions(cadastroFluxoService, transicaoService, permissionEvaluator);
    }

    @Test
    @DisplayName("iniciar mapeamento deve falhar quando processo não possui participantes")
    void iniciarMapeamentoSemParticipantes() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setTipo(MAPEAMENTO);
        processo.setSituacao(CRIADO);

        when(repo.buscar(Processo.class, 1L)).thenReturn(processo);

        assertThatThrownBy(() -> processoService.iniciar(1L, List.of()))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining(Mensagens.SEM_UNIDADES_PARTICIPANTES);
    }

    @Test
    @DisplayName("finalizar deve falhar quando subprocessos não estiverem aptos")
    void finalizarComSubprocessosNaoHomologados() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setTipo(MAPEAMENTO);
        processo.setSituacao(EM_ANDAMENTO);
        when(repo.buscar(Processo.class, 1L)).thenReturn(processo);
        when(validacaoService.validarSubprocessosParaFinalizacao(1L)).thenReturn(
                SubprocessoValidacaoService.ResultadoValidacao.ofInvalido("pendente"));

        assertThatThrownBy(() -> processoService.finalizar(1L))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining(Mensagens.SUBPROCESSOS_NAO_HOMOLOGADOS);
    }

    @Test
    @DisplayName("listarIniciadosPorParticipantes - deve retornar pagina normal")
    void listarIniciadosPorParticipantes_Sucesso() {
        when(processoRepo.listarCodigosPorParticipantesESituacaoDiferente(anyList(), eq(SituacaoProcesso.CRIADO), any()))
                .thenReturn(new PageImpl<>(List.of(1L)));
        Processo p = new Processo();
        p.setCodigo(1L);
        when(processoRepo.listarPorCodigosComParticipantes(anyList())).thenReturn(List.of(p));

        Page<Processo> result = processoService.listarIniciadosPorParticipantes(List.of(10L), PageRequest.of(0, 10));
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("obterDetalhesCompleto deve lançar erro quando participante tiver nome vazio")
    void obterDetalhesCompletoDeveLancarErroQuandoParticipanteTiverNomeVazio() {
        Long codProcesso = 1L;
        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setTipo(MAPEAMENTO);

        Unidade unidade = criarUnidadeValida(10L);
        unidade.setNome(" ");
        processo.adicionarParticipantes(Set.of(unidade));

        Usuario usuario = new Usuario();
        usuario.setPerfilAtivo(Perfil.ADMIN);
        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
        when(repo.buscar(Processo.class, codProcesso)).thenReturn(processo);
        when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of());

        assertThatThrownBy(() -> processoService.obterDetalhesCompleto(codProcesso, false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Snapshot inconsistente");
    }

    @Test
    @DisplayName("obterDetalhesCompleto deve lançar erro quando participante tiver sigla vazia")
    void obterDetalhesCompletoDeveLancarErroQuandoParticipanteTiverSiglaVazia() {
        Long codProcesso = 1L;
        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setTipo(MAPEAMENTO);

        Unidade unidade = criarUnidadeValida(10L);
        unidade.setSigla("");
        processo.adicionarParticipantes(Set.of(unidade));

        Usuario usuario = new Usuario();
        usuario.setPerfilAtivo(Perfil.ADMIN);
        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
        when(repo.buscar(Processo.class, codProcesso)).thenReturn(processo);
        when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of());

        assertThatThrownBy(() -> processoService.obterDetalhesCompleto(codProcesso, false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Snapshot inconsistente");
    }

    @Nested
    @DisplayName("Gaps de Data Limite no DTO")
    class DataLimiteGaps {
        @Test
        @DisplayName("obterDetalhesCompleto deve lançar IllegalStateException quando etapa 2 existe sem etapa 1")
        void etapa2SemEtapa1() {
            Long cod = 1L;
            Processo p = new Processo();
            p.setCodigo(cod);
            p.setTipo(MAPEAMENTO);
            Usuario u = new Usuario();
            u.setPerfilAtivo(Perfil.ADMIN);
            u.setUnidadeAtivaCodigo(10L);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            Unidade uni = new Unidade();
            uni.setCodigo(10L);
            uni.setNome("Unidade 10");
            uni.setSigla("U10");
            sp.setUnidade(uni);
            sp.setDataLimiteEtapa2(java.time.LocalDateTime.now());

            when(repo.buscar(Processo.class, cod)).thenReturn(p);
            when(consultaService.listarEntidadesPorProcesso(cod)).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), uni));
            when(permissionEvaluator.verificarPermissao(u, p, sgc.seguranca.AcaoPermissao.FINALIZAR_PROCESSO)).thenReturn(true);
            when(validacaoService.validarSubprocessosParaFinalizacao(cod)).thenReturn(sgc.subprocesso.service.SubprocessoValidacaoService.ResultadoValidacao.ofValido());

            when(usuarioService.usuarioAutenticado()).thenReturn(u);
            assertThatThrownBy(() -> processoService.obterDetalhesCompleto(cod, true))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("sem data limite da etapa 1");
        }

        @Test
        @DisplayName("obterDetalhesCompleto deve priorizar dataLimite2 quando etapa 1 é posterior à etapa 2")
        void etapa1PosteriorEtapa2() {
            Long cod = 1L;
            Processo p = new Processo();
            p.setCodigo(cod);
            p.setTipo(MAPEAMENTO);
            Usuario u = new Usuario();
            u.setPerfilAtivo(Perfil.ADMIN);
            u.setUnidadeAtivaCodigo(10L);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            Unidade uni = new Unidade();
            uni.setCodigo(10L);
            uni.setNome("Unidade 10");
            uni.setSigla("U10");
            sp.setUnidade(uni);
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            sp.setDataLimiteEtapa1(now.plusDays(2));
            sp.setDataLimiteEtapa2(now.plusDays(1));

            when(repo.buscar(Processo.class, cod)).thenReturn(p);
            when(consultaService.listarEntidadesPorProcesso(cod)).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), uni));
            when(permissionEvaluator.verificarPermissao(u, p, sgc.seguranca.AcaoPermissao.FINALIZAR_PROCESSO)).thenReturn(true);
            when(validacaoService.validarSubprocessosParaFinalizacao(cod)).thenReturn(sgc.subprocesso.service.SubprocessoValidacaoService.ResultadoValidacao.ofValido());

            when(usuarioService.usuarioAutenticado()).thenReturn(u);
            ProcessoDetalheDto res = processoService.obterDetalhesCompleto(cod, true);
            assertThat(res.getElegiveis()).isNotEmpty();
            assertThat(res.getElegiveis().getFirst().getUltimaDataLimite()).isEqualTo(sp.getDataLimiteEtapa2());
        }

        @Test
        @DisplayName("obterDetalhesCompleto deve retornar dataLimite2 quando válida via elegíveis")
        void etapa2Valida() {
            Long cod = 1L;
            Processo p = new Processo();
            p.setCodigo(cod);
            p.setTipo(MAPEAMENTO);
            Usuario u = new Usuario();
            u.setPerfilAtivo(Perfil.ADMIN);
            u.setUnidadeAtivaCodigo(10L);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            Unidade uni = new Unidade();
            uni.setCodigo(10L);
            uni.setNome("Unidade 10");
            uni.setSigla("U10");
            sp.setUnidade(uni);
            java.time.LocalDateTime d1 = java.time.LocalDateTime.now().plusDays(1);
            java.time.LocalDateTime d2 = java.time.LocalDateTime.now().plusDays(2);
            sp.setDataLimiteEtapa1(d1);
            sp.setDataLimiteEtapa2(d2);

            when(repo.buscar(Processo.class, cod)).thenReturn(p);
            when(consultaService.listarEntidadesPorProcesso(cod)).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), uni));
            when(permissionEvaluator.verificarPermissao(u, p, sgc.seguranca.AcaoPermissao.FINALIZAR_PROCESSO)).thenReturn(true);
            when(validacaoService.validarSubprocessosParaFinalizacao(cod)).thenReturn(sgc.subprocesso.service.SubprocessoValidacaoService.ResultadoValidacao.ofValido());

            when(usuarioService.usuarioAutenticado()).thenReturn(u);
            ProcessoDetalheDto res = processoService.obterDetalhesCompleto(cod, true);
            assertThat(res.getElegiveis()).isNotEmpty();
            assertThat(res.getElegiveis().getFirst().getUltimaDataLimite()).isEqualTo(d2);
        }

        @Test
        @DisplayName("obterDetalhesCompleto deve tratar subprocesso sem mapa")
        void subprocessoSemMapa() {
            Long cod = 1L;
            Processo p = new Processo();
            p.setCodigo(cod);
            p.setTipo(MAPEAMENTO);
            Usuario u = new Usuario();
            u.setPerfilAtivo(Perfil.ADMIN);
            u.setUnidadeAtivaCodigo(10L);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            Unidade uni = new Unidade();
            uni.setCodigo(10L);
            uni.setSigla("U10");
            uni.setNome("Unidade 10");
            uni.setTipo(TipoUnidade.OPERACIONAL);
            uni.setSituacao(SituacaoUnidade.ATIVA);
            sp.setUnidade(uni);
            sp.setDataLimiteEtapa1(java.time.LocalDateTime.now());
            sp.setMapa(null); // branch 441

            p.adicionarParticipantes(Set.of(uni));

            when(repo.buscar(Processo.class, cod)).thenReturn(p);
            when(consultaService.listarEntidadesPorProcesso(cod)).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), uni));
            when(permissionEvaluator.verificarPermissao(any(Usuario.class), any(Processo.class), any(AcaoPermissao.class))).thenReturn(true);
            when(validacaoService.validarSubprocessosParaFinalizacao(cod)).thenReturn(sgc.subprocesso.service.SubprocessoValidacaoService.ResultadoValidacao.ofValido());

            when(usuarioService.usuarioAutenticado()).thenReturn(u);
            ProcessoDetalheDto res = processoService.obterDetalhesCompleto(cod, false);
            assertThat(res.getUnidades()).hasSize(1);
            assertThat(res.getUnidades().getFirst().getMapaCodigo()).isNull();
        }
    }

    @Test
    @DisplayName("enviarLembrete deve lançar ErroValidacao quando unidade nao participa do processo")
    void enviarLembreteDeveLancarErroQuandoUnidadeNaoParticipaDoProcesso() {
        Long codProcesso = 1L;
        Long codUnidade = 99L;

        Processo p = new Processo();
        p.setCodigo(codProcesso);
        Unidade outraUnidade = criarUnidadeValida(10L);
        p.adicionarParticipantes(Set.of(outraUnidade));

        when(processoRepo.buscarPorCodigoComParticipantes(codProcesso)).thenReturn(Optional.of(p));
        when(unidadeService.buscarPorCodigo(codUnidade)).thenReturn(criarUnidadeValida(codUnidade));

        assertThatThrownBy(() -> processoService.enviarLembrete(codProcesso, codUnidade))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("Unidade não participa");
    }

    @Test
    @DisplayName("finalizar deve criar notificacao consolidada para unidade INTEROPERACIONAL")
    void finalizarDeveCriarNotificacaoConsolidadaParaUnidadeInteroperacional() {
        Long codProcesso = 200L;
        Processo p = new Processo();
        p.setCodigo(codProcesso);
        p.setDescricao("Processo INTEROPERACIONAL");
        p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        p.setTipo(TipoProcesso.MAPEAMENTO);

        Unidade unidadeInterop = criarUnidadeValida(20L);
        unidadeInterop.setTipo(TipoUnidade.INTEROPERACIONAL);
        unidadeInterop.setSigla("INTEROP");
        p.adicionarParticipantes(Set.of(unidadeInterop));

        when(repo.buscar(Processo.class, codProcesso)).thenReturn(p);
        when(unidadeService.buscarPorCodigos(anyList())).thenReturn(List.of(unidadeInterop));
        when(unidadeHierarquiaService.buscarCodigosSuperiores(20L)).thenReturn(List.of());
        when(emailModelosService.criarEmailProcessoFinalizadoPorUnidade(anyString(), anyString()))
                .thenReturn("<html>finalizado interop</html>");
        when(validacaoService.validarSubprocessosParaFinalizacao(codProcesso))
                .thenReturn(ResultadoValidacao.ofValido());
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(2000L);
        subprocesso.setUnidade(unidadeInterop);
        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocesso);
        when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(subprocesso));
        when(mapaRepo.listarPorSubprocessos(List.of(2000L))).thenReturn(List.of(mapa));

        processoService.finalizar(codProcesso);

        verify(notificacaoService, atLeastOnce()).enfileirar(any());
        verify(unidadeService).definirMapasVigentesEmBloco(Map.of(20L, mapa));
    }

    @Test
    @DisplayName("finalizar deve criar notificacao para unidade INTERMEDIARIA com subordinadas")
    void finalizarDeveCriarNotificacaoParaUnidadeIntermediariaComSubordinadas() {
        Long codProcesso = 201L;
        Processo p = new Processo();
        p.setCodigo(codProcesso);
        p.setDescricao("Processo INTERMEDIARIA");
        p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        p.setTipo(TipoProcesso.MAPEAMENTO);

        Unidade unidadeInter = criarUnidadeValida(30L);
        unidadeInter.setTipo(TipoUnidade.INTERMEDIARIA);
        unidadeInter.setSigla("INTER");
        Unidade unidadeOper = criarUnidadeValida(31L);
        unidadeOper.setTipo(TipoUnidade.OPERACIONAL);
        unidadeOper.setSigla("OPER");
        p.adicionarParticipantes(Set.of(unidadeInter, unidadeOper));

        when(repo.buscar(Processo.class, codProcesso)).thenReturn(p);
        when(unidadeService.buscarPorCodigos(anyList())).thenReturn(List.of(unidadeInter, unidadeOper));
        when(unidadeHierarquiaService.buscarCodigosSuperiores(30L)).thenReturn(List.of());
        when(unidadeHierarquiaService.buscarCodigosSuperiores(31L)).thenReturn(List.of(30L));
        when(emailModelosService.criarEmailProcessoFinalizadoPorUnidade(anyString(), anyString()))
                .thenReturn("<html>finalizado inter</html>");
        when(emailModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas(anyString(), anyString(), anyList()))
                .thenReturn("<html>consolidado</html>");
        when(validacaoService.validarSubprocessosParaFinalizacao(codProcesso))
                .thenReturn(ResultadoValidacao.ofValido());
        Subprocesso subprocessoInter = new Subprocesso();
        subprocessoInter.setCodigo(3000L);
        subprocessoInter.setUnidade(unidadeInter);
        Subprocesso subprocessoOper = new Subprocesso();
        subprocessoOper.setCodigo(3001L);
        subprocessoOper.setUnidade(unidadeOper);
        Mapa mapaInter = new Mapa();
        mapaInter.setSubprocesso(subprocessoInter);
        Mapa mapaOper = new Mapa();
        mapaOper.setSubprocesso(subprocessoOper);
        when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(subprocessoInter, subprocessoOper));
        when(mapaRepo.listarPorSubprocessos(List.of(3000L, 3001L))).thenReturn(List.of(mapaInter, mapaOper));

        processoService.finalizar(codProcesso);

        verify(notificacaoService, atLeastOnce()).enfileirar(any());
        verify(unidadeService).definirMapasVigentesEmBloco(Map.of(30L, mapaInter, 31L, mapaOper));
    }

    @Test
    @DisplayName("finalizar deve falhar quando subprocesso homologado não tiver mapa persistido")
    void finalizarDeveFalharQuandoSubprocessoHomologadoNaoTiverMapa() {
        Long codProcesso = 202L;
        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setDescricao("Processo sem mapa");
        processo.setSituacao(EM_ANDAMENTO);
        processo.setTipo(MAPEAMENTO);

        Unidade unidade = criarUnidadeValida(40L);
        processo.adicionarParticipantes(Set.of(unidade));

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(4000L);
        subprocesso.setUnidade(unidade);
        subprocesso.setSituacaoForcada(MAPEAMENTO_MAPA_HOMOLOGADO);

        when(repo.buscar(Processo.class, codProcesso)).thenReturn(processo);
        when(validacaoService.validarSubprocessosParaFinalizacao(codProcesso)).thenReturn(ResultadoValidacao.ofValido());
        when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(subprocesso));
        when(mapaRepo.listarPorSubprocessos(List.of(4000L))).thenReturn(List.of());

        assertThatThrownBy(() -> processoService.finalizar(codProcesso))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("Subprocesso homologado sem mapa");

        verify(unidadeService, never()).definirMapasVigentesEmBloco(anyMap());
        verify(processoRepo, never()).save(any());
    }

}
}
