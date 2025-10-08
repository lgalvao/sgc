package sgc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.mapa.CopiaMapaServico;
import sgc.mapa.MapaRepository;
import sgc.mapa.UnidadeMapaRepository;
import sgc.notificacao.ServicoNotificacaoEmail;
import sgc.notificacao.ServicoDeTemplateDeEmail;
import sgc.processo.*;
import sgc.sgrh.service.SgrhService;
import sgc.subprocesso.MovimentacaoRepository;
import sgc.subprocesso.Subprocesso;
import sgc.subprocesso.SubprocessoRepository;
import sgc.unidade.Unidade;
import sgc.unidade.UnidadeRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServicoProcessoFinalizarTest {

    @Mock
    private RepositorioProcesso repositorioProcesso;
    @Mock
    private SubprocessoRepository subprocessoRepository;
    @Mock
    private UnidadeMapaRepository unidadeMapaRepository;
    @Mock
    private ApplicationEventPublisher publicadorDeEventos;
    @Mock
    private ServicoNotificacaoEmail servicoNotificacaoEmail;
    @Mock
    private ServicoDeTemplateDeEmail servicoDeTemplateDeEmail;
    @Mock
    private SgrhService sgrhService;
    @Mock
    private ProcessoMapper processoMapper;

    @Mock private UnidadeRepository unidadeRepository;
    @Mock private UnidadeProcessoRepository unidadeProcessoRepository;
    @Mock private MapaRepository mapaRepository;
    @Mock private MovimentacaoRepository movimentacaoRepository;
    @Mock private CopiaMapaServico servicoDeCopiaDeMapa;
    @Mock private ProcessoDetalheMapper processoDetalheMapper;


    @InjectMocks
    private ServicoProcesso servicoProcesso;

    private Processo processo;
    private Subprocesso subprocessoHomologado;
    private Subprocesso subprocessoPendente;
    private Unidade unidade;

    @BeforeEach
    void setUp() {
        processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("Processo de Teste");
        processo.setSituacao("EM_ANDAMENTO");

        unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setSigla("TEST");

        subprocessoHomologado = new Subprocesso();
        subprocessoHomologado.setCodigo(100L);
        subprocessoHomologado.setUnidade(unidade);
        subprocessoHomologado.setSituacaoId("MAPA_HOMOLOGADO");

        subprocessoPendente = new Subprocesso();
        subprocessoPendente.setCodigo(101L);
        subprocessoPendente.setUnidade(unidade);
        subprocessoPendente.setSituacaoId("MAPA_VALIDADO");
    }

    @Test
    @DisplayName("finalizar deve lançar ProcessoErro quando subprocessos não estão homologados")
    void finalizar_deveLancarErroProcesso_quandoSubprocessosNaoEstaoHomologados() {
        when(repositorioProcesso.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepository.findByProcessoCodigo(1L))
            .thenReturn(List.of(subprocessoHomologado, subprocessoPendente));

        assertThatThrownBy(() -> servicoProcesso.finalizar(1L))
            .isInstanceOf(ProcessoErro.class)
            .hasMessageContaining("Unidades pendentes de homologação:");

        verify(repositorioProcesso, never()).save(any(Processo.class));
        verify(publicadorDeEventos, never()).publishEvent(any(ProcessoFinalizadoEvento.class));
    }

    @Test
    @DisplayName("finalizar deve atualizar status e tornar mapas vigentes quando todos os subprocessos estão homologados")
    void finalizar_deveAtualizarStatusETornarMapasVigentes_quandoTodosSubprocessosEstaoHomologados() {
        // Arrange
        subprocessoHomologado.setMapa(new sgc.mapa.Mapa());
        when(repositorioProcesso.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepository.findByProcessoCodigo(1L)).thenReturn(List.of(subprocessoHomologado));
        when(repositorioProcesso.save(any(Processo.class))).thenReturn(processo);
        when(unidadeMapaRepository.findByUnidadeCodigo(anyLong())).thenReturn(Optional.empty());
        when(processoMapper.toDTO(any(Processo.class))).thenReturn(new sgc.processo.dto.ProcessoDTO());

        // Act
        servicoProcesso.finalizar(1L);

        // Assert
        verify(repositorioProcesso, times(1)).save(processo);
        verify(unidadeMapaRepository, times(1)).save(any(sgc.mapa.UnidadeMapa.class));
        verify(publicadorDeEventos, times(1)).publishEvent(any(ProcessoFinalizadoEvento.class));
    }
}