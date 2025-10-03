package sgc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import sgc.events.ProcessStartedEvent;
import sgc.model.*;
import sgc.repository.*;
import sgc.service.MapCopyService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para startMappingProcess.
 */
public class ProcessoServiceStartMappingTest {
    private ProcessoRepository processoRepository;
    private UnidadeRepository unidadeRepository;
    private UnidadeProcessoRepository unidadeProcessoRepository;
    private SubprocessoRepository subprocessoRepository;
    private MapaRepository mapaRepository;
    private MovimentacaoRepository movimentacaoRepository;
    private ApplicationEventPublisher publisher;

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
        MapCopyService mapCopyService = mock(MapCopyService.class);
        publisher = mock(ApplicationEventPublisher.class);
        
        processoService = new ProcessoService(
                processoRepository,
                unidadeRepository,
                unidadeProcessoRepository,
                subprocessoRepository,
                mapaRepository,
                movimentacaoRepository,
                unidadeMapaRepository,
                mapCopyService,
                publisher
        );
    }

    @Test
    public void startMappingProcess_shouldCreateSubprocessoMapaMovimentacao_andPublishEvent_whenHappyPath() {
        Long processoId = 10L;
        Long unidadeId = 1L;

        Processo proc = new Processo();
        proc.setCodigo(processoId);
        proc.setSituacao("CRIADO");
        proc.setTipo("MAPEAMENTO");

        Unidade u = new Unidade();
        u.setCodigo(unidadeId);
        u.setSigla("U1");
        u.setNome("Unidade 1");

        // mocks
        when(processoRepository.findById(processoId)).thenReturn(Optional.of(proc));
        when(unidadeRepository.findById(unidadeId)).thenReturn(Optional.of(u));
        when(unidadeProcessoRepository.findBySigla("U1")).thenReturn(List.of());
        when(mapaRepository.save(any(Mapa.class))).thenAnswer(inv -> {
            Mapa m = inv.getArgument(0);
            m.setCodigo(100L);
            return m;
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

        // execute
        var dto = processoService.startMappingProcess(processoId, List.of(unidadeId));

        // asserts
        assertThat(dto).isNotNull();
        assertThat(dto.getCodigo()).isEqualTo(processoId);
        assertThat(dto.getSituacao()).isEqualToIgnoringCase("EM_ANDAMENTO");

        // verify saves and event publication
        verify(unidadeProcessoRepository, times(1)).save(any(UnidadeProcesso.class));
        verify(mapaRepository, times(1)).save(any(Mapa.class));
        verify(subprocessoRepository, times(1)).save(any(Subprocesso.class));
        verify(movimentacaoRepository, times(1)).save(any(Movimentacao.class));
        verify(publisher, times(1)).publishEvent(any(ProcessStartedEvent.class));
    }

    @Test
    public void startMappingProcess_shouldThrow_whenProcessoNotCriado() {
        Long processoId = 11L;
        Processo proc = new Processo();
        proc.setCodigo(processoId);
        proc.setSituacao("EM_ANDAMENTO"); // inválido para iniciar

        when(processoRepository.findById(processoId)).thenReturn(Optional.of(proc));

        assertThatThrownBy(() -> processoService.startMappingProcess(processoId, List.of(1L)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void startMappingProcess_shouldThrow_whenUnidadeAlreadyInActiveProcess() {
        Long processoId = 12L;
        Long unidadeId = 2L;
        Processo proc = new Processo();
        proc.setCodigo(processoId);
        proc.setSituacao("CRIADO");
        proc.setTipo("MAPEAMENTO");

        Unidade u = new Unidade();
        u.setCodigo(unidadeId);
        u.setSigla("U2");

        // existente em unidade_processo apontando para processo ativo
        UnidadeProcesso existente = new UnidadeProcesso();
        existente.setProcessoCodigo(99L);
        when(unidadeProcessoRepository.findBySigla("U2")).thenReturn(List.of(existente));

        Processo ativo = new Processo();
        ativo.setCodigo(99L);
        ativo.setSituacao("EM_ANDAMENTO");

        when(processoRepository.findById(99L)).thenReturn(Optional.of(ativo));
        when(processoRepository.findById(processoId)).thenReturn(Optional.of(proc));
        when(unidadeRepository.findById(unidadeId)).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> processoService.startMappingProcess(processoId, List.of(unidadeId)))
                .isInstanceOf(IllegalStateException.class);
    }
}