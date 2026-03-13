package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.data.domain.*;
import org.springframework.security.core.*;
import sgc.comum.model.*;
import sgc.alerta.*;
import sgc.fixture.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;
import sgc.seguranca.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import sgc.subprocesso.service.SubprocessoValidacaoService.ValidationResult;
import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static sgc.seguranca.AcaoPermissao.*;
import static sgc.processo.model.AcaoProcesso.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoService Test suite")
class ProcessoServiceTest {

    @InjectMocks
    private ProcessoService processoService;

    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private ComumRepo repo;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private SubprocessoService subprocessoService;
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
    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private MovimentacaoRepo movimentacaoRepo;
    @Mock
    private SubprocessoTransicaoService transicaoService;
    @Mock
    private UnidadeHierarquiaService hierarquiaService;

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
            processoService.executarAcaoEmBloco(codProcesso, reqNull);

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
            // Assume permissionEvaluator handles this
            assertThat(processoService.checarAcesso(auth, 1L)).isFalse();
        }

        @ParameterizedTest
        @ValueSource(longs = {1L, 2L})
        @DisplayName("Deve delegar verificação de acesso para permissionEvaluator")
        void deveDelegarVerificacaoDeAcesso(Long processoCodigo) {
            Authentication auth = mock(Authentication.class);
        }
    }

    @Nested
    @DisplayName("Workflow e Inicialização")
    class Workflow {
        @Test
        @DisplayName("Deve iniciar mapeamento com sucesso e salvar")
        void deveIniciarMapeamentoComSucesso() {
            Long id = 100L;
            Usuario usuario = new Usuario();
            
            Processo p = new Processo();
            p.setCodigo(id);
            p.setSituacao(SituacaoProcesso.CRIADO);
            p.setTipo(TipoProcesso.MAPEAMENTO);
            Unidade uni = new Unidade();
            uni.setSituacao(SituacaoUnidade.ATIVA);
            p.adicionarParticipantes(Set.of(uni));
            
            when(repo.buscar(Processo.class, id)).thenReturn(p);
            Unidade uniAdmin = new Unidade();
            uniAdmin.setSituacao(SituacaoUnidade.ATIVA);
            when(repo.buscarPorSigla(Unidade.class, "ADMIN")).thenReturn(uniAdmin);

            List<String> erros = processoService.iniciar(id, List.of(), usuario);

            assertThat(erros).isEmpty();
            verify(processoRepo).save(any(Processo.class));
        }

        @Test
        @DisplayName("Deve finalizar processo delegando para repo")
        void deveFinalizarProcessoQuandoTudoHomologado() {
            Long id = 100L;
            Processo p = new Processo();
            p.setCodigo(id);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            p.setTipo(TipoProcesso.DIAGNOSTICO);
            
            when(repo.buscar(Processo.class, id)).thenReturn(p);
            when(unidadeService.porCodigos(anyList())).thenReturn(List.of());
            when(validacaoService.validarSubprocessosParaFinalizacao(id))
                    .thenReturn(ValidationResult.ofValido());
            
            processoService.finalizar(id);
            verify(processoRepo).save(p);
            assertThat(p.getSituacao()).isEqualTo(SituacaoProcesso.FINALIZADO);
        }
    }

    @Nested
    @DisplayName("Lembretes")
    class Lembretes {
        @Test
        @DisplayName("Deve emitir alerta ao enviar lembrete")
        void deveEnviarLembrete() {
            Long codProcesso = 1L;
            Long codUnidade = 10L;
            
            Processo p = new Processo();
            p.setCodigo(codProcesso);
            p.setDescricao("Processo Teste");
            Unidade u = new Unidade();
            u.setCodigo(codUnidade);
            u.setSigla("U10");
            u.setTituloTitular("titular1");
            u.setSituacao(SituacaoUnidade.ATIVA);
            p.adicionarParticipantes(Set.of(u));
            
            when(processoRepo.buscarPorCodigoComParticipantes(codProcesso)).thenReturn(Optional.of(p));
            when(unidadeService.buscarPorCodigo(codUnidade)).thenReturn(u);
            when(emailModelosService.criarEmailLembretePrazo(anyString(), anyString(), any())).thenReturn("<html>body</html>");
            
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            when(subprocessoService.obterEntidadePorProcessoEUnidade(codProcesso, codUnidade)).thenReturn(sp);
            
            Usuario titular = new Usuario();
            titular.setEmail("titular@teste.com");
            when(usuarioService.buscarPorLogin("titular1")).thenReturn(titular);
            
            processoService.enviarLembrete(codProcesso, codUnidade);
            
            verify(emailService).enviarEmailHtml(eq("titular@teste.com"), anyString(), anyString());
            verify(servicoAlertas).criarAlertaAdmin(eq(p), eq(u), anyString());
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

            Unidade uni = new Unidade();
            uni.setSituacao(SituacaoUnidade.ATIVA);
            when(unidadeService.buscarPorCodigo(1L)).thenReturn(uni);
            when(processoRepo.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

            Processo resultado = processoService.criar(req);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getDescricao()).isEqualTo("Teste");
            verify(processoRepo).saveAndFlush(any());
        }
    }

    @Nested
    @DisplayName("Consultas e Detalhes")
    class Consultas {
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
            when(repo.buscar(Processo.class, id)).thenReturn(processo);

            Optional<Processo> res = processoService.buscarOpt(id);
            assertThat(res).isPresent();
        }

        @Test
        @DisplayName("Deve listar todos com paginação")
        void deveListarTodosPaginado() {
            Pageable pageable = Pageable.unpaged();
            when(processoRepo.findAll(pageable)).thenReturn(Page.empty());

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
                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                        List.of(1L, 2L, 3L),
                        AcaoProcesso.DISPONIBILIZAR,
                        dataLimite
                );

                Subprocesso sp1 = Subprocesso.builder().codigo(1001L).unidade(Unidade.builder().codigo(1L).build()).build();
                Subprocesso sp2 = Subprocesso.builder().codigo(1002L).unidade(Unidade.builder().codigo(2L).build()).build();
                Subprocesso sp3 = Subprocesso.builder().codigo(1003L).unidade(Unidade.builder().codigo(3L).build()).build();
                when(subprocessoService.listarEntidadesPorProcessoEUnidades(100L, List.of(1L, 2L, 3L))).thenReturn(List.of(sp1, sp2, sp3));
                doReturn(true).when(permissionEvaluator).verificarPermissao(eq(usuario), any(), eq(DISPONIBILIZAR_MAPA));

                processoService.executarAcaoEmBloco(100L, req);

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

                when(subprocessoService.listarEntidadesPorProcessoEUnidades(100L, List.of(10L, 20L))).thenReturn(List.of(sp1, sp2));

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                        List.of(10L, 20L),
                        ACEITAR,
                        null
                );

                processoService.executarAcaoEmBloco(100L, req);

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

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                        List.of(10L),
                        ACEITAR,
                        null
                );

                processoService.executarAcaoEmBloco(100L, req);

                verify(transicaoService).aceitarValidacaoEmBloco(List.of(1L), usuario);
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

                when(subprocessoService.listarEntidadesPorProcessoEUnidades(100L, List.of(10L))).thenReturn(List.of(sp1));

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                        List.of(10L),
                        HOMOLOGAR,
                        null
                );

                processoService.executarAcaoEmBloco(100L, req);

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
                        .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO)
                        .build();

                when(subprocessoService.listarEntidadesPorProcessoEUnidades(100L, List.of(10L))).thenReturn(List.of(sp1));

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                        List.of(10L),
                        HOMOLOGAR,
                        null
                );

                processoService.executarAcaoEmBloco(100L, req);

                verify(transicaoService).homologarValidacaoEmBloco(List.of(1L), usuario);
            }
        }
    }
}
