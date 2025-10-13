package sgc.alerta.dto;

import java.time.LocalDateTime;

public record AlertaDto(
    Long codigo,
    Long processoCodigo,
    String descricao,
    LocalDateTime dataHora,
    Long unidadeOrigemCodigo,
    Long unidadeDestinoCodigo,
    String usuarioDestinoTitulo
) {}