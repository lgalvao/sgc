package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.security.core.*;
import org.springframework.test.util.*;
import sgc.alerta.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.mapa.model.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;
import sgc.seguranca.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoService Extra Coverage Test")
@SuppressWarnings("NullAway.Init")
class ProcessoServiceExtraCoverageTest {

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
    private EmailService emailService;

    @Mock
    private EmailModelosService emailModelosService;

    @Mock
    private SgcPermissionEvaluator permissionEvaluator;

    private void mockarResponsaveisEfetivos() {
        when(responsavelUnidadeService.todasPossuemResponsavelEfetivo(anyList())).thenReturn(true);
    }

    private Unidade criarUnidadeValida(Long codigo) {
        Unidade unidade = new Unidade();
        unidade.setCodigo(codigo);
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        return unidade;
    }

    private Processo criarProcessoComParticipante(Long codProcesso, Long codUnidade) {
        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setDescricao("Processo " + codProcesso);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDataLimite(LocalDateTime.now().plusDays(5));

        UnidadeProcesso participante = new UnidadeProcesso();
        participante.setUnidadeCodigo(codUnidade);
        participante.setSigla("U" + codUnidade);
        processo.setParticipantes(List.of(participante));
        return processo;
    }

    @Nested
    @DisplayName("buscarPorCodigoComParticipantes")
    class BuscarPorCodigoComParticipantes {
        @Test
        @DisplayName("deve lancar excecao se nao encontrar")
        void deveLancarExcecao() {
            when(processoRepo.buscarPorCodigoComParticipantes(1L)).thenReturn(Optional.empty());

            assertThrows(sgc.comum.erros.ErroEntidadeNaoEncontrada.class, () -> processoService.buscarPorCodigoComParticipantes(1L));
        }

        @Test
        @DisplayName("deve retornar se encontrar")
        void deveRetornar() {
            Processo p = new Processo();
            when(processoRepo.buscarPorCodigoComParticipantes(1L)).thenReturn(Optional.of(p));

            Processo res = processoService.buscarPorCodigoComParticipantes(1L);

            assertThat(res).isEqualTo(p);
        }
    }

    @Nested
    @DisplayName("listarFinalizados")
    class ListarFinalizados {
        @Test
        @DisplayName("deve retornar listarPorSituacaoComParticipantes se admin")
        void admin() {
            Usuario u = new Usuario();
            u.setPerfilAtivo(Perfil.ADMIN);
            when(usuarioService.usuarioAutenticado()).thenReturn(u);

            Processo p = new Processo();
            when(processoRepo.listarPorSituacaoComParticipantes(SituacaoProcesso.FINALIZADO)).thenReturn(List.of(p));

            List<Processo> res = processoService.listarFinalizados();

            assertThat(res).containsExactly(p);
        }

        @Test
        @DisplayName("deve retornar listarPorSituacaoEUnidadeCodigos se nao admin")
        void naoAdmin() {
            Usuario u = new Usuario();
            u.setPerfilAtivo(Perfil.GESTOR);
            u.setUnidadeAtivaCodigo(1L);
            when(usuarioService.usuarioAutenticado()).thenReturn(u);

            Unidade uni = new Unidade();
            uni.setCodigo(1L);
            when(unidadeHierarquiaService.buscarIdsDescendentes(1L)).thenReturn(List.of());

            Processo p = new Processo();
            when(processoRepo.listarPorSituacaoEUnidadeCodigos(eq(SituacaoProcesso.FINALIZADO), anyList())).thenReturn(List.of(p));

            List<Processo> res = processoService.listarFinalizados();

            assertThat(res).containsExactly(p);
        }
    }

    @Nested
    @DisplayName("atualizar")
    class Atualizar {
        @Test
        @DisplayName("deve lancar erro se nao estiver em criacao")
        void situacaoInvalida() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            AtualizarProcessoRequest req = new AtualizarProcessoRequest(1L, "desc", TipoProcesso.MAPEAMENTO, LocalDateTime.now().plusDays(1), List.of());

            assertThrows(ErroValidacao.class, () -> processoService.atualizar(1L, req));
        }

        @Test
        @DisplayName("deve lancar erro se unidade invalida")
        void unidadeInvalida() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.CRIADO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            Unidade u = new Unidade();
            u.setTipo(TipoUnidade.INTERMEDIARIA);
            when(unidadeService.buscarPorCodigos(List.of(1L))).thenReturn(List.of(u));

            AtualizarProcessoRequest req = new AtualizarProcessoRequest(1L, "desc", TipoProcesso.MAPEAMENTO, LocalDateTime.now().plusDays(1), List.of(1L));

            assertThrows(ErroValidacao.class, () -> processoService.atualizar(1L, req));
        }

        @Test
        @DisplayName("deve atualizar com sucesso")
        void sucesso() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.CRIADO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            Unidade u = criarUnidadeValida(1L);
            when(unidadeService.buscarPorCodigos(List.of(1L))).thenReturn(List.of(u));
            mockarResponsaveisEfetivos();

            when(processoRepo.saveAndFlush(p)).thenReturn(p);

            AtualizarProcessoRequest req = new AtualizarProcessoRequest(1L, "desc2", TipoProcesso.MAPEAMENTO, LocalDateTime.now().plusDays(1), List.of(1L));

            Processo res = processoService.atualizar(1L, req);

            assertThat(res.getDescricao()).isEqualTo("desc2");
        }
    }

    @Nested
    @DisplayName("apagar")
    class Apagar {
        @Test
        @DisplayName("deve lancar erro se nao estiver em criacao")
        void situacaoInvalida() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            assertThrows(ErroValidacao.class, () -> processoService.apagar(1L));
        }

        @Test
        @DisplayName("deve apagar com sucesso")
        void sucesso() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.CRIADO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            processoService.apagar(1L);

            verify(processoRepo).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("iniciar")
    class Iniciar {
        @Test
        @DisplayName("deve lancar erro se nao estiver em criacao")
        void situacaoInvalida() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            assertThrows(ErroValidacao.class, () -> processoService.iniciar(1L, List.of(), new Usuario()));
        }

        @Test
        @DisplayName("deve lancar erro se revisao sem unidades")
        void revisaoSemUnidades() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.CRIADO);
            p.setTipo(TipoProcesso.REVISAO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            assertThrows(ErroValidacao.class, () -> processoService.iniciar(1L, List.of(), new Usuario()));
        }

        @Test
        @DisplayName("deve lancar erro se mapeamento sem participantes")
        void mapeamentoSemParticipantes() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.CRIADO);
            p.setTipo(TipoProcesso.MAPEAMENTO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            assertThrows(ErroValidacao.class, () -> processoService.iniciar(1L, List.of(), new Usuario()));
        }
    }

    @Nested
    @DisplayName("finalizar")
    class Finalizar {
        @Test
        @DisplayName("deve lancar erro se nao em andamento")
        void naoAndamento() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.CRIADO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            assertThrows(ErroValidacao.class, () -> processoService.finalizar(1L));
        }

        @Test
        @DisplayName("deve lancar erro se nao validado")
        void naoValidado() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            when(validacaoService.validarSubprocessosParaFinalizacao(any())).thenReturn(sgc.subprocesso.service.SubprocessoValidacaoService.ValidationResult.ofInvalido("Erro de validacao"));

            assertThrows(ErroValidacao.class, () -> processoService.finalizar(1L));
        }
    }

    @Test
    @DisplayName("enviarLembrete deve falhar quando processo estiver sem data limite")
    void enviarLembreteDeveFalharSemDataLimite() {
        Processo processo = criarProcessoComParticipante(1L, 10L);
        processo.setDataLimite(null);
        when(processoRepo.buscarPorCodigoComParticipantes(1L)).thenReturn(Optional.of(processo));

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setSigla("U10");
        unidade.setTituloTitular("TITULO10");
        when(unidadeService.buscarPorCodigo(10L)).thenReturn(unidade);

        assertThatThrownBy(() -> processoService.enviarLembrete(1L, 10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sem data limite");
    }

    @Test
    @DisplayName("enviarLembrete deve falhar quando unidade estiver sem titular oficial")
    void enviarLembreteDeveFalharSemTitularOficial() {
        Processo processo = criarProcessoComParticipante(2L, 20L);
        when(processoRepo.buscarPorCodigoComParticipantes(2L)).thenReturn(Optional.of(processo));

        Unidade unidade = new Unidade();
        unidade.setCodigo(20L);
        unidade.setSigla("U20");
        unidade.setTituloTitular("   ");
        when(unidadeService.buscarPorCodigo(20L)).thenReturn(unidade);

        when(emailModelosService.criarEmailLembretePrazo(anyString(), anyString(), any())).thenReturn("<html/>");

        assertThatThrownBy(() -> processoService.enviarLembrete(2L, 20L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sem titular oficial");
    }

    @Test
    @DisplayName("validarUnidadesInicio deve ignorar erro de mapa quando nenhuma sigla for retornada")
    void validarUnidadesInicioSemSiglaNaoAdicionaErroMapa() {
        when(unidadeService.buscarPorCodigos(List.of(30L))).thenReturn(List.of(criarUnidadeValida(30L)));
        when(responsavelUnidadeService.todasPossuemResponsavelEfetivo(List.of(30L))).thenReturn(true);
        when(unidadeService.buscarTodosCodigosUnidadesComMapa()).thenReturn(List.of());
        when(unidadeService.buscarSiglasPorCodigos(List.of(30L))).thenReturn(List.of());
        when(processoRepo.listarUnidadesEmProcessoAtivo(SituacaoProcesso.EM_ANDAMENTO, List.of(30L))).thenReturn(List.of());

        List<String> erros = ReflectionTestUtils.invokeMethod(
                processoService,
                "validarUnidadesInicio",
                TipoProcesso.REVISAO,
                List.of(30L)
        );

        assertThat(erros).isEmpty();
    }

    @Test
    @DisplayName("efetivarInicioSubprocessos deve executar fluxo de diagnostico")
    void efetivarInicioSubprocessosDeveExecutarFluxoDiagnostico() {
        Processo processo = new Processo();
        Unidade unidade = criarUnidadeValida(40L);
        Set<Unidade> participantes = Set.of(unidade);

        UnidadeMapa unidadeMapa = new UnidadeMapa();
        unidadeMapa.setUnidadeCodigo(40L);
        unidadeMapa.setMapaVigente(new Mapa());
        List<UnidadeMapa> unidadesMapa = List.of(unidadeMapa);

        Unidade unidadeAdmin = criarUnidadeValida(99L);
        Usuario usuario = new Usuario();

        ReflectionTestUtils.invokeMethod(
                processoService,
                "efetivarInicioSubprocessos",
                processo,
                TipoProcesso.DIAGNOSTICO,
                List.of(40L),
                participantes,
                unidadesMapa,
                unidadeAdmin,
                usuario
        );

        verify(subprocessoService).criarParaDiagnostico(eq(processo), eq(unidade), eq(unidadeMapa), eq(unidadeAdmin), eq(usuario));
    }

    @Test
    @DisplayName("isElegivelParaAcaoEmBloco deve aceitar revisao mapa ajustado quando houver permissao")
    void isElegivelParaAcaoEmBlocoDeveAceitarRevisaoMapaAjustado() {
        Usuario usuario = new Usuario();
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);

        when(permissionEvaluator.verificarPermissao(usuario, subprocesso, AcaoPermissao.DISPONIBILIZAR_MAPA)).thenReturn(true);

        Boolean elegivel = ReflectionTestUtils.invokeMethod(
                processoService,
                "isElegivelParaAcaoEmBloco",
                subprocesso,
                usuario
        );

        assertThat(elegivel).isTrue();
    }

    @Test
    @DisplayName("enviarLembrete deve falhar quando unidade não participa do processo")
    void enviarLembreteDeveFalharQuandoUnidadeNaoParticipa() {
        Processo processo = criarProcessoComParticipante(3L, 30L);
        when(processoRepo.buscarPorCodigoComParticipantes(3L)).thenReturn(Optional.of(processo));
        when(unidadeService.buscarPorCodigo(99L)).thenReturn(criarUnidadeValida(99L));

        assertThatThrownBy(() -> processoService.enviarLembrete(3L, 99L))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("não participa");
    }

    @Test
    @DisplayName("validarUnidadesInicio deve retornar erro quando houver unidade bloqueada em processo ativo")
    void validarUnidadesInicioDeveRetornarErroQuandoHouverUnidadeBloqueada() {
        when(unidadeService.buscarPorCodigos(List.of(50L))).thenReturn(List.of(criarUnidadeValida(50L)));
        when(responsavelUnidadeService.todasPossuemResponsavelEfetivo(List.of(50L))).thenReturn(true);
        when(processoRepo.listarUnidadesEmProcessoAtivo(SituacaoProcesso.EM_ANDAMENTO, List.of(50L))).thenReturn(List.of(50L));

        List<String> erros = ReflectionTestUtils.invokeMethod(
                processoService,
                "validarUnidadesInicio",
                TipoProcesso.MAPEAMENTO,
                List.of(50L)
        );

        assertThat(erros).isNotEmpty();
    }

    @Test
    @DisplayName("efetivarInicioSubprocessos deve executar fluxo de revisão")
    void efetivarInicioSubprocessosDeveExecutarFluxoRevisao() {
        Processo processo = new Processo();
        Unidade unidade = criarUnidadeValida(60L);
        UnidadeMapa unidadeMapa = new UnidadeMapa();
        unidadeMapa.setUnidadeCodigo(60L);
        unidadeMapa.setMapaVigente(new Mapa());
        Unidade unidadeAdmin = criarUnidadeValida(99L);
        Usuario usuario = new Usuario();

        ReflectionTestUtils.invokeMethod(
                processoService,
                "efetivarInicioSubprocessos",
                processo,
                TipoProcesso.REVISAO,
                List.of(60L),
                Set.of(unidade),
                List.of(unidadeMapa),
                unidadeAdmin,
                usuario
        );

        verify(subprocessoService).criarParaRevisao(eq(processo), eq(unidade), eq(unidadeMapa), eq(unidadeAdmin), eq(usuario));
    }

    @Test
    @DisplayName("isElegivelParaAcaoEmBloco deve retornar falso para situação não elegível")
    void isElegivelParaAcaoEmBlocoDeveRetornarFalsoParaSituacaoNaoElegivel() {
        Usuario usuario = new Usuario();
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacao(SituacaoSubprocesso.NAO_INICIADO);

        Boolean elegivel = ReflectionTestUtils.invokeMethod(
                processoService,
                "isElegivelParaAcaoEmBloco",
                subprocesso,
                usuario
        );

        assertThat(elegivel).isFalse();
    }

    @Test
    @DisplayName("enviarLembrete deve falhar quando titular oficial for nulo")
    void enviarLembreteDeveFalharQuandoTitularForNulo() {
        Processo processo = criarProcessoComParticipante(4L, 40L);
        when(processoRepo.buscarPorCodigoComParticipantes(4L)).thenReturn(Optional.of(processo));

        Unidade unidade = new Unidade();
        unidade.setCodigo(40L);
        unidade.setSigla("U40");
        unidade.setTituloTitular(null);
        when(unidadeService.buscarPorCodigo(40L)).thenReturn(unidade);
        when(emailModelosService.criarEmailLembretePrazo(anyString(), anyString(), any())).thenReturn("<html/>");

        assertThatThrownBy(() -> processoService.enviarLembrete(4L, 40L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sem titular oficial");
    }

    @Test
    @DisplayName("validarUnidadesInicio deve adicionar erro de mapa para processo de diagnostico")
    void validarUnidadesInicioDeveAdicionarErroMapaParaDiagnostico() {
        when(unidadeService.buscarPorCodigos(List.of(70L))).thenReturn(List.of(criarUnidadeValida(70L)));
        when(responsavelUnidadeService.todasPossuemResponsavelEfetivo(List.of(70L))).thenReturn(true);
        when(unidadeService.buscarTodosCodigosUnidadesComMapa()).thenReturn(List.of());
        when(unidadeService.buscarSiglasPorCodigos(List.of(70L))).thenReturn(List.of("U70"));
        when(processoRepo.listarUnidadesEmProcessoAtivo(SituacaoProcesso.EM_ANDAMENTO, List.of(70L))).thenReturn(List.of());

        List<String> erros = ReflectionTestUtils.invokeMethod(
                processoService,
                "validarUnidadesInicio",
                TipoProcesso.DIAGNOSTICO,
                List.of(70L)
        );

        assertThat(erros).isNotEmpty();
    }

    @Test
    @DisplayName("efetivarInicioSubprocessos deve executar fluxo de mapeamento")
    void efetivarInicioSubprocessosDeveExecutarFluxoMapeamento() {
        Processo processo = new Processo();
        Unidade unidade = criarUnidadeValida(80L);
        Unidade unidadeAdmin = criarUnidadeValida(99L);
        Usuario usuario = new Usuario();

        ReflectionTestUtils.invokeMethod(
                processoService,
                "efetivarInicioSubprocessos",
                processo,
                TipoProcesso.MAPEAMENTO,
                List.of(80L),
                Set.of(unidade),
                List.of(),
                unidadeAdmin,
                usuario
        );

        verify(subprocessoService).criarParaMapeamento(eq(processo), eq(Set.of(unidade)), eq(unidadeAdmin), eq(usuario));
    }

    @Test
    @DisplayName("isElegivelParaAcaoEmBloco deve aceitar fluxo de mapa quando houver permissão de homologação")
    void isElegivelParaAcaoEmBlocoDeveAceitarFluxoMapaComHomologacao() {
        Usuario usuario = new Usuario();
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_MAPA_VALIDADO);

        when(permissionEvaluator.verificarPermissao(usuario, subprocesso, AcaoPermissao.ACEITAR_MAPA)).thenReturn(false);
        when(permissionEvaluator.verificarPermissao(usuario, subprocesso, AcaoPermissao.HOMOLOGAR_MAPA)).thenReturn(true);

        Boolean elegivel = ReflectionTestUtils.invokeMethod(
                processoService,
                "isElegivelParaAcaoEmBloco",
                subprocesso,
                usuario
        );

        assertThat(elegivel).isTrue();
    }

    @Nested
    @DisplayName("enviarLembrete")
    class EnviarLembrete {
        @Test
        @DisplayName("deve lancar erro se unidade nao participa")
        void unidadeNaoParticipa() {
            Processo p = new Processo();
            p.setParticipantes(new ArrayList<>());
            when(processoRepo.buscarPorCodigoComParticipantes(1L)).thenReturn(Optional.of(p));
            when(unidadeService.buscarPorCodigo(2L)).thenReturn(new Unidade());

            assertThrows(ErroValidacao.class, () -> processoService.enviarLembrete(1L, 2L));
        }

        @Test
        @DisplayName("deve enviar lembrete com sucesso")
        void sucesso() {
            Processo p = new Processo();
            p.setDescricao("Processo Teste");
            p.setDataLimite(LocalDateTime.of(2026, 3, 22, 12, 0));
            UnidadeProcesso up = new UnidadeProcesso();
            up.setUnidadeCodigo(1L);
            p.setParticipantes(new ArrayList<>(List.of(up)));
            when(processoRepo.buscarPorCodigoComParticipantes(1L)).thenReturn(Optional.of(p));

            Unidade u = new Unidade();
            u.setCodigo(1L);
            u.setSigla("UNI1");
            u.setTituloTitular("TITULAR");
            when(unidadeService.buscarPorCodigo(1L)).thenReturn(u);

            when(emailModelosService.criarEmailLembretePrazo(anyString(), anyString(), any())).thenReturn("html");
            Usuario titular = new Usuario();
            titular.setEmail("titular@teste.com");
            when(usuarioService.buscarPorLogin("TITULAR")).thenReturn(titular);

            processoService.enviarLembrete(1L, 1L);

            verify(emailService).enviarEmailHtml(eq("titular@teste.com"), anyString(), eq("html"));
            verify(servicoAlertas).criarAlertaAdmin(eq(p), eq(u), anyString());
        }
    }

    @Nested
    @DisplayName("listarSubprocessosElegiveis")
    class ListarSubprocessosElegiveis {
        @Test
        @DisplayName("deve listar para nao-admin")
        void naoAdmin() {
            Usuario u = new Usuario();
            u.setPerfilAtivo(Perfil.GESTOR);
            u.setUnidadeAtivaCodigo(1L);
            when(usuarioService.usuarioAutenticado()).thenReturn(u);

            Unidade uni = new Unidade();
            uni.setCodigo(1L);
            when(unidadeHierarquiaService.buscarIdsDescendentes(1L)).thenReturn(List.of());

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            sp.setUnidade(uni);
            when(consultaService.listarEntidadesPorProcessoEUnidades(eq(1L), anyList())).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(uni);
            when(permissionEvaluator.verificarPermissao(u, sp, sgc.seguranca.AcaoPermissao.DISPONIBILIZAR_MAPA)).thenReturn(true);

            List<SubprocessoElegivelDto> res = processoService.listarSubprocessosElegiveis(1L);

            assertThat(res).hasSize(1);
            assertThat(res.getFirst().getCodigo()).isEqualTo(100L);
        }
    }

    @Nested
    @DisplayName("checarAcesso")
    class ChecarAcesso {
        @Test
        @DisplayName("deve retornar falso se nao autenticado")
        void naoAutenticado() {
            assertThat(processoService.checarAcesso(null, 1L)).isFalse();
            
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(false);
            assertThat(processoService.checarAcesso(auth, 1L)).isFalse();
        }

        @Test
        @DisplayName("deve retornar verdadeiro se admin")
        void admin() {
            Usuario u = new Usuario();
            u.setPerfilAtivo(Perfil.ADMIN);
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getPrincipal()).thenReturn(u);

            assertThat(processoService.checarAcesso(auth, 1L)).isTrue();
        }

        @Test
        @DisplayName("deve retornar verdadeiro se participante")
        void participante() {
            Usuario u = new Usuario();
            u.setPerfilAtivo(Perfil.GESTOR);
            u.setUnidadeAtivaCodigo(1L);
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getPrincipal()).thenReturn(u);

            Unidade uni = new Unidade();
            uni.setCodigo(1L);
            when(unidadeHierarquiaService.buscarIdsDescendentes(1L)).thenReturn(List.of());

            Processo p = new Processo();
            UnidadeProcesso up = new UnidadeProcesso();
            up.setUnidadeCodigo(1L);
            p.setParticipantes(new ArrayList<>(List.of(up)));
            when(processoRepo.buscarPorCodigoComParticipantes(1L)).thenReturn(Optional.of(p));

            assertThat(processoService.checarAcesso(auth, 1L)).isTrue();
        }

        @Test
        @DisplayName("deve retornar falso se nao participante")
        void naoParticipante() {
            Usuario u = new Usuario();
            u.setPerfilAtivo(Perfil.GESTOR);
            u.setUnidadeAtivaCodigo(1L);
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getPrincipal()).thenReturn(u);

            Unidade uni = new Unidade();
            uni.setCodigo(1L);
            when(unidadeHierarquiaService.buscarIdsDescendentes(1L)).thenReturn(List.of());

            Processo p = new Processo();
            UnidadeProcesso up = new UnidadeProcesso();
            up.setUnidadeCodigo(2L);
            p.setParticipantes(new ArrayList<>(List.of(up)));
            when(processoRepo.buscarPorCodigoComParticipantes(1L)).thenReturn(Optional.of(p));

            assertThat(processoService.checarAcesso(auth, 1L)).isFalse();
        }
    }

    @Nested
    @DisplayName("finalizar")
    class FinalizarMaisGaps {
        @Test
        @DisplayName("deve tornar mapas vigentes se nao for diagnostico")
        void naoDiagnostico() {
            Processo p = new Processo();
            p.setCodigo(1L);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            p.setTipo(TipoProcesso.MAPEAMENTO);
            p.setParticipantes(new ArrayList<>());
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            when(validacaoService.validarSubprocessosParaFinalizacao(1L)).thenReturn(sgc.subprocesso.service.SubprocessoValidacaoService.ValidationResult.ofValido());

            Subprocesso sp = new Subprocesso();
            Unidade uni = new Unidade();
            uni.setCodigo(10L);
            sp.setUnidade(uni);
            Mapa mapa = new Mapa();
            sp.setMapa(mapa);
            when(consultaService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));

            processoService.finalizar(1L);

            verify(unidadeService).definirMapaVigente(10L, mapa);
        }
    }

    @Nested
    @DisplayName("iniciar")
    class IniciarMaisGaps {
        @Test
        @DisplayName("deve iniciar diagnostico")
        void diagnostico() {
            Processo p = new Processo();
            p.setCodigo(1L);
            p.setSituacao(SituacaoProcesso.CRIADO);
            p.setTipo(TipoProcesso.DIAGNOSTICO);
            
            UnidadeProcesso up = new UnidadeProcesso();
            up.setUnidadeCodigo(10L);
            p.setParticipantes(new ArrayList<>(List.of(up)));
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            Unidade uni = new Unidade();
            uni.setCodigo(10L);
            uni.setSigla("UNI10");
            when(unidadeService.buscarPorCodigos(anyList())).thenReturn(List.of(uni));
            when(unidadeService.buscarTodosCodigosUnidadesComMapa()).thenReturn(List.of(10L));
            UnidadeMapa unidadeMapa = new UnidadeMapa();
            unidadeMapa.setUnidadeCodigo(10L);
            Mapa mapaVigente = new Mapa();
            mapaVigente.setCodigo(20L);
            unidadeMapa.setMapaVigente(mapaVigente);
            when(unidadeService.buscarMapasPorUnidades(anyList())).thenReturn(List.of(unidadeMapa));
            mockarResponsaveisEfetivos();

            Unidade admin = new Unidade();
            when(unidadeService.buscarAdmin()).thenReturn(admin);

            processoService.iniciar(1L, List.of(), new Usuario());

            verify(subprocessoService).criarParaDiagnostico(eq(p), eq(uni), any(), eq(admin), any());
        }
    }

    @Nested
    @DisplayName("isElegivelParaAcaoEmBloco")
    class IsElegivelParaAcaoEmBloco {
        @Test
        @DisplayName("deve ser elegivel para mapa")
        void elegivelMapa() {
            Usuario u = new Usuario();
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            
            when(permissionEvaluator.verificarPermissao(u, sp, sgc.seguranca.AcaoPermissao.ACEITAR_MAPA)).thenReturn(true);

            // Chamando via listarSubprocessosElegiveis para testar o metodo privado
            u.setPerfilAtivo(Perfil.ADMIN);
            when(usuarioService.usuarioAutenticado()).thenReturn(u);
            when(consultaService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
            Unidade uni = new Unidade();
            uni.setCodigo(1L);
            sp.setUnidade(uni);
            when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(uni);

            List<SubprocessoElegivelDto> res = processoService.listarSubprocessosElegiveis(1L);
            assertThat(res).hasSize(1);
        }

        @Test
        @DisplayName("nao deve ser elegivel se situacao nao permite")
        void naoElegivel() {
            Usuario u = new Usuario();
            Subprocesso sp = new Subprocesso();
            sp.setSituacao(SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
            
            u.setPerfilAtivo(Perfil.ADMIN);
            when(usuarioService.usuarioAutenticado()).thenReturn(u);
            when(consultaService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));

            List<SubprocessoElegivelDto> res = processoService.listarSubprocessosElegiveis(1L);
            assertThat(res).isEmpty();
        }
    }
}
