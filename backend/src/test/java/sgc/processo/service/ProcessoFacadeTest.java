package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.data.domain.*;
import org.springframework.security.core.*;
import sgc.comum.erros.*;
import sgc.fixture.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;
import sgc.seguranca.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sgc.processo.model.AcaoProcesso.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoFacade Test Suite")
class ProcessoFacadeTest {

    @InjectMocks
    private ProcessoFacade processoFacade;

    @Mock
    private ProcessoManutencaoService processoManutencaoService;
    @Mock
    private ProcessoWorkflowService processoWorkflowService;
    @Mock
    private ProcessoConsultaService processoConsultaService;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private SubprocessoService subprocessoService;
    @Mock
    private SubprocessoTransicaoService transicaoService;
    @Mock
    private UsuarioFacade usuarioService;
    @Mock
    private ProcessoValidacaoService processoValidacaoService;
    @Mock
    private ProcessoDetalheBuilder processoDetalheBuilder;
    @Mock
    private ProcessoNotificacaoService processoNotificacaoService;
    @Mock
    private SgcPermissionEvaluator permissionEvaluator;

    @Nested
    @DisplayName("Cobertura e Casos de Borda")
    class CoverageTests {
        @Test
        @DisplayName("iniciarProcesso deve delegar para inicializador")
        void iniciarProcesso_DeveDelegar() {
            Long codigo = 1L;
            List<Long> unidades = List.of(2L, 3L);
            Usuario usuario = new Usuario();
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
            when(processoWorkflowService.iniciar(codigo, unidades, usuario)).thenReturn(List.of("OK"));

            var result = processoFacade.iniciarProcesso(codigo, unidades);

            assertEquals(1, result.size());
            verify(processoWorkflowService).iniciar(codigo, unidades, usuario);
        }

        @Test
        @DisplayName("buscarIdsUnidadesEmProcessosAtivos deve delegar para consulta service")
        void buscarIdsUnidadesEmProcessosAtivos_DeveDelegar() {
            Long codigoIgnorar = 1L;
            Set<Long> unidades = Set.of(10L, 20L);
            when(processoConsultaService.buscarIdsUnidadesComProcessosAtivos(codigoIgnorar)).thenReturn(unidades);

            Set<Long> resultado = processoFacade.buscarIdsUnidadesEmProcessosAtivos(codigoIgnorar);

            assertThat(resultado).isEqualTo(unidades);
            verify(processoConsultaService).buscarIdsUnidadesComProcessosAtivos(codigoIgnorar);
        }

        @Test
        @DisplayName("enviarLembrete deve delegar para processoNotificacaoService")
        void enviarLembrete_DeveDelegar() {
            processoFacade.enviarLembrete(1L, 2L);
            verify(processoNotificacaoService).enviarLembrete(1L, 2L);
        }

        @Test
        @DisplayName("executarAcaoEmBloco ignora ação null na categorização")
        void executarAcaoEmBloco_IgnoraAcaoNull() {
            Long codProcesso = 1L;
            AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                    List.of(10L),
                    ACEITAR, // Mock a valid action for the initial check, then we'll test categorization
                    LocalDate.now()
            );

            Usuario usuario = new Usuario();
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Subprocesso sub = Subprocesso.builder()
                    .codigo(100L)
                    .unidade(Unidade.builder().codigo(10L).build())
                    .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                    .build();

            when(subprocessoService.listarEntidadesPorProcessoEUnidades(codProcesso, req.unidadeCodigos()))
                    .thenReturn(List.of(sub));

            AcaoEmBlocoRequest reqNull = new AcaoEmBlocoRequest(List.of(10L), null, LocalDate.now());
            processoFacade.executarAcaoEmBloco(codProcesso, reqNull);

            verify(transicaoService, never()).aceitarCadastroEmBloco(any(), any());
            verify(transicaoService, never()).homologarCadastroEmBloco(any(), any());
        }
    }

    @Nested
    @DisplayName("Segurança e Controle de Acesso")
    class SecurityTests {
        @Test
        @DisplayName("Deve negar acesso quando usuário não autenticado")
        void deveNegarAcessoQuandoNaoAutenticado() {

            Authentication auth = mock(Authentication.class);
            when(processoValidacaoService.checarAcesso(auth, 1L)).thenReturn(false);
            when(processoValidacaoService.checarAcesso(null, 1L)).thenReturn(false);

            assertThat(processoFacade.checarAcesso(auth, 1L)).isFalse();
            assertThat(processoFacade.checarAcesso(null, 1L)).isFalse();
        }

        @ParameterizedTest
        @ValueSource(longs = {1L, 2L})
        @DisplayName("Deve delegar verificação de acesso para o ProcessoAcessoService")
        void deveDelegarVerificacaoDeAcesso(Long processoCodigo) {
            Authentication auth = mock(Authentication.class);
            when(processoValidacaoService.checarAcesso(auth, processoCodigo)).thenReturn(true);
            assertThat(processoFacade.checarAcesso(auth, processoCodigo)).isTrue();

            when(processoValidacaoService.checarAcesso(auth, processoCodigo)).thenReturn(false);
            assertThat(processoFacade.checarAcesso(auth, processoCodigo)).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando checarAcesso recebe null")
        void deveRetornarFalseQuandoAuthenticationForNull() {
            when(processoValidacaoService.checarAcesso(null, 1L)).thenReturn(false);
            assertThat(processoFacade.checarAcesso(null, 1L)).isFalse();
        }
    }

    @Nested
    @DisplayName("Workflow e Inicialização")
    class Workflow {
        @Test
        @DisplayName("Deve iniciar mapeamento com sucesso delegando para ProcessoInicializador")
        void deveIniciarMapeamentoComSucesso() {
            Long id = 100L;
            Usuario usuario = new Usuario();
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
            when(processoWorkflowService.iniciar(id, List.of(1L), usuario)).thenReturn(List.of());

            List<String> erros = processoFacade.iniciarProcesso(id, List.of(1L));

            assertThat(erros).isEmpty();
            verify(processoWorkflowService).iniciar(id, List.of(1L), usuario);
        }

        @Test
        @DisplayName("Deve retornar erro ao iniciar mapeamento se unidade já em uso")
        void deveRetornarErroAoIniciarMapeamentoSeUnidadeEmUso() {
            Long id = 100L;
            Usuario usuario = new Usuario();
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
            String mensagemErro = "As seguintes unidades já participam de outro processo ativo: U1";
            when(processoWorkflowService.iniciar(id, List.of(1L), usuario)).thenReturn(List.of(mensagemErro));

            List<String> erros = processoFacade.iniciarProcesso(id, List.of(1L));

            assertThat(erros).contains(mensagemErro);
        }

        @Test
        @DisplayName("Deve iniciar revisão com sucesso delegando para ProcessoInicializador")
        void deveIniciarRevisaoComSucesso() {
            Long id = 100L;
            Usuario usuario = new Usuario();
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
            when(processoWorkflowService.iniciar(id, List.of(1L), usuario)).thenReturn(List.of());

            List<String> erros = processoFacade.iniciarProcesso(id, List.of(1L));

            assertThat(erros).isEmpty();
            verify(processoWorkflowService).iniciar(id, List.of(1L), usuario);
        }

        @Test
        @DisplayName("Deve finalizar processo com sucesso quando tudo homologado")
        void deveFinalizarProcessoQuandoTudoHomologado() {
            Long id = 100L;
            processoFacade.finalizar(id);
            verify(processoWorkflowService).finalizar(id);
        }
    }

    @Nested
    @DisplayName("Lembretes")
    class Lembretes {
        @Test
        @DisplayName("Deve delegar enviarLembrete para processoNotificacaoService")
        void deveEnviarLembrete() {
            processoFacade.enviarLembrete(1L, 10L);
            verify(processoNotificacaoService).enviarLembrete(1L, 10L);
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

            when(processoManutencaoService.criar(req)).thenAnswer(
                    i -> {
                        // Simula retorno do serviço
                        Processo p = new Processo();
                        p.setCodigo(100L);
                        p.setDescricao(req.descricao());
                        p.setTipo(req.tipo());
                        p.setSituacao(SituacaoProcesso.CRIADO);
                        return p;
                    });

            Processo resultado = processoFacade.criar(req);

            assertThat(resultado).isNotNull();
            verify(processoManutencaoService).criar(req);
        }

        @Test
        @DisplayName("Deve lançar exceção quando unidade não encontrada (propagada do serviço)")
        void deveLancarExcecaoQuandoUnidadeNaoEncontrada() {

            CriarProcessoRequest req = new CriarProcessoRequest(
                    "Teste", TipoProcesso.MAPEAMENTO, LocalDateTime.now(), List.of(99L));

            when(processoManutencaoService.criar(req))
                    .thenThrow(new ErroEntidadeNaoEncontrada("Unidade", 99L));

            assertThatThrownBy(() -> processoFacade.criar(req))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Unidade")
                    .hasNoCause();
        }
    }

    @Nested
    @DisplayName("Consultas e Detalhes")
    class Consultas {
        private Usuario criarUsuarioMock() {
            Usuario usuario = new Usuario();
            usuario.setTituloEleitoral("12345678901");
            return usuario;
        }

        @Test
        @DisplayName("Deve retornar detalhes do processo (DTO)")
        void deveRetornarDetalhesDoProcesso() {

            Usuario usuario = criarUsuarioMock();
            Long id = 100L;
            Processo processo = ProcessoFixture.processoPadrao();
            when(processoConsultaService.buscarProcessoCodigo(id)).thenReturn(processo);
            when(processoDetalheBuilder.build(eq(processo), any(Usuario.class))).thenReturn(ProcessoDetalheDto.builder().build());

            var res = processoFacade.obterDetalhes(id, usuario);

            assertThat(res).isNotNull();
        }

        @Test
        @DisplayName("Deve buscar entidade por ID")
        void deveBuscarEntidadePorId() {
            Long id = 100L;
            Processo processo = ProcessoFixture.processoPadrao();
            when(processoConsultaService.buscarProcessoCodigo(id)).thenReturn(processo);

            Processo res = processoFacade.buscarEntidadePorId(id);
            assertThat(res).isEqualTo(processo);
        }

        @Test
        @DisplayName("Deve obter processo por ID (Optional)")
        void deveObterPorIdOptional() {
            Long id = 100L;
            Processo processo = ProcessoFixture.processoPadrao();
            when(processoConsultaService.buscarProcessoCodigoOpt(id)).thenReturn(Optional.of(processo));

            Optional<Processo> res = processoFacade.obterPorId(id);
            assertThat(res).isPresent();
        }

        @Test
        @DisplayName("Deve listar processos finalizados e ativos")
        void deveListarProcessosFinalizadosEAtivos() {

            when(processoConsultaService.processosFinalizados())
                    .thenReturn(List.of(ProcessoFixture.processoPadrao()));
            when(processoConsultaService.processosAndamento())
                    .thenReturn(List.of(ProcessoFixture.processoPadrao()));
            assertThat(processoFacade.listarFinalizados()).hasSize(1);
            assertThat(processoFacade.listarAtivos()).hasSize(1);
        }

        @Test
        @DisplayName("Deve listar todos com paginação")
        void deveListarTodosPaginado() {
            Pageable pageable = Pageable.unpaged();
            when(processoConsultaService.processos(pageable)).thenReturn(Page.empty());

            var res = processoFacade.listarTodos(pageable);
            assertThat(res).isEmpty();
        }

        @Test
        @DisplayName("Deve listar unidades bloqueadas por tipo")
        void deveListarUnidadesBloqueadasPorTipo() {

            when(processoConsultaService.unidadesBloqueadasPorTipo(TipoProcesso.MAPEAMENTO))
                    .thenReturn(List.of(1L));

            List<Long> bloqueadas = processoFacade.listarUnidadesBloqueadasPorTipo("MAPEAMENTO");

            assertThat(bloqueadas).contains(1L);
        }

        @Test
        @DisplayName("Deve retornar contexto completo do processo")
        void deveRetornarContextoCompleto() {

            Usuario usuario = criarUsuarioMock();
            Long id = 100L;
            Processo processo = ProcessoFixture.processoPadrao();
            ProcessoDetalheDto detalhes = ProcessoDetalheDto.builder().build();

            when(processoConsultaService.buscarProcessoCodigo(id)).thenReturn(processo);
            when(processoDetalheBuilder.build(eq(processo), any(Usuario.class))).thenReturn(detalhes);
            when(processoConsultaService.subprocessosElegiveis(id))
                    .thenReturn(List.of());

            var res = processoFacade.obterContextoCompleto(id, usuario);

            assertThat(res)
                    .isNotNull()
                    .isEqualTo(detalhes);
        }

        @Test
        @DisplayName("Deve listar subprocessos elegíveis delegando para service")
        void deveListarSubprocessosElegiveisDelegandoParaService() {
            Long codProcesso = 1L;
            List<SubprocessoElegivelDto> lista = List.of(SubprocessoElegivelDto.builder().build());
            when(processoConsultaService.subprocessosElegiveis(codProcesso)).thenReturn(lista);

            var res = processoFacade.listarSubprocessosElegiveis(codProcesso);
            assertThat(res).isSameAs(lista);
            verify(processoConsultaService).subprocessosElegiveis(codProcesso);
        }

        @Test
        @DisplayName("Deve listar entidades subprocessos delegando para service")
        void deveListarEntidadesSubprocessosDelegandoParaService() {
            Long codProcesso = 1L;
            List<Subprocesso> lista = List.of(Subprocesso.builder().build());
            when(subprocessoService.listarEntidadesPorProcesso(codProcesso)).thenReturn(lista);

            var res = processoFacade.listarEntidadesSubprocessos(codProcesso);
            assertThat(res).isSameAs(lista);
        }
    }

    @Nested
    @DisplayName("Operações em Bloco")
    class OperacoesEmBloco {
        @Nested
        @DisplayName("Executar Ação em Bloco - DISPONIBILIZAR")
        class AcaoDisponibilizar {
            @Test
            @DisplayName("Deve disponibilizar mapas em bloco quando ação é DISPONIBILIZAR")
            void deveDisponibilizarMapasEmBloco() {

                Usuario usuario = new Usuario();
                usuario.setTituloEleitoral("12345678901");
                when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

                LocalDate dataLimite = LocalDate.now().plusDays(30);
                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                        List.of(1L, 2L, 3L),
                        AcaoProcesso.DISPONIBILIZAR,
                        dataLimite
                );

                Subprocesso sp1 = Subprocesso.builder().codigo(1001L).unidade(Unidade.builder().codigo(1L).build()).build();
                Subprocesso sp2 = Subprocesso.builder().codigo(1002L).unidade(Unidade.builder().codigo(2L).build()).build();
                Subprocesso sp3 = Subprocesso.builder().codigo(1003L).unidade(Unidade.builder().codigo(3L).build()).build();
                when(subprocessoService.listarEntidadesPorProcessoEUnidades(100L, List.of(1L, 2L, 3L))).thenReturn(List.of(sp1, sp2, sp3));
                doReturn(true).when(permissionEvaluator).checkPermission(eq(usuario), any(), eq("DISPONIBILIZAR_MAPA"));

                processoFacade.executarAcaoEmBloco(100L, req);

                ArgumentCaptor<DisponibilizarMapaRequest> captor =
                        ArgumentCaptor.forClass(DisponibilizarMapaRequest.class);
                verify(transicaoService).disponibilizarMapaEmBloco(
                        eq(List.of(1001L, 1002L, 1003L)),
                        captor.capture(),
                        eq(usuario)
                );

                DisponibilizarMapaRequest captured = captor.getValue();
                assertThat(captured.dataLimite()).isEqualTo(dataLimite);
                assertThat(captured.observacoes()).isEqualTo("Disponibilização em bloco");
            }
        }

        @Nested
        @DisplayName("Executar Ação em Bloco - ACEITAR")
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

                when(subprocessoService.listarEntidadesPorProcessoEUnidades(100L, List.of(10L, 20L))).thenReturn(List.of(sp1, sp2));
                doReturn(true).when(permissionEvaluator).checkPermission(eq(usuario), any(), eq("ACEITAR_CADASTRO"));

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                        List.of(10L, 20L),
                        ACEITAR,
                        null
                );

                processoFacade.executarAcaoEmBloco(100L, req);

                verify(transicaoService).aceitarCadastroEmBloco(List.of(1L, 2L), usuario);
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

                when(subprocessoService.listarEntidadesPorProcessoEUnidades(100L, List.of(10L))).thenReturn(List.of(sp1));
                doReturn(true).when(permissionEvaluator).checkPermission(eq(usuario), any(), eq("ACEITAR_MAPA"));

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                        List.of(10L),
                        ACEITAR,
                        null
                );

                processoFacade.executarAcaoEmBloco(100L, req);

                verify(transicaoService).aceitarValidacaoEmBloco(List.of(1L), usuario);
            }
        }

        @Nested
        @DisplayName("Executar Ação em Bloco - HOMOLOGAR")
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

                when(subprocessoService.listarEntidadesPorProcessoEUnidades(100L, List.of(10L))).thenReturn(List.of(sp1));
                doReturn(true).when(permissionEvaluator).checkPermission(eq(usuario), any(), eq("HOMOLOGAR_CADASTRO"));

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                        List.of(10L),
                        HOMOLOGAR,
                        null
                );

                processoFacade.executarAcaoEmBloco(100L, req);

                verify(transicaoService).homologarCadastroEmBloco(List.of(1L), usuario);
            }

            @Test
            @DisplayName("Deve homologar validação quando subprocessos estão em validação")
            void deveHomologarValidacaoQuandoValidacao() {

                Usuario usuario = new Usuario();
                when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

                Subprocesso sp1 = Subprocesso.builder()
                        .codigo(1L)
                        .unidade(Unidade.builder().codigo(10L).build())
                        .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO) // Situação que não é cadastro
                        .build();

                when(subprocessoService.listarEntidadesPorProcessoEUnidades(100L, List.of(10L))).thenReturn(List.of(sp1));
                doReturn(true).when(permissionEvaluator).checkPermission(eq(usuario), any(), eq("HOMOLOGAR_MAPA"));

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                        List.of(10L),
                        HOMOLOGAR,
                        null
                );

                processoFacade.executarAcaoEmBloco(100L, req);

                verify(transicaoService).homologarValidacaoEmBloco(List.of(1L), usuario);
            }
        }
    }
}
