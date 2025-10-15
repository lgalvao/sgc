package sgc.sgrh.dto;

import java.util.List;
import java.util.ArrayList;

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
    public UnidadeDto {
        subunidades = subunidades != null ? new ArrayList<>(subunidades) : null;
    }

    /**
     * Construtor sem subunidades.
     */
    public UnidadeDto(Long codigo, String nome, String sigla, Long codigoPai, String tipo) {
        this(codigo, nome, sigla, codigoPai, tipo, null);
    }

    @Override
    public List<UnidadeDto> subunidades() {
        return subunidades != null ? new ArrayList<>(subunidades) : null;
    }
}