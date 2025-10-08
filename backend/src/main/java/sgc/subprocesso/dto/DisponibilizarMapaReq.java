package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO para requisição de disponibilização do mapa de competências (CDU-17).
 */
public record DisponibilizarMapaReq(
    String observacoes,  // Opcional
    @NotNull(message = "Data limite para validação é obrigatória")
    LocalDate dataLimiteEtapa2  // Obrigatório - prazo para validação
) {}