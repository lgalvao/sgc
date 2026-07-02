package sgc.alerta;

import lombok.*;
import org.jspecify.annotations.*;
import sgc.alerta.model.*;
import sgc.subprocesso.model.*;

@Builder
public record EnfileirarNotificacaoCommand(
        @Nullable Subprocesso subprocesso,
        TipoNotificacao tipoNotificacao,
        @Nullable String usuarioDestinoTitulo,
        @Nullable String unidadeDestinoSigla,
        String destinatario,
        String assunto,
        String corpoHtml,
        String chaveIdempotencia
) {
}
