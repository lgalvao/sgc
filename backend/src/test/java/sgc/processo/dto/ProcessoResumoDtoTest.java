package sgc.processo.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.processo.ProcessoDtoMapper;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProcessoResumoDto")
class ProcessoResumoDtoTest {
    private final ProcessoDtoMapper mapper = new ProcessoDtoMapper();


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
                .dataFinalizacao(LocalDateTime.of(2025, 2, 1, 15, 30))
                .dataLimite(LocalDateTime.of(2025, 1, 30, 10, 0))
                .build();
        processo.setCodigo(5L);
        processo.adicionarParticipantes(Set.of(unidade));

        ProcessoResumoDto dto = mapper.paraResumo(processo);

        assertThat(dto.codigo()).isEqualTo(5L);
        assertThat(dto.tipo()).isEqualTo("MAPEAMENTO");
        assertThat(dto.dataFinalizacao()).isEqualTo(LocalDateTime.of(2025, 2, 1, 15, 30));
        assertThat(dto.unidadesParticipantes()).isEqualTo("UND");
    }
}
