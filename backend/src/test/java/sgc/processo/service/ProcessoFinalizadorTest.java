package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.mapa.model.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ProcessoFinalizador")
class ProcessoFinalizadorTest {

    @InjectMocks
    private ProcessoFinalizador finalizador;

    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private ComumRepo repo;
    @Mock
    private OrganizacaoFacade unidadeService;
    @Mock
    private ConsultasSubprocessoService queryService;
    @Mock
    private ProcessoNotificacaoService processoNotificacaoService;
    @Mock
    private ProcessoValidador processoValidador;

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

        verify(processoNotificacaoService).emailFinalizacaoProcesso(codigo);
    }

    @Test
    @DisplayName("Deve falhar se processo não encontrado")
    void deveFailharSeNaoEncontrado() {
        when(repo.buscar(Processo.class, 1L))
                .thenThrow(new ErroEntidadeNaoEncontrada("Processo", 1L));
        assertThatThrownBy(() -> finalizador.finalizar(1L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }
}
