package sgc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import sgc.mapa.CopiaMapaService;
import sgc.mapa.Mapa;
import sgc.mapa.MapaRepository;
import sgc.mapa.UnidadeMapaRepository;
import sgc.notificacao.EmailNotificationService;
import sgc.notificacao.EmailTemplateService;
import sgc.processo.*;
import sgc.processo.dto.ProcessoDTO;
import sgc.sgrh.service.SgrhService;
import sgc.subprocesso.Movimentacao;
import sgc.subprocesso.MovimentacaoRepository;
import sgc.subprocesso.Subprocesso;
import sgc.subprocesso.SubprocessoRepository;
import sgc.unidade.Unidade;
import sgc.unidade.UnidadeRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class ProcessoServiceStartMappingTest {
    private ProcessoRepository processoRepository;
    private UnidadeRepository unidadeRepository;
    private UnidadeProcessoRepository unidadeProcessoRepository;
    private SubprocessoRepository subprocessoRepository;
    private MapaRepository mapaRepository;
    private MovimentacaoRepository movimentacaoRepository;
    private ApplicationEventPublisher publicadorDeEventos;
    private ProcessoMapper processoMapper;
    private ProcessoService processoService;

    @BeforeEach
    public void setup() {
        processoRepository = mock(ProcessoRepository.class);
        unidadeRepository = mock(UnidadeRepository.class);
        unidadeProcessoRepository = mock(UnidadeProcessoRepository.class);
        subprocessoRepository = mock(SubprocessoRepository.class);
        mapaRepository = mock(MapaRepository.class);
        movimentacaoRepository = mock(MovimentacaoRepository.class);
        UnidadeMapaRepository unidadeMapaRepository = mock(UnidadeMapaRepository.class);
        CopiaMapaService servicoDeCopiaDeMapa = mock(CopiaMapaService.class);
        publicadorDeEventos = mock(ApplicationEventPublisher.class);
        EmailNotificationService servicoDeEmail = mock(EmailNotificationService.class);
        EmailTemplateService servicoDeTemplateDeEmail = mock(EmailTemplateService.class);
        SgrhService sgrhService = mock(SgrhService.class);
        processoMapper = mock(ProcessoMapper.class);
        ProcessoDetalheMapper processoDetalheMapper = mock(ProcessoDetalheMapper.class);

        processoService = new ProcessoService(
                processoRepository,
                unidadeRepository,
                unidadeProcessoRepository,
                subprocessoRepository,
                mapaRepository,
                movimentacaoRepository,
                unidadeMapaRepository,
                servicoDeCopiaDeMapa,
                publicadorDeEventos,
                servicoDeEmail,
                servicoDeTemplateDeEmail,
                sgrhService,
                processoMapper,
                processoDetalheMapper
        );
    }

    @Test
    public void iniciarProcessoMapeamento_deveCriarSubprocessoMapaMovimentacao_ePublicarEvento_quandoFluxoNormal() {
        Long idProcesso = 10L;
        Long idUnidade = 1L;

        Processo processo = new Processo();
        processo.setCodigo(idProcesso);
        processo.setSituacao("CRIADO");
        processo.setTipo("MAPEAMENTO");

        Unidade unidade = new Unidade();
        unidade.setCodigo(idUnidade);
        unidade.setSigla("U1");
        unidade.setNome("Unidade 1");

        // Mocks
        when(processoRepository.findById(idProcesso)).thenReturn(Optional.of(processo));
        when(unidadeRepository.findById(idUnidade)).thenReturn(Optional.of(unidade));
        when(processoRepository.findBySituacao(anyString())).thenReturn(List.of()); // Simula que a unidade não está em outro processo
        when(mapaRepository.save(any(Mapa.class))).thenAnswer(inv -> {
            Mapa mapa = inv.getArgument(0);
            mapa.setCodigo(100L);
            return mapa;
        });
        when(subprocessoRepository.save(any(Subprocesso.class))).thenAnswer(inv -> {
            Subprocesso s = inv.getArgument(0);
            s.setCodigo(200L);
            return s;
        });
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenAnswer(inv -> {
            Movimentacao mv = inv.getArgument(0);
            mv.setCodigo(300L);
            return mv;
        });
        when(processoRepository.save(any(Processo.class))).thenAnswer(inv -> inv.getArgument(0));
        when(processoMapper.toDTO(any(Processo.class))).thenAnswer(invocation -> {
            Processo p = invocation.getArgument(0);
            return new ProcessoDTO(p.getCodigo(), p.getDataCriacao(), p.getDataFinalizacao(), p.getDataLimite(), p.getDescricao(), p.getSituacao(), p.getTipo());
        });

        // Execução
        var dto = processoService.iniciarProcessoMapeamento(idProcesso, List.of(idUnidade));

        // Asserções
        assertThat(dto).isNotNull();
        assertThat(dto.getCodigo()).isEqualTo(idProcesso);
        assertThat(dto.getSituacao()).isEqualToIgnoringCase("EM_ANDAMENTO");

        // Verificar saves e publicação de evento
        verify(unidadeProcessoRepository, times(1)).save(any(UnidadeProcesso.class));
        verify(mapaRepository, times(1)).save(any(Mapa.class));
        verify(subprocessoRepository, times(1)).save(any(Subprocesso.class));
        verify(movimentacaoRepository, times(1)).save(any(Movimentacao.class));
        verify(publicadorDeEventos, times(1)).publishEvent(any(ProcessoService.EventoDeProcessoIniciado.class));
    }

    @Test
    public void iniciarProcessoMapeamento_deveLancarExcecao_quandoProcessoNaoEstaNaSituacaoCriado() {
        Long idProcesso = 11L;
        Processo processo = new Processo();
        processo.setCodigo(idProcesso);
        processo.setSituacao("EM_ANDAMENTO"); // Inválido para iniciar

        when(processoRepository.findById(idProcesso)).thenReturn(Optional.of(processo));

        assertThatThrownBy(() -> processoService.iniciarProcessoMapeamento(idProcesso, List.of(1L)))
                .isInstanceOf(IllegalStateException.class);
    }
}