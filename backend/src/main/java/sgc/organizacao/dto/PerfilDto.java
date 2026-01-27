package sgc.organizacao.dto;

import lombok.Builder;
import sgc.organizacao.model.Perfil;

/**
 * DTO para representação de perfil com dados de usuário e unidade.
 */
@Builder
public record PerfilDto(
                String usuarioTitulo,
                Long unidadeCodigo,
                String unidadeNome,
                String perfil,
                String descricao) {

        /**
         * Construtor alternativo para criar PerfilDto apenas a partir de um Perfil
         * (enum).
         */
        public static PerfilDto from(Perfil perfil) {
                return new PerfilDto(null, null, null, perfil.name(), perfil.name());
        }
}
