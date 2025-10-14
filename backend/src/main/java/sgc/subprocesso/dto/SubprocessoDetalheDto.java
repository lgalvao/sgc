package sgc.subprocesso.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO com os detalhes necessários para a tela de Detalhes do Subprocesso (CDU-07).
 */
@Getter
@Builder
public class SubprocessoDetalheDto {
    private final UnidadeDTO unidade;
    private final ResponsavelDTO responsavel;
    private final String situacao;
    private final String localizacaoAtual;
    private final LocalDate prazoEtapaAtual;
    private final List<MovimentacaoDto> movimentacoes;
    private final List<ElementoProcessoDTO> elementosDoProcesso;

    @Getter
    @Builder
    public static class UnidadeDTO {
        private final Long codigo;
        private final String sigla;
        private final String nome;
    }

    @Getter
    @Builder
    public static class ResponsavelDTO {
        private final Long id;
        private final String nome;
        private final String tipoResponsabilidade;
        private final String ramal;
        private final String email;
    }

    public record ElementoProcessoDTO(
            String tipo,
            Object payload
    ) {
    }
}