package sgc.processo.dto;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.*;
import sgc.processo.model.*;
import sgc.seguranca.sanitizacao.*;

import java.time.*;
import java.util.*;

/**
 * DTO de requisição para criar um processo.
 */
@Builder
public record CriarProcessoRequest(
        @NotBlank(message = Mensagens.DESCRICAO_OBRIGATORIA) @Size(max = 255, message = Mensagens.DESCRICAO_MAX) @SanitizarHtml String descricao,
        @NotNull(message = Mensagens.TIPO_PROCESSO_OBRIGATORIO) TipoProcesso tipo,
        @NotNull(message = Mensagens.DATA_LIMITE_OBRIGATORIA) @Future(message = Mensagens.DATA_LIMITE_FUTURA) @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime dataLimiteEtapa1,
        @NotEmpty(message = Mensagens.UNIDADES_PARTICIPANTES_OBRIGATORIO) List<Long> unidades) {
}
