package sgc.organizacao.dto;

import lombok.*;
import org.jspecify.annotations.*;
import sgc.organizacao.model.*;

import java.util.*;

@Builder
public record UnidadeResumoDto(
        Long codigo,
        String nome,
        String sigla,
        @Nullable String tipo,
        @Nullable String tituloTitular) {

    public static UnidadeResumoDto fromEntityObrigatoria(Unidade unidade) {
        Objects.requireNonNull(unidade, "Unidade obrigatoria para resumo");
        return fromResumoObrigatorio(
                unidade.getCodigo(),
                unidade.getNome(),
                unidade.getSigla(),
                unidade.getTipo(),
                unidade.getTituloTitular()
        );
    }

    public static UnidadeResumoDto fromResumoObrigatorio(
            Long codigo,
            @Nullable String nome,
            @Nullable String sigla,
            TipoUnidade tipo,
            String tituloTitular
    ) {
        return UnidadeResumoDto.builder()
                .codigo(Objects.requireNonNull(codigo, "Codigo da unidade obrigatorio"))
                .nome(nome != null ? nome : "Unidade Desconhecida")
                .sigla(sigla != null ? sigla : "UNID")
                .tipo(tipo != null ? tipo.name() : null)
                .tituloTitular(tituloTitular)
                .build();
    }
}
