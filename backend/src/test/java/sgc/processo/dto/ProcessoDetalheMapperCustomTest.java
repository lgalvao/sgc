package sgc.processo.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.UnidadeProcesso;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.unidade.modelo.Unidade;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessoDetalheMapperCustomTest {
    @Mock
    private ProcessoDetalheMapperInterface processoDetalheMapperInterface;

    private ProcessoDetalheMapperCustom customMapper;

    @BeforeEach
    void setUp() {
        customMapper = new ProcessoDetalheMapperCustom(processoDetalheMapperInterface);
    }

    @Test
    @DisplayName("Deve retornar nulo se o processo for nulo")
    void toDetailDTO_quandoProcessoNulo_retornaNulo() {
        ProcessoDetalheDto resultDto = customMapper.toDetailDTO(null, Collections.emptyList(), Collections.emptyList());
        assertNull(resultDto);
    }

    @Test
    @DisplayName("Deve mapear processo e associar UnidadeProcesso com Subprocesso correspondente")
    void toDetailDTO_deveAssociarUnidadeESubprocesso() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("Processo Teste");

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setSigla("UNID-A");
        unidade.setNome("Unidade A");

        UnidadeProcesso unidadeProcesso = new UnidadeProcesso();
        unidadeProcesso.setProcessoCodigo(processo.getCodigo());
        unidadeProcesso.setUnidadeCodigo(unidade.getCodigo());
        unidadeProcesso.setSigla(unidade.getSigla());

        Subprocesso subprocesso = new Subprocesso(processo, unidade, null, "EM_ANDAMENTO", LocalDate.of(2025, 10, 31));

        ProcessoDetalheDto baseDto = new ProcessoDetalheDto();
        ProcessoDetalheDto.UnidadeParticipanteDTO unidadeDto = new ProcessoDetalheDto.UnidadeParticipanteDTO();
        unidadeDto.setSigla("UNID-A");

        when(processoDetalheMapperInterface.toDetailDTO(processo)).thenReturn(baseDto);
        when(processoDetalheMapperInterface.unidadeProcessoToUnidadeParticipanteDTO(unidadeProcesso)).thenReturn(unidadeDto);

        ProcessoDetalheDto resultDto = customMapper.toDetailDTO(processo, List.of(unidadeProcesso), List.of(subprocesso));

        assertNotNull(resultDto);
        assertEquals(1, resultDto.getUnidades().size());
        ProcessoDetalheDto.UnidadeParticipanteDTO resultUnidade = resultDto.getUnidades().get(0);
        assertEquals(subprocesso.getSituacaoId(), resultUnidade.getSituacaoSubprocesso());
        assertEquals(subprocesso.getDataLimiteEtapa1(), resultUnidade.getDataLimite());
        verify(processoDetalheMapperInterface, never()).subprocessoToUnidadeParticipanteDTO(any());
    }

    @Test
    @DisplayName("Deve criar nova unidade participante se Subprocesso não tem UnidadeProcesso correspondente")
    void toDetailDTO_deveCriarUnidadeParaSubprocessoSemCorrespondencia() {
        Processo processo = new Processo();
        processo.setCodigo(1L);

        Unidade unidadeSub = new Unidade();
        unidadeSub.setCodigo(20L);
        unidadeSub.setSigla("UNID-B");

        Subprocesso subprocesso = new Subprocesso(processo, unidadeSub, null, "INICIADO", LocalDate.of(2025, 11, 30));

        ProcessoDetalheDto baseDto = new ProcessoDetalheDto();
        ProcessoDetalheDto.UnidadeParticipanteDTO unidadeDtoSub = new ProcessoDetalheDto.UnidadeParticipanteDTO();
        unidadeDtoSub.setSigla("UNID-B");

        when(processoDetalheMapperInterface.toDetailDTO(processo)).thenReturn(baseDto);
        when(processoDetalheMapperInterface.subprocessoToUnidadeParticipanteDTO(subprocesso)).thenReturn(unidadeDtoSub);

        ProcessoDetalheDto resultDto = customMapper.toDetailDTO(processo, Collections.emptyList(), List.of(subprocesso));

        assertNotNull(resultDto);
        assertEquals(1, resultDto.getUnidades().size());
        assertEquals("UNID-B", resultDto.getUnidades().get(0).getSigla());
        verify(processoDetalheMapperInterface, times(1)).subprocessoToUnidadeParticipanteDTO(subprocesso);
        verify(processoDetalheMapperInterface, never()).unidadeProcessoToUnidadeParticipanteDTO(any());
    }
}