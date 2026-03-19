package sgc.subprocesso.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.SgcMensagens;

import java.time.*;

/**
 * DTO de requisição para criar um novo subprocesso.
 *
 * <p>
 * Usado exclusivamente como entrada de API para o endpoint de criação.
 * A validação Bean validation é aplicada neste DTO.
 */
@Builder
public record CriarSubprocessoRequest(
        @NotNull(message = SgcMensagens.CODIGO_PROCESSO_OBRIGATORIO)
        Long codProcesso,

        @NotNull(message = SgcMensagens.CODIGO_UNIDADE_OBRIGATORIO)
        Long codUnidade,

        Long codMapa,

        @NotNull(message = SgcMensagens.DATA_LIMITE_ETAPA1_OBRIGATORIA)
        LocalDateTime dataLimiteEtapa1,

        LocalDateTime dataLimiteEtapa2) {
}
