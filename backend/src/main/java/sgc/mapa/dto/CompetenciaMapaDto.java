package sgc.mapa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.jspecify.annotations.Nullable;
import sgc.seguranca.sanitizacao.SanitizarHtml;

import java.util.List;

/**
 * DTO que representa uma competência no contexto do mapa completo. Contém os
 * dados da competência e
 * os códigos das atividades vinculadas.
 */
@Builder
public record CompetenciaMapaDto(
        @Nullable Long codigo,

                @NotBlank(message = "Descrição da competência é obrigatória") @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres") @SanitizarHtml String descricao,

                @NotEmpty(message = "Lista de atividades não pode ser vazia") List<Long> atividadesCodigos) {
}
