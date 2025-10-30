package sgc.sgrh.dto;

import java.util.ArrayList;
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