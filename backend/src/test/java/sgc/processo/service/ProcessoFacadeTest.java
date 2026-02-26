package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import sgc.alerta.AlertaFacade;
import sgc.alerta.EmailModelosService;
import sgc.alerta.EmailService;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.UnidadeFixture;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.ProcessoFacade;
import sgc.processo.dto.AcaoEmBlocoRequest;
import sgc.processo.dto.CriarProcessoRequest;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.dto.SubprocessoElegivelDto;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.model.*;
import sgc.seguranca.SgcPermissionEvaluator;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static sgc.processo.model.AcaoProcesso.ACEITAR;
import static sgc.processo.model.AcaoProcesso.HOMOLOGAR;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoFacade Test Suite")
class ProcessoFacadeTest {

    @InjectMocks
    private ProcessoFacade processoFacade;

    @Mock
    private ProcessoManutencaoService processoManutencaoService;
    @Mock
    private ProcessoInicializador processoInicializador;
    @Mock
    private ProcessoConsultaService processoConsultaService;
    @Mock
    private OrganizacaoFacade unidadeService;
    @Mock
    private AlertaFacade alertaService;
    @Mock
    private EmailService emailService;
    @Mock
    private EmailModelosService emailModelosService;
    @Mock
    private SubprocessoService subprocessoService;
    @Mock
    private UsuarioFacade usuarioService;
    @Mock
    private ProcessoAcessoService processoAcessoService;
    @Mock
    private ProcessoDetalheBuilder processoDetalheBuilder;
    @Mock
    private ProcessoFinalizador processoFinalizador;
    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private ProcessoValidador processoValidador;
    @Mock
    private SgcPermissionEvaluator permissionEvaluator;


    @Nested
    @DisplayName("Cobertura e Casos de Borda")
    class CoverageTests {
        @Test
        @DisplayName("iniciarProcessoDiagnostico deve delegar para inicializador")
        void iniciarProcessoDiagnostico_DeveDelegar() {
            Long codigo = 1L;
            List<Long> unidades = List.of(2L, 3L);
            Usuario usuario = new Usuario();
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
            when(processoInicializador.iniciar(codigo, unidades, usuario)).thenReturn(List.of("OK"));

            var result = processoFacade.iniciarProcessoDiagnostico(codigo, unidades);
            
            assertEquals(1, result.size());
            verify(processoInicializador).iniciar(codigo, unidades, usuario);
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
        @DisplayName("enviarLembrete deve formatar data corretamente quando presente")
        void enviarLembrete_DeveFormatarDataQuandoPresente() {
            Long codProcesso = 1L;
            Long codUnidade = 2L;
            LocalDateTime dataLimite = LocalDateTime.of(2026, 3, 15, 23, 59);

            Processo processo = new Processo();
            processo.setDescricao("Processo com prazo");
            processo.setDataLimite(dataLimite);
            Unidade unidade = UnidadeFixture.unidadeComId(codUnidade);
            unidade.setSigla("U1");
            processo.adicionarParticipantes(Set.of(unidade));

            when(processoConsultaService.buscarProcessoCodigo(codProcesso)).thenReturn(processo);
            when(unidadeService.unidadePorCodigo(codUnidade)).thenReturn(unidade);
            Subprocesso subprocesso = Subprocesso.builder().codigo(99L).build();
            when(subprocessoService.obterEntidadePorProcessoEUnidade(codProcesso, codUnidade)).thenReturn(subprocesso);
            unidade.setTituloTitular("T1");
            Usuario titular = new Usuario();
            titular.setEmail("titular@teste.com");
            when(usuarioService.buscarPorLogin("T1")).thenReturn(titular);
            
            when(emailModelosService.criarEmailLembretePrazo(anyString(), anyString(), any())).thenReturn("HTML");

            processoFacade.enviarLembrete(codProcesso, codUnidade);

            verify(alertaService).criarAlertaAdmin(eq(processo), eq(unidade), contains("15/03/2026"));
            verify(subprocessoService).registrarMovimentacaoLembrete(99L);
            verify(emailService).enviarEmailHtml(
                eq("titular@teste.com"), 
                contains("SGC: Lembrete de prazo"), 
                anyString()
            );
        }

        @Test
        @DisplayName("enviarLembrete deve lançar exceção quando unidade não participa")
        void enviarLembrete_DeveLancarExcecaoQuandoUnidadeNaoParticipa() {
            Long codProcesso = 1L;
            Long codUnidade = 99L; // Unidade que não participa
            
            Processo processo = new Processo();
            processo.setDescricao("Processo");
            Unidade unidadeParticipante = UnidadeFixture.unidadeComId(2L);
            processo.adicionarParticipantes(Set.of(unidadeParticipante));
            
            when(processoConsultaService.buscarProcessoCodigo(codProcesso)).thenReturn(processo);
            
            assertThatThrownBy(() -> processoFacade.enviarLembrete(codProcesso, codUnidade))
                    .isInstanceOf(ErroProcesso.class)
                    .hasMessageContaining("não participa");
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

            verify(subprocessoService, never()).aceitarCadastroEmBloco(any(), any());
            verify(subprocessoService, never()).homologarCadastroEmBloco(any(), any());
        }
    }

    @Nested
    @DisplayName("Segurança e Controle de Acesso")
    class SecurityTests {
        @Test
        @DisplayName("Deve negar acesso quando usuário não autenticado")
        void deveNegarAcessoQuandoNaoAutenticado() {
            // Arrange
            Authentication auth = mock(Authentication.class);
            when(processoAcessoService.checarAcesso(auth, 1L)).thenReturn(false);
            when(processoAcessoService.checarAcesso(null, 1L)).thenReturn(false);

            // Act & Assert
            assertThat(processoFacade.checarAcesso(auth, 1L)).isFalse();
            assertThat(processoFacade.checarAcesso(null, 1L)).isFalse();
        }

        @ParameterizedTest
        @ValueSource(longs = {1L, 2L})
        @DisplayName("Deve delegar verificação de acesso para o ProcessoAcessoService")
        void deveDelegarVerificacaoDeAcesso(Long processoCodigo) {
            Authentication auth = mock(Authentication.class);
            when(processoAcessoService.checarAcesso(auth, processoCodigo)).thenReturn(true);
            assertThat(processoFacade.checarAcesso(auth, processoCodigo)).isTrue();

            when(processoAcessoService.checarAcesso(auth, processoCodigo)).thenReturn(false);
            assertThat(processoFacade.checarAcesso(auth, processoCodigo)).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando checarAcesso recebe null")
        void deveRetornarFalseQuandoAuthenticationForNull() {
            when(processoAcessoService.checarAcesso(null, 1L)).thenReturn(false);
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
            when(processoInicializador.iniciar(id, List.of(1L), usuario)).thenReturn(List.of());

            List<String> erros = processoFacade.iniciarProcessoMapeamento(id, List.of(1L));

            assertThat(erros).isEmpty();
            verify(processoInicializador).iniciar(id, List.of(1L), usuario);
        }

        @Test
        @DisplayName("Deve retornar erro ao iniciar mapeamento se unidade já em uso")
        void deveRetornarErroAoIniciarMapeamentoSeUnidadeEmUso() {
            Long id = 100L;
            Usuario usuario = new Usuario();
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
            String mensagemErro = "As seguintes unidades já participam de outro processo ativo: U1";
            when(processoInicializador.iniciar(id, List.of(1L), usuario)).thenReturn(List.of(mensagemErro));

            List<String> erros = processoFacade.iniciarProcessoMapeamento(id, List.of(1L));

            assertThat(erros).contains(mensagemErro);
        }

        @Test
        @DisplayName("Deve iniciar revisão com sucesso delegando para ProcessoInicializador")
        void deveIniciarRevisaoComSucesso() {
            Long id = 100L;
            Usuario usuario = new Usuario();
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
            when(processoInicializador.iniciar(id, List.of(1L), usuario)).thenReturn(List.of());

            List<String> erros = processoFacade.iniciarProcessoRevisao(id, List.of(1L));

            assertThat(erros).isEmpty();
            verify(processoInicializador).iniciar(id, List.of(1L), usuario);
        }

        @Test
        @DisplayName("Deve finalizar processo com sucesso quando tudo homologado")
        void deveFinalizarProcessoQuandoTudoHomologado() {
            Long id = 100L;
            processoFacade.finalizar(id);
            verify(processoFinalizador).finalizar(id);
        }
    }

    @Nested
    @DisplayName("Lembretes")
    class Lembretes {
        @Test
        @DisplayName("Deve enviar lembrete com sucesso")
        void deveEnviarLembrete() {
            Processo p = ProcessoFixture.processoEmAndamento();
            p.setCodigo(1L);
            Unidade u = UnidadeFixture.unidadeComId(10L);
            u.setSigla("U1");
            p.adicionarParticipantes(Set.of(u));

            when(processoConsultaService.buscarProcessoCodigo(1L)).thenReturn(p);
            when(unidadeService.unidadePorCodigo(10L)).thenReturn(u);
            Subprocesso subprocesso = Subprocesso.builder().codigo(99L).build();
            when(subprocessoService.obterEntidadePorProcessoEUnidade(1L, 10L)).thenReturn(subprocesso);
            u.setTituloTitular("T10");
            Usuario titular = new Usuario();
            titular.setEmail("u10@teste.com");
            when(usuarioService.buscarPorLogin("T10")).thenReturn(titular);
            when(emailModelosService.criarEmailLembretePrazo(anyString(), anyString(), any())).thenReturn("HTML");

            processoFacade.enviarLembrete(1L, 10L);
            verify(alertaService).criarAlertaAdmin(eq(p), eq(u), anyString());
            verify(subprocessoService).registrarMovimentacaoLembrete(99L);
            verify(emailService).enviarEmailHtml(eq("u10@teste.com"), contains(p.getDescricao()), anyString());
        }

        @Test
        @DisplayName("Deve falhar ao enviar lembrete se unidade nao participa")
        void deveFalharEnviarLembreteUnidadeNaoParticipa() {
            Processo p = ProcessoFixture.processoEmAndamento();
            p.setCodigo(1L);
            Unidade u = UnidadeFixture.unidadeComId(10L);
            Unidade outra = UnidadeFixture.unidadeComId(20L);
            p.adicionarParticipantes(Set.of(outra));

            when(processoConsultaService.buscarProcessoCodigo(1L)).thenReturn(p);
            when(unidadeService.unidadePorCodigo(10L)).thenReturn(u);

            assertThatThrownBy(() -> processoFacade.enviarLembrete(1L, 10L))
                    .isInstanceOf(ErroProcesso.class)
                    .hasMessageContaining("não participa");
        }

    }

    @Nested
    @DisplayName("Criação de Processo")
    class Criacao {
        @Test
        @DisplayName("Deve criar processo quando dados válidos")
        void deveCriarProcessoQuandoDadosValidos() {
            // Arrange
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
            // Act
            Processo resultado = processoFacade.criar(req);

            // Assert
            assertThat(resultado).isNotNull();
            verify(processoManutencaoService).criar(req);
        }

        @Test
        @DisplayName("Deve lançar exceção quando unidade não encontrada (propagada do serviço)")
        void deveLancarExcecaoQuandoUnidadeNaoEncontrada() {
            // Arrange
            CriarProcessoRequest req = new CriarProcessoRequest(
                    "Teste", TipoProcesso.MAPEAMENTO, LocalDateTime.now(), List.of(99L));
            
            when(processoManutencaoService.criar(req))
                    .thenThrow(new ErroEntidadeNaoEncontrada("Unidade", 99L));

            // Act & Assert
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
            // Arrange
            Usuario usuario = criarUsuarioMock();
            Long id = 100L;
            Processo processo = ProcessoFixture.processoPadrao();
            when(processoConsultaService.buscarProcessoCodigo(id)).thenReturn(processo);
            when(processoDetalheBuilder.build(eq(processo), any(Usuario.class))).thenReturn(ProcessoDetalheDto.builder().build());

            // Act
            var res = processoFacade.obterDetalhes(id, usuario);

            // Assert
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
            // Arrange
            when(processoConsultaService.processosFinalizados())
                    .thenReturn(List.of(ProcessoFixture.processoPadrao()));
            when(processoConsultaService.processosAndamento())
                    .thenReturn(List.of(ProcessoFixture.processoPadrao()));
            // Act & Assert
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
            // Arrange
            when(processoConsultaService.unidadesBloqueadasPorTipo(TipoProcesso.MAPEAMENTO))
                    .thenReturn(List.of(1L));

            // Act
            List<Long> bloqueadas = processoFacade.listarUnidadesBloqueadasPorTipo("MAPEAMENTO");

            // Assert
            assertThat(bloqueadas).contains(1L);
        }

        @Test
        @DisplayName("Deve retornar contexto completo do processo")
        void deveRetornarContextoCompleto() {
            // Arrange
            Usuario usuario = criarUsuarioMock();
            Long id = 100L;
            Processo processo = ProcessoFixture.processoPadrao();
            ProcessoDetalheDto detalhes = ProcessoDetalheDto.builder().build();

            when(processoConsultaService.buscarProcessoCodigo(id)).thenReturn(processo);
            when(processoDetalheBuilder.build(eq(processo), any(Usuario.class))).thenReturn(detalhes);
            when(processoConsultaService.subprocessosElegiveis(id))
                    .thenReturn(List.of());

            // Act
            var res = processoFacade.obterContextoCompleto(id, usuario);

            // Assert
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
        @DisplayName("Deve listar todos os subprocessos delegando para service")
        void deveListarTodosSubprocessosDelegandoParaService() {
            Long codProcesso = 1L;
            List<Subprocesso> lista = List.of(Subprocesso.builder().build());
            when(subprocessoService.listarEntidadesPorProcesso(codProcesso)).thenReturn(lista);

            var res = processoFacade.listarTodosSubprocessos(codProcesso);
            assertThat(res).isSameAs(lista);
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
                // Arrange
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

                // Act
                processoFacade.executarAcaoEmBloco(100L, req);

                // Assert
                ArgumentCaptor<DisponibilizarMapaRequest> captor = 
                    ArgumentCaptor.forClass(DisponibilizarMapaRequest.class);
                verify(subprocessoService).disponibilizarMapaEmBloco(
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
                // Arrange
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

                // Act
                processoFacade.executarAcaoEmBloco(100L, req);

                // Assert
                verify(subprocessoService).aceitarCadastroEmBloco(List.of(1L, 2L), usuario);
            }

            @Test
            @DisplayName("Deve aceitar validação quando subprocessos estão em situação de mapa disponibilizado")
            void deveAceitarValidacaoQuandoMapaDisponibilizado() {
                // Arrange
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

                // Act
                processoFacade.executarAcaoEmBloco(100L, req);

                // Assert
                verify(subprocessoService).aceitarValidacaoEmBloco(List.of(1L), usuario);
            }
        }

        @Nested
        @DisplayName("Executar Ação em Bloco - HOMOLOGAR")
        class AcaoHomologar {
            @Test
            @DisplayName("Deve homologar cadastro quando subprocessos estão em situação de cadastro")
            void deveHomologarCadastroQuandoCadastro() {
                // Arrange
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

                // Act
                processoFacade.executarAcaoEmBloco(100L, req);

                // Assert
                verify(subprocessoService).homologarCadastroEmBloco(List.of(1L), usuario);
            }

            @Test
            @DisplayName("Deve homologar validação quando subprocessos estão em validação")
            void deveHomologarValidacaoQuandoValidacao() {
                // Arrange
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

                // Act
                processoFacade.executarAcaoEmBloco(100L, req);

                // Assert
                verify(subprocessoService).homologarValidacaoEmBloco(List.of(1L), usuario);
            }
        }
    }
}
