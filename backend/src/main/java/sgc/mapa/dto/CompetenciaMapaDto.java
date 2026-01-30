package sgc.mapa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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

                @NotBlank(message = "Descrição da competência é obrigatória") @SanitizarHtml String descricao,

                @NotEmpty(message = "Lista de atividades não pode ser vazia") List<Long> atividadesCodigos) {
}
