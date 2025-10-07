package sgc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.mapa.CopiaMapaService;
import sgc.mapa.MapaRepository;
import sgc.mapa.UnidadeMapaRepository;
import sgc.notificacao.EmailNotificationService;
import sgc.notificacao.EmailTemplateService;
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
public class ProcessoServiceFinalizeTest {

    @Mock
    private ProcessoRepository processoRepository;
    @Mock
    private SubprocessoRepository subprocessoRepository;
    @Mock
    private UnidadeMapaRepository unidadeMapaRepository;
    @Mock
    private ApplicationEventPublisher publicadorDeEventos;
    @Mock
    private EmailNotificationService servicoDeEmail;
    @Mock
    private EmailTemplateService servicoDeTemplateDeEmail;
    @Mock
    private SgrhService sgrhService;
    @Mock
    private ProcessoMapper processoMapper;

    // Mocks para dependências não usadas diretamente em finalizarProcesso, mas exigidas pelo construtor
    @Mock private UnidadeRepository unidadeRepository;
    @Mock private UnidadeProcessoRepository unidadeProcessoRepository;
    @Mock private MapaRepository mapaRepository;
    @Mock private MovimentacaoRepository movimentacaoRepository;
    @Mock private CopiaMapaService servicoDeCopiaDeMapa;
    @Mock private ProcessoDetalheMapper processoDetalheMapper;


    @InjectMocks
    private ProcessoService processoService;

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
        subprocessoPendente.setSituacaoId("MAPA_VALIDADO"); // Não homologado
    }

    @Test
    void finalizarProcesso_deveLancarErroProcesso_quandoSubprocessosNaoEstaoHomologados() {
        when(processoRepository.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepository.findByProcessoCodigo(1L))
            .thenReturn(List.of(subprocessoHomologado, subprocessoPendente));

        assertThatThrownBy(() -> processoService.finalizarProcesso(1L))
            .isInstanceOf(ErroProcesso.class)
            .hasMessageContaining("Unidades pendentes:")
            .hasMessageContaining("TEST (Situação: MAPA_VALIDADO)");

        verify(processoRepository, never()).save(any(Processo.class));
        verify(publicadorDeEventos, never()).publishEvent(any(ProcessoService.EventoDeProcessoFinalizado.class));
    }

    @Test
    void finalizarProcesso_deveAtualizarStatusETornarMapasVigentes_quandoTodosSubprocessosEstaoHomologados() {
        // Arrange
        subprocessoHomologado.setMapa(new sgc.mapa.Mapa()); // Garante que o mapa existe
        when(processoRepository.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepository.findByProcessoCodigo(1L)).thenReturn(List.of(subprocessoHomologado));
        when(processoRepository.save(any(Processo.class))).thenReturn(processo);
        when(unidadeMapaRepository.findByUnidadeCodigo(anyLong())).thenReturn(Optional.empty()); // Assume que não há mapa vigente
        when(processoMapper.toDTO(any(Processo.class))).thenReturn(new sgc.processo.dto.ProcessoDTO());

        // Act
        processoService.finalizarProcesso(1L);

        // Assert
        verify(processoRepository, times(1)).save(processo);
        verify(unidadeMapaRepository, times(1)).save(any(sgc.mapa.UnidadeMapa.class));
        verify(publicadorDeEventos, times(1)).publishEvent(any(ProcessoService.EventoDeProcessoFinalizado.class));
        // Podemos adicionar verificações mais detalhadas para o envio de notificações, se necessário
    }
}