package sgc.subprocesso.dto;

import lombok.*;

import java.time.*;

/**
 * DTO de resposta representando uma movimentação no histórico.
 */
@Builder
public record MovimentacaoDto(
        Long codigo,
        LocalDateTime dataHora,

        Long unidadeOrigemCodigo,
        String unidadeOrigemSigla,
        String unidadeOrigemNome,
        Long unidadeDestinoCodigo,
        String unidadeDestinoSigla,
        String unidadeDestinoNome,
    String descricao
) {
    public static MovimentacaoDto from(sgc.subprocesso.model.Movimentacao m) {
        if (m == null) return null;
        
        return MovimentacaoDto.builder()
                .codigo(m.getCodigo())
                .dataHora(m.getDataHora())
                .unidadeOrigemCodigo(m.getUnidadeOrigem() != null ? m.getUnidadeOrigem().getCodigo() : null)
                .unidadeOrigemSigla(m.getUnidadeOrigem() != null ? mapUnidadeSiglaParaUsuario(m.getUnidadeOrigem()) : null)
                .unidadeOrigemNome(m.getUnidadeOrigem() != null ? m.getUnidadeOrigem().getNome() : null)
                .unidadeDestinoCodigo(m.getUnidadeDestino() != null ? m.getUnidadeDestino().getCodigo() : null)
                .unidadeDestinoSigla(m.getUnidadeDestino() != null ? mapUnidadeSiglaParaUsuario(m.getUnidadeDestino()) : null)
                .unidadeDestinoNome(m.getUnidadeDestino() != null ? m.getUnidadeDestino().getNome() : null)
                .descricao(m.getDescricao())
                .build();
    }

    private static String mapUnidadeSiglaParaUsuario(sgc.organizacao.model.Unidade unidade) {
        if (unidade == null || unidade.getCodigo() == null) return null;
        return java.util.Objects.equals(unidade.getCodigo(), 1L) ? "ADMIN" : unidade.getSigla();
    }
}
