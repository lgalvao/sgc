package sgc.parametros;

import lombok.*;
import sgc.parametros.model.*;

@Builder
public record ParametroDto(
        Long codigo,
        String chave,
        String descricao,
        String valor) {

    public static ParametroDto fromEntity(Parametro parametro) {
        return ParametroDto.builder()
                .codigo(parametro.getCodigo())
                .chave(parametro.getChave())
                .descricao(parametro.getDescricao())
                .valor(parametro.getValor())
                .build();
    }
}
