package sgc.organizacao.model;

import lombok.*;
import org.jspecify.annotations.*;

@Builder
public record UsuarioConsultaLeitura(
        String tituloEleitoral,
        String matricula,
        String nome,
        String email,
        String ramal,
        Long unidadeCodigo,
        String unidadeNome,
        String unidadeSigla,
        @Nullable TipoUnidade unidadeTipo,
        @Nullable String unidadeTituloTitular
) {
}
