package sgc.organizacao.dto;

import lombok.*;
import sgc.organizacao.model.*;

import java.util.*;

@Builder
public record UsuarioConsultaDto(
        String tituloEleitoral,
        String matricula,
        String nome,
        String email,
        String ramal,
        UnidadeDto unidade,
        List<String> perfis) {

    public static UsuarioConsultaDto fromEntity(Usuario usuario) {
        Unidade unidadeLotacao = usuario.getUnidadeLotacao();

        UnidadeDto unidade = unidadeLotacao != null
                ? UnidadeDto.builder()
                .codigo(unidadeLotacao.getCodigo())
                .nome(unidadeLotacao.getNome())
                .sigla(unidadeLotacao.getSigla())
                .tipo(unidadeLotacao.getTipo() != null ? unidadeLotacao.getTipo().name() : null)
                .tituloTitular(unidadeLotacao.getTituloTitular())
                .build()
                : null;

        return UsuarioConsultaDto.builder()
                .tituloEleitoral(usuario.getTituloEleitoral())
                .matricula(usuario.getMatricula())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .ramal(usuario.getRamal())
                .unidade(unidade)
                .perfis(List.of())
                .build();
    }
}
