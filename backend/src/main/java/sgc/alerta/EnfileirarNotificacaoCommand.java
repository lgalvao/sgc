package sgc.alerta;

import lombok.*;
import org.jspecify.annotations.*;
import sgc.alerta.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

@Builder
public record EnfileirarNotificacaoCommand(
        @Nullable Subprocesso subprocesso,
        @Nullable Processo processo,
        TipoNotificacao tipoNotificacao,
        @Nullable String usuarioDestinoTitulo,
        @Nullable String unidadeDestinoSigla,
        @Nullable String unidadeOrigemSigla,
        String destinatario,
        String assunto,
        String corpoHtml,
        String chaveIdempotencia
) {
}
