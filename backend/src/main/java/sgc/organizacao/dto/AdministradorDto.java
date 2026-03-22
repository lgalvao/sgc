package sgc.organizacao.dto;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import sgc.comum.model.*;
import sgc.organizacao.model.*;

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
