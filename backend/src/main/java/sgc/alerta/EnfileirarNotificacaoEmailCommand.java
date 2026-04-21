package sgc.alerta;

import org.jspecify.annotations.*;
import sgc.alerta.model.*;
import sgc.subprocesso.model.*;

public record EnfileirarNotificacaoEmailCommand(
        @Nullable Alerta alerta,
        @Nullable Subprocesso subprocesso,
        @Nullable String tipoNotificacao,
        @Nullable String usuarioDestinoTitulo,
        String destinatario,
        String assunto,
        String corpoHtml,
        String chaveIdempotencia
) {
}
