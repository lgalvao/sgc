package sgc.subprocesso.dto;

import lombok.Builder;
import lombok.Getter;
import sgc.mapa.dto.ConhecimentoResponse;

import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * DTO agregado retornado pelo endpoint GET /api/subprocessos/{codigo}/cadastro Estrutura: {
 * subprocessoId: Long, siglaUnidade: String, atividades: [ { codigo: Long, descricao: String,
 * conhecimentos: [ConhecimentoResponse...] } ] }
 */
@Getter
@Builder
public class SubprocessoCadastroDto {

    private final Long subprocessoCodigo;
    private final @Nullable String unidadeSigla;
    private final List<AtividadeCadastroDto> atividades;

    @Getter
    @Builder
    public static class AtividadeCadastroDto {
        private final Long codigo;
        private final String descricao;
        private final List<ConhecimentoResponse> conhecimentos;
    }
}
