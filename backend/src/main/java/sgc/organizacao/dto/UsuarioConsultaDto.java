package sgc.organizacao.dto;

import lombok.*;
import org.jspecify.annotations.*;
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

        UnidadeDto unidade = UnidadeDto.fromEntityObrigatoria(unidadeLotacao);

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

    public static UsuarioConsultaDto fromLeitura(UsuarioConsultaLeitura usuario) {
        UnidadeDto unidade = UnidadeDto.builder()
                .codigo(usuario.unidadeCodigo())
                .nome(usuario.unidadeNome())
                .sigla(usuario.unidadeSigla())
                .tipo(usuario.unidadeTipo() != null ? usuario.unidadeTipo().name() : null)
                .tituloTitular(usuario.unidadeTituloTitular())
                .build();

        return UsuarioConsultaDto.builder()
                .tituloEleitoral(usuario.tituloEleitoral())
                .matricula(usuario.matricula())
                .nome(usuario.nome())
                .email(usuario.email())
                .ramal(usuario.ramal())
                .unidade(unidade)
                .perfis(List.of())
                .build();
    }
}
