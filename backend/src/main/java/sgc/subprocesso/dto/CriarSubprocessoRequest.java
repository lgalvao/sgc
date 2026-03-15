package sgc.subprocesso.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.MsgValidacao;

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
        @NotNull(message = MsgValidacao.CODIGO_PROCESSO_OBRIGATORIO)
        Long codProcesso,

        @NotNull(message = MsgValidacao.CODIGO_UNIDADE_OBRIGATORIO)
        Long codUnidade,

        Long codMapa,

        @NotNull(message = MsgValidacao.DATA_LIMITE_ETAPA1_OBRIGATORIA)
        LocalDateTime dataLimiteEtapa1,

        LocalDateTime dataLimiteEtapa2) {
}
