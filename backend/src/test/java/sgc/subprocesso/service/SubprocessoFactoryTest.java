package sgc.subprocesso.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.mapa.internal.model.Mapa;
import sgc.mapa.internal.model.MapaRepo;
import sgc.mapa.internal.service.CopiaMapaService;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.model.Processo;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoMovimentacaoRepo;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.internal.model.TipoUnidade;
import sgc.unidade.internal.model.Unidade;
import sgc.unidade.internal.model.UnidadeMapa;
import sgc.unidade.internal.model.UnidadeMapaRepo;

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

        when(subprocessoRepo.save(any(Subprocesso.class))).thenAnswer(i -> i.getArgument(0));
        when(mapaRepo.save(any(Mapa.class))).thenAnswer(i -> i.getArgument(0));

        factory.criarParaMapeamento(processo, unidade);

        verify(subprocessoRepo, times(2)).save(any(Subprocesso.class));
        verify(mapaRepo).save(any(Mapa.class));
        verify(movimentacaoRepo).save(any(Movimentacao.class));
    }

    @Test
    @DisplayName("criarParaMapeamento deve ignorar unidade nao elegivel")
    void criarParaMapeamento_IgnorarIneligivel() {
        Processo processo = new Processo();
        Unidade unidade = new Unidade();
        unidade.setTipo(TipoUnidade.INTERMEDIARIA);

        factory.criarParaMapeamento(processo, unidade);

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

        when(unidadeMapaRepo.findById(1L)).thenReturn(Optional.of(unidadeMapa));
        when(subprocessoRepo.save(any(Subprocesso.class))).thenAnswer(i -> i.getArgument(0));

        Mapa mapaCopiado = new Mapa();
        mapaCopiado.setCodigo(200L);
        when(servicoDeCopiaDeMapa.copiarMapaParaUnidade(100L, 1L)).thenReturn(mapaCopiado);
        when(mapaRepo.save(any(Mapa.class))).thenAnswer(i -> i.getArgument(0));

        factory.criarParaRevisao(processo, unidade);

        verify(subprocessoRepo, times(2)).save(any(Subprocesso.class));
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

        when(unidadeMapaRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ErroProcesso.class, () -> factory.criarParaRevisao(processo, unidade));
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

        when(unidadeMapaRepo.findById(1L)).thenReturn(Optional.of(unidadeMapa));
        when(subprocessoRepo.save(any(Subprocesso.class))).thenAnswer(i -> i.getArgument(0));

        when(servicoDeCopiaDeMapa.copiarMapaParaUnidade(100L, 1L)).thenReturn(null);

        assertThrows(ErroProcesso.class, () -> factory.criarParaRevisao(processo, unidade));
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

        when(unidadeMapaRepo.findById(1L)).thenReturn(Optional.of(unidadeMapa));
        when(subprocessoRepo.save(any(Subprocesso.class))).thenAnswer(i -> i.getArgument(0));

        Mapa mapaCopiado = new Mapa();
        mapaCopiado.setCodigo(200L);
        when(servicoDeCopiaDeMapa.copiarMapaParaUnidade(100L, 1L)).thenReturn(mapaCopiado);
        when(mapaRepo.save(any(Mapa.class))).thenAnswer(i -> i.getArgument(0));

        factory.criarParaDiagnostico(processo, unidade);

        // Verifica situacao inicial
        verify(subprocessoRepo, atLeastOnce()).save(argThat(s -> s.getSituacao() == SituacaoSubprocesso.DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO));
        verify(movimentacaoRepo).save(any(Movimentacao.class));
    }
}
