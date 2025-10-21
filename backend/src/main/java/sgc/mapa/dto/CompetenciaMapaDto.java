package sgc.mapa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO que representa uma competência no contexto do mapa completo.
 * Contém os dados da competência e os códigos das atividades vinculadas.
 *
 * @param codigo O código da competência (pode ser nulo para novas competências).
 * @param descricao A descrição da competência.
 * @param atividadesCodigos Lista com os códigos das atividades vinculadas à competência.
 */
public record CompetenciaMapaDto(
    Long codigo,  // null quando for nova competência
    
    @NotBlank(message = "Descrição da competência é obrigatória")
    String descricao,
    
    @NotNull(message = "Lista de atividades não pode ser nula")
    List<Long> atividadesCodigos  // IDs das atividades vinculadas à competência
) {
    public CompetenciaMapaDto {
        atividadesCodigos = new ArrayList<>(atividadesCodigos);
    }

    @Override
    public List<Long> atividadesCodigos() {
        return new ArrayList<>(atividadesCodigos);
    }
}