package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.ComumRepo;
import sgc.mapa.model.Mapa;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.query.ConsultasSubprocessoService;

import java.util.List;

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

    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private ComumRepo repo;
    @Mock
    private OrganizacaoFacade unidadeService;
    @Mock
    private ConsultasSubprocessoService queryService;
    @Mock
    private ProcessoValidador processoValidador;
    @Mock
    private ProcessoNotificacaoService processoNotificacaoService;

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
