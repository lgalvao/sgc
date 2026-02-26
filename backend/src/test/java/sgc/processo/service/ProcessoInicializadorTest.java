package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.model.ComumRepo;
import sgc.organizacao.model.*;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.service.SubprocessoService;
import sgc.testutils.UnidadeTestBuilder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
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
