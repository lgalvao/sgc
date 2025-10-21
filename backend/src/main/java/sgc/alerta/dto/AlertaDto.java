package sgc.alerta.dto;

import java.time.LocalDateTime;

/**
 * DTO para representar um alerta.
 *
 * @param codigo O código do alerta.
 * @param processoCodigo O código do processo associado.
 * @param descricao A descrição do alerta.
 * @param dataHora A data e hora em que o alerta foi gerado.
 * @param unidadeOrigemCodigo O código da unidade de origem.
 * @param unidadeDestinoCodigo O código da unidade de destino.
 * @param usuarioDestinoTitulo O título de eleitor do usuário de destino.
 */
public record AlertaDto(
    Long codigo,
    Long processoCodigo,
    String descricao,
    LocalDateTime dataHora,
    Long unidadeOrigemCodigo,
    Long unidadeDestinoCodigo,
    String usuarioDestinoTitulo
) {}