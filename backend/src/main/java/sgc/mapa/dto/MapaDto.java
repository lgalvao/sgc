package sgc.mapa.dto;

import java.time.LocalDateTime;

/**
 * DTO para Mapa usado nas APIs.
 */
public record MapaDto(
    Long codigo,
    LocalDateTime dataHoraDisponibilizado,
    String observacoesDisponibilizacao,
    Boolean sugestoesApresentadas,
    LocalDateTime dataHoraHomologado,
    String sugestoes
) {}