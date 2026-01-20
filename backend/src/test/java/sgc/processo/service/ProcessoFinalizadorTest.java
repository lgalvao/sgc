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
import sgc.processo.erros.ErroProcesso;
import sgc.processo.eventos.EventoProcessoFinalizado;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes para ProcessoFinalizador")
class ProcessoFinalizadorTest {

    @InjectMocks
    private ProcessoFinalizador finalizador;

    @Mock private ProcessoRepo processoRepo;
    @Mock private UnidadeFacade unidadeService;
    @Mock private SubprocessoFacade subprocessoFacade;
    @Mock private ProcessoValidador processoValidador;
    @Mock private ApplicationEventPublisher publicadorEventos;

    @Test
    @DisplayName("Deve finalizar processo com sucesso")
    void deveFinalizarComSucesso() {
        Long codigo = 1L;
        Processo p = new Processo();
        p.setCodigo(codigo);
        p.setDescricao("Processo 1");
        when(processoRepo.findById(codigo)).thenReturn(Optional.of(p));

        Subprocesso s = new Subprocesso();
        s.setCodigo(10L);
        Unidade u = new Unidade();
        u.setCodigo(100L);
        s.setUnidade(u);
        Mapa m = new Mapa();
        s.setMapa(m);

        when(subprocessoFacade.listarEntidadesPorProcesso(codigo)).thenReturn(List.of(s));

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
        when(processoRepo.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> finalizador.finalizar(1L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Deve falhar se subprocesso não tem unidade")
    void deveFalharSemUnidade() {
        Long codigo = 1L;
        Processo p = new Processo();
        p.setCodigo(codigo);
        when(processoRepo.findById(codigo)).thenReturn(Optional.of(p));

        Subprocesso s = new Subprocesso();
        s.setCodigo(10L);
        s.setUnidade(null);

        when(subprocessoFacade.listarEntidadesPorProcesso(codigo)).thenReturn(List.of(s));

        assertThatThrownBy(() -> finalizador.finalizar(codigo))
                .isInstanceOf(ErroProcesso.class)
                .hasMessageContaining("sem unidade");
    }

    @Test
    @DisplayName("Deve falhar se subprocesso não tem mapa")
    void deveFalharSemMapa() {
        Long codigo = 1L;
        Processo p = new Processo();
        p.setCodigo(codigo);
        when(processoRepo.findById(codigo)).thenReturn(Optional.of(p));

        Subprocesso s = new Subprocesso();
        s.setCodigo(10L);
        Unidade u = new Unidade();
        u.setCodigo(100L);
        s.setUnidade(u);
        s.setMapa(null);

        when(subprocessoFacade.listarEntidadesPorProcesso(codigo)).thenReturn(List.of(s));

        assertThatThrownBy(() -> finalizador.finalizar(codigo))
                .isInstanceOf(ErroProcesso.class)
                .hasMessageContaining("sem mapa");
    }
}
