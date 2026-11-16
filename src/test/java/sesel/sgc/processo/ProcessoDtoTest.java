package sesel.sgc.processo;

import org.junit.jupiter.api.Test;
import sesel.sgc.mapa.MapaUnidadeSituacao;
import sesel.sgc.processounidade.ProcessoUnidade;
import sesel.sgc.unidade.Unidade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProcessoDtoTest {
    @Test
    void testFromEntityComProcessosUnidades() {
        Processo processo = new Processo();
        processo.setId(UUID.randomUUID());
        processo.setNome("Processo Teste");
        processo.setDataLimite(LocalDate.now());
        processo.setSituacao(ProcessoSituacao.FINALIZADO);
        processo.setProcessosUnidades(new ArrayList<>());

        ProcessoUnidade pu = new ProcessoUnidade();
        pu.setId(UUID.randomUUID());
        pu.setSituacao(MapaUnidadeSituacao.EM_ANDAMENTO);
        pu.setDataHoraInicioRegistro(LocalDateTime.now());

        Unidade unidade = new Unidade();
        unidade.setId(UUID.randomUUID());
        unidade.setNome("Unidade Teste");
        unidade.setTitular(null);
        pu.setUnidade(unidade);
        processo.getProcessosUnidades().add(pu);

        ProcessoDto dto = ProcessoDto.fromEntity(processo, 1L);
        assertEquals(processo.getId().toString(), dto.getId());
        assertEquals("Processo Teste", dto.getNome());
        assertEquals(ProcessoSituacao.FINALIZADO, dto.getSituacao());
        assertEquals(1, dto.getTotalUnidades());
        assertNotNull(dto.getProcessosUnidades());
        assertEquals(1, dto.getProcessosUnidades().size());

        ProcessoDto.ProcessoUnidadeDto partDto = dto.getProcessosUnidades().getFirst();
        assertEquals(unidade.getId(), partDto.getUnidadeId());
        assertEquals("Unidade Teste", partDto.getUnidadeNome());
        assertEquals("N/A", partDto.getResponsavelNome());
        assertEquals(MapaUnidadeSituacao.EM_ANDAMENTO, partDto.getSituacao());
        assertNotNull(partDto.getUltimaAtualizacao());
    }

    @Test
    void testFromEntitySemProcessosUnidades() {
        Processo processo = new Processo();
        processo.setId(UUID.randomUUID());
        processo.setNome("Processo Vazio");
        processo.setDataLimite(LocalDate.now());
        processo.setSituacao(ProcessoSituacao.FINALIZADO);
        processo.setProcessosUnidades(null);

        ProcessoDto dto = ProcessoDto.fromEntity(processo, 0L);
        assertEquals("Processo Vazio", dto.getNome());
        assertNull(dto.getProcessosUnidades());
    }
}