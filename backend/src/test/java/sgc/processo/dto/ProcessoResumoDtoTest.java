package sgc.processo.dto;

import org.junit.jupiter.api.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ProcessoResumoDto")
class ProcessoResumoDtoTest {

    @Test
    @DisplayName("deve mapear processo com participantes")
    void deveMapearProcessoComParticipantes() {
        Unidade unidade = Unidade.builder()
                .codigo(10L)
                .nome("Unidade")
                .sigla("UND")
                .matriculaTitular("12345678")
                .tituloTitular("123456789012")
                .dataInicioTitularidade(LocalDateTime.now())
                .tipo(TipoUnidade.OPERACIONAL)
                .situacao(SituacaoUnidade.ATIVA)
                .build();

        Processo processo = Processo.builder()
                .descricao("Processo")
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .tipo(TipoProcesso.MAPEAMENTO)
                .dataCriacao(LocalDateTime.of(2025, 1, 1, 10, 0))
                .dataLimite(LocalDateTime.of(2025, 1, 30, 10, 0))
                .build();
        processo.setCodigo(5L);
        processo.adicionarParticipantes(Set.of(unidade));

        ProcessoResumoDto dto = ProcessoResumoDto.fromEntity(processo);

        assertThat(dto.codigo()).isEqualTo(5L);
        assertThat(dto.tipo()).isEqualTo("MAPEAMENTO");
        assertThat(dto.unidadesParticipantes()).isEqualTo("UND");
    }
}
