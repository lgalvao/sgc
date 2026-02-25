package sgc.organizacao.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Builder;
import sgc.comum.model.TituloEleitoral;
import sgc.organizacao.model.OrganizacaoViews;

/**
 * DTO para representação de administrador.
 */
@Builder
public record AdministradorDto(
        @JsonView(OrganizacaoViews.Publica.class)
        @TituloEleitoral
        String tituloEleitoral,

        @JsonView(OrganizacaoViews.Publica.class) String nome,
        @JsonView(OrganizacaoViews.Publica.class) String matricula,
        @JsonView(OrganizacaoViews.Publica.class) Long unidadeCodigo,
        @JsonView(OrganizacaoViews.Publica.class) String unidadeSigla) {
}
