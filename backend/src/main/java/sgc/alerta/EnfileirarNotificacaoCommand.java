package sgc.alerta;

import lombok.*;
import org.jspecify.annotations.*;
import sgc.alerta.model.*;
import sgc.subprocesso.model.*;

@Builder
public record EnfileirarNotificacaoCommand(
        @Nullable Subprocesso subprocesso,
        @Nullable TipoNotificacao tipoNotificacao,
        @Nullable String usuarioDestinoTitulo,
        String destinatario,
        String assunto,
        String corpoHtml,
        String chaveIdempotencia
) {
}
