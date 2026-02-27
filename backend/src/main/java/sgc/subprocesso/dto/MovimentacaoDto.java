package sgc.subprocesso.dto;

import lombok.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.model.*;

import java.time.*;

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
        String usuarioTitulo,
        String usuarioNome,
        String descricao
) {
    public static MovimentacaoDto from(Movimentacao m) {
        Unidade unidadeOrigem = m.getUnidadeOrigem();
        Unidade unidadeDestino = m.getUnidadeDestino();
        Usuario usuario = m.getUsuario();

        return MovimentacaoDto.builder()
                .codigo(m.getCodigo())
                .dataHora(m.getDataHora())
                .unidadeOrigemCodigo(unidadeOrigem.getCodigo())
                .unidadeOrigemSigla(unidadeOrigem.getSigla())
                .unidadeOrigemNome(unidadeOrigem.getNome())
                .unidadeDestinoCodigo(unidadeDestino.getCodigo())
                .unidadeDestinoSigla(unidadeDestino.getSigla())
                .unidadeDestinoNome(unidadeDestino.getNome())
                .usuarioTitulo(usuario.getTituloEleitoral())
                .usuarioNome(usuario.getNome())
                .descricao(m.getDescricao())
                .build();
    }
}
