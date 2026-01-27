package sgc.processo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import sgc.processo.model.TipoProcesso;
import sgc.seguranca.sanitizacao.SanitizarHtml;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de requisição para criar um processo.
 */
@Builder
public record CriarProcessoRequest(
                @NotBlank(message = "Preencha a descrição") @SanitizarHtml String descricao,

                @NotNull(message = "Tipo do processo é obrigatório") TipoProcesso tipo,

                @NotNull(message = "Preencha a data limite") @Future(message = "A data limite deve ser futura") @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime dataLimiteEtapa1,

                @NotEmpty(message = "Pelo menos uma unidade participante deve ser incluída.") List<Long> unidades) {
}
