package sgc.organizacao.dto;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
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
    public static UsuarioResumoDto fromEntity(Usuario usuario) {
        if (usuario == null) return null;
        return UsuarioResumoDto.builder()
                .tituloEleitoral(usuario.getTituloEleitoral())
                .matricula(usuario.getMatricula())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .ramal(usuario.getRamal())
                .build();
    }
}
