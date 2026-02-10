package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.Mapa;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.model.Unidade;
import sgc.processo.eventos.EventoProcessoFinalizado;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.query.ProcessoSubprocessoQueryService;
import sgc.comum.repo.ComumRepo;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import sgc.processo.erros.ErroProcesso;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes para ProcessoFinalizador")
class ProcessoFinalizadorTest {

    @InjectMocks
    private ProcessoFinalizador finalizador;

    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private ComumRepo repo;
    @Mock
    private UnidadeFacade unidadeService;
    @Mock
    private ProcessoSubprocessoQueryService queryService;
    @Mock
    private ProcessoValidador processoValidador;
    @Mock
    private ApplicationEventPublisher publicadorEventos;

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

        when(queryService.listarEntidadesPorProcesso(codigo)).thenReturn(List.of(s));

        finalizador.finalizar(codigo);

        assertThat(p.getSituacao()).isEqualTo(SituacaoProcesso.FINALIZADO);
        verify(unidadeService).definirMapaVigente(100L, m);
        verify(processoRepo).save(p);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(publicadorEventos).publishEvent(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(EventoProcessoFinalizado.class);
    }

    @Test
    @DisplayName("Deve falhar se processo não encontrado")
    void deveFailharSeNaoEncontrado() {
        when(repo.buscar(Processo.class, 1L))
                .thenThrow(new ErroEntidadeNaoEncontrada("Processo", 1L));
        assertThatThrownBy(() -> finalizador.finalizar(1L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Deve falhar se subprocesso não tiver unidade")
    void deveFalharSeSubprocessoSemUnidade() {
        Long codigo = 1L;
        Processo p = new Processo();
        p.setCodigo(codigo);
        when(repo.buscar(Processo.class, codigo)).thenReturn(p);

        Subprocesso s = new Subprocesso();
        s.setCodigo(10L);
        s.setUnidade(null);

        when(queryService.listarEntidadesPorProcesso(codigo)).thenReturn(List.of(s));

        assertThatThrownBy(() -> finalizador.finalizar(codigo))
                .isInstanceOf(ErroProcesso.class)
                .hasMessageContaining("sem unidade associada");
    }

    @Test
    @DisplayName("Deve falhar se subprocesso não tiver mapa")
    void deveFalharSeSubprocessoSemMapa() {
        Long codigo = 1L;
        Processo p = new Processo();
        p.setCodigo(codigo);
        when(repo.buscar(Processo.class, codigo)).thenReturn(p);

        Subprocesso s = new Subprocesso();
        s.setCodigo(10L);
        s.setUnidade(new Unidade());
        s.setMapa(null);

        when(queryService.listarEntidadesPorProcesso(codigo)).thenReturn(List.of(s));

        assertThatThrownBy(() -> finalizador.finalizar(codigo))
                .isInstanceOf(ErroProcesso.class)
                .hasMessageContaining("sem mapa associado");
    }
}
