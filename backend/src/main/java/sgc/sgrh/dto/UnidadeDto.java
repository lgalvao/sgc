package sgc.sgrh.dto;

import java.util.List;

/**
 * DTO para dados de unidade do SGRH.
 * Suporta estrutura hierárquica com subunidades.
 */
public record UnidadeDto(
    Long codigo,
    String nome,
    String sigla,
    Long codigoPai,
    String tipo,
    List<UnidadeDto> subunidades  // Para árvore hierárquica
) {
    /**
     * Construtor sem subunidades.
     */
    public UnidadeDto(Long codigo, String nome, String sigla, Long codigoPai, String tipo) {
        this(codigo, nome, sigla, codigoPai, tipo, null);
    }
}