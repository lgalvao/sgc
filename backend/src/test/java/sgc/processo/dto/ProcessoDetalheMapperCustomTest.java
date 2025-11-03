package sgc.processo.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.SituacaoProcesso;
import sgc.processo.modelo.UnidadeProcesso;
import sgc.subprocesso.modelo.SituacaoSubprocesso;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.unidade.modelo.Unidade;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessoDetalheMapperCustomTest {
    @Mock
    private ProcessoDetalheMapper processoDetalheMapper;

    private ProcessoDetalheMapperCustom customMapper;

    @BeforeEach
    void setUp() {
        customMapper = new ProcessoDetalheMapperCustom(processoDetalheMapper);
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
        unidadeProcesso.setCodProcesso(processo.getCodigo());
        unidadeProcesso.setCodUnidade(unidade.getCodigo());
        unidadeProcesso.setSigla(unidade.getSigla());

        Subprocesso subprocesso = new Subprocesso(
                processo,
                unidade,
                null,
                SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO,
                LocalDateTime.now()
        );

        var baseDto = ProcessoDetalheDto.builder()
            .codigo(1L)
            .descricao("Processo Teste")
            .tipo("TIPO")
            .situacao(SituacaoProcesso.CRIADO)
            .build();
        var unidadeDto = ProcessoDetalheDto.UnidadeParticipanteDto.builder()
            .codUnidade(10L)
            .nome("Unidade A")
            .sigla("UNID-A")
            .build();

        when(processoDetalheMapper.toDetailDTO(processo)).thenReturn(baseDto);
        when(processoDetalheMapper.unidadeProcessoToUnidadeParticipanteDTO(unidadeProcesso)).thenReturn(unidadeDto);

        ProcessoDetalheDto resultDto = customMapper.toDetailDTO(processo, List.of(unidadeProcesso), List.of(subprocesso));

        assertNotNull(resultDto);
        assertEquals(1, resultDto.getUnidades().size());
        ProcessoDetalheDto.UnidadeParticipanteDto resultUnidade = resultDto.getUnidades().getFirst();
        assertEquals(subprocesso.getSituacao(), resultUnidade.getSituacaoSubprocesso());
        assertEquals(subprocesso.getDataLimiteEtapa1(), resultUnidade.getDataLimite());
        verify(processoDetalheMapper, never()).subprocessoToUnidadeParticipanteDTO(any());
    }

    @Test
    @DisplayName("Deve criar nova unidade participante se Subprocesso não tem UnidadeProcesso correspondente")
    void toDetailDTO_deveCriarUnidadeParaSubprocessoSemCorrespondencia() {
        Processo processo = new Processo();
        processo.setCodigo(1L);

        Unidade unidadeSub = new Unidade();
        unidadeSub.setCodigo(20L);
        unidadeSub.setSigla("UNID-B");

        Subprocesso subprocesso = new Subprocesso(processo, unidadeSub, null, SituacaoSubprocesso.NAO_INICIADO, LocalDateTime.now());

        var baseDto = ProcessoDetalheDto.builder().codigo(1L).build();
        var unidadeDtoSub = ProcessoDetalheDto.UnidadeParticipanteDto.builder()
            .codUnidade(20L)
            .nome("Unidade B")
            .sigla("UNID-B")
            .situacaoSubprocesso(SituacaoSubprocesso.NAO_INICIADO)
            .dataLimite(LocalDateTime.now())
            .build();

        when(processoDetalheMapper.toDetailDTO(processo)).thenReturn(baseDto);
        when(processoDetalheMapper.subprocessoToUnidadeParticipanteDTO(subprocesso)).thenReturn(unidadeDtoSub);

        ProcessoDetalheDto resultDto = customMapper.toDetailDTO(processo, Collections.emptyList(), List.of(subprocesso));

        assertNotNull(resultDto);
        assertEquals(1, resultDto.getUnidades().size());
        assertEquals("UNID-B", resultDto.getUnidades().getFirst().getSigla());
        verify(processoDetalheMapper, times(1)).subprocessoToUnidadeParticipanteDTO(subprocesso);
        verify(processoDetalheMapper, never()).unidadeProcessoToUnidadeParticipanteDTO(any());
    }
}
