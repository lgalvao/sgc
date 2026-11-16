package sesel.sgc.processo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import sesel.sgc.mapa.MapaUnidadeSituacao;
import sesel.sgc.processounidade.ProcessoUnidade;
import sesel.sgc.processounidade.ProcessoUnidadeRepository;
import sesel.sgc.sgrh.SgrhService;
import sesel.sgc.unidade.Unidade;
import sesel.sgc.unidade.UnidadeRepository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
class ProcessoServiceTest {
    @MockitoBean
    private ProcessoRepository processoRepository;

    @MockitoSpyBean
    private ProcessoService processoService;

    @MockitoBean
    private SgrhService sgrhService;

    @MockitoBean
    private UnidadeRepository unidadeRepository;

    @MockitoBean
    private ProcessoUnidadeRepository processoUnidadeRepository;

    @BeforeEach
    void setUp() {
        doNothing().when(sgrhService).sincronizarDados();
    }

    @Test
    void deveCriarProcessoComSucesso() {
        Processo processo = new Processo();
        processo.setNome("Processo Teste");
        UUID unidadeId = UUID.randomUUID();
        processo.setUnidadesParticipantes(Set.of(unidadeId));

        Unidade unidade = new Unidade();
        unidade.setId(unidadeId);
        when(unidadeRepository.findById(unidadeId)).thenReturn(Optional.of(unidade));
        when(processoRepository.saveAndFlush(any(Processo.class))).thenAnswer(invocation -> {
            Processo p = invocation.getArgument(0);
            if (p.getId() == null)
                p.setId(UUID.randomUUID());
            return p;
        });

        Processo resultado = processoService.criarProcesso(processo);
        assertNotNull(resultado);
        assertNotNull(resultado.getId());
    }

    @Test
    void deveBuscarProcessoPorIdComSucesso() {
        UUID id = UUID.randomUUID();
        Processo processo = new Processo();

        when(processoRepository.findById(id)).thenReturn(java.util.Optional.of(processo));
        when(processoRepository.findByIdWithProcessoUnidades(id)).thenReturn(Optional.of(processo));

        Processo resultado = processoService.buscarPorId(id);
        assertNotNull(resultado);
    }

    @Test
    void deveCalcularPercentualConclusaoComProcesso() {
        Processo processo = new Processo();
        processoService.calcularPercentualConclusao(processo);
    }

    @Test
    void deveCalcularPercentualConclusaoComUUID() {
        UUID id = UUID.randomUUID();
        Processo processo = new Processo();

        when(processoRepository.findById(id)).thenReturn(java.util.Optional.of(processo));

        processoService.calcularPercentualConclusao(id);
    }

    @Test
    void deveFinalizarProcessoUnidadeSemExcecao() {
        UUID processoId = UUID.randomUUID();
        UUID unidadeId = UUID.randomUUID();

        ProcessoUnidade processoUnidade = new ProcessoUnidade();
        when(processoUnidadeRepository.findByProcessoIdAndUnidadeId(processoId, unidadeId))
                .thenReturn(Optional.of(processoUnidade));

        processoService.finalizarProcessoUnidade(processoId, unidadeId);
    }

    @Test
    void deveListarTodosProcessosDTO() {
        when(processoRepository.findAll()).thenReturn(java.util.List.of(new Processo()));
        when(processoRepository.countProcessoUnidadesByProcessoId(any(UUID.class))).thenReturn(1L);
        java.util.List<ProcessoDto> dtos = processoService.listarTodosProcessosDTO();
        assertNotNull(dtos);
    }

    @Test
    void deveListarUnidadesDisponiveis() {
        when(unidadeRepository.findAll()).thenReturn(java.util.List.of(new Unidade()));
        java.util.List<Unidade> unidades = processoService.listarUnidadesDisponiveis();
        assertNotNull(unidades);
    }

    @Test
    void deveBuscarProcessoDTO() {
        UUID id = UUID.randomUUID();
        Processo processo = new Processo();
        when(processoRepository.findByIdWithProcessoUnidadesAndUnidades(id)).thenReturn(Optional.of(processo));
        ProcessoDto dto = processoService.buscarProcessoDTO(id);
        assertNotNull(dto);
    }

    @Test
    void deveBuscarProcessoUnidade() {
        UUID processoId = UUID.randomUUID();
        UUID unidadeId = UUID.randomUUID();
        ProcessoUnidade pu = new ProcessoUnidade();
        when(processoUnidadeRepository.findByProcessoIdAndUnidadeId(processoId, unidadeId)).thenReturn(Optional.of(pu));
        ProcessoUnidade resultado = processoService.buscarProcessoUnidade(processoId, unidadeId);
        assertNotNull(resultado);
    }

    @Test
    void deveContarProcessos() {
        when(processoRepository.count()).thenReturn(5L);
        long count = processoService.contarProcessos();
        org.junit.jupiter.api.Assertions.assertEquals(5L, count);
    }

    @Test
    void deveContarProcessosFinalizados() {
        when(processoRepository.countBySituacao(ProcessoSituacao.FINALIZADO)).thenReturn(2L);
        long count = processoService.contarProcessosFinalizados();
        org.junit.jupiter.api.Assertions.assertEquals(2L, count);
    }

    @Test
    void deveReabrirProcessoUnidade() {
        UUID processoId = UUID.randomUUID();
        UUID unidadeId = UUID.randomUUID();
        ProcessoUnidade pu = new ProcessoUnidade();
        when(processoUnidadeRepository.findByProcessoIdAndUnidadeId(processoId, unidadeId)).thenReturn(Optional.of(pu));
        when(processoUnidadeRepository.save(any(ProcessoUnidade.class))).thenReturn(pu);
        processoService.reabrirProcessoUnidade(processoId, unidadeId);
    }

    @Test
    void deveListarProcessosUnidade() {
        UUID processoId = UUID.randomUUID();
        when(processoUnidadeRepository.findByProcessoId(processoId))
                .thenReturn(java.util.List.of(new ProcessoUnidade()));
        java.util.List<ProcessoUnidade> list = processoService.listarProcessosUnidade(processoId);
        assertNotNull(list);
    }

    @Test
    void deveLancarExcecaoQuandoProcessoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(processoRepository.findByIdWithProcessoUnidades(id)).thenReturn(Optional.empty());
        org.junit.jupiter.api.Assertions.assertThrows(ProcessoNaoEncontradoErro.class,
                () -> processoService.buscarPorId(id));
    }

    @Test
    void deveLancarExcecaoQuandoBuscarProcessoDTOENaoEncontrar() {
        UUID id = UUID.randomUUID();
        when(processoRepository.findByIdWithProcessoUnidadesAndUnidades(id)).thenReturn(Optional.empty());
        org.junit.jupiter.api.Assertions.assertThrows(ProcessoNaoEncontradoErro.class,
                () -> processoService.buscarProcessoDTO(id));
    }

    @Test
    void deveLancarExcecaoQuandoBuscarProcessoUnidadeENaoEncontrar() {
        UUID processoId = UUID.randomUUID();
        UUID unidadeId = UUID.randomUUID();
        when(processoUnidadeRepository.findByProcessoIdAndUnidadeId(processoId, unidadeId))
                .thenReturn(Optional.empty());
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> processoService.buscarProcessoUnidade(processoId, unidadeId));
    }

    @Test
    void deveCalcularPercentualConclusaoZeroQuandoNenhumaUnidadeFinalizada() {
        UUID processoId = UUID.randomUUID();
        when(processoRepository.countProcessoUnidadesByProcessoId(processoId)).thenReturn(2L);
        when(processoRepository.countProcessoUnidadesByProcessoIdAndSituacao(processoId,
                MapaUnidadeSituacao.FINALIZADA)).thenReturn(0L);
        int percentual = processoService.calcularPercentualConclusao(processoId);
        org.junit.jupiter.api.Assertions.assertEquals(0, percentual);
    }

    @Test
    void deveCalcularPercentualConclusaoCemQuandoTodasUnidadesFinalizadas() {
        UUID processoId = UUID.randomUUID();
        when(processoRepository.countProcessoUnidadesByProcessoId(processoId)).thenReturn(2L);
        when(processoRepository.countProcessoUnidadesByProcessoIdAndSituacao(processoId,
                MapaUnidadeSituacao.FINALIZADA)).thenReturn(2L);
        int percentual = processoService.calcularPercentualConclusao(processoId);
        org.junit.jupiter.api.Assertions.assertEquals(100, percentual);
    }

    @Test
    void deveCalcularPercentualConclusaoParcial() {
        UUID processoId = UUID.randomUUID();
        when(processoRepository.countProcessoUnidadesByProcessoId(processoId)).thenReturn(2L);
        when(processoRepository.countProcessoUnidadesByProcessoIdAndSituacao(processoId,
                MapaUnidadeSituacao.FINALIZADA)).thenReturn(1L);
        int percentual = processoService.calcularPercentualConclusao(processoId);
        org.junit.jupiter.api.Assertions.assertEquals(50, percentual);
    }
}