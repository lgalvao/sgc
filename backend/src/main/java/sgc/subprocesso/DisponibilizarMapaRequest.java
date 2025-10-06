package sgc.subprocesso;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO para requisição de disponibilização do mapa de competências (CDU-17).
 */
public record DisponibilizarMapaRequest(
    String observacoes,  // Opcional
    @NotNull(message = "Data limite para validação é obrigatória")
    LocalDate dataLimiteEtapa2  // Obrigatório - prazo para validação
) {}