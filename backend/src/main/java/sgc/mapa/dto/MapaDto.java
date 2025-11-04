package sgc.mapa.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO para Mapa usado nas APIs.
 */
@Getter
@Builder
// TODO tem necessidade desses AccesslLevel aqui?
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MapaDto {
    private Long codigo;
    private LocalDateTime dataHoraDisponibilizado;
    private String observacoesDisponibilizacao;
    private Boolean sugestoesApresentadas;
    private LocalDateTime dataHoraHomologado;
    private String sugestoes;
}