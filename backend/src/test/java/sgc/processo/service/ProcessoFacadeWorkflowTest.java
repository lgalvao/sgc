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
import sgc.organizacao.UnidadeService;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.mapper.ProcessoMapper;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.model.SubprocessoMovimentacaoRepo;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("ProcessoFacade - Workflow e Inicialização")
class ProcessoFacadeWorkflowTest {
    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private UnidadeService unidadeService;
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
    private SubprocessoMovimentacaoRepo movimentacaoRepo;
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

        @Test
        @DisplayName("Deve falhar ao finalizar se houver subprocessos não homologados")
        void deveFalharAoFinalizarSeSubprocessosNaoHomologados() {
            // Arrange
            Long id = 100L;

            doThrow(new ErroProcesso("Não é possível encerrar o processo. Unidades pendentes de homologação:\n- U1 (Situação: MAPEAMENTO_CADASTRO_EM_ANDAMENTO)"))
                    .when(processoFinalizador).finalizar(id);

            // Act & Assert
            assertThatThrownBy(() -> processoFacade.finalizar(id))
                    .isInstanceOf(ErroProcesso.class)
                    .hasMessageContaining("pendentes de homologação");
        }

        @Test
        @DisplayName("Deve falhar ao finalizar se processo não está em andamento")
        void deveFalharAoFinalizarSeProcessoNaoEmAndamento() {
            // Arrange
            Long id = 100L;

            doThrow(new ErroProcesso("Apenas processos 'EM ANDAMENTO' podem ser finalizados."))
                    .when(processoFinalizador).finalizar(id);

            // Act & Assert
            assertThatThrownBy(() -> processoFacade.finalizar(id))
                    .isInstanceOf(ErroProcesso.class)
                    .hasMessageContaining("Apenas processos 'EM ANDAMENTO'");
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

        @Test
        @DisplayName("Deve falhar ao finalizar se subprocesso não tem unidade associada")
        void deveFalharAoFinalizarSeSubprocessoSemUnidade() {
            // Arrange
            Long id = 100L;

            doThrow(new ErroProcesso("Subprocesso 1 sem unidade associada."))
                    .when(processoFinalizador).finalizar(id);

            // Act & Assert
            assertThatThrownBy(() -> processoFacade.finalizar(id))
                    .isInstanceOf(ErroProcesso.class)
                    .hasMessageContaining("sem unidade associada");
        }

        @Test
        @DisplayName("Deve falhar ao finalizar se subprocesso não tem mapa")
        void deveFalharAoFinalizarSeSubprocessoSemMapa() {
            // Arrange
            Long id = 100L;

            doThrow(new ErroProcesso("Subprocesso 1 sem mapa associado."))
                    .when(processoFinalizador).finalizar(id);

            // Act & Assert
            assertThatThrownBy(() -> processoFacade.finalizar(id))
                    .isInstanceOf(ErroProcesso.class)
                    .hasMessageContaining("sem mapa associado");
        }

        @Test
        @DisplayName("Deve formatar mensagem de erro corretamente para subprocesso sem unidade ao finalizar")
        void deveFormatarMensagemErroParaSubprocessoSemUnidade() {
            Long id = 100L;

            doThrow(new ErroProcesso("Não é possível encerrar o processo. Unidades pendentes de homologação:\n- Subprocesso 55 (Situação: MAPEAMENTO_CADASTRO_EM_ANDAMENTO)"))
                    .when(processoFinalizador).finalizar(id);

            assertThatThrownBy(() -> processoFacade.finalizar(id))
                    .isInstanceOf(ErroProcesso.class)
                    .hasMessageContaining("Subprocesso 55 (Situação: MAPEAMENTO_CADASTRO_EM_ANDAMENTO)");
        }

        @Test
        @DisplayName("iniciarProcessoDiagnostico: delega para inicializador")
        void iniciarProcessoDiagnostico_Sucesso() {
            processoFacade.iniciarProcessoDiagnostico(1L, List.of(2L));
            verify(processoInicializador).iniciar(1L, List.of(2L));
        }

        @Test
        @DisplayName("finalizar: erro se processo não está em andamento")
        void finalizar_ErroStatus() {
            doThrow(new ErroProcesso("Apenas processos 'EM ANDAMENTO' podem ser finalizados."))
                    .when(processoFinalizador).finalizar(1L);

            assertThatThrownBy(() -> processoFacade.finalizar(1L))
                .isInstanceOf(ErroProcesso.class)
                .hasMessageContaining("Apenas processos 'EM ANDAMENTO' podem ser finalizados");
        }

        @Test
        @DisplayName("finalizar: erro se subprocesso sem unidade")
        void finalizar_SubprocessoSemUnidade() {
            doThrow(new ErroProcesso("Subprocesso 1 sem unidade associada."))
                    .when(processoFinalizador).finalizar(1L);
            
            assertThatThrownBy(() -> processoFacade.finalizar(1L))
                .isInstanceOf(ErroProcesso.class)
                .hasMessageContaining("sem unidade associada");
        }

         @Test
        @DisplayName("finalizar: erro se subprocesso sem mapa")
        void finalizar_SubprocessoSemMapa() {
            doThrow(new ErroProcesso("Subprocesso 1 sem mapa associado."))
                    .when(processoFinalizador).finalizar(1L);

            assertThatThrownBy(() -> processoFacade.finalizar(1L))
                .isInstanceOf(ErroProcesso.class)
                .hasMessageContaining("sem mapa associado");
        }

        @Test
        @DisplayName("finalizar: sucesso define mapa vigente")
        void finalizar_Sucesso() {
            doNothing().when(processoFinalizador).finalizar(1L);

            processoFacade.finalizar(1L);

            verify(processoFinalizador).finalizar(1L);
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

            when(processoRepo.findById(1L)).thenReturn(Optional.of(p));
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

            when(processoRepo.findById(1L)).thenReturn(Optional.of(p));
            when(unidadeService.buscarEntidadePorId(10L)).thenReturn(u);

            assertThatThrownBy(() -> processoFacade.enviarLembrete(1L, 10L))
                    .isInstanceOf(ErroProcesso.class)
                    .hasMessageContaining("não participa");
        }

        @Test
        @DisplayName("enviarLembrete: lança erro se unidade não participa")
        void enviarLembrete_NaoParticipa() {
            Processo p = new Processo();
            p.setParticipantes(java.util.Set.of());

            when(processoRepo.findById(1L)).thenReturn(Optional.of(p));
            when(unidadeService.buscarEntidadePorId(2L)).thenReturn(new Unidade());

            assertThatThrownBy(() -> processoFacade.enviarLembrete(1L, 2L))
                .isInstanceOf(ErroProcesso.class)
                .hasMessage("Unidade não participa deste processo.");
        }

        @Test
        @DisplayName("enviarLembrete: sucesso")
        void enviarLembrete_Sucesso() {
            Unidade u = new Unidade(); u.setCodigo(2L);
            Processo p = new Processo();
            p.setDescricao("P1");
            p.setDataLimite(java.time.LocalDateTime.now());
            p.setParticipantes(java.util.Set.of(u));

            when(processoRepo.findById(1L)).thenReturn(Optional.of(p));
            when(unidadeService.buscarEntidadePorId(2L)).thenReturn(u);

            processoFacade.enviarLembrete(1L, 2L);
            verify(alertaService).criarAlertaSedoc(eq(p), eq(u), anyString());
        }
    }
}
