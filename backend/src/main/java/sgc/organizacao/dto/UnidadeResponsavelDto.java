package sgc.organizacao.dto;

import lombok.*;
import org.jspecify.annotations.*;

/**
 * DTO para dados de responsável (titular/substituto) de uma unidade.
 */
@Builder
public record UnidadeResponsavelDto(
        Long unidadeCodigo,
        String titularTitulo,
        String titularNome,
        @Nullable String substitutoTitulo,
        @Nullable String substitutoNome) {
}
