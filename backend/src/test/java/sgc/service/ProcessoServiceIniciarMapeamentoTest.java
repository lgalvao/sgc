package sgc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import sgc.comum.enums.SituacaoProcesso;
import sgc.mapa.CopiaMapaService;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.mapa.modelo.UnidadeMapaRepo;
import sgc.notificacao.NotificacaoEmailService;
import sgc.notificacao.NotificacaoTemplateEmailService;
import sgc.processo.ProcessoService;
import sgc.processo.dto.ProcessoDetalheMapperCustom;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.dto.ProcessoMapper;
import sgc.processo.eventos.ProcessoIniciadoEvento;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.UnidadeProcesso;
import sgc.processo.modelo.UnidadeProcessoRepo;
import sgc.sgrh.SgrhService;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import sgc.processo.enums.TipoProcesso;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("Testes para o início de mapeamento no ProcessoService")
public class ProcessoServiceIniciarMapeamentoTest {
    private ProcessoRepo processoRepo;
    private UnidadeRepo unidadeRepo;
    private UnidadeProcessoRepo unidadeProcessoRepo;
    private SubprocessoRepo subprocessoRepo;
    private MapaRepo mapaRepo;
    private MovimentacaoRepo movimentacaoRepo;
    private ApplicationEventPublisher publicadorDeEventos;
    private ProcessoMapper processoMapper;
    private ProcessoService processoService;

    @BeforeEach
    public void setup() {
        processoRepo = mock(ProcessoRepo.class);
        unidadeRepo = mock(UnidadeRepo.class);
        unidadeProcessoRepo = mock(UnidadeProcessoRepo.class);
        subprocessoRepo = mock(SubprocessoRepo.class);
        mapaRepo = mock(MapaRepo.class);
        movimentacaoRepo = mock(MovimentacaoRepo.class);
        UnidadeMapaRepo unidadeMapaRepo = mock(UnidadeMapaRepo.class);
        CopiaMapaService servicoDeCopiaDeMapa = mock(CopiaMapaService.class);
        publicadorDeEventos = mock(ApplicationEventPublisher.class);
        NotificacaoEmailService servicoNotificacaoEmail = mock(NotificacaoEmailService.class);
        NotificacaoTemplateEmailService notificacaoTemplateEmailService = mock(NotificacaoTemplateEmailService.class);
        SgrhService sgrhService = mock(SgrhService.class);
        processoMapper = mock(ProcessoMapper.class);
        ProcessoDetalheMapperCustom processoDetalheMapperCustom = mock(ProcessoDetalheMapperCustom.class);

        processoService = new ProcessoService(
                processoRepo,
                unidadeRepo,
                unidadeProcessoRepo,
                subprocessoRepo,
                mapaRepo,
                movimentacaoRepo,
                unidadeMapaRepo,
                servicoDeCopiaDeMapa,
                publicadorDeEventos,
                servicoNotificacaoEmail,
                notificacaoTemplateEmailService,
                sgrhService,
                processoMapper,
                processoDetalheMapperCustom
        );
    }

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
        when(processoMapper.toDTO(any(Processo.class))).thenAnswer(invocation -> {
            Processo p = invocation.getArgument(0);
            return ProcessoDto.builder()
                .codigo(p.getCodigo())
                .dataCriacao(p.getDataCriacao())
                .dataFinalizacao(p.getDataFinalizacao())
                .dataLimite(p.getDataLimite())
                .descricao(p.getDescricao())
                .situacao(p.getSituacao())
                .tipo(p.getTipo().name())
                .build();
        });

        // Execução
        var dto = processoService.iniciarProcessoMapeamento(idProcesso, List.of(idUnidade));

        // Asserções
        assertThat(dto).isNotNull();
        assertThat(dto.getCodigo()).isEqualTo(idProcesso);
        assertThat(dto.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);

        // Verificar saves e publicação de evento
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

        assertThatThrownBy(() -> processoService.iniciarProcessoMapeamento(idProcesso, List.of(1L)))
                .isInstanceOf(IllegalStateException.class);
    }
}
