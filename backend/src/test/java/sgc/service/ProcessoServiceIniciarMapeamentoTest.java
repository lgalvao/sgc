package sgc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import sgc.mapa.CopiaMapaService;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.mapa.modelo.UnidadeMapaRepo;
import sgc.notificacao.NotificacaoModeloEmailService;
import sgc.notificacao.NotificacaoService;
import sgc.processo.ProcessoService;
import sgc.processo.SituacaoProcesso;
import sgc.processo.dto.ProcessoDetalheMapperCustom;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.dto.ProcessoMapper;
import sgc.processo.eventos.ProcessoIniciadoEvento;
import sgc.processo.modelo.*;
import sgc.sgrh.SgrhService;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.processo.ProcessoIniciacaoService;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@DisplayName("Testes para o início de mapeamento no ProcessoService")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProcessoServiceIniciarMapeamentoTest {
    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private UnidadeRepo unidadeRepo;
    @Mock
    private UnidadeProcessoRepo unidadeProcessoRepo;
    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private MovimentacaoRepo movimentacaoRepo;
    @Mock
    private ApplicationEventPublisher publicadorDeEventos;
    @Mock
    private UnidadeMapaRepo unidadeMapaRepo;
    @Mock
    private CopiaMapaService servicoDeCopiaDeMapa;

    @InjectMocks
    private ProcessoIniciacaoService processoIniciacaoService;

    @Test
    @DisplayName("iniciarProcessoMapeamento deve criar subprocesso, mapa, movimentação e publicar evento no fluxo feliz")
    public void iniciarProcessoMapeamento_deveCriarSubprocessoMapaMovimentacao_ePublicarEvento_quandoFluxoNormal() {
        Long idProcesso = 10L;
        Long idUnidade = 1L;

        Processo processo = new Processo();
        processo.setCodigo(idProcesso);
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setTipo(TipoProcesso.MAPEAMENTO);

        Unidade unidade = new Unidade();
        unidade.setCodigo(idUnidade);
        unidade.setSigla("U1");
        unidade.setNome("Unidade 1");

        // Mocks
        when(processoRepo.findById(idProcesso)).thenReturn(Optional.of(processo));
        when(unidadeRepo.findById(idUnidade)).thenReturn(Optional.of(unidade));
        when(unidadeProcessoRepo.findUnidadesInProcessosAtivos(anyList())).thenReturn(List.of());

        when(mapaRepo.save(any(Mapa.class))).thenAnswer(inv -> {
            Mapa m = inv.getArgument(0);
            m.setCodigo(100L);
            return m;
        });
        when(subprocessoRepo.save(any(Subprocesso.class))).thenAnswer(inv -> {
            Subprocesso s = inv.getArgument(0);
            s.setCodigo(200L);
            return s;
        });
        when(movimentacaoRepo.save(any(Movimentacao.class))).thenAnswer(inv -> {
            Movimentacao mv = inv.getArgument(0);
            mv.setCodigo(300L);
            return mv;
        });
        when(processoRepo.save(any(Processo.class))).thenAnswer(inv -> inv.getArgument(0));

        // Execução
        processoIniciacaoService.iniciarProcessoMapeamento(idProcesso, List.of(idUnidade));

        // Verificar saves e publicação de evento
        verify(processoRepo, times(1)).save(any(Processo.class));
        verify(unidadeProcessoRepo, times(1)).save(any(UnidadeProcesso.class));
        verify(publicadorDeEventos, times(1)).publishEvent(any(ProcessoIniciadoEvento.class));
    }

    @Test
    @DisplayName("iniciarProcessoMapeamento deve lançar exceção quando o processo não está na situação CRIADO")
    public void iniciarProcessoMapeamento_deveLancarExcecao_quandoProcessoNaoEstaNaSituacaoCriado() {
        Long idProcesso = 11L;
        Processo processo = new Processo();
        processo.setCodigo(idProcesso);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

        when(processoRepo.findById(idProcesso)).thenReturn(Optional.of(processo));

        assertThatThrownBy(() -> processoIniciacaoService.iniciarProcessoMapeamento(idProcesso, List.of(1L)))
                .isInstanceOf(IllegalStateException.class);
    }
}
