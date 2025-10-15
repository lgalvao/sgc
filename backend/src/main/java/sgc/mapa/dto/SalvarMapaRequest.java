package sgc.mapa.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request DTO para salvar um mapa completo.
 * Usado para criar ou atualizar o mapa com todas as suas competências
 * e vínculos com atividades de uma vez.
 */
import java.util.ArrayList;

public record SalvarMapaRequest(
    String observacoes,
    
    @NotNull(message = "Lista de competências não pode ser nula")
    @Valid
    List<CompetenciaMapaDto> competencias
) {
    public SalvarMapaRequest {
        competencias = new ArrayList<>(competencias);
    }

    @Override
    public List<CompetenciaMapaDto> competencias() {
        return new ArrayList<>(competencias);
    }
}