package sgc.mapa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.ArrayList;

/**
 * DTO que representa uma competência no contexto do mapa completo.
 * Contém os dados da competência e os códigos das atividades vinculadas.
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