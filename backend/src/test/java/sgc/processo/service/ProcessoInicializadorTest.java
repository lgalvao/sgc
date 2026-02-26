package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.service.*;
import sgc.testutils.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoInicializador Test")
class ProcessoInicializadorTest {

    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private ComumRepo repo;
    @Mock
    private UnidadeRepo unidadeRepo;
    @Mock
    private UnidadeMapaRepo unidadeMapaRepo;
    @Mock
    private ProcessoNotificacaoService notificacaoService;
    @Mock
    private SubprocessoService subprocessoService;
    @Mock
    private ProcessoValidador processoValidador;

    @InjectMocks
    private ProcessoInicializador inicializador;

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
        when(unidadeRepo.findAllById(any())).thenReturn(List.of(UnidadeTestBuilder.umaDe().comCodigo("10").build()));
        when(repo.buscarPorSigla(Unidade.class, "ADMIN")).thenReturn(admin);

        List<String> erros = inicializador.iniciar(1L, null, usuario);

        assertThat(erros).isEmpty();
        verify(subprocessoService).criarParaMapeamento(eq(p), any(), eq(admin), eq(usuario));
        verify(notificacaoService).emailInicioProcesso(eq(1L), anyList());
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
        lenient().when(repo.buscar(Unidade.class, 10L)).thenReturn(u);
        when(unidadeMapaRepo.findAllById(anyList())).thenReturn(List.of(um));
        when(repo.buscarPorSigla(Unidade.class, "ADMIN")).thenReturn(admin);
        um.setUnidadeCodigo(10L);

        List<String> erros = inicializador.iniciar(1L, List.of(10L), usuario);

        assertThat(erros).isEmpty();
        verify(unidadeMapaRepo).findAllById(anyList());
        verify(subprocessoService).criarParaRevisao(p, u, um, admin, usuario);
        verify(notificacaoService).emailInicioProcesso(eq(1L), anyList());
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
        when(unidadeRepo.findAllById(any())).thenReturn(List.of(UnidadeTestBuilder.umaDe().comCodigo("10").build()));
        when(unidadeMapaRepo.findAllById(any())).thenReturn(List.of(um));
        when(repo.buscarPorSigla(Unidade.class, "ADMIN")).thenReturn(admin);

        List<String> erros = inicializador.iniciar(1L, null, usuario);

        assertThat(erros).isEmpty();
        verify(subprocessoService).criarParaDiagnostico(eq(p), any(), any(), eq(admin), eq(usuario));
        verify(notificacaoService).emailInicioProcesso(eq(1L), anyList());
    }
}
