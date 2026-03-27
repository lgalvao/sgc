package sgc.organizacao.dto;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import org.jspecify.annotations.*;
import sgc.organizacao.model.*;

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
    public static @Nullable UsuarioResumoDto fromEntity(@Nullable Usuario usuario) {
        if (usuario == null) return null;
        return fromEntityObrigatorio(usuario);
    }

    public static UsuarioResumoDto fromEntityObrigatorio(Usuario usuario) {
        return UsuarioResumoDto.builder()
                .tituloEleitoral(usuario.getTituloEleitoral())
                .matricula(usuario.getMatricula())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .ramal(usuario.getRamal())
                .build();
    }
}
