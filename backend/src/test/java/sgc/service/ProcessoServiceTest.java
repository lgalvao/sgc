package sgc.service;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import sgc.mapa.CopiaMapaService;
import sgc.mapa.MapaRepository;
import sgc.mapa.UnidadeMapaRepository;
import sgc.notificacao.EmailNotificationService;
import sgc.notificacao.EmailTemplateService;
import sgc.processo.*;
import sgc.processo.dto.ProcessoDTO;
import sgc.processo.dto.ReqCriarProcesso;
import sgc.sgrh.service.SgrhService;
import sgc.subprocesso.MovimentacaoRepository;
import sgc.subprocesso.SubprocessoRepository;
import sgc.unidade.Unidade;
import sgc.unidade.UnidadeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ProcessoService, cobrindo o fluxo de criação e validações.
 */
public class ProcessoServiceTest {
    private ProcessoRepository processoRepository;
    private UnidadeRepository unidadeRepository;
    private ApplicationEventPublisher publicadorDeEventos;
    private ProcessoMapper processoMapper;

    private ProcessoService processoService;

    @BeforeEach
    public void setup() {
        processoRepository = mock(ProcessoRepository.class);
        unidadeRepository = mock(UnidadeRepository.class);
        UnidadeProcessoRepository unidadeProcessoRepository = mock(UnidadeProcessoRepository.class);
        SubprocessoRepository subprocessoRepository = mock(SubprocessoRepository.class);
        MapaRepository mapaRepository = mock(MapaRepository.class);
        MovimentacaoRepository movimentacaoRepository = mock(MovimentacaoRepository.class);
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
    public void criar_devePersistirERetornarDTO_quandoRequisicaoForValida() {
        ReqCriarProcesso requisicao = new ReqCriarProcesso();
        requisicao.setDescricao("Processo de teste");
        requisicao.setTipo("MAPEAMENTO");
        requisicao.setDataLimiteEtapa1(LocalDate.now().plusDays(10));
        requisicao.setUnidades(List.of(1L, 2L));

        // Preparar mocks: as unidades existem
        Unidade unidade1 = new Unidade();
        unidade1.setCodigo(1L);
        unidade1.setNome("Unidade 1");
        unidade1.setSigla("U1");
        Unidade unidade2 = new Unidade();
        unidade2.setCodigo(2L);
        unidade2.setNome("Unidade 2");
        unidade2.setSigla("U2");

        when(unidadeRepository.findById(1L)).thenReturn(Optional.of(unidade1));
        when(unidadeRepository.findById(2L)).thenReturn(Optional.of(unidade2));

        // Quando salvar o processo, retornar com o código gerado
        when(processoRepository.save(any(Processo.class))).thenAnswer(invocation -> {
            Processo processo = invocation.getArgument(0);
            processo.setCodigo(123L);
            return processo;
        });

        when(processoMapper.toDTO(any(Processo.class))).thenAnswer(invocation -> {
            Processo processo = invocation.getArgument(0);
            return new ProcessoDTO(processo.getCodigo(), processo.getDataCriacao(), processo.getDataFinalizacao(), processo.getDataLimite(), processo.getDescricao(), processo.getSituacao(), processo.getTipo());
        });

        ProcessoDTO dto = processoService.criar(requisicao);

        assertThat(dto).isNotNull();
        assertThat(dto.getCodigo()).isEqualTo(123L);
        assertThat(dto.getDescricao()).isEqualTo("Processo de teste");
        assertThat(dto.getTipo()).isEqualTo("MAPEAMENTO");
        assertThat(dto.getSituacao()).isEqualTo("CRIADO");

        // Verificar que o evento de criação foi publicado
        verify(publicadorDeEventos, times(1)).publishEvent(any(ProcessoService.EventoDeProcessoCriado.class));
    }

    @Test
    public void criar_deveLancarExcecaoDeViolacaoDeRestricao_quandoDescricaoEstiverEmBranco() {
        ReqCriarProcesso requisicao = new ReqCriarProcesso();
        requisicao.setDescricao("   "); // em branco
        requisicao.setTipo("MAPEAMENTO");
        requisicao.setUnidades(List.of(1L));

        assertThatThrownBy(() -> processoService.criar(requisicao))
                .isInstanceOf(ConstraintViolationException.class);

        verifyNoInteractions(processoRepository);
        verifyNoInteractions(publicadorDeEventos);
    }
}