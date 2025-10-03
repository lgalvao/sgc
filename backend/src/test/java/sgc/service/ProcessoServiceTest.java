package sgc.service;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import sgc.dto.CreateProcessRequest;
import sgc.dto.ProcessoDTO;
import sgc.model.Processo;
import sgc.model.Unidade;
import sgc.repository.ProcessoRepository;
import sgc.repository.SubprocessoRepository;
import sgc.repository.UnidadeProcessoRepository;
import sgc.repository.UnidadeRepository;
import sgc.repository.MapaRepository;
import sgc.repository.MovimentacaoRepository;
import sgc.repository.UnidadeMapaRepository;
import sgc.service.MapCopyService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ProcessoService cobrindo fluxo feliz de criação e validação.
 */
public class ProcessoServiceTest {
    private ProcessoRepository processoRepository;
    private UnidadeRepository unidadeRepository;
    private ApplicationEventPublisher publisher;

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
    public void create_shouldPersistAndReturnDTO_whenRequestIsValid() {
        CreateProcessRequest req = new CreateProcessRequest();
        req.setDescricao("Processo de teste");
        req.setTipo("MAPEAMENTO");
        req.setDataLimiteEtapa1(LocalDate.now().plusDays(10));
        req.setUnidades(List.of(1L, 2L));

        // preparar mocks: unidades existem
        Unidade u1 = new Unidade();
        u1.setCodigo(1L);
        u1.setNome("Unidade 1");
        u1.setSigla("U1");
        Unidade u2 = new Unidade();
        u2.setCodigo(2L);
        u2.setNome("Unidade 2");
        u2.setSigla("U2");

        when(unidadeRepository.findById(1L)).thenReturn(Optional.of(u1));
        when(unidadeRepository.findById(2L)).thenReturn(Optional.of(u2));

        // quando salvar o processo, retornar com código gerado
        when(processoRepository.save(any(Processo.class))).thenAnswer(invocation -> {
            Processo p = invocation.getArgument(0);
            p.setCodigo(123L);
            return p;
        });

        ProcessoDTO dto = processoService.create(req);

        assertThat(dto).isNotNull();
        assertThat(dto.getCodigo()).isEqualTo(123L);
        assertThat(dto.getDescricao()).isEqualTo("Processo de teste");
        assertThat(dto.getTipo()).isEqualTo("MAPEAMENTO");
        assertThat(dto.getSituacao()).isEqualTo("CRIADO");

        // verificar que evento de criação foi publicado
        verify(publisher, times(1)).publishEvent(any(ProcessoService.ProcessCreatedEvent.class));
    }

    @Test
    public void create_shouldThrowConstraintViolationException_whenDescricaoBlank() {
        CreateProcessRequest req = new CreateProcessRequest();
        req.setDescricao("   "); // em branco
        req.setTipo("MAPEAMENTO");
        req.setUnidades(List.of(1L));

        assertThatThrownBy(() -> processoService.create(req))
                .isInstanceOf(ConstraintViolationException.class);

        verifyNoInteractions(processoRepository);
        verifyNoInteractions(publisher);
    }
}