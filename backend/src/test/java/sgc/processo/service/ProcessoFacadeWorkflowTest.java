package sgc.processo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.AlertaFacade;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.UnidadeFixture;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.mapper.ProcessoMapper;
import sgc.processo.model.Processo;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.service.SubprocessoFacade;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("ProcessoFacade - Workflow e Inicialização")
class ProcessoFacadeWorkflowTest {
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

    private ProcessoFacade processoFacade;

    @BeforeEach
    void setUp() throws Exception {
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
    @DisplayName("Workflow e Inicialização")
    class Workflow {
        @Test
        @DisplayName("Deve iniciar mapeamento com sucesso delegando para ProcessoInicializador")
        void deveIniciarMapeamentoComSucesso() {
            Long id = 100L;
            when(processoInicializador.iniciar(id, List.of(1L))).thenReturn(List.of());
            List<String> erros = processoFacade.iniciarProcessoMapeamento(id, List.of(1L));
            assertThat(erros).isEmpty();
            verify(processoInicializador).iniciar(id, List.of(1L));
        }

        @Test
        @DisplayName("Deve retornar erro ao iniciar mapeamento se unidade já em uso")
        void deveRetornarErroAoIniciarMapeamentoSeUnidadeEmUso() {
            Long id = 100L;
            String mensagemErro = "As seguintes unidades já participam de outro processo ativo: U1";
            when(processoInicializador.iniciar(id, List.of(1L))).thenReturn(List.of(mensagemErro));
            List<String> erros = processoFacade.iniciarProcessoMapeamento(id, List.of(1L));
            assertThat(erros).contains(mensagemErro);
        }

        @Test
        @DisplayName("Deve iniciar revisão com sucesso delegando para ProcessoInicializador")
        void deveIniciarRevisaoComSucesso() {
            Long id = 100L;
            when(processoInicializador.iniciar(id, List.of(1L))).thenReturn(List.of());
            List<String> erros = processoFacade.iniciarProcessoRevisao(id, List.of(1L));
            assertThat(erros).isEmpty();
            verify(processoInicializador).iniciar(id, List.of(1L));
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
            p.setParticipantes(Set.of(u));

            when(processoConsultaService.buscarPorId(1L)).thenReturn(p);
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
            p.setParticipantes(Set.of(outra));

            when(processoConsultaService.buscarPorId(1L)).thenReturn(p);
            when(unidadeService.buscarEntidadePorId(10L)).thenReturn(u);

            assertThatThrownBy(() -> processoFacade.enviarLembrete(1L, 10L))
                    .isInstanceOf(ErroProcesso.class)
                    .hasMessageContaining("não participa");
        }
    }
}
