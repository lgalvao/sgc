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
 * Unit tests for startRevisionProcess in ProcessoService (uses MapCopyService).
 */
public class ProcessoServiceStartRevisionTest {

    private ProcessoRepository processoRepository;
    private UnidadeRepository unidadeRepository;
    private UnidadeProcessoRepository unidadeProcessoRepository;
    private SubprocessoRepository subprocessoRepository;
    private MovimentacaoRepository movimentacaoRepository;
    private UnidadeMapaRepository unidadeMapaRepository;
    private MapCopyService mapCopyService;
    private ApplicationEventPublisher publisher;

    private ProcessoService processoService;

    @BeforeEach
    public void setup() {
        processoRepository = mock(ProcessoRepository.class);
        unidadeRepository = mock(UnidadeRepository.class);
        unidadeProcessoRepository = mock(UnidadeProcessoRepository.class);
        subprocessoRepository = mock(SubprocessoRepository.class);
        MapaRepository mapaRepository = mock(MapaRepository.class);
        movimentacaoRepository = mock(MovimentacaoRepository.class);
        unidadeMapaRepository = mock(UnidadeMapaRepository.class);
        mapCopyService = mock(MapCopyService.class);
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
    public void startRevisionProcess_shouldCopyMapaCreateSubprocessoMovimentacao_andPublishEvent_whenHappyPath() {
        Long processoId = 20L;
        Long unidadeId = 2L;
        Long sourceMapaId = 400L;
        Long novoMapaId = 500L;

        Processo proc = new Processo();
        proc.setCodigo(processoId);
        proc.setSituacao("CRIADO");
        proc.setTipo("REVISAO");

        Unidade unidade = new Unidade();
        unidade.setCodigo(unidadeId);
        unidade.setSigla("UX");
        unidade.setNome("Unidade X");

        Mapa sourceMapa = new Mapa();
        sourceMapa.setCodigo(sourceMapaId);

        UnidadeMapa um = new UnidadeMapa();
        UnidadeMapa.Id id = new UnidadeMapa.Id();
        id.setUnidadeCodigo(unidadeId);
        id.setMapaVigenteCodigo(sourceMapaId);
        um.setId(id);
        um.setUnidade(unidade);
        um.setMapaVigente(sourceMapa);

        // mocks
        when(processoRepository.findById(processoId)).thenReturn(Optional.of(proc));
        when(unidadeRepository.findById(unidadeId)).thenReturn(Optional.of(unidade));
        when(unidadeMapaRepository.findByUnidadeCodigo(unidadeId)).thenReturn(Optional.of(um));

        Mapa novoMapa = new Mapa();
        novoMapa.setCodigo(novoMapaId);
        when(mapCopyService.copyMapForUnit(sourceMapaId, unidadeId)).thenReturn(novoMapa);

        when(unidadeProcessoRepository.save(any(UnidadeProcesso.class))).thenAnswer(inv -> {
            UnidadeProcesso up = inv.getArgument(0);
            up.setCodigo(700L);
            return up;
        });
        when(subprocessoRepository.save(any(Subprocesso.class))).thenAnswer(inv -> {
            Subprocesso s = inv.getArgument(0);
            s.setCodigo(800L);
            return s;
        });
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenAnswer(inv -> {
            Movimentacao mv = inv.getArgument(0);
            mv.setCodigo(900L);
            return mv;
        });
        when(processoRepository.save(any(Processo.class))).thenAnswer(inv -> inv.getArgument(0));

        // execute
        var dto = processoService.startRevisionProcess(processoId, List.of(unidadeId));

        // asserts
        assertThat(dto).isNotNull();
        assertThat(dto.getCodigo()).isEqualTo(processoId);
        assertThat(dto.getSituacao()).isEqualToIgnoringCase("EM_ANDAMENTO");

        // verify that map copy was invoked and subprocesso created with novo mapa
        verify(mapCopyService, times(1)).copyMapForUnit(sourceMapaId, unidadeId);
        verify(unidadeProcessoRepository, times(1)).save(any(UnidadeProcesso.class));
        verify(subprocessoRepository, times(1)).save(any(Subprocesso.class));
        verify(movimentacao_repository_safe(), times(1)).save(any(Movimentacao.class)); // helper to avoid import collision
        verify(publisher, times(1)).publishEvent(any(ProcessStartedEvent.class));
    }

    // workaround method to reference movimentacaoRepository verify without naming collision in static imports
    private MovimentacaoRepository movimentacao_repository_safe() {
        return movimentacaoRepository;
    }

    @Test
    public void startRevisionProcess_shouldThrow_whenMapaVigenteNaoEncontrado() {
        Long processoId = 21L;
        Long unidadeId = 3L;

        Processo proc = new Processo();
        proc.setCodigo(processoId);
        proc.setSituacao("CRIADO");
        proc.setTipo("REVISAO");

        Unidade unidade = new Unidade();
        unidade.setCodigo(unidadeId);

        when(processoRepository.findById(processoId)).thenReturn(Optional.of(proc));
        when(unidadeRepository.findById(unidadeId)).thenReturn(Optional.of(unidade));
        when(unidadeMapaRepository.findByUnidadeCodigo(unidadeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> processoService.startRevisionProcess(processoId, List.of(unidadeId)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nenhum mapa vigente encontrado");
    }
}