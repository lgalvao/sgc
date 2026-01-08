package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
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
import sgc.organizacao.model.UnidadeMapaRepo;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.model.Processo;
import sgc.subprocesso.model.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubprocessoFactoryTest {

    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private SubprocessoMovimentacaoRepo movimentacaoRepo;
    @Mock
    private CopiaMapaService servicoDeCopiaDeMapa;
    @Mock
    private UnidadeMapaRepo unidadeMapaRepo;

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

        when(subprocessoRepo.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
        when(mapaRepo.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        factory.criarParaMapeamento(processo, List.of(unidade));

        verify(subprocessoRepo, times(1)).saveAll(anyList());
        verify(mapaRepo).saveAll(anyList());
        verify(movimentacaoRepo).saveAll(anyList());
    }

    @Test
    @DisplayName("criarParaMapeamento deve ignorar unidade nao elegivel")
    void criarParaMapeamento_IgnorarIneligivel() {
        Processo processo = new Processo();
        Unidade unidade = new Unidade();
        unidade.setTipo(TipoUnidade.INTERMEDIARIA);

        factory.criarParaMapeamento(processo, List.of(unidade));

        verifyNoInteractions(subprocessoRepo);
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

        when(subprocessoRepo.save(any(Subprocesso.class))).thenAnswer(i -> i.getArgument(0));

        Mapa mapaCopiado = new Mapa();
        mapaCopiado.setCodigo(200L);
        when(servicoDeCopiaDeMapa.copiarMapaParaUnidade(100L)).thenReturn(mapaCopiado);
        when(mapaRepo.save(any(Mapa.class))).thenAnswer(i -> i.getArgument(0));

        factory.criarParaRevisao(processo, unidade, unidadeMapa);

        verify(subprocessoRepo, times(1)).save(any(Subprocesso.class));
        verify(mapaRepo).save(any(Mapa.class));
        verify(movimentacaoRepo).save(any(Movimentacao.class));
    }

    @Test
    @DisplayName("criarParaRevisao erro se unidade nao tem mapa")
    void criarParaRevisao_SemMapa() {
        Processo processo = new Processo();
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSigla("U1");

        assertThrows(ErroProcesso.class, () -> factory.criarParaRevisao(processo, unidade, null));
    }

    @Test
    @DisplayName("criarParaRevisao erro se copia falha")
    void criarParaRevisao_FalhaCopia() {
        Processo processo = new Processo();
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSigla("U1");

        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(100L);
        UnidadeMapa unidadeMapa = new UnidadeMapa();
        unidadeMapa.setMapaVigente(mapaVigente);

        when(subprocessoRepo.save(any(Subprocesso.class))).thenAnswer(i -> i.getArgument(0));

        when(servicoDeCopiaDeMapa.copiarMapaParaUnidade(100L)).thenReturn(null);

        assertThrows(ErroProcesso.class, () -> factory.criarParaRevisao(processo, unidade, unidadeMapa));
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

        when(subprocessoRepo.save(any(Subprocesso.class))).thenAnswer(i -> i.getArgument(0));

        Mapa mapaCopiado = new Mapa();
        mapaCopiado.setCodigo(200L);
        when(servicoDeCopiaDeMapa.copiarMapaParaUnidade(100L)).thenReturn(mapaCopiado);
        when(mapaRepo.save(any(Mapa.class))).thenAnswer(i -> i.getArgument(0));

        factory.criarParaDiagnostico(processo, unidade, unidadeMapa);

        // Verifica situacao inicial
        verify(subprocessoRepo, atLeastOnce()).save(argThat(s -> s.getSituacao() == SituacaoSubprocesso.DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO));
        verify(movimentacaoRepo).save(any(Movimentacao.class));
    }

    @Test
    @DisplayName("criarParaDiagnostico deve lançar exceção quando unidadeMapa é null")
    void criarParaDiagnostico_UnidadeMapaNull() {
        Processo processo = new Processo();
        Unidade unidade = new Unidade();
        unidade.setSigla("U1");

        assertThatThrownBy(() -> factory.criarParaDiagnostico(processo, unidade, null))
                .isInstanceOf(ErroProcesso.class)
                .hasMessageContaining("não possui mapa vigente");
    }
}
