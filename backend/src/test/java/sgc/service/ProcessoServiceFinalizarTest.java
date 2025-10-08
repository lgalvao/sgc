package sgc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.UnidadeMapa;
import sgc.mapa.modelo.UnidadeMapaRepo;
import sgc.processo.ProcessoService;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.dto.ProcessoMapper;
import sgc.processo.eventos.ProcessoFinalizadoEvento;
import sgc.processo.modelo.ErroProcesso;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProcessoServiceFinalizarTest {

    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private UnidadeMapaRepo unidadeMapaRepo;
    @Mock
    private ApplicationEventPublisher publicadorDeEventos;
    @Mock
    private ProcessoMapper processoMapper;
    @InjectMocks
    private ProcessoService processoService;

    private Processo processo;
    private Subprocesso subprocessoHomologado;
    private Subprocesso subprocessoPendente;

    @BeforeEach
    void setUp() {
        processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("Processo de Teste");
        processo.setSituacao("EM_ANDAMENTO");

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setSigla("TEST");

        subprocessoHomologado = new Subprocesso();
        subprocessoHomologado.setCodigo(100L);
        subprocessoHomologado.setUnidade(unidade);
        subprocessoHomologado.setSituacaoId("MAPA_HOMOLOGADO");

        subprocessoPendente = new Subprocesso();
        subprocessoPendente.setCodigo(101L);
        subprocessoPendente.setUnidade(unidade);
        subprocessoPendente.setSituacaoId("MAPA_VALIDADO");
    }

    @Test
    @DisplayName("finalizar deve lançar ErroProcesso quando subprocessos não estão homologados")
    void finalizar_deveLancarErroProcesso_quandoSubprocessosNaoEstaoHomologados() {
        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigo(1L))
                .thenReturn(List.of(subprocessoHomologado, subprocessoPendente));

        assertThatThrownBy(() -> processoService.finalizar(1L))
                .isInstanceOf(ErroProcesso.class)
                .hasMessageContaining("Unidades pendentes de homologação:");

        verify(processoRepo, never()).save(any(Processo.class));
        verify(publicadorDeEventos, never()).publishEvent(any(ProcessoFinalizadoEvento.class));
    }

    @Test
    @DisplayName("finalizar deve atualizar status e tornar mapas vigentes quando todos os subprocessos estão homologados")
    void finalizar_deveAtualizarStatusETornarMapasVigentes_quandoTodosSubprocessosEstaoHomologados() {
        // Arrange
        subprocessoHomologado.setMapa(new Mapa());
        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigo(1L)).thenReturn(List.of(subprocessoHomologado));
        when(processoRepo.save(any(Processo.class))).thenReturn(processo);
        when(unidadeMapaRepo.findByUnidadeCodigo(anyLong())).thenReturn(Optional.empty());
        when(processoMapper.toDTO(any(Processo.class))).thenReturn(new ProcessoDto());

        // Act
        processoService.finalizar(1L);

        // Assert
        verify(processoRepo, times(1)).save(processo);
        verify(unidadeMapaRepo, times(1)).save(any(UnidadeMapa.class));
        verify(publicadorDeEventos, times(1)).publishEvent(any(ProcessoFinalizadoEvento.class));
    }
}