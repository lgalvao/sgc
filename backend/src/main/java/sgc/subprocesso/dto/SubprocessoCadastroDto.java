package sgc.subprocesso.dto;

import lombok.Builder;
import lombok.Getter;
import sgc.atividade.dto.ConhecimentoDto;

import java.util.List;

/**
 * DTO agregado retornado pelo endpoint GET /api/subprocessos/{codigo}/cadastro
 * Estrutura:
 * {
 * subprocessoId: Long,
 * siglaUnidade: String,
 * atividades: [
 * { codigo: Long, descricao: String, conhecimentos: [ConhecimentoDto...] }
 * ]
 * }
 */
@Getter
@Builder
public class SubprocessoCadastroDto {
    private final Long subprocessoId;
    private final String unidadeSigla;
    private final List<AtividadeCadastroDto> atividades;

    @Getter
    @Builder
    public static class AtividadeCadastroDto {
        private final Long codigo;
        private final String descricao;
        private final List<ConhecimentoDto> conhecimentos;
    }
}