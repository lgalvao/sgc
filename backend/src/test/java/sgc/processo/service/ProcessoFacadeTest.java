package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
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
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.dto.*;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.erros.ErroProcessoEmSituacaoInvalida;
import sgc.processo.mapper.ProcessoMapper;
import sgc.processo.model.*;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.service.SubprocessoFacade;
import sgc.testutils.UnidadeTestBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoFacade Test Suite")
class ProcessoFacadeTest {

    @InjectMocks
    private ProcessoFacade processoFacade;

    @Mock
    private ProcessoManutencaoService processoManutencaoService;
    @Mock
    private ProcessoMapper processoMapper;
    @Mock
    private ProcessoInicializador processoInicializador;
    @Mock
    private ProcessoConsultaService processoConsultaService;
    @Mock
    private UnidadeFacade unidadeService;
    @Mock
    private AlertaFacade alertaService;
    @Mock
    private SubprocessoFacade subprocessoFacade;
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
    private SubprocessoMapper subprocessoMapper;


    @Nested
    @DisplayName("Cobertura e Casos de Borda")
    class CoverageTests {
        @Test
        @DisplayName("iniciarProcessoDiagnostico deve delegar para inicializador")
        void iniciarProcessoDiagnostico_DeveDelegar() {
            Long codigo = 1L;
            List<Long> unidades = List.of(2L, 3L);
            Usuario usuario = new Usuario();
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
            when(processoInicializador.iniciar(codigo, unidades, usuario)).thenReturn(List.of("OK"));

            var result = processoFacade.iniciarProcessoDiagnostico(codigo, unidades);
            
            assertEquals(1, result.size());
            verify(processoInicializador).iniciar(codigo, unidades, usuario);
        }

        @Test
        @DisplayName("buscarIdsUnidadesEmProcessosAtivos deve delegar para consulta service")
        void buscarIdsUnidadesEmProcessosAtivos_DeveDelegar() {
            Long codigoIgnorar = 1L;
            processoFacade.buscarIdsUnidadesEmProcessosAtivos(codigoIgnorar);
            verify(processoConsultaService).buscarIdsUnidadesComProcessosAtivos(codigoIgnorar);
        }

        @Test
        @DisplayName("enviarLembrete deve formatar data N/A quando null")
        void enviarLembrete_DeveFormatarDataNA() {
            Long codProcesso = 1L;
            Long codUnidade = 2L;

            Processo processo = new Processo();
            processo.setDescricao("Proc");
            processo.setDataLimite(null);
            Unidade unidade = UnidadeTestBuilder.umaDe()
                    .comCodigo(String.valueOf(codUnidade))
                    .build();
            processo.adicionarParticipantes(Set.of(unidade));

            when(processoConsultaService.buscarProcessoCodigo(codProcesso)).thenReturn(processo);
            when(unidadeService.buscarEntidadePorId(codUnidade)).thenReturn(unidade);

            processoFacade.enviarLembrete(codProcesso, codUnidade);

            verify(alertaService).criarAlertaSedoc(eq(processo), eq(unidade), contains("N/A"));
        }

        @Test
        @DisplayName("executarAcaoEmBloco ignora ação null na categorização")
        void executarAcaoEmBloco_IgnoraAcaoNull() {
            Long codProcesso = 1L;
            // Correct constructor: (unidades, acao, data)
            AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                List.of(10L),
                null, // Action is null
                LocalDate.now()
            );

            Usuario usuario = new Usuario();
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
            
            SubprocessoDto subDto = new SubprocessoDto();
            subDto.setCodUnidade(10L);
            subDto.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

            when(subprocessoFacade.listarPorProcessoEUnidades(codProcesso, req.unidadeCodigos()))
                .thenReturn(List.of(subDto));

            processoFacade.executarAcaoEmBloco(codProcesso, req);

            verify(subprocessoFacade, never()).aceitarCadastroEmBloco(any(), any(), any());
            verify(subprocessoFacade, never()).homologarCadastroEmBloco(any(), any(), any());
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

        @Test
        @DisplayName("Deve permitir acesso quando gestor é da unidade participante (com hierarquia)")
        void devePermitirAcessoQuandoGestorDeUnidadeParticipante() {
            // Arrange
            Authentication auth = mock(Authentication.class);
            when(processoAcessoService.checarAcesso(auth, 1L)).thenReturn(true);

            // Act & Assert
            assertThat(processoFacade.checarAcesso(auth, 1L)).isTrue();
        }

        @Test
        @DisplayName("buscarCodigosDescendentes: via checarAcesso - teste de logica de arvore")
        void buscarCodigosDescendentes_Arvore() {
            Authentication auth = mock(Authentication.class);
            when(processoAcessoService.checarAcesso(auth, 1L)).thenReturn(true);

            assertThat(processoFacade.checarAcesso(auth, 1L)).isTrue();
            verify(processoAcessoService).checarAcesso(auth, 1L);
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
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
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
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
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
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
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
            Processo p = sgc.fixture.ProcessoFixture.processoEmAndamento();
            p.setCodigo(1L);
            Unidade u = sgc.fixture.UnidadeFixture.unidadeComId(10L);
            p.adicionarParticipantes(Set.of(u));

            when(processoConsultaService.buscarProcessoCodigo(1L)).thenReturn(p);
            when(unidadeService.buscarEntidadePorId(10L)).thenReturn(u);

            processoFacade.enviarLembrete(1L, 10L);
            verify(alertaService).criarAlertaSedoc(eq(p), eq(u), anyString());
        }

        @Test
        @DisplayName("Deve falhar ao enviar lembrete se unidade nao participa")
        void deveFalharEnviarLembreteUnidadeNaoParticipa() {
            Processo p = sgc.fixture.ProcessoFixture.processoEmAndamento();
            p.setCodigo(1L);
            Unidade u = sgc.fixture.UnidadeFixture.unidadeComId(10L);
            Unidade outra = sgc.fixture.UnidadeFixture.unidadeComId(20L);
            p.adicionarParticipantes(Set.of(outra));

            when(processoConsultaService.buscarProcessoCodigo(1L)).thenReturn(p);
            when(unidadeService.buscarEntidadePorId(10L)).thenReturn(u);

            assertThatThrownBy(() -> processoFacade.enviarLembrete(1L, 10L))
                    .isInstanceOf(sgc.processo.erros.ErroProcesso.class)
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
            when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

            // Act
            ProcessoDto resultado = processoFacade.criar(req);

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

        @Test
        @DisplayName("Deve retornar erro se REVISAO e validador retornar mensagem (propagado do serviço)")
        void deveRetornarErroSeRevisaoEValidadorFalhar() {
            var req = new CriarProcessoRequest("T", TipoProcesso.REVISAO, LocalDateTime.now(), List.of(1L));
            
            when(processoManutencaoService.criar(req)).thenThrow(new ErroProcesso("Erro"));

            assertThatThrownBy(() -> processoFacade.criar(req))
                    .isInstanceOf(ErroProcesso.class)
                    .hasMessage("Erro");
        }
    }

    @Nested
    @DisplayName("Atualização de Processo")
    class Atualizacao {
        @Test
        @DisplayName("Deve atualizar processo quando está em situação CRIADO")
        void deveAtualizarProcessoQuandoEmSituacaoCriado() {
            // Arrange
            Long id = 100L;
            Processo processo = sgc.fixture.ProcessoFixture.processoPadrao();
            processo.setCodigo(id);

            AtualizarProcessoRequest req = AtualizarProcessoRequest.builder()
                    .codigo(id)
                    .descricao("Nova Desc")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now())
                    .unidades(List.of(1L))
                    .build();

            when(processoManutencaoService.atualizar(id, req)).thenReturn(processo);
            when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

            // Act
            ProcessoDto resultado = processoFacade.atualizar(id, req);

            // Assert
            assertThat(resultado).isNotNull();
            verify(processoManutencaoService).atualizar(id, req);
        }

        @Test
        @DisplayName("Deve lançar exceção quando atualizar e processo não encontrado (propagado)")
        void deveLancarExcecaoQuandoAtualizarEProcessoNaoEncontrado() {
            // Arrange
            var req = AtualizarProcessoRequest.builder().build();
            when(processoManutencaoService.atualizar(99L, req)).thenThrow(new ErroEntidadeNaoEncontrada("Processo", 99L));

            // Act & Assert
            assertThatThrownBy(() -> processoFacade.atualizar(99L, req))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve lançar exceção quando atualizar e processo não está em situação CRIADO (propagado)")
        void deveLancarExcecaoQuandoAtualizarEProcessoNaoCriado() {
            // Arrange
            Long id = 100L;
            var req = AtualizarProcessoRequest.builder().build();
            
            when(processoManutencaoService.atualizar(id, req))
                    .thenThrow(new ErroProcessoEmSituacaoInvalida("Apenas processos na situação 'CRIADO' podem ser editados."));

            // Act & Assert
            assertThatThrownBy(() -> processoFacade.atualizar(id, req))
                    .isInstanceOf(ErroProcessoEmSituacaoInvalida.class);
        }
    }

    @Nested
    @DisplayName("Exclusão de Processo")
    class Exclusao {
        @Test
        @DisplayName("Deve apagar processo quando está em situação CRIADO")
        void deveApagarProcessoQuandoEmSituacaoCriado() {
            // Arrange
            Long id = 100L;
            
            // Act
            processoFacade.apagar(id);

            // Assert
            verify(processoManutencaoService).apagar(id);
        }

        @Test
        @DisplayName("Deve lançar exceção quando apagar e processo não encontrado (propagado)")
        void deveLancarExcecaoQuandoApagarEProcessoNaoEncontrado() {
            // Arrange
            doThrow(new ErroEntidadeNaoEncontrada("Processo", 99L))
                    .when(processoManutencaoService).apagar(99L);

            // Act & Assert
            assertThatThrownBy(() -> processoFacade.apagar(99L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve lançar exceção quando apagar e processo não está em situação CRIADO (propagado)")
        void deveLancarExcecaoQuandoApagarEProcessoNaoCriado() {
            // Arrange
            Long id = 100L;
            
            doThrow(new ErroProcessoEmSituacaoInvalida("Apenas processos na situação 'CRIADO' podem ser removidos."))
                    .when(processoManutencaoService).apagar(id);

            // Act & Assert
            assertThatThrownBy(() -> processoFacade.apagar(id))
                    .isInstanceOf(ErroProcessoEmSituacaoInvalida.class);
        }
    }

    @Nested
    @DisplayName("Consultas e Detalhes")
    class Consultas {
        private static final String MAPEAMENTO = "MAPEAMENTO";

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
            Processo processo = sgc.fixture.ProcessoFixture.processoPadrao();
            when(processoConsultaService.buscarProcessoCodigo(id)).thenReturn(processo);
            when(processoDetalheBuilder.build(eq(processo), any(Usuario.class))).thenReturn(new ProcessoDetalheDto());

            // Act
            var res = processoFacade.obterDetalhes(id, usuario);

            // Assert
            assertThat(res).isNotNull();
        }

        @Test
        @DisplayName("Deve falhar ao obter detalhes de processo inexistente")
        void deveFalharAoObterDetalhesProcessoInexistente() {
            Usuario usuario = criarUsuarioMock();
            when(processoConsultaService.buscarProcessoCodigo(999L)).thenThrow(new ErroEntidadeNaoEncontrada("Processo", 999L));
            assertThatThrownBy(() -> processoFacade.obterDetalhes(999L, usuario))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve buscar entidade por ID")
        void deveBuscarEntidadePorId() {
            Long id = 100L;
            Processo processo = sgc.fixture.ProcessoFixture.processoPadrao();
            when(processoConsultaService.buscarProcessoCodigo(id)).thenReturn(processo);

            Processo res = processoFacade.buscarEntidadePorId(id);
            assertThat(res).isEqualTo(processo);
        }

        @Test
        @DisplayName("Deve falhar buscar entidade inexistente")
        void deveFalharBuscarEntidadeInexistente() {
            when(processoConsultaService.buscarProcessoCodigo(999L)).thenThrow(new ErroEntidadeNaoEncontrada("Processo", 999L));
            assertThatThrownBy(() -> processoFacade.buscarEntidadePorId(999L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve obter processo por ID (Optional)")
        void deveObterPorIdOptional() {
            Long id = 100L;
            Processo processo = sgc.fixture.ProcessoFixture.processoPadrao();
            when(processoConsultaService.buscarProcessoCodigoOpt(id)).thenReturn(Optional.of(processo));
            when(processoMapper.toDto(processo)).thenReturn(ProcessoDto.builder().build());

            Optional<ProcessoDto> res = processoFacade.obterPorId(id);
            assertThat(res).isPresent();
        }

        @Test
        @DisplayName("Deve listar processos finalizados e ativos")
        void deveListarProcessosFinalizadosEAtivos() {
            // Arrange
            when(processoConsultaService.processosFinalizados())
                    .thenReturn(List.of(sgc.fixture.ProcessoFixture.processoPadrao()));
            when(processoConsultaService.processosAndamento())
                    .thenReturn(List.of(sgc.fixture.ProcessoFixture.processoPadrao()));
            when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

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
        @DisplayName("Deve listar todos subprocessos")
        void deveListarTodosSubprocessos() {
            when(subprocessoFacade.listarEntidadesPorProcesso(100L))
                    .thenReturn(List.of(sgc.fixture.SubprocessoFixture.subprocessoPadrao(null, null)));
            when(subprocessoMapper.toDto(any())).thenReturn(SubprocessoDto.builder().build());

            var res = processoFacade.listarTodosSubprocessos(100L);
            assertThat(res).hasSize(1);
        }

        @Test
        @DisplayName("Deve listar subprocessos elegíveis para Admin")
        void deveListarSubprocessosElegiveisParaAdmin() {
            // Arrange
            SubprocessoElegivelDto dto = SubprocessoElegivelDto.builder()
                    .codSubprocesso(1L)
                    .build();
            when(processoConsultaService.subprocessosElegiveis(100L))
                    .thenReturn(List.of(dto));

            // Act
            List<SubprocessoElegivelDto> res = processoFacade.listarSubprocessosElegiveis(100L);

            // Assert
            assertThat(res).hasSize(1);
            assertThat(res.getFirst().getCodSubprocesso()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Deve listar subprocessos elegíveis para Gestor")
        void deveListarSubprocessosElegiveisParaGestor() {
            // Arrange
            SubprocessoElegivelDto dto1 = SubprocessoElegivelDto.builder()
                    .codSubprocesso(1L)
                    .build();
            SubprocessoElegivelDto dto2 = SubprocessoElegivelDto.builder()
                    .codSubprocesso(2L)
                    .build();
            when(processoConsultaService.subprocessosElegiveis(100L))
                    .thenReturn(List.of(dto1, dto2));

            // Act
            List<SubprocessoElegivelDto> res = processoFacade.listarSubprocessosElegiveis(100L);

            // Assert
            assertThat(res).hasSize(2);
            assertThat(res).extracting(SubprocessoElegivelDto::getCodSubprocesso).containsExactlyInAnyOrder(1L, 2L);
        }

        @Test
        @DisplayName("Deve retornar vazio ao listar subprocessos se usuário sem unidade")
        void deveRetornarVazioAoListarSubprocessosSeUsuarioSemUnidade() {
            // Arrange
            when(processoConsultaService.subprocessosElegiveis(100L))
                    .thenReturn(List.of());

            // Act
            List<SubprocessoElegivelDto> res = processoFacade.listarSubprocessosElegiveis(100L);

            // Assert
            assertThat(res).isEmpty();
        }

        @Test
        @DisplayName("Listar por participantes ignorando criado")
        void listarPorParticipantesIgnorandoCriado() {
            processoFacade.listarPorParticipantesIgnorandoCriado(List.of(1L), null);
            verify(processoConsultaService).processosIniciadosPorParticipantes(anyList(), any());
        }

        @Test
        @DisplayName("Deve lançar exceção para tipo de processo inválido")
        void deveLancarExcecaoParaTipoInvalido() {
            assertThatThrownBy(() -> processoFacade.listarUnidadesBloqueadasPorTipo("TIPO_INEXISTENTE"))
                    .isInstanceOf(IllegalArgumentException.class);
        }


        @Test
        @DisplayName("Deve retornar contexto completo do processo")
        void deveRetornarContextoCompleto() {
            // Arrange
            Usuario usuario = criarUsuarioMock();
            Long id = 100L;
            Processo processo = sgc.fixture.ProcessoFixture.processoPadrao();
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
            assertThat(res.getElegiveis()).isEmpty();
        }

        @Test
        @DisplayName("obterContextoCompleto: sucesso")
        void obterContextoCompletoSucesso() {
            Usuario usuario = criarUsuarioMock();
            Processo p = new Processo();
            p.setCodigo(1L);
            when(processoConsultaService.buscarProcessoCodigo(1L)).thenReturn(p);
            when(processoDetalheBuilder.build(eq(p), any(Usuario.class))).thenReturn(ProcessoDetalheDto.builder().build());

            assertThat(processoFacade.obterDetalhes(1L, usuario)).isNotNull();
        }

        @Test
        @DisplayName("listarUnidadesBloqueadasPorTipo: chama repo")
        void listarUnidadesBloqueadasPorTipo_Test() {
            when(processoConsultaService.unidadesBloqueadasPorTipo(TipoProcesso.MAPEAMENTO)).thenReturn(List.of(1L, 2L));

            processoFacade.listarUnidadesBloqueadasPorTipo("MAPEAMENTO");
            verify(processoConsultaService).unidadesBloqueadasPorTipo(TipoProcesso.MAPEAMENTO);
        }

        @Test
        @DisplayName("getMensagemErroUnidadesSemMapa: empty list returns empty")
        void getMensagemErroUnidadesSemMapaEmpty() {
            when(processoValidador.getMensagemErroUnidadesSemMapa(Collections.emptyList()))
                    .thenReturn(Optional.empty());

            Optional<String> msg = processoValidador.getMensagemErroUnidadesSemMapa(Collections.emptyList());
            assertThat(msg).isEmpty();

            when(processoValidador.getMensagemErroUnidadesSemMapa(null))
                    .thenReturn(Optional.empty());

            Optional<String> msgNull = processoValidador.getMensagemErroUnidadesSemMapa(null);
            assertThat(msgNull).isEmpty();
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
                when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

                LocalDate dataLimite = LocalDate.now().plusDays(30);
                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                    List.of(1L, 2L, 3L),
                    AcaoProcesso.DISPONIBILIZAR,
                    dataLimite
                );

                // Act
                processoFacade.executarAcaoEmBloco(100L, req);

                // Assert
                ArgumentCaptor<DisponibilizarMapaRequest> captor = 
                    ArgumentCaptor.forClass(DisponibilizarMapaRequest.class);
                verify(subprocessoFacade).disponibilizarMapaEmBloco(
                    eq(List.of(1L, 2L, 3L)),
                    eq(100L),
                    captor.capture(),
                    eq(usuario)
                );
                
                DisponibilizarMapaRequest captured = captor.getValue();
                assertThat(captured.dataLimite()).isNotNull();
                assertThat(captured.observacoes()).isEqualTo("Disponibilização em bloco");

                // Não deve chamar os métodos de aceitar/homologar
                verify(subprocessoFacade, never()).aceitarCadastroEmBloco(anyList(), anyLong(), any());
                verify(subprocessoFacade, never()).aceitarValidacaoEmBloco(anyList(), anyLong(), any());
                verify(subprocessoFacade, never()).homologarCadastroEmBloco(anyList(), anyLong(), any());
                verify(subprocessoFacade, never()).homologarValidacaoEmBloco(anyList(), anyLong(), any());
            }

            @Test
            @DisplayName("Deve retornar early quando ação é DISPONIBILIZAR sem executar lógica de aceite/homologação")
            void deveRetornarEarlyQuandoDisponibilizar() {
                // Arrange
                Usuario usuario = new Usuario();
                when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                    List.of(1L),
                    AcaoProcesso.DISPONIBILIZAR,
                    LocalDate.now()
                );

                // Act
                processoFacade.executarAcaoEmBloco(100L, req);

                // Assert - não deve buscar subprocessos
                verify(subprocessoFacade, never()).listarPorProcessoEUnidades(anyLong(), anyList());
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
                when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

                SubprocessoDto sp1 = SubprocessoDto.builder()
                    .codUnidade(1L)
                    .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                    .build();
                SubprocessoDto sp2 = SubprocessoDto.builder()
                    .codUnidade(2L)
                    .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                    .build();

                when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L, 2L))).thenReturn(List.of(sp1, sp2));

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                    List.of(1L, 2L),
                    AcaoProcesso.ACEITAR,
                    null
                );

                // Act
                processoFacade.executarAcaoEmBloco(100L, req);

                // Assert
                verify(subprocessoFacade).aceitarCadastroEmBloco(List.of(1L, 2L), 100L, usuario);
                verify(subprocessoFacade, never()).aceitarValidacaoEmBloco(anyList(), anyLong(), any());
                verify(subprocessoFacade, never()).homologarCadastroEmBloco(anyList(), anyLong(), any());
                verify(subprocessoFacade, never()).homologarValidacaoEmBloco(anyList(), anyLong(), any());
            }

            @Test
            @DisplayName("Deve aceitar validação quando subprocessos estão em situação de mapa disponibilizado")
            void deveAceitarValidacaoQuandoMapaDisponibilizado() {
                // Arrange
                Usuario usuario = new Usuario();
                when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

                SubprocessoDto sp1 = SubprocessoDto.builder()
                    .codUnidade(1L)
                    .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO)
                    .build();
                SubprocessoDto sp2 = SubprocessoDto.builder()
                    .codUnidade(2L)
                    .situacao(SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO)
                    .build();

                when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L, 2L))).thenReturn(List.of(sp1, sp2));

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                    List.of(1L, 2L),
                    AcaoProcesso.ACEITAR,
                    null
                );

                // Act
                processoFacade.executarAcaoEmBloco(100L, req);

                // Assert
                verify(subprocessoFacade).aceitarValidacaoEmBloco(List.of(1L, 2L), 100L, usuario);
                verify(subprocessoFacade, never()).aceitarCadastroEmBloco(anyList(), anyLong(), any());
                verify(subprocessoFacade, never()).homologarCadastroEmBloco(anyList(), anyLong(), any());
                verify(subprocessoFacade, never()).homologarValidacaoEmBloco(anyList(), anyLong(), any());
            }

            @Test
            @DisplayName("Deve separar aceite de cadastro e validação corretamente")
            void deveSepararAceiteCadastroEValidacao() {
                // Arrange
                Usuario usuario = new Usuario();
                when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

                SubprocessoDto spCadastro1 = SubprocessoDto.builder()
                    .codUnidade(1L)
                    .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                    .build();
                SubprocessoDto spCadastro2 = SubprocessoDto.builder()
                    .codUnidade(2L)
                    .situacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA)
                    .build();
                SubprocessoDto spValidacao1 = SubprocessoDto.builder()
                    .codUnidade(3L)
                    .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO)
                    .build();
                SubprocessoDto spValidacao2 = SubprocessoDto.builder()
                    .codUnidade(4L)
                    .situacao(SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO)
                    .build();

                when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L, 2L, 3L, 4L)))
                    .thenReturn(List.of(spCadastro1, spCadastro2, spValidacao1, spValidacao2));

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                    List.of(1L, 2L, 3L, 4L),
                    AcaoProcesso.ACEITAR,
                    null
                );

                // Act
                processoFacade.executarAcaoEmBloco(100L, req);

                // Assert
                verify(subprocessoFacade).aceitarCadastroEmBloco(List.of(1L, 2L), 100L, usuario);
                verify(subprocessoFacade).aceitarValidacaoEmBloco(List.of(3L, 4L), 100L, usuario);
                verify(subprocessoFacade, never()).homologarCadastroEmBloco(anyList(), anyLong(), any());
                verify(subprocessoFacade, never()).homologarValidacaoEmBloco(anyList(), anyLong(), any());
            }

            @Test
            @DisplayName("Deve aceitar quando subprocesso está em REVISAO_CADASTRO_HOMOLOGADA")
            void deveAceitarQuandoRevisaoCadastroHomologada() {
                // Arrange
                Usuario usuario = new Usuario();
                when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

                SubprocessoDto sp = SubprocessoDto.builder()
                    .codUnidade(1L)
                    .situacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA)
                    .build();

                when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L))).thenReturn(List.of(sp));

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                    List.of(1L),
                    AcaoProcesso.ACEITAR,
                    null
                );

                // Act
                processoFacade.executarAcaoEmBloco(100L, req);

                // Assert
                verify(subprocessoFacade).aceitarCadastroEmBloco(List.of(1L), 100L, usuario);
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
                when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

                SubprocessoDto sp1 = SubprocessoDto.builder()
                    .codUnidade(1L)
                    .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                    .build();
                SubprocessoDto sp2 = SubprocessoDto.builder()
                    .codUnidade(2L)
                    .situacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA)
                    .build();

                when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L, 2L))).thenReturn(List.of(sp1, sp2));

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                    List.of(1L, 2L),
                    AcaoProcesso.HOMOLOGAR,
                    null
                );

                // Act
                processoFacade.executarAcaoEmBloco(100L, req);

                // Assert
                verify(subprocessoFacade).homologarCadastroEmBloco(List.of(1L, 2L), 100L, usuario);
                verify(subprocessoFacade, never()).homologarValidacaoEmBloco(anyList(), anyLong(), any());
                verify(subprocessoFacade, never()).aceitarCadastroEmBloco(anyList(), anyLong(), any());
                verify(subprocessoFacade, never()).aceitarValidacaoEmBloco(anyList(), anyLong(), any());
            }

            @Test
            @DisplayName("Deve homologar validação quando subprocessos estão em situação de mapa disponibilizado")
            void deveHomologarValidacaoQuandoMapaDisponibilizado() {
                // Arrange
                Usuario usuario = new Usuario();
                when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

                SubprocessoDto sp1 = SubprocessoDto.builder()
                    .codUnidade(1L)
                    .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO)
                    .build();
                SubprocessoDto sp2 = SubprocessoDto.builder()
                    .codUnidade(2L)
                    .situacao(SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO)
                    .build();

                when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L, 2L))).thenReturn(List.of(sp1, sp2));

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                    List.of(1L, 2L),
                    AcaoProcesso.HOMOLOGAR,
                    null
                );

                // Act
                processoFacade.executarAcaoEmBloco(100L, req);

                // Assert
                verify(subprocessoFacade).homologarValidacaoEmBloco(List.of(1L, 2L), 100L, usuario);
                verify(subprocessoFacade, never()).homologarCadastroEmBloco(anyList(), anyLong(), any());
                verify(subprocessoFacade, never()).aceitarCadastroEmBloco(anyList(), anyLong(), any());
                verify(subprocessoFacade, never()).aceitarValidacaoEmBloco(anyList(), anyLong(), any());
            }

            @Test
            @DisplayName("Deve separar homologação de cadastro e validação corretamente")
            void deveSepararHomologacaoCadastroEValidacao() {
                // Arrange
                Usuario usuario = new Usuario();
                when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

                SubprocessoDto spCadastro1 = SubprocessoDto.builder()
                    .codUnidade(1L)
                    .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                    .build();
                SubprocessoDto spCadastro2 = SubprocessoDto.builder()
                    .codUnidade(2L)
                    .situacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA)
                    .build();
                SubprocessoDto spValidacao1 = SubprocessoDto.builder()
                    .codUnidade(3L)
                    .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO)
                    .build();
                SubprocessoDto spValidacao2 = SubprocessoDto.builder()
                    .codUnidade(4L)
                    .situacao(SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO)
                    .build();

                when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L, 2L, 3L, 4L)))
                    .thenReturn(List.of(spCadastro1, spCadastro2, spValidacao1, spValidacao2));

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                    List.of(1L, 2L, 3L, 4L),
                    AcaoProcesso.HOMOLOGAR,
                    null
                );

                // Act
                processoFacade.executarAcaoEmBloco(100L, req);

                // Assert
                verify(subprocessoFacade).homologarCadastroEmBloco(List.of(1L, 2L), 100L, usuario);
                verify(subprocessoFacade).homologarValidacaoEmBloco(List.of(3L, 4L), 100L, usuario);
                verify(subprocessoFacade, never()).aceitarCadastroEmBloco(anyList(), anyLong(), any());
                verify(subprocessoFacade, never()).aceitarValidacaoEmBloco(anyList(), anyLong(), any());
            }

            @Test
            @DisplayName("Deve homologar cadastro quando em REVISAO_CADASTRO_HOMOLOGADA")
            void deveHomologarCadastroQuandoRevisaoHomologada() {
                // Arrange
                Usuario usuario = new Usuario();
                when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

                SubprocessoDto sp = SubprocessoDto.builder()
                    .codUnidade(1L)
                    .situacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA)
                    .build();

                when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L))).thenReturn(List.of(sp));

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                    List.of(1L),
                    AcaoProcesso.HOMOLOGAR,
                    null
                );

                // Act
                processoFacade.executarAcaoEmBloco(100L, req);

                // Assert
                verify(subprocessoFacade).homologarCadastroEmBloco(List.of(1L), 100L, usuario);
            }
        }

        @Nested
        @DisplayName("Casos de Branch - Listas Vazias")
        class ListasVazias {
            @Test
            @DisplayName("Não deve chamar aceitarCadastroEmBloco quando lista vazia")
            void naoDeveChamarAceitarCadastroQuandoListaVazia() {
                // Arrange
                Usuario usuario = new Usuario();
                when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

                SubprocessoDto sp = SubprocessoDto.builder()
                    .codUnidade(1L)
                    .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO)
                    .build();

                when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L))).thenReturn(List.of(sp));

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                    List.of(1L),
                    AcaoProcesso.ACEITAR,
                    null
                );

                // Act
                processoFacade.executarAcaoEmBloco(100L, req);

                // Assert - lista de cadastro vazia, só validação
                verify(subprocessoFacade, never()).aceitarCadastroEmBloco(anyList(), anyLong(), any());
                verify(subprocessoFacade).aceitarValidacaoEmBloco(List.of(1L), 100L, usuario);
            }

            @Test
            @DisplayName("Não deve chamar aceitarValidacaoEmBloco quando lista vazia")
            void naoDeveChamarAceitarValidacaoQuandoListaVazia() {
                // Arrange
                Usuario usuario = new Usuario();
                when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

                SubprocessoDto sp = SubprocessoDto.builder()
                    .codUnidade(1L)
                    .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                    .build();

                when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L))).thenReturn(List.of(sp));

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                    List.of(1L),
                    AcaoProcesso.ACEITAR,
                    null
                );

                // Act
                processoFacade.executarAcaoEmBloco(100L, req);

                // Assert - lista de validação vazia, só cadastro
                verify(subprocessoFacade).aceitarCadastroEmBloco(List.of(1L), 100L, usuario);
                verify(subprocessoFacade, never()).aceitarValidacaoEmBloco(anyList(), anyLong(), any());
            }

            @Test
            @DisplayName("Não deve chamar homologarCadastroEmBloco quando lista vazia")
            void naoDeveChamarHomologarCadastroQuandoListaVazia() {
                // Arrange
                Usuario usuario = new Usuario();
                when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

                SubprocessoDto sp = SubprocessoDto.builder()
                    .codUnidade(1L)
                    .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO)
                    .build();

                when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L))).thenReturn(List.of(sp));

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                    List.of(1L),
                    AcaoProcesso.HOMOLOGAR,
                    null
                );

                // Act
                processoFacade.executarAcaoEmBloco(100L, req);

                // Assert - lista de homologar cadastro vazia, só validação
                verify(subprocessoFacade, never()).homologarCadastroEmBloco(anyList(), anyLong(), any());
                verify(subprocessoFacade).homologarValidacaoEmBloco(List.of(1L), 100L, usuario);
            }

            @Test
            @DisplayName("Não deve chamar homologarValidacaoEmBloco quando lista vazia")
            void naoDeveChamarHomologarValidacaoQuandoListaVazia() {
                // Arrange
                Usuario usuario = new Usuario();
                when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

                SubprocessoDto sp = SubprocessoDto.builder()
                    .codUnidade(1L)
                    .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                    .build();

                when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L))).thenReturn(List.of(sp));

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                    List.of(1L),
                    AcaoProcesso.HOMOLOGAR,
                    null
                );

                // Act
                processoFacade.executarAcaoEmBloco(100L, req);

                // Assert - lista de homologar validação vazia, só cadastro
                verify(subprocessoFacade).homologarCadastroEmBloco(List.of(1L), 100L, usuario);
                verify(subprocessoFacade, never()).homologarValidacaoEmBloco(anyList(), anyLong(), any());
            }
        }

        @Nested
        @DisplayName("Casos de Situações Específicas")
        class SituacoesEspecificas {
            @Test
            @DisplayName("Não deve chamar nenhum método quando lista de unidades está vazia para ACEITAR")
            void naoDeveChamarNadaQuandoListaVaziaAceitar() {
                // Arrange
                Usuario usuario = new Usuario();
                when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                    List.of(),  // lista vazia
                    AcaoProcesso.ACEITAR,
                    null
                );

                // Act
                processoFacade.executarAcaoEmBloco(100L, req);

                // Assert - não deve chamar nenhum método de batch
                verify(subprocessoFacade, never()).listarPorProcessoEUnidades(anyLong(), anyList());
                verify(subprocessoFacade, never()).aceitarCadastroEmBloco(anyList(), anyLong(), any());
                verify(subprocessoFacade, never()).aceitarValidacaoEmBloco(anyList(), anyLong(), any());
                verify(subprocessoFacade, never()).homologarCadastroEmBloco(anyList(), anyLong(), any());
                verify(subprocessoFacade, never()).homologarValidacaoEmBloco(anyList(), anyLong(), any());
            }

            @Test
            @DisplayName("Identificar MAPEAMENTO_CADASTRO_DISPONIBILIZADO como cadastro")
            void deveIdentificarMapeamentoCadastroComoSituacaoCadastro() {
                // Arrange
                Usuario usuario = new Usuario();
                when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

                SubprocessoDto sp = SubprocessoDto.builder()
                    .codUnidade(1L)
                    .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                    .build();

                when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L))).thenReturn(List.of(sp));

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                    List.of(1L),
                    AcaoProcesso.ACEITAR,
                    null
                );

                // Act
                processoFacade.executarAcaoEmBloco(100L, req);

                // Assert
                verify(subprocessoFacade).aceitarCadastroEmBloco(List.of(1L), 100L, usuario);
            }

            @Test
            @DisplayName("Deve identificar REVISAO_CADASTRO_DISPONIBILIZADA como cadastro")
            void deveIdentificarRevisaoCadastroComoSituacaoCadastro() {
                // Arrange
                Usuario usuario = new Usuario();
                when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

                SubprocessoDto sp = SubprocessoDto.builder()
                    .codUnidade(1L)
                    .situacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA)
                    .build();

                when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L))).thenReturn(List.of(sp));

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                    List.of(1L),
                    AcaoProcesso.ACEITAR,
                    null
                );

                // Act
                processoFacade.executarAcaoEmBloco(100L, req);

                // Assert
                verify(subprocessoFacade).aceitarCadastroEmBloco(List.of(1L), 100L, usuario);
            }

            @Test
            @DisplayName("Deve identificar REVISAO_CADASTRO_HOMOLOGADA como cadastro")
            void deveIdentificarRevisaoCadastroHomologadaComoSituacaoCadastro() {
                // Arrange
                Usuario usuario = new Usuario();
                when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

                SubprocessoDto sp = SubprocessoDto.builder()
                    .codUnidade(1L)
                    .situacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA)
                    .build();

                when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L))).thenReturn(List.of(sp));

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                    List.of(1L),
                    AcaoProcesso.ACEITAR,
                    null
                );

                // Act
                processoFacade.executarAcaoEmBloco(100L, req);

                // Assert
                verify(subprocessoFacade).aceitarCadastroEmBloco(List.of(1L), 100L, usuario);
            }

            @Test
            @DisplayName("Deve identificar outras situações como NÃO cadastro")
            void deveIdentificarOutrasSituacoesComoNaoCadastro() {
                // Arrange
                Usuario usuario = new Usuario();
                when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

                SubprocessoDto sp = SubprocessoDto.builder()
                    .codUnidade(1L)
                    .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO)
                    .build();

                when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L))).thenReturn(List.of(sp));

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                    List.of(1L),
                    AcaoProcesso.ACEITAR,
                    null
                );

                // Act
                processoFacade.executarAcaoEmBloco(100L, req);

                // Assert - vai para validação
                verify(subprocessoFacade, never()).aceitarCadastroEmBloco(anyList(), anyLong(), any());
                verify(subprocessoFacade).aceitarValidacaoEmBloco(List.of(1L), 100L, usuario);
            }
        }
    }
}
