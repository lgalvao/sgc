package sgc.mapa.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO para salvar um mapa completo. Usado para criar ou atualizar o mapa com todas as suas
 * competências e vínculos com atividades de uma vez.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalvarMapaRequest {
    /** Observações gerais sobre o mapa. */
    private String observacoes;

    /** Lista de competências do mapa. */
    @NotNull(message = "Lista de competências não pode ser nula")
    @Valid
    private List<CompetenciaMapaDto> competencias;
}
