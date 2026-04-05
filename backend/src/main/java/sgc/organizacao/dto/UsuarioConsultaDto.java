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
        UnidadeResumoDto unidade,
        List<String> perfis) {

    public static UsuarioConsultaDto fromEntity(Usuario usuario) {
        Unidade unidadeLotacao = usuario.getUnidadeLotacao();

        UnidadeResumoDto unidade = UnidadeResumoDto.fromEntityObrigatoria(unidadeLotacao);

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
        UnidadeResumoDto unidade = UnidadeResumoDto.fromResumoObrigatorio(
                usuario.unidadeCodigo(),
                usuario.unidadeNome(),
                usuario.unidadeSigla(),
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
