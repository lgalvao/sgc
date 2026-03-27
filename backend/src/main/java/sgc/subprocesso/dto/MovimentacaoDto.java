package sgc.subprocesso.dto;

import lombok.*;
import org.jspecify.annotations.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.model.*;

import java.time.*;

@Builder
public record MovimentacaoDto(
        @Nullable Long codigo,
        LocalDateTime dataHora,
        @Nullable Long unidadeOrigemCodigo,
        String unidadeOrigemSigla,
        String unidadeOrigemNome,
        @Nullable Long unidadeDestinoCodigo,
        @Nullable String unidadeDestinoSigla,
        @Nullable String unidadeDestinoNome,
        String usuarioTitulo,
        String usuarioNome,
        String descricao
) {
    public static MovimentacaoDto from(Movimentacao m) {
        Unidade unidadeOrigem = m.getUnidadeOrigem();
        @Nullable Unidade unidadeDestino = m.getUnidadeDestino();
        Usuario usuario = m.getUsuario();

        return MovimentacaoDto.builder()
                .codigo(m.getCodigo())
                .dataHora(m.getDataHora())
                .unidadeOrigemCodigo(unidadeOrigem.getCodigo())
                .unidadeOrigemSigla(unidadeOrigem.getSigla())
                .unidadeOrigemNome(unidadeOrigem.getNome())
                .unidadeDestinoCodigo(unidadeDestino != null ? unidadeDestino.getCodigo() : null)
                .unidadeDestinoSigla(unidadeDestino != null ? unidadeDestino.getSigla() : null)
                .unidadeDestinoNome(unidadeDestino != null ? unidadeDestino.getNome() : null)
                .usuarioTitulo(usuario.getTituloEleitoral())
                .usuarioNome(usuario.getNome())
                .descricao(m.getDescricao())
                .build();
    }
}
