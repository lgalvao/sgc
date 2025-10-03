package sgc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sgc.exception.DomainAccessDeniedException;
import sgc.model.Processo;
import sgc.model.Subprocesso;
import sgc.model.Unidade;
import sgc.model.UnidadeProcesso;
import sgc.repository.*;
import sgc.dto.ProcessDetailDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ProcessoService.getDetails(...)
 * <p>
 * - caso feliz: retorna ProcessDetailDTO com unidades e resumo de subprocessos
 * - caso sem permissão: lança DomainAccessDeniedException
 */
public class ProcessoServiceDetailsTest {

    private ProcessoRepository processoRepository;
    private UnidadeProcessoRepository unidadeProcessoRepository;
    private SubprocessoRepository subprocessoRepository;

    private ProcessoService service;

    @BeforeEach
    public void setup() {
        processoRepository = mock(ProcessoRepository.class);
        UnidadeRepository unidadeRepository = mock(UnidadeRepository.class);
        unidadeProcessoRepository = mock(UnidadeProcessoRepository.class);
        subprocessoRepository = mock(SubprocessoRepository.class);
        MapaRepository mapaRepository = mock(MapaRepository.class);
        MovimentacaoRepository movimentacaoRepository = mock(MovimentacaoRepository.class);
        UnidadeMapaRepository unidadeMapaRepository = mock(UnidadeMapaRepository.class);
        MapCopyService mapCopyService = mock(MapCopyService.class);
        ApplicationEventPublisherStub publisher = new ApplicationEventPublisherStub();

        service = new ProcessoService(
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
    public void testGetDetails_HappyPath() {
        // Arrange
        Processo p = new Processo();
        p.setCodigo(1L);
        p.setDescricao("Proc Teste");
        p.setTipo("MAPEAMENTO");
        p.setSituacao("EM_ANDAMENTO");
        p.setDataCriacao(LocalDateTime.now());
        p.setDataLimite(LocalDate.now().plusDays(7));

        UnidadeProcesso up = new UnidadeProcesso();
        up.setCodigo(10L);
        up.setNome("Diretoria X");
        up.setSigla("DX");
        up.setSituacao("PENDENTE");
        up.setUnidadeSuperiorCodigo(null);

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setSigla("DX");
        unidade.setNome("Diretoria X");

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setUnidade(unidade);
        sp.setSituacaoId("PENDENTE");
        sp.setDataLimiteEtapa1(LocalDate.now().plusDays(5));

        when(processoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(unidadeProcessoRepository.findByProcessoCodigo(1L)).thenReturn(List.of(up));
        when(subprocessoRepository.findByProcessoCodigoWithUnidade(1L)).thenReturn(List.of(sp));

        // Act
        ProcessDetailDTO dto = service.getDetails(1L, "ADMIN", null);

        // Assert
        assertNotNull(dto);
        assertEquals(p.getCodigo(), dto.getCodigo());
        assertNotNull(dto.getUnidades());
        assertTrue(dto.getUnidades().stream().anyMatch(u -> "DX".equals(u.getSigla())));
        assertNotNull(dto.getResumoSubprocessos());
        assertTrue(dto.getResumoSubprocessos().stream().anyMatch(s -> s.getCodigo() != null && s.getSituacao() != null));
    }

    @Test
    public void testGetDetails_UnauthorizedForGestor() {
        // Arrange
        Processo p = new Processo();
        p.setCodigo(2L);
        p.setDescricao("Proc Teste 2");

        Unidade unidade = new Unidade();
        unidade.setCodigo(99L);
        unidade.setSigla("OX");
        unidade.setNome("Outra");

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(200L);
        sp.setUnidade(unidade);
        sp.setSituacaoId("PENDENTE");

        when(processoRepository.findById(2L)).thenReturn(Optional.of(p));
        when(unidadeProcessoRepository.findByProcessoCodigo(2L)).thenReturn(List.of());
        when(subprocessoRepository.findByProcessoCodigoWithUnidade(2L)).thenReturn(List.of(sp));

        // Act & Assert: gestor da unidade 10 não está presente nos subprocessos -> acesso negado
        assertThrows(DomainAccessDeniedException.class, () -> service.getDetails(2L, "GESTOR", 10L));
    }

    // Pequeno stub local para ApplicationEventPublisher (não usamos eventos nos testes)
    static class ApplicationEventPublisherStub implements org.springframework.context.ApplicationEventPublisher {
        @Override
        public void publishEvent(Object event) {}
    }
}