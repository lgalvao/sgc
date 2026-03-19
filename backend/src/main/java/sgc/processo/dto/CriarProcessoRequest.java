package sgc.processo.dto;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.SgcMensagens;
import sgc.processo.model.*;
import sgc.seguranca.sanitizacao.*;

import java.time.*;
import java.util.*;

/**
 * DTO de requisição para criar um processo.
 */
@Builder
public record CriarProcessoRequest(
        @NotBlank(message = SgcMensagens.DESCRICAO_OBRIGATORIA) @Size(max = 255, message = SgcMensagens.DESCRICAO_MAX) @SanitizarHtml String descricao,
        @NotNull(message = SgcMensagens.TIPO_PROCESSO_OBRIGATORIO) TipoProcesso tipo,
        @NotNull(message = SgcMensagens.DATA_LIMITE_OBRIGATORIA) @Future(message = SgcMensagens.DATA_LIMITE_FUTURA) @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime dataLimiteEtapa1,
        @NotEmpty(message = SgcMensagens.UNIDADES_PARTICIPANTES_OBRIGATORIO) List<Long> unidades) {
}
