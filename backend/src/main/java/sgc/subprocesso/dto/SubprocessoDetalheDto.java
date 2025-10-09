package sgc.subprocesso.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO com os detalhes necessários para a tela de Detalhes do Subprocesso (CDU-07).
 */
@Data
@Builder
public class SubprocessoDetalheDto {
    private UnidadeDTO unidade;
    private ResponsavelDTO responsavel;
    private String situacao;
    private String localizacaoAtual;
    private LocalDate prazoEtapaAtual;
    private List<MovimentacaoDto> movimentacoes;
    private List<ElementoProcessoDTO> elementosDoProcesso;

    public record UnidadeDTO(
        Long codigo,
        String sigla,
        String nome
    ) {}

    public record ResponsavelDTO(
        Long id,
        String nome,
        String tipoResponsabilidade,
        String ramal,
        String email
    ) {}

    public record ElementoProcessoDTO(
        /**
         * Ex.: "ATIVIDADE", "CONHECIMENTO", "MAPA", "DIAGNOSTICO", etc.
         */
        String tipo,

        /**
         * Payload variável: pode ser AtividadeDto, ConhecimentoDto, MapaResumoDTO ou outro objeto.
         * Usamos Object para permitir flexibilidade no mapeamento do backend.
         */
        Object payload
    ) {}
}