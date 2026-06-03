package sgc.alerta.dto;

import lombok.*;

import java.time.*;

@Builder
public record NotificacaoDto(
        Long codigo,
        Long subprocessoCodigo,
        String unidadeSigla,
        String processoDescricao,
        TipoNotificacaoDto tipoNotificacao,
        String usuarioDestinoTitulo,
        String destinatario,
        String assunto,
        String corpoHtml,
        SituacaoNotificacaoEmailDto situacao,
        int tentativas,
        LocalDateTime dataHoraCriacao,
        LocalDateTime dataHoraEnvio,
        LocalDateTime proximaTentativaEm,
        String ultimoErro
) {
}
