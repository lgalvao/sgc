package sgc.processo.dto;

import org.junit.jupiter.api.Test;
import sgc.comum.enums.SituacaoProcesso;
import sgc.comum.enums.SituacaoSubprocesso;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProcessoDetalheDtoTest {

    @Test
    void testProcessoDetalheDtoConstructorAndAccessors() {
        var now = LocalDateTime.now();
        var limitDate = LocalDate.now();
        var subprocessoResumo = new ProcessoResumoDto(1L, "Subprocess 1", SituacaoProcesso.EM_ANDAMENTO, "TIPO_A", limitDate, now, 1L, "Unidade Teste");
        var unidade = new ProcessoDetalheDto.UnidadeParticipanteDTO(1L, "Test Unit", "TU", 10L, SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO, limitDate, new ArrayList<>());

        var dto = new ProcessoDetalheDto(
            1L,
            "Test Description",
            "TIPO_A",
            SituacaoProcesso.EM_ANDAMENTO,
            limitDate,
            now,
            now,
            List.of(unidade),
            List.of(subprocessoResumo)
        );

        assertEquals(1L, dto.codigo());
        assertEquals("Test Description", dto.descricao());
        assertEquals("TIPO_A", dto.tipo());
        assertEquals(SituacaoProcesso.EM_ANDAMENTO, dto.situacao());
        assertEquals(limitDate, dto.dataLimite());
        assertEquals(now, dto.dataCriacao());
        assertEquals(now, dto.dataFinalizacao());
        assertEquals(1, dto.unidades().size());
        assertEquals(1, dto.resumoSubprocessos().size());
    }

    @Test
    void testUnidadeParticipanteDTOConstructorAndAccessors() {
        var limitDate = LocalDate.now();
        var filho = new ProcessoDetalheDto.UnidadeParticipanteDTO(2L, "Filho Unit", "FU", 1L, SituacaoSubprocesso.NAO_INICIADO, limitDate, Collections.emptyList());
        var dto = new ProcessoDetalheDto.UnidadeParticipanteDTO(1L, "Test Unit", "TU", 10L, SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO, limitDate, List.of(filho));

        assertEquals(1L, dto.unidadeCodigo());
        assertEquals("Test Unit", dto.nome());
        assertEquals("TU", dto.sigla());
        assertEquals(10L, dto.unidadeSuperiorCodigo());
        assertEquals(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO, dto.situacaoSubprocesso());
        assertEquals(limitDate, dto.dataLimite());
        assertNotNull(dto.filhos());
        assertEquals(1, dto.filhos().size());
        assertEquals(2L, dto.filhos().getFirst().unidadeCodigo());
    }

    @Test
    void testDtoWithNullListsIsInitialized() {
        var dto = new ProcessoDetalheDto(1L, "Desc", "Tipo", SituacaoProcesso.CRIADO, null, null, null, null, null);
        assertNotNull(dto.unidades());
        assertTrue(dto.unidades().isEmpty());
        assertNotNull(dto.resumoSubprocessos());
        assertTrue(dto.resumoSubprocessos().isEmpty());

        var unidadeDto = new ProcessoDetalheDto.UnidadeParticipanteDTO(1L, "Nome", "Sigla", null, null, null, null);
        assertNotNull(unidadeDto.filhos());
        assertTrue(unidadeDto.filhos().isEmpty());
    }
}
