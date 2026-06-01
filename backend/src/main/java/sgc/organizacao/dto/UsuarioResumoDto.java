package sgc.organizacao.dto;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import org.jspecify.annotations.*;
import sgc.organizacao.model.OrganizacaoViews;

@Builder
public record UsuarioResumoDto(
        @JsonView(OrganizacaoViews.Publica.class)
        String tituloEleitoral,
        @JsonView(OrganizacaoViews.Publica.class)
        String matricula,
        @JsonView(OrganizacaoViews.Publica.class)
        String nome,
        @JsonView(OrganizacaoViews.Publica.class)
        String email,
        @JsonView(OrganizacaoViews.Publica.class)
        String ramal
) {
}
