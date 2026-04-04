package sgc.organizacao.model;

import org.jspecify.annotations.*;

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
