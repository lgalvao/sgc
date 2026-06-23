package sgc.organizacao.dto;

import lombok.*;

import java.util.*;

@Builder
public record UnidadeResumoDto(
        Long codigo,
        String nome,
        String sigla,
        String tipo,
        String tituloTitular) {

    public static UnidadeResumoDto fromResumoObrigatorio(
            Long codigo,
            String nome,
            String sigla,
            String tipo,
            String tituloTitular
    ) {
        return UnidadeResumoDto.builder()
                .codigo(Objects.requireNonNull(codigo, "Codigo da unidade obrigatorio"))
                .nome(Objects.requireNonNull(nome, "Nome da unidade obrigatorio"))
                .sigla(Objects.requireNonNull(sigla, "Sigla da unidade obrigatoria"))
                .tipo(tipo)
                .tituloTitular(tituloTitular)
                .build();
    }
}
