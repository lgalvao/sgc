package sgc.processo.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.processo.dto.mappers.ProcessoDetalheMapper;
import sgc.processo.dto.mappers.ProcessoDetalheMapperCustom;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.unidade.model.Unidade;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessoDetalheMapperCustomTest {

    @Mock
    private ProcessoDetalheMapper delegate;

    @InjectMocks
    private ProcessoDetalheMapperCustom customMapper;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Deve retornar nulo se o processo for nulo")
    void toDetailDTO_quandoProcessoNulo_retornaNulo() {
        assertNull(customMapper.toDetailDTO(null));
    }

    @Test
    @DisplayName("Deve mapear processo e associar Unidade com Subprocesso correspondente")
    void toDetailDTO_deveAssociarUnidadeESubprocesso() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("Processo Teste");

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setSigla("UNID-A");
        unidade.setNome("Unidade A");

        processo.setParticipantes(Set.of(unidade));

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

        when(delegate.toDetailDTO(processo)).thenReturn(baseDto);
        when(delegate.unidadeToUnidadeParticipanteDTO(unidade)).thenReturn(unidadeDto);

        ProcessoDetalheDto resultDto = customMapper.toDetailDTO(processo);

        assertNotNull(resultDto);
    }
}
