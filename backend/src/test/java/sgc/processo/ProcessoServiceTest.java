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

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ProcessoService, cobrindo o fluxo de criação e validações.
 */
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProcessoServiceTest {
    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private UnidadeRepo unidadeRepo;
    @Mock
    private UnidadeProcessoRepo unidadeProcessoRepo;
    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private ApplicationEventPublisher publicadorDeEventos;
    @Mock
    private ProcessoMapper processoMapper;
    @Mock
    private ProcessoDetalheMapperCustom processoDetalheMapperCustom;
    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private MovimentacaoRepo movimentacaoRepo;
    @Mock
    private UnidadeMapaRepo unidadeMapaRepo;
    @Mock
    private CopiaMapaService copiaMapaService;
    @Mock
    private ProcessoNotificacaoService processoNotificacaoService;

    @InjectMocks
    private ProcessoService processoService;

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