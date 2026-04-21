package sgc.alerta;

import org.jspecify.annotations.*;
import sgc.subprocesso.model.*;

public record EnfileirarNotificacaoEmailCommand(
        @Nullable Subprocesso subprocesso,
        @Nullable String tipoTransicao,
        String destinatario,
        String assunto,
        String corpoHtml,
        String chaveIdempotencia
) {
}
