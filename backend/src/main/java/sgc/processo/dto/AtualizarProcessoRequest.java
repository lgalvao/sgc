package sgc.processo.dto;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.*;
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
        @NotBlank(message = "Preencha a descrição") @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres") @SanitizarHtml String descricao,
        @NotNull(message = "Tipo do processo é obrigatório") TipoProcesso tipo,
        @NotNull(message = "Preencha a data limite") @Future(message = "A data limite deve ser futura") @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime dataLimiteEtapa1,
        @NotEmpty(message = "Pelo menos uma unidade participante deve ser incluída.") List<Long> unidades) {
}
