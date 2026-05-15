package sgc.mapa.dto;

import sgc.mapa.model.*;

public record ConhecimentoResumoDto(
        Long codigo,
        String descricao) {

    public static ConhecimentoResumoDto fromEntity(Conhecimento conhecimento) {
        return new ConhecimentoResumoDto(
                conhecimento.getCodigo(),
                conhecimento.getDescricao());
    }
}
