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
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.UnidadeMapaRepo;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.UnidadeProcessoRepo;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessoFinalizacaoServiceTest {

    @InjectMocks
    private ProcessoFinalizacaoService service;

    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private ProcessoNotificacaoService notificacaoService;
    @Mock
    private UnidadeMapaRepo unidadeMapaRepo;
    @Mock
    private UnidadeProcessoRepo unidadeProcessoRepo;

    private Processo processo;

    @BeforeEach
    void setUp() {
        processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
    }

    @Nested
    @DisplayName("Testes para finalizar processo")
    class FinalizarProcessoTests {
        @Test
        @DisplayName("Deve finalizar processo com sucesso")
        void finalizar_Sucesso() {
            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setSituacao(SituacaoSubprocesso.MAPA_HOMOLOGADO);
            Unidade unidade = new Unidade();
            unidade.setCodigo(1L);
            subprocesso.setUnidade(unidade);
            subprocesso.setMapa(new Mapa());
            when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
            when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L)).thenReturn(Collections.singletonList(subprocesso));

            service.finalizar(1L);

            verify(processoRepo).save(processo);
            verify(eventPublisher).publishEvent(any());
            verify(notificacaoService).enviarNotificacoesDeFinalizacao(any(), any());
        }

        @Test
        @DisplayName("Deve lançar exceção se processo não estiver em andamento")
        void finalizar_NaoEmAndamento_LancaExcecao() {
            processo.setSituacao(SituacaoProcesso.FINALIZADO);
            when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));

            assertThrows(Exception.class, () -> service.finalizar(1L));
        }

        @Test
        @DisplayName("Deve lançar exceção se houver subprocessos pendentes")
        void finalizar_SubprocessosPendentes_LancaExcecao() {
            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setSituacao(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);
            when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
            when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L)).thenReturn(Collections.singletonList(subprocesso));

            assertThrows(Exception.class, () -> service.finalizar(1L));
        }
    }
}
