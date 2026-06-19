package sgc.alerta;

import lombok.Builder;
import org.jspecify.annotations.Nullable;
import sgc.alerta.model.TipoNotificacao;
import sgc.subprocesso.model.Subprocesso;

@Builder
public record EnfileirarNotificacaoCommand(
        @Nullable Subprocesso subprocesso,
        @Nullable TipoNotificacao tipoNotificacao,
        @Nullable String usuarioDestinoTitulo,
        @Nullable String unidadeDestinoSigla,
        String destinatario,
        String assunto,
        String corpoHtml,
        String chaveIdempotencia
) {
}
