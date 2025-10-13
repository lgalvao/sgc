package sgc.processo.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.modelo.SituacaoProcesso;
import sgc.comum.modelo.SituacaoSubprocesso;
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

    private ProcessoDetalheMapperCustomizado customMapper;

    @BeforeEach
    void setUp() {
        customMapper = new ProcessoDetalheMapperCustomizado(processoDetalheMapperInterface);
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

        Subprocesso subprocesso = new Subprocesso(processo, unidade, null, SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO, LocalDate.of(2025, 10, 31));

        var baseDto = ProcessoDetalheDto.builder()
            .codigo(1L)
            .descricao("Processo Teste")
            .tipo("TIPO")
            .situacao(SituacaoProcesso.CRIADO)
            .build();
        var unidadeDto = ProcessoDetalheDto.UnidadeParticipanteDTO.builder()
            .unidadeCodigo(10L)
            .nome("Unidade A")
            .sigla("UNID-A")
            .build();


        when(processoDetalheMapperInterface.toDetailDTO(processo)).thenReturn(baseDto);
        when(processoDetalheMapperInterface.unidadeProcessoToUnidadeParticipanteDTO(unidadeProcesso)).thenReturn(unidadeDto);

        ProcessoDetalheDto resultDto = customMapper.toDetailDTO(processo, List.of(unidadeProcesso), List.of(subprocesso));

        assertNotNull(resultDto);
        assertEquals(1, resultDto.getUnidades().size());
        ProcessoDetalheDto.UnidadeParticipanteDTO resultUnidade = resultDto.getUnidades().getFirst();
        assertEquals(subprocesso.getSituacao(), resultUnidade.getSituacaoSubprocesso());
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

        Subprocesso subprocesso = new Subprocesso(processo, unidadeSub, null, SituacaoSubprocesso.NAO_INICIADO, LocalDate.of(2025, 11, 30));

        var baseDto = ProcessoDetalheDto.builder().codigo(1L).build();
        var unidadeDtoSub = ProcessoDetalheDto.UnidadeParticipanteDTO.builder()
            .unidadeCodigo(20L)
            .nome("Unidade B")
            .sigla("UNID-B")
            .situacaoSubprocesso(SituacaoSubprocesso.NAO_INICIADO)
            .dataLimite(LocalDate.of(2025, 11, 30))
            .build();

        when(processoDetalheMapperInterface.toDetailDTO(processo)).thenReturn(baseDto);
        when(processoDetalheMapperInterface.subprocessoToUnidadeParticipanteDTO(subprocesso)).thenReturn(unidadeDtoSub);

        ProcessoDetalheDto resultDto = customMapper.toDetailDTO(processo, Collections.emptyList(), List.of(subprocesso));

        assertNotNull(resultDto);
        assertEquals(1, resultDto.getUnidades().size());
        assertEquals("UNID-B", resultDto.getUnidades().getFirst().getSigla());
        verify(processoDetalheMapperInterface, times(1)).subprocessoToUnidadeParticipanteDTO(subprocesso);
        verify(processoDetalheMapperInterface, never()).unidadeProcessoToUnidadeParticipanteDTO(any());
    }
}
