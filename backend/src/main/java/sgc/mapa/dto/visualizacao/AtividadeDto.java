package sgc.mapa.dto.visualizacao;

import java.util.List;

import java.util.ArrayList;

public record AtividadeDto(
    Long id,
    String descricao,
    List<ConhecimentoDto> conhecimentos
) {
    public AtividadeDto {
        conhecimentos = new ArrayList<>(conhecimentos);
    }

    @Override
    public List<ConhecimentoDto> conhecimentos() {
        return new ArrayList<>(conhecimentos);
    }
}