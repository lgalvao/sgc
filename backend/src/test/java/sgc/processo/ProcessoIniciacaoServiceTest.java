package sgc.processo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.processo.eventos.ProcessoIniciadoEvento;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.TipoProcesso;
import sgc.processo.modelo.UnidadeProcessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessoIniciacaoServiceTest {

    @InjectMocks
    private ProcessoIniciacaoService service;

    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private UnidadeRepo unidadeRepo;
    @Mock
    private UnidadeProcessoRepo unidadeProcessoRepo;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private Processo processo;

    @BeforeEach
    void setUp() {
        processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(SituacaoProcesso.CRIADO);
    }

    @Nested
    @DisplayName("Testes para iniciarProcessoMapeamento")
    class IniciarProcessoMapeamentoTests {
        @Test
        @DisplayName("Deve iniciar processo de mapeamento com sucesso")
        void iniciarProcessoMapeamento_Sucesso() {
            processo.setTipo(TipoProcesso.MAPEAMENTO);
            when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
            when(unidadeRepo.findById(1L)).thenReturn(Optional.of(new Unidade()));

            service.iniciarProcessoMapeamento(1L, Collections.singletonList(1L));

            verify(processoRepo).save(processo);
            verify(eventPublisher).publishEvent(any(ProcessoIniciadoEvento.class));
        }

        @Test
        @DisplayName("Deve lançar exceção se processo não estiver no estado CRIADO")
        void iniciarProcessoMapeamento_NaoCriado_LancaExcecao() {
            processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));

            assertThrows(IllegalStateException.class, () -> service.iniciarProcessoMapeamento(1L, Collections.singletonList(1L)));
        }

        @Test
        @DisplayName("Deve lançar exceção se não houver unidades")
        void iniciarProcessoMapeamento_SemUnidades_LancaExcecao() {
            when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
            assertThrows(IllegalArgumentException.class, () -> service.iniciarProcessoMapeamento(1L, Collections.emptyList()));
        }
    }
}
