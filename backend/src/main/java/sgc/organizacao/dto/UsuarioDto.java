package sgc.organizacao.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Builder;
import sgc.comum.validacao.TituloEleitoral;
import sgc.organizacao.model.OrganizacaoViews;

/**
 * DTO para representação de usuário.
 */
@Builder
public record UsuarioDto(
        @JsonView(OrganizacaoViews.Publica.class) @TituloEleitoral String tituloEleitoral,
        @JsonView(OrganizacaoViews.Publica.class) String nome,
        @JsonView(OrganizacaoViews.Publica.class) String email,
        @JsonView(OrganizacaoViews.Publica.class) String matricula,
        @JsonView(OrganizacaoViews.Publica.class) Long unidadeCodigo) {
}
