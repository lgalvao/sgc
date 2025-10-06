package sgc.mapa;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO para Mapa usado nas APIs.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MapaDTO {
    private Long codigo;
    private LocalDateTime dataHoraDisponibilizado;
    private String observacoesDisponibilizacao;
    private Boolean sugestoesApresentadas;
    private LocalDateTime dataHoraHomologado;
}