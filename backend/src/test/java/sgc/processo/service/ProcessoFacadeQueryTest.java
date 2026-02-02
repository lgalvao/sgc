package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sgc.alerta.AlertaFacade;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Usuario;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.dto.SubprocessoElegivelDto;
import sgc.processo.mapper.ProcessoMapper;
import sgc.processo.model.Processo;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("ProcessoFacade - Consultas e Detalhes")
class ProcessoFacadeQueryTest {
    @Mock
    private ProcessoConsultaService processoConsultaService;
    @Mock
    private ProcessoManutencaoService processoManutencaoService;
    @Mock
    private UnidadeFacade unidadeService;
    @Mock
    private SubprocessoFacade subprocessoFacade;
    @Mock
    private ProcessoMapper processoMapper;
    @Mock
    private ProcessoDetalheBuilder processoDetalheBuilder;
    @Mock
    private SubprocessoMapper subprocessoMapper;
    @Mock
    private UsuarioFacade usuarioService;
    @Mock
    private ProcessoInicializador processoInicializador;
    @Mock
    private AlertaFacade alertaService;
    @Mock
    private ProcessoAcessoService processoAcessoService;
    @Mock
    private ProcessoFinalizador processoFinalizador;
    
    private static final String MAPEAMENTO = "MAPEAMENTO";
    @Mock
    private ProcessoValidador processoValidador;

    private ProcessoFacade processoFacade;

    private Usuario criarUsuarioMock() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("12345678901");
        return usuario;
    }

    @BeforeEach
    void setUp() {
        processoFacade = new ProcessoFacade(
            processoConsultaService,
            processoManutencaoService,
            unidadeService,
            subprocessoFacade,
            processoMapper,
            processoDetalheBuilder,
            subprocessoMapper,
            usuarioService,
            processoInicializador,
            alertaService,
            processoAcessoService,
            processoFinalizador
        );
    }

    @Nested
    @DisplayName("Consultas e Detalhes")
    class Consultas {
        @Test
        @DisplayName("Deve retornar detalhes do processo (DTO)")
        void deveRetornarDetalhesDoProcesso() {
            // Arrange
            Usuario usuario = criarUsuarioMock();
            Long id = 100L;
            Processo processo = ProcessoFixture.processoPadrao();
            when(processoConsultaService.buscarPorId(id)).thenReturn(processo);
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
            when(processoConsultaService.buscarPorId(999L)).thenThrow(new ErroEntidadeNaoEncontrada("Processo", 999L));
            assertThatThrownBy(() -> processoFacade.obterDetalhes(999L, usuario))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve buscar entidade por ID")
        void deveBuscarEntidadePorId() {
            Long id = 100L;
            Processo processo = ProcessoFixture.processoPadrao();
            when(processoConsultaService.buscarPorId(id)).thenReturn(processo);

            Processo res = processoFacade.buscarEntidadePorId(id);
            assertThat(res).isEqualTo(processo);
        }

        @Test
        @DisplayName("Deve falhar buscar entidade inexistente")
        void deveFalharBuscarEntidadeInexistente() {
            when(processoConsultaService.buscarPorId(999L)).thenThrow(new ErroEntidadeNaoEncontrada("Processo", 999L));
            assertThatThrownBy(() -> processoFacade.buscarEntidadePorId(999L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve obter processo por ID (Optional)")
        void deveObterPorIdOptional() {
            Long id = 100L;
            Processo processo = ProcessoFixture.processoPadrao();
            when(processoConsultaService.buscarPorIdOpcional(id)).thenReturn(Optional.of(processo));
            when(processoMapper.toDto(processo)).thenReturn(ProcessoDto.builder().build());

            Optional<ProcessoDto> res = processoFacade.obterPorId(id);
            assertThat(res).isPresent();
        }

        @Test
        @DisplayName("Deve listar processos finalizados e ativos")
        void deveListarProcessosFinalizadosEAtivos() {
            // Arrange
            when(processoConsultaService.listarFinalizados())
                    .thenReturn(List.of(ProcessoFixture.processoPadrao()));
            when(processoConsultaService.listarAtivos())
                    .thenReturn(List.of(ProcessoFixture.processoPadrao()));
            when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

            // Act & Assert
            assertThat(processoFacade.listarFinalizados()).hasSize(1);
            assertThat(processoFacade.listarAtivos()).hasSize(1);
        }

        @Test
        @DisplayName("Deve listar todos com paginação")
        void deveListarTodosPaginado() {
            Pageable pageable = Pageable.unpaged();
            when(processoConsultaService.listarTodos(pageable)).thenReturn(Page.empty());

            var res = processoFacade.listarTodos(pageable);
            assertThat(res).isEmpty();
        }

        @Test
        @DisplayName("Deve listar unidades bloqueadas por tipo")
        void deveListarUnidadesBloqueadasPorTipo() {
            // Arrange
            when(processoConsultaService.listarUnidadesBloqueadasPorTipo(MAPEAMENTO))
                    .thenReturn(List.of(1L));

            // Act
            List<Long> bloqueadas = processoFacade.listarUnidadesBloqueadasPorTipo(MAPEAMENTO);

            // Assert
            assertThat(bloqueadas).contains(1L);
        }

        @Test
        @DisplayName("Deve listar todos subprocessos")
        void deveListarTodosSubprocessos() {
            when(subprocessoFacade.listarEntidadesPorProcesso(100L))
                    .thenReturn(List.of(SubprocessoFixture.subprocessoPadrao(null, null)));
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
            when(processoConsultaService.listarSubprocessosElegiveis(100L))
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
            when(processoConsultaService.listarSubprocessosElegiveis(100L))
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
            when(processoConsultaService.listarSubprocessosElegiveis(100L))
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
            verify(processoConsultaService).listarPorParticipantesIgnorandoCriado(anyList(), any());
        }

        @Test
        @DisplayName("Deve lançar exceção para tipo de processo inválido")
        void deveLancarExcecaoParaTipoInvalido() {
            when(processoConsultaService.listarUnidadesBloqueadasPorTipo("TIPO_INEXISTENTE"))
                    .thenThrow(new IllegalArgumentException("No enum constant"));

            assertThatThrownBy(() -> processoFacade.listarUnidadesBloqueadasPorTipo("TIPO_INEXISTENTE"))
                    .isInstanceOf(IllegalArgumentException.class);
        }


        @Test
        @DisplayName("Deve retornar contexto completo do processo")
        void deveRetornarContextoCompleto() {
            // Arrange
            Usuario usuario = criarUsuarioMock();
            Long id = 100L;
            Processo processo = ProcessoFixture.processoPadrao();
            ProcessoDetalheDto detalhes = ProcessoDetalheDto.builder().build();

            when(processoConsultaService.buscarPorId(id)).thenReturn(processo);
            when(processoDetalheBuilder.build(eq(processo), any(Usuario.class))).thenReturn(detalhes);
            when(processoConsultaService.listarSubprocessosElegiveis(id))
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
            when(processoConsultaService.buscarPorId(1L)).thenReturn(p);
            when(processoDetalheBuilder.build(eq(p), any(Usuario.class))).thenReturn(ProcessoDetalheDto.builder().build());

            assertThat(processoFacade.obterDetalhes(1L, usuario)).isNotNull();
        }

        @Test
        @DisplayName("listarUnidadesBloqueadasPorTipo: chama repo")
        void listarUnidadesBloqueadasPorTipo_Test() {
            when(processoConsultaService.listarUnidadesBloqueadasPorTipo(MAPEAMENTO)).thenReturn(List.of(1L, 2L));

            processoFacade.listarUnidadesBloqueadasPorTipo(MAPEAMENTO);
            verify(processoConsultaService).listarUnidadesBloqueadasPorTipo(MAPEAMENTO);
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
}
