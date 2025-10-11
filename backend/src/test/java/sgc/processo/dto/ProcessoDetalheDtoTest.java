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
    void testProcessoDetalheDtoBuilderAndAccessors() {
        var now = LocalDateTime.now();
        var limitDate = LocalDate.now();
        var subprocessoResumo = ProcessoResumoDto.builder()
            .codigo(1L)
            .descricao("Subprocess 1")
            .situacao(SituacaoProcesso.EM_ANDAMENTO)
            .tipo("TIPO_A")
            .dataLimite(limitDate)
            .dataCriacao(now)
            .unidadeCodigo(1L)
            .unidadeNome("Unidade Teste")
            .build();
        var unidade = ProcessoDetalheDto.UnidadeParticipanteDTO.builder()
            .unidadeCodigo(1L)
            .nome("Test Unit")
            .sigla("TU")
            .unidadeSuperiorCodigo(10L)
            .situacaoSubprocesso(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO)
            .dataLimite(limitDate)
            .build();

        var dto = ProcessoDetalheDto.builder()
            .codigo(1L)
            .descricao("Test Description")
            .tipo("TIPO_A")
            .situacao(SituacaoProcesso.EM_ANDAMENTO)
            .dataLimite(limitDate)
            .dataCriacao(now)
            .dataFinalizacao(now)
            .unidades(List.of(unidade))
            .resumoSubprocessos(List.of(subprocessoResumo))
            .build();

        assertEquals(1L, dto.getCodigo());
        assertEquals("Test Description", dto.getDescricao());
        assertEquals("TIPO_A", dto.getTipo());
        assertEquals(SituacaoProcesso.EM_ANDAMENTO, dto.getSituacao());
        assertEquals(limitDate, dto.getDataLimite());
        assertEquals(now, dto.getDataCriacao());
        assertEquals(now, dto.getDataFinalizacao());
        assertEquals(1, dto.getUnidades().size());
        assertEquals(1, dto.getResumoSubprocessos().size());
    }

    @Test
    void testUnidadeParticipanteDTOBuilderAndAccessors() {
        var limitDate = LocalDate.now();
        var filho = ProcessoDetalheDto.UnidadeParticipanteDTO.builder()
            .unidadeCodigo(2L)
            .nome("Filho Unit")
            .sigla("FU")
            .unidadeSuperiorCodigo(1L)
            .situacaoSubprocesso(SituacaoSubprocesso.NAO_INICIADO)
            .dataLimite(limitDate)
            .build();

        var dto = ProcessoDetalheDto.UnidadeParticipanteDTO.builder()
            .unidadeCodigo(1L)
            .nome("Test Unit")
            .sigla("TU")
            .unidadeSuperiorCodigo(10L)
            .situacaoSubprocesso(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO)
            .dataLimite(limitDate)
            .filhos(List.of(filho))
            .build();

        assertEquals(1L, dto.getUnidadeCodigo());
        assertEquals("Test Unit", dto.getNome());
        assertEquals("TU", dto.getSigla());
        assertEquals(10L, dto.getUnidadeSuperiorCodigo());
        assertEquals(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO, dto.getSituacaoSubprocesso());
        assertEquals(limitDate, dto.getDataLimite());
        assertNotNull(dto.getFilhos());
        assertEquals(1, dto.getFilhos().size());
        assertEquals(2L, dto.getFilhos().getFirst().getUnidadeCodigo());
    }

    @Test
    void testDtoBuilderWithDefaultLists() {
        var dto = ProcessoDetalheDto.builder()
            .codigo(1L)
            .descricao("Desc")
            .tipo("Tipo")
            .situacao(SituacaoProcesso.CRIADO)
            .build();
        assertNotNull(dto.getUnidades());
        assertTrue(dto.getUnidades().isEmpty());
        assertNotNull(dto.getResumoSubprocessos());
        assertTrue(dto.getResumoSubprocessos().isEmpty());

        var unidadeDto = ProcessoDetalheDto.UnidadeParticipanteDTO.builder()
            .unidadeCodigo(1L)
            .nome("Nome")
            .sigla("Sigla")
            .build();
        assertNotNull(unidadeDto.getFilhos());
        assertTrue(unidadeDto.getFilhos().isEmpty());
    }
}
