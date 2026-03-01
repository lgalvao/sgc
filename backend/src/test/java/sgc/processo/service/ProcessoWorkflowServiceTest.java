package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;
import sgc.testutils.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoWorkflowService - Testes")
class ProcessoWorkflowServiceTest {

    @InjectMocks
    private ProcessoWorkflowService workflowService;

    @Mock private ProcessoRepo processoRepo;
    @Mock private ComumRepo repo;
    @Mock private UnidadeService unidadeService;
    @Mock private SubprocessoService subprocessoService;
    @Mock private ProcessoValidacaoService processoValidador;
    @Mock private ProcessoNotificacaoService notificacaoService;

    // ---- Finalizar ----

    @Test
    @DisplayName("Deve finalizar processo com sucesso")
    void deveFinalizarComSucesso() {
        Long codigo = 1L;
        Processo p = new Processo();
        p.setCodigo(codigo);
        p.setDescricao("Processo 1");
        when(repo.buscar(Processo.class, codigo)).thenReturn(p);

        Subprocesso s = new Subprocesso();
        s.setCodigo(10L);
        Unidade u = new Unidade();
        u.setCodigo(100L);
        s.setUnidade(u);
        Mapa m = new Mapa();
        s.setMapa(m);

        when(subprocessoService.listarEntidadesPorProcesso(codigo)).thenReturn(List.of(s));

        workflowService.finalizar(codigo);

        assertThat(p.getSituacao()).isEqualTo(SituacaoProcesso.FINALIZADO);
        verify(unidadeService).definirMapaVigente(100L, m);
        verify(processoRepo).save(p);
        verify(notificacaoService).emailFinalizacaoProcesso(codigo);
    }

    @Test
    @DisplayName("Deve falhar ao finalizar se processo não encontrado")
    void deveFalharSeProcessoNaoEncontrado() {
        when(repo.buscar(Processo.class, 1L))
                .thenThrow(new ErroEntidadeNaoEncontrada("Processo", 1L));
        assertThatThrownBy(() -> workflowService.finalizar(1L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("finalizar deve ignorar mapas se tipo for DIAGNOSTICO")
    void deveIgnorarMapasSeDiagnostico() {
        Long codigo = 1L;
        Processo processo = new Processo();
        processo.setCodigo(codigo);
        processo.setTipo(TipoProcesso.DIAGNOSTICO);

        when(repo.buscar(Processo.class, codigo)).thenReturn(processo);

        workflowService.finalizar(codigo);

        verify(subprocessoService, never()).listarEntidadesPorProcesso(any());
        verify(processoRepo).save(processo);
        verify(notificacaoService).emailFinalizacaoProcesso(codigo);
    }

    // ---- Iniciar ----

    @Test
    @DisplayName("Deve iniciar processo de mapeamento para unidades participantes")
    void deveIniciarMapeamento() {
        Processo p = new Processo();
        p.setCodigo(1L);
        p.setTipo(TipoProcesso.MAPEAMENTO);
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.adicionarParticipantes(Set.of(UnidadeTestBuilder.umaDe().comCodigo("10").build()));

        Usuario usuario = new Usuario();
        Unidade admin = new Unidade();

        when(repo.buscar(Processo.class, 1L)).thenReturn(p);
        when(unidadeService.porCodigos(any())).thenReturn(List.of(UnidadeTestBuilder.umaDe().comCodigo("10").build()));
        when(repo.buscarPorSigla(Unidade.class, "ADMIN")).thenReturn(admin);

        List<String> erros = workflowService.iniciar(1L, null, usuario);

        assertThat(erros).isEmpty();
        verify(subprocessoService).criarParaMapeamento(eq(p), any(), eq(admin), eq(usuario));
        verify(notificacaoService).emailInicioProcesso(eq(1L));
        assertThat(p.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);
    }

    @Test
    @DisplayName("Deve iniciar processo de revisão para unidades listadas")
    void deveIniciarRevisao() {
        Processo p = new Processo();
        p.setCodigo(1L);
        p.setTipo(TipoProcesso.REVISAO);
        p.setSituacao(SituacaoProcesso.CRIADO);

        Usuario usuario = new Usuario();
        Unidade admin = new Unidade();
        Unidade u = UnidadeTestBuilder.umaDe().comCodigo("10").build();
        UnidadeMapa um = new UnidadeMapa();

        when(repo.buscar(Processo.class, 1L)).thenReturn(p);
        when(repo.buscar(Unidade.class, 10L)).thenReturn(u);
        when(unidadeService.buscarMapasPorUnidades(anyList())).thenReturn(List.of(um));
        when(repo.buscarPorSigla(Unidade.class, "ADMIN")).thenReturn(admin);
        when(processoValidador.getMensagemErroUnidadesSemMapa(any())).thenReturn(Optional.empty());
        when(unidadeService.porCodigos(anyList())).thenReturn(List.of(u));
        um.setUnidadeCodigo(10L);

        List<String> erros = workflowService.iniciar(1L, List.of(10L), usuario);

        assertThat(erros).isEmpty();
        verify(unidadeService).buscarMapasPorUnidades(anyList());
        verify(subprocessoService).criarParaRevisao(p, u, um, admin, usuario);
        verify(notificacaoService).emailInicioProcesso(eq(1L));
    }

    @Test
    @DisplayName("Deve iniciar processo de diagnóstico para unidades participantes")
    void deveIniciarDiagnostico() {
        Processo p = new Processo();
        p.setCodigo(1L);
        p.setTipo(TipoProcesso.DIAGNOSTICO);
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.adicionarParticipantes(Set.of(UnidadeTestBuilder.umaDe().comCodigo("10").build()));

        Usuario usuario = new Usuario();
        Unidade admin = new Unidade();
        UnidadeMapa um = new UnidadeMapa();
        um.setUnidadeCodigo(10L);

        when(repo.buscar(Processo.class, 1L)).thenReturn(p);
        when(unidadeService.porCodigos(any())).thenReturn(List.of(UnidadeTestBuilder.umaDe().comCodigo("10").build()));
        when(unidadeService.buscarMapasPorUnidades(any())).thenReturn(List.of(um));
        when(repo.buscarPorSigla(Unidade.class, "ADMIN")).thenReturn(admin);
        when(processoValidador.getMensagemErroUnidadesSemMapa(any())).thenReturn(Optional.empty());

        List<String> erros = workflowService.iniciar(1L, null, usuario);

        assertThat(erros).isEmpty();
        verify(subprocessoService).criarParaDiagnostico(eq(p), any(), any(), eq(admin), eq(usuario));
        verify(notificacaoService).emailInicioProcesso(eq(1L));
    }

    @Test
    @DisplayName("Deve lançar erro ao iniciar revisão sem unidades")
    void deveErrarIniciarRevisaoSemUnidades() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setTipo(TipoProcesso.REVISAO);

        when(repo.buscar(Processo.class, 1L)).thenReturn(p);

        assertThatThrownBy(() -> workflowService.iniciar(1L, List.of(), new Usuario()))
                .isInstanceOf(ErroValidacao.class);
    }

    @Test
    @DisplayName("Deve lançar erro ao iniciar mapeamento sem participantes")
    void deveErrarIniciarSemParticipantes() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setTipo(TipoProcesso.MAPEAMENTO);

        when(repo.buscar(Processo.class, 1L)).thenReturn(p);

        assertThatThrownBy(() -> workflowService.iniciar(1L, List.of(), new Usuario()))
                .isInstanceOf(ErroValidacao.class);
    }

    @Test
    @DisplayName("Deve retornar erro de validação se unidades já estiverem em processo ativo")
    void deveRetornarErroSeUnidadesEmProcessoAtivo() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setTipo(TipoProcesso.MAPEAMENTO);
        Unidade u = new Unidade();
        u.setCodigo(2L);
        u.setSituacao(sgc.organizacao.model.SituacaoUnidade.ATIVA);
        p.adicionarParticipantes(java.util.Set.of(u));

        when(repo.buscar(Processo.class, 1L)).thenReturn(p);
        when(processoRepo.findUnidadeCodigosBySituacaoAndUnidadeCodigosIn(
                eq(SituacaoProcesso.EM_ANDAMENTO), any())).thenReturn(List.of(2L));
        when(unidadeService.buscarSiglasPorIds(any())).thenReturn(List.of("UN1"));

        List<String> erros = workflowService.iniciar(1L, List.of(), new Usuario());
        assertThat(erros).hasSize(1);
    }
}
