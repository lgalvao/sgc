package sgc.subprocesso.service.factory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.mapa.service.CopiaMapaService;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeMapa;
import sgc.processo.model.Processo;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.MovimentacaoRepositoryService;
import sgc.subprocesso.service.SubprocessoRepositoryService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class SubprocessoFactoryTest {
    @Mock
    private SubprocessoRepositoryService subprocessoService;

    @Mock
    private MapaRepo mapaRepo;

    @Mock
    private MovimentacaoRepositoryService movimentacaoService;

    @Mock
    private CopiaMapaService servicoDeCopiaDeMapa;

    @InjectMocks
    private SubprocessoFactory factory;

    @Test
    @DisplayName("criarParaMapeamento sucesso para unidade OPERACIONAL")
    void criarParaMapeamento_Sucesso() {
        Processo processo = new Processo();
        processo.setDataLimite(LocalDateTime.now().plusDays(10));
        Unidade unidade = new Unidade();
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setSigla("U1");

        when(subprocessoService.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
        when(mapaRepo.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        factory.criarParaMapeamento(processo, List.of(unidade));

        verify(subprocessoService, times(1)).saveAll(anyList());
        verify(mapaRepo).saveAll(anyList());
        verify(movimentacaoService).saveAll(anyList());
    }

    @Test
    @DisplayName("criarParaMapeamento deve ignorar unidade nao elegivel")
    void criarParaMapeamento_IgnorarIneligivel() {
        Processo processo = new Processo();
        Unidade unidade = new Unidade();
        unidade.setTipo(TipoUnidade.INTERMEDIARIA);

        factory.criarParaMapeamento(processo, List.of(unidade));

        verifyNoInteractions(subprocessoService);
    }

    @Test
    @DisplayName("criarParaRevisao sucesso")
    void criarParaRevisao_Sucesso() {
        Processo processo = new Processo();
        processo.setDataLimite(LocalDateTime.now().plusDays(10));
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSigla("U1");

        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(100L);
        UnidadeMapa unidadeMapa = new UnidadeMapa();
        unidadeMapa.setMapaVigente(mapaVigente);

        when(subprocessoService.save(any(Subprocesso.class))).thenAnswer(i -> i.getArgument(0));

        Mapa mapaCopiado = new Mapa();
        mapaCopiado.setCodigo(200L);
        when(servicoDeCopiaDeMapa.copiarMapaParaUnidade(100L)).thenReturn(mapaCopiado);
        when(mapaRepo.save(any(Mapa.class))).thenAnswer(i -> i.getArgument(0));

        factory.criarParaRevisao(processo, unidade, unidadeMapa);

        verify(subprocessoService, times(1)).save(any(Subprocesso.class));
        verify(mapaRepo).save(any(Mapa.class));
        verify(movimentacaoService).save(any(Movimentacao.class));
    }

    @Test
    @DisplayName("criarParaDiagnostico sucesso")
    void criarParaDiagnostico_Sucesso() {
        Processo processo = new Processo();
        processo.setDataLimite(LocalDateTime.now().plusDays(10));
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSigla("U1");

        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(100L);
        UnidadeMapa unidadeMapa = new UnidadeMapa();
        unidadeMapa.setMapaVigente(mapaVigente);

        when(subprocessoService.save(any(Subprocesso.class))).thenAnswer(i -> i.getArgument(0));

        Mapa mapaCopiado = new Mapa();
        mapaCopiado.setCodigo(200L);
        when(servicoDeCopiaDeMapa.copiarMapaParaUnidade(100L)).thenReturn(mapaCopiado);
        when(mapaRepo.save(any(Mapa.class))).thenAnswer(i -> i.getArgument(0));

        factory.criarParaDiagnostico(processo, unidade, unidadeMapa);

        // Verifica situacao inicial
        verify(subprocessoService, atLeastOnce()).save(argThat(s -> s.getSituacao() == SituacaoSubprocesso.DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO));
        verify(movimentacaoService).save(any(Movimentacao.class));
    }
}
