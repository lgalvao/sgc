package sesel.sgc.processounidade;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import sesel.sgc.mapa.MapaUnidadeSituacao;
import sesel.sgc.mapa.atividades.AtividadeRepository;
import sesel.sgc.processo.ProcessoDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class ProcessoUnidadeServiceTest {
    @MockitoBean
    private ProcessoUnidadeRepository processoUnidadeRepository;

    @MockitoBean
    private AtividadeRepository atividadeRepository;

    @MockitoSpyBean
    private ProcessoUnidadeService processoUnidadeService;

    @Test
    void deveBuscarProcessoUnidadeComSucesso() {
        UUID processoId = UUID.randomUUID();
        UUID unidadeId = UUID.randomUUID();

        ProcessoUnidade pu = new ProcessoUnidade();
        when(processoUnidadeRepository.findByProcessoIdAndUnidadeId(processoId, unidadeId)).thenReturn(Optional.of(pu));

        ProcessoUnidade resultado = processoUnidadeService.buscarProcessoUnidade(processoId, unidadeId);
        assertNotNull(resultado);

        verify(processoUnidadeRepository).findByProcessoIdAndUnidadeId(processoId, unidadeId);
    }

    @Test
    void deveLancarExcecaoQuandoProcessoUnidadeNaoEncontrado() {
        UUID processoId = UUID.randomUUID();
        UUID unidadeId = UUID.randomUUID();

        when(processoUnidadeRepository.findByProcessoIdAndUnidadeId(processoId, unidadeId))
                .thenReturn(Optional.empty());

        assertThrows(ProcessoUnidadeNaoEncontradoErro.class,
                () -> processoUnidadeService.buscarProcessoUnidade(processoId, unidadeId));
    }

    @Test
    void deveFinalizarProcessoUnidade() {
        UUID processoId = UUID.randomUUID();
        UUID unidadeId = UUID.randomUUID();

        ProcessoUnidade pu = new ProcessoUnidade();
        when(processoUnidadeRepository.findByProcessoIdAndUnidadeId(processoId, unidadeId)).thenReturn(Optional.of(pu));
        when(processoUnidadeRepository.save(any())).thenReturn(pu);
        when(atividadeRepository.findByProcessoUnidadeIdComConhecimentos(any()))
                .thenReturn(List.of(new sesel.sgc.mapa.atividades.Atividade()));
        assertDoesNotThrow(() -> processoUnidadeService.finalizarProcessoUnidade(processoId, unidadeId));
        assertEquals(MapaUnidadeSituacao.FINALIZADA, pu.getSituacao());
        assertNotNull(pu.getDataHoraFimRegistro());
    }

    @Test
    void deveReabrirProcessoUnidade() {
        UUID processoId = UUID.randomUUID();
        UUID unidadeId = UUID.randomUUID();

        ProcessoUnidade pu = new ProcessoUnidade();
        pu.setSituacao(MapaUnidadeSituacao.FINALIZADA);
        pu.setDataHoraFimRegistro(LocalDateTime.now());

        when(processoUnidadeRepository.findByProcessoIdAndUnidadeId(processoId, unidadeId)).thenReturn(Optional.of(pu));
        when(processoUnidadeRepository.save(any())).thenReturn(pu);

        assertDoesNotThrow(() -> processoUnidadeService.reabrirProcessoUnidade(processoId, unidadeId));
        assertEquals(MapaUnidadeSituacao.EM_ANDAMENTO, pu.getSituacao());
        assertNull(pu.getDataHoraFimRegistro());
    }

    @Test
    void deveAtualizarProcessoUnidadeComSucesso() {
        ProcessoUnidade pu = new ProcessoUnidade();
        pu.setId(UUID.randomUUID());

        when(processoUnidadeRepository.existsById(pu.getId())).thenReturn(true);
        when(processoUnidadeRepository.save(pu)).thenReturn(pu);
        assertDoesNotThrow(() -> processoUnidadeService.atualizarProcessoUnidade(pu));
    }

    @Test
    void deveLancarExcecaoAoAtualizarProcessoUnidadeInexistente() {
        ProcessoUnidade pu = new ProcessoUnidade();
        pu.setId(UUID.randomUUID());

        when(processoUnidadeRepository.existsById(pu.getId())).thenReturn(false);
        assertThrows(ProcessoUnidadeNaoEncontradoErro.class, () -> processoUnidadeService.atualizarProcessoUnidade(pu));
    }

    @Test
    void deveLancarExcecaoAoAtualizarProcessoUnidadeNula() {
        assertThrows(IllegalArgumentException.class, () -> processoUnidadeService.atualizarProcessoUnidade(null));
    }

    @Test
    void deveBuscarProcessoUnidadeDetalhada() {
        UUID processoId = UUID.randomUUID();
        UUID unidadeId = UUID.randomUUID();
        ProcessoUnidade pu = new ProcessoUnidade();
        when(processoUnidadeRepository.findByProcessoIdAndUnidadeId(processoId, unidadeId)).thenReturn(Optional.of(pu));
        ProcessoUnidade resultado = processoUnidadeService.buscarProcessoUnidadeDetalhada(processoId, unidadeId);
        assertNotNull(resultado);
    }

    @Test
    void deveConverterParaProcessoUnidadeDTO() {
        ProcessoUnidade pu = new ProcessoUnidade();
        pu.setSituacao(MapaUnidadeSituacao.EM_ANDAMENTO);
        pu.setDataHoraInicioRegistro(java.time.LocalDateTime.now());
        sesel.sgc.unidade.Unidade unidade = new sesel.sgc.unidade.Unidade();
        unidade.setId(UUID.randomUUID());
        unidade.setNome("Unidade Teste");
        unidade.setTitular(null);
        pu.setUnidade(unidade);
        ProcessoDto.ProcessoUnidadeDto dto = processoUnidadeService.toProcessoUnidadeDto(pu);
        assertNotNull(dto);
        assertEquals(unidade.getId(), dto.getUnidadeId());
        assertEquals("Unidade Teste", dto.getUnidadeNome());
        assertEquals("N/A", dto.getResponsavelNome());
        assertEquals(MapaUnidadeSituacao.EM_ANDAMENTO, dto.getSituacao());
        assertNotNull(dto.getUltimaAtualizacao());
    }

    @Test
    void deveConverterParaProcessoUnidadeDTOSemUnidade() {
        ProcessoUnidade pu = new ProcessoUnidade();
        pu.setSituacao(MapaUnidadeSituacao.EM_ANDAMENTO);
        pu.setDataHoraInicioRegistro(java.time.LocalDateTime.now());
        pu.setUnidade(null);
        ProcessoDto.ProcessoUnidadeDto dto = processoUnidadeService.toProcessoUnidadeDto(pu);
        assertNotNull(dto);
        assertNull(dto.getUnidadeId());
        assertNull(dto.getUnidadeNome());
        assertNull(dto.getResponsavelNome());
    }

    @Test
    void deveBuscarProcessoUnidadePorId() {
        UUID id = UUID.randomUUID();
        ProcessoUnidade pu = new ProcessoUnidade();
        when(processoUnidadeRepository.findById(id)).thenReturn(Optional.of(pu));
        ProcessoUnidade resultado = processoUnidadeService.buscarProcessoUnidadePorId(id);
        assertNotNull(resultado);
    }

    @Test
    void deveLancarExcecaoQuandoBuscarProcessoUnidadePorIdInexistente() {
        UUID id = UUID.randomUUID();
        when(processoUnidadeRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ProcessoUnidadeNaoEncontradoErro.class,
                () -> processoUnidadeService.buscarProcessoUnidadePorId(id));
    }
}