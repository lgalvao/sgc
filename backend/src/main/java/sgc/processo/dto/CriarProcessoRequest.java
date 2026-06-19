package sgc.processo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Builder;
import sgc.comum.Mensagens;
import sgc.processo.model.TipoProcesso;
import sgc.seguranca.sanitizacao.SanitizarHtml;

import java.time.LocalDateTime;
import java.util.List;

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
