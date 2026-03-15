package sgc.processo.dto;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.MsgValidacao;
import sgc.processo.model.*;
import sgc.seguranca.sanitizacao.*;

import java.time.*;
import java.util.*;

/**
 * DTO usado para atualizar um processo existente.
 */
@Builder
public record AtualizarProcessoRequest(
        Long codigo,
        @NotBlank(message = MsgValidacao.DESCRICAO_OBRIGATORIA) @Size(max = 255, message = MsgValidacao.DESCRICAO_MAX) @SanitizarHtml String descricao,
        @NotNull(message = MsgValidacao.TIPO_PROCESSO_OBRIGATORIO) TipoProcesso tipo,
        @NotNull(message = MsgValidacao.DATA_LIMITE_OBRIGATORIA) @Future(message = MsgValidacao.DATA_LIMITE_FUTURA) @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime dataLimiteEtapa1,
        @NotEmpty(message = MsgValidacao.UNIDADES_PARTICIPANTES_OBRIGATORIO) List<Long> unidades) {
}
