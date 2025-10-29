package sgc.alerta.dto;

import java.time.LocalDateTime;

/**
 * DTO para representar um alerta.
 *
 * @param codigo O código do alerta.
 * @param codProcesso O código do processo associado.
 * @param descricao A descrição do alerta.
 * @param dataHora A data e hora em que o alerta foi gerado.
 * @param codUnidadeOrigem O código da unidade de origem.
 * @param codUunidadeDestino O código da unidade de destino.
 * @param tituloUsuarioDestino O título de eleitor do usuário de destino.
 */
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AlertaDto {
    Long codigo;
    Long codProcesso;
    String descricao;
    LocalDateTime dataHora;
    Long codUnidadeOrigem;
    Long codUunidadeDestino;
    String tituloUsuarioDestino;
}