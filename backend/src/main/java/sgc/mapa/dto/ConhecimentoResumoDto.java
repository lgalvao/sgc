package sgc.mapa.dto;

import org.jspecify.annotations.*;
import sgc.mapa.model.*;

public record ConhecimentoResumoDto(
        @Nullable Long codigo,
        String descricao) {

    public static ConhecimentoResumoDto fromEntity(Conhecimento conhecimento) {
        return new ConhecimentoResumoDto(
                conhecimento.getCodigo(),
                conhecimento.getDescricao());
    }
}

