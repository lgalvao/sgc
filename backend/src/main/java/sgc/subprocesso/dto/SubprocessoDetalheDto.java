package sgc.subprocesso.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO com os detalhes necessários para a tela de Detalhes do Subprocesso (CDU-07).
 */
public record SubprocessoDetalheDto(
        UnidadeDTO unidade,
        ResponsavelDTO responsavel,
        String situacao,
        String localizacaoAtual,
        LocalDate prazoEtapaAtual,
        List<MovimentacaoDto> movimentacoes,
        List<ElementoProcessoDTO> elementosDoProcesso
) {

    public record UnidadeDTO(
            Long codigo,
            String sigla,
            String nome
    ) {
    }

    public record ResponsavelDTO(
            Long id,
            String nome,
            String tipoResponsabilidade,
            String ramal,
            String email
    ) {
    }

    public record ElementoProcessoDTO(
            String tipo,
            Object payload
    ) {
    }
}