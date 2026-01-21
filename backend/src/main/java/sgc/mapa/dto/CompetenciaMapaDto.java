package sgc.mapa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import org.jspecify.annotations.Nullable;
import java.util.List;

/**
 * DTO que representa uma competência no contexto do mapa completo. Contém os dados da competência e
 * os códigos das atividades vinculadas.
 */
@Getter
@Builder
@AllArgsConstructor
public class CompetenciaMapaDto {

    /**
     * O código da competência (pode ser nulo para novas competências).
     */
    @Nullable
    private final Long codigo;

    /**
     * A descrição da competência.
     */
    @NotBlank(message = "Descrição da competência é obrigatória")
    private final String descricao;

    /**
     * Lista com os códigos das atividades vinculadas à competência.
     */
    @NotEmpty(message = "Lista de atividades não pode ser vazia")
    private final List<Long> atividadesCodigos;
}
