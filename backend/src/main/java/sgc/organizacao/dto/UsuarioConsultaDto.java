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
        UnidadeDto unidade = UnidadeDto.fromResumoObrigatorio(
                usuario.unidadeCodigo(),
                usuario.unidadeNome(),
                usuario.unidadeSigla(),
                null,
                usuario.unidadeTipo(),
                usuario.unidadeTituloTitular()
        );

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
