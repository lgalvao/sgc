package sgc.processo;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import sgc.mapa.CopiaMapaService;
import sgc.mapa.modelo.MapaRepo;
import sgc.mapa.modelo.UnidadeMapaRepo;
import sgc.processo.dto.CriarProcessoReq;
import sgc.processo.dto.ProcessoDetalheMapperCustom;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.dto.ProcessoMapper;
import sgc.processo.eventos.ProcessoCriadoEvento;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.TipoProcesso;
import sgc.processo.modelo.UnidadeProcessoRepo;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ProcessoService, cobrindo o fluxo de criação e validações.
 */
public class ProcessoServiceTest {
    private ProcessoRepo processoRepo;
    private UnidadeRepo unidadeRepo;
    private ApplicationEventPublisher publicadorDeEventos;
    private ProcessoMapper processoMapper;

    private ProcessoService processoService;

    @BeforeEach
    public void setup() {
        processoRepo = mock(ProcessoRepo.class);
        unidadeRepo = mock(UnidadeRepo.class);
        UnidadeProcessoRepo unidadeProcessoRepo = mock(UnidadeProcessoRepo.class);
        SubprocessoRepo subprocessoRepo = mock(SubprocessoRepo.class);
        MapaRepo mapaRepo = mock(MapaRepo.class);
        MovimentacaoRepo movimentacaoRepo = mock(MovimentacaoRepo.class);
        UnidadeMapaRepo unidadeMapaRepo = mock(UnidadeMapaRepo.class);
        CopiaMapaService servicoDeCopiaDeMapa = mock(CopiaMapaService.class);
        publicadorDeEventos = mock(ApplicationEventPublisher.class);
        processoMapper = mock(ProcessoMapper.class);
        ProcessoDetalheMapperCustom processoDetalheMapperCustom = mock(ProcessoDetalheMapperCustom.class);
        ProcessoIniciacaoService processoIniciacaoService = mock(ProcessoIniciacaoService.class);
        ProcessoFinalizacaoService processoFinalizacaoService = mock(ProcessoFinalizacaoService.class);

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
                mock(sgc.processo.ProcessoNotificacaoService.class),
                processoIniciacaoService,
                processoFinalizacaoService,
                processoMapper,
                processoDetalheMapperCustom
        );
    }

    @Test
    public void criar_devePersistirERetornarDTO_quandoRequisicaoForValida() {
        var requisicao = new CriarProcessoReq("Processo de teste", TipoProcesso.MAPEAMENTO.name(), LocalDateTime.now().plusDays(10), List.of(1L, 2L));

        Unidade u1 = new Unidade();
        u1.setCodigo(1L);
        u1.setNome("Unidade 1");
        u1.setSigla("U1");
        Unidade u2 = new Unidade();
        u2.setCodigo(2L);
        u2.setNome("Unidade 2");
        u2.setSigla("U2");

        when(unidadeRepo.findById(1L)).thenReturn(Optional.of(u1));
        when(unidadeRepo.findById(2L)).thenReturn(Optional.of(u2));

        when(processoRepo.save(any(Processo.class))).thenAnswer(invocation -> {
            Processo p = invocation.getArgument(0);
            p.setCodigo(123L);
            return p;
        });

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

        ProcessoDto dto = processoService.criar(requisicao);

        assertThat(dto).isNotNull();
        assertThat(dto.getCodigo()).isEqualTo(123L);
        assertThat(dto.getDescricao()).isEqualTo("Processo de teste");
        assertThat(dto.getTipo()).isEqualTo(TipoProcesso.MAPEAMENTO.name());
        assertThat(dto.getSituacao()).isEqualTo(SituacaoProcesso.CRIADO);

        verify(publicadorDeEventos, times(1)).publishEvent(any(ProcessoCriadoEvento.class));
    }

    @Test
    public void criar_deveLancarExcecaoDeViolacaoDeRestricao_quandoDescricaoEstiverEmBranco() {
        var requisicao = new CriarProcessoReq("   ", TipoProcesso.MAPEAMENTO.name(), null, List.of(1L));

        assertThatThrownBy(() -> processoService.criar(requisicao))
                .isInstanceOf(ConstraintViolationException.class);

        verifyNoInteractions(processoRepo);
        verifyNoInteractions(publicadorDeEventos);
    }
}