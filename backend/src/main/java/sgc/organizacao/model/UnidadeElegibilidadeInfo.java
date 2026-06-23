package sgc.organizacao.model;

import org.jspecify.annotations.*;

/**
 * Dados escalares de uma unidade usados para verificação de elegibilidade.
 * Evita a construção de entidade JPA falsa para testar predicados de negócio.
 */
public record UnidadeElegibilidadeInfo(
        Long codigo,
        TipoUnidade tipo,
        @Nullable String tituloResponsavel
) {
    public boolean possuiResponsavelEfetivo() {
        return tituloResponsavel != null && !tituloResponsavel.isBlank();
    }
}
