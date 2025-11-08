package sgc.processo;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import sgc.processo.dto.CriarProcessoReq;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.dto.mappers.ProcessoMapper;
import sgc.processo.eventos.EventoProcessoCriado;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoService;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProcessoServiceTest {
    @Mock
    private ProcessoRepo processoRepo;

    @Mock
    private UnidadeRepo unidadeRepo;

    @Mock
    private ApplicationEventPublisher publicadorDeEventos;

    @Mock
    private ProcessoMapper processoMapper;

    @InjectMocks
    private ProcessoService processoService;

    @Test
    public void criar_devePersistirERetornarDTO_quandoRequisicaoForValida() {
        var requisicao = new CriarProcessoReq("Processo de teste", TipoProcesso.MAPEAMENTO, LocalDateTime.now().plusDays(10), List.of(1L, 2L));

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

        when(processoMapper.toDto(any(Processo.class))).thenAnswer(invocation -> {
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

        verify(publicadorDeEventos, times(1)).publishEvent(any(EventoProcessoCriado.class));
    }

    @Test
    public void criar_deveLancarExcecaoDeViolacaoDeRestricao_quandoDescricaoEstiverEmBranco() {
        var requisicao = new CriarProcessoReq("   ", TipoProcesso.MAPEAMENTO, null, List.of(1L));

        assertThatThrownBy(() -> processoService.criar(requisicao))
                .isInstanceOf(ConstraintViolationException.class);

        verifyNoInteractions(processoRepo);
        verifyNoInteractions(publicadorDeEventos);
    }
}
