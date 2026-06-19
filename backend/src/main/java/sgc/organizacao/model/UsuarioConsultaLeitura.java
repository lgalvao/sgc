package sgc.organizacao.model;

import lombok.Builder;
import org.jspecify.annotations.Nullable;

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
        @Nullable String unidadeTituloTitular,
        @Nullable Long unidadeCompetenciaCodigo
) {
}
