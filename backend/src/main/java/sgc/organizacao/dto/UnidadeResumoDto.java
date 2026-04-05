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
            String nome,
            String sigla,
            TipoUnidade tipo,
            String tituloTitular
    ) {
        return UnidadeResumoDto.builder()
                .codigo(Objects.requireNonNull(codigo, "Codigo da unidade obrigatorio"))
                .nome(Objects.requireNonNull(nome, "Nome da unidade obrigatorio"))
                .sigla(Objects.requireNonNull(sigla, "Sigla da unidade obrigatoria"))
                .tipo(tipo != null ? tipo.name() : null)
                .tituloTitular(tituloTitular)
                .build();
    }
}
