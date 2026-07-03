package sgc.alerta.dto;

import lombok.*;
import org.jspecify.annotations.*;

import java.time.*;

@Builder
public record NotificacaoDto(
        Long codigo,
        @Nullable Long subprocessoCodigo,
        @Nullable String unidadeSigla,
        @Nullable String unidadeOrigemSigla,
        @Nullable String processoDescricao,
        TipoNotificacaoDto tipoNotificacao,
        @Nullable String usuarioDestinoTitulo,
        String destinatario,
        String assunto,
        String corpoHtml,
        SituacaoNotificacaoEmailDto situacao,
        int tentativas,
        LocalDateTime dataHoraCriacao,
        @Nullable LocalDateTime dataHoraEnvio,
        @Nullable LocalDateTime proximaTentativaEm,
        @Nullable String ultimoErro
) {
}
