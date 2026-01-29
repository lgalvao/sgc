package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.alerta.AlertaFacade;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.UnidadeFixture;
import sgc.mapa.model.MapaRepo;
import sgc.mapa.service.CopiaMapaService;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.mapper.ProcessoMapper;
import sgc.processo.model.Processo;

import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("ProcessoFacade - Workflow e Inicialização")
class ProcessoFacadeWorkflowTest {
    @Mock
    private ProcessoRepositoryService processoRepositoryService;
    @Mock
    private UnidadeFacade unidadeService;
    @Mock
    private SubprocessoFacade subprocessoFacade;
    @Mock
    private ApplicationEventPublisher publicadorEventos;
    @Mock
    private ProcessoMapper processoMapper;
    @Mock
    private ProcessoDetalheBuilder processoDetalheBuilder;
    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private MovimentacaoRepo movimentacaoRepo;
    @Mock
    private SubprocessoMapper subprocessoMapper;
    @Mock
    private CopiaMapaService servicoDeCopiaDeMapa;
    @Mock
    private UsuarioFacade usuarioService;
    @Mock
    private ProcessoInicializador processoInicializador;
    @Mock
    private AlertaFacade alertaService;

    // Specialized services
    @Mock
    private ProcessoAcessoService processoAcessoService;
    @Mock
    private ProcessoValidador processoValidador;
    @Mock
    private ProcessoFinalizador processoFinalizador;
    @Mock
    private ProcessoConsultaService processoConsultaService;

    @InjectMocks
    private ProcessoFacade processoFacade;

    @org.junit.jupiter.api.BeforeEach
    void injectSelf() throws Exception {
        java.lang.reflect.Field selfField = ProcessoFacade.class.getDeclaredField("self");
        selfField.setAccessible(true);
        selfField.set(processoFacade, processoFacade);
    }

    @Nested
    @DisplayName("Workflow e Inicialização")
    class Workflow {
        @Test
        @DisplayName("Deve iniciar mapeamento com sucesso delegando para ProcessoInicializador")
        void deveIniciarMapeamentoComSucesso() {
            // Arrange
            Long id = 100L;
            when(processoInicializador.iniciar(id, List.of(1L))).thenReturn(List.of());

            // Act
            List<String> erros = processoFacade.iniciarProcessoMapeamento(id, List.of(1L));

            // Assert
            assertThat(erros).isEmpty();
            verify(processoInicializador).iniciar(id, List.of(1L));
        }

        @Test
        @DisplayName("Deve retornar erro ao iniciar mapeamento se unidade já em uso")
        void deveRetornarErroAoIniciarMapeamentoSeUnidadeEmUso() {
            // Arrange
            Long id = 100L;
            String mensagemErro = "As seguintes unidades já participam de outro processo ativo: U1";
            when(processoInicializador.iniciar(id, List.of(1L))).thenReturn(List.of(mensagemErro));

            // Act
            List<String> erros = processoFacade.iniciarProcessoMapeamento(id, List.of(1L));

            // Assert
            assertThat(erros).contains(mensagemErro);
        }

        @Test
        @DisplayName("Deve iniciar revisão com sucesso delegando para ProcessoInicializador")
        void deveIniciarRevisaoComSucesso() {
            // Arrange
            Long id = 100L;
            when(processoInicializador.iniciar(id, List.of(1L))).thenReturn(List.of());

            // Act
            List<String> erros = processoFacade.iniciarProcessoRevisao(id, List.of(1L));

            // Assert
            assertThat(erros).isEmpty();
            verify(processoInicializador).iniciar(id, List.of(1L));
        }

        @Test
        @DisplayName("Deve retornar erro ao iniciar revisão se unidade sem mapa")
        void deveRetornarErroAoIniciarRevisaoSeUnidadeSemMapa() {
            // Arrange
            Long id = 100L;
            String mensagemErro = "As seguintes unidades não possuem mapa vigente e não podem participar de um processo de revisão: U1";
            when(processoInicializador.iniciar(id, List.of(1L))).thenReturn(List.of(mensagemErro));

            // Act
            List<String> erros = processoFacade.iniciarProcessoRevisao(id, List.of(1L));

            // Assert
            assertThat(erros).contains(mensagemErro);
        }

        @Test
        @DisplayName("Deve iniciar diagnostico com sucesso delegando para ProcessoInicializador")
        void deveIniciarDiagnosticoComSucesso() {
            Long id = 100L;
            when(processoInicializador.iniciar(id, List.of(1L))).thenReturn(List.of());
            List<String> erros = processoFacade.iniciarProcessoDiagnostico(id, List.of(1L));
            assertThat(erros).isEmpty();
            verify(processoInicializador).iniciar(id, List.of(1L));
        }

        @org.junit.jupiter.params.ParameterizedTest
        @org.junit.jupiter.params.provider.CsvSource({
            "Não é possível encerrar o processo. Unidades pendentes de homologação:, pendentes de homologação",
            "Apenas processos 'EM ANDAMENTO' podem ser finalizados., Apenas processos 'EM ANDAMENTO'",
            "Subprocesso 1 sem unidade associada., sem unidade associada",
            "Subprocesso 1 sem mapa associado., sem mapa associado",
            "'Não é possível encerrar o processo. Unidades pendentes de homologação:\n- Subprocesso 55 (Situação: MAPEAMENTO_CADASTRO_EM_ANDAMENTO)', Subprocesso 55"
        })
        @DisplayName("Deve falhar ao finalizar em casos de erro de negócio")
        void deveFalharAoFinalizar(String mensagemInjetada, String mensagemEsperada) {
            Long id = 100L;
            doThrow(new ErroProcesso(mensagemInjetada)).when(processoFinalizador).finalizar(id);
            assertThatThrownBy(() -> processoFacade.finalizar(id))
                    .isInstanceOf(ErroProcesso.class)
                    .hasMessageContaining(mensagemEsperada);
        }

        @Test
        @DisplayName("Deve finalizar processo com sucesso quando tudo homologado")
        void deveFinalizarProcessoQuandoTudoHomologado() {
            // Arrange
            Long id = 100L;

            // Act
            processoFacade.finalizar(id);

            // Assert
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
            p.setParticipantes(java.util.Set.of(u));

            when(processoRepositoryService.buscarPorId(1L)).thenReturn(p);
            when(unidadeService.buscarEntidadePorId(10L)).thenReturn(u);

            processoFacade.enviarLembrete(1L, 10L);
            verify(alertaService).criarAlertaSedoc(eq(p), eq(u), anyString());
        }

        @Test
        @DisplayName("Deve falhar ao enviar lembrete se unidade nao participa")
        void deveFalharEnviarLembreteUnidadeNaoParticipa() {
            Processo p = ProcessoFixture.processoEmAndamento();
            p.setCodigo(1L);
            Unidade u = UnidadeFixture.unidadeComId(10L);
            Unidade outra = UnidadeFixture.unidadeComId(20L);
            p.setParticipantes(java.util.Set.of(outra));

            when(processoRepositoryService.buscarPorId(1L)).thenReturn(p);
            when(unidadeService.buscarEntidadePorId(10L)).thenReturn(u);

            assertThatThrownBy(() -> processoFacade.enviarLembrete(1L, 10L))
                    .isInstanceOf(ErroProcesso.class)
                    .hasMessageContaining("não participa");
        }

    }
}
