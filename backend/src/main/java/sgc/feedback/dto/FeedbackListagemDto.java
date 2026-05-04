package sgc.feedback.dto;

import org.jspecify.annotations.*;
import sgc.feedback.*;

import java.time.*;
import java.util.*;

public record FeedbackListagemDto(
        UUID codigo,
        FeedbackTipo tipo,
        String nota,
        @Nullable String metadataJson,
        @Nullable String caminhoScreenshot,
        boolean screenshotDisponivel,
        String usuarioCodigo,
        String usuarioNome,
        OffsetDateTime enviadoEm,
        String rota,
        FeedbackStatus status
) {
    public static FeedbackListagemDto from(FeedbackRegistro registro, boolean screenshotDisponivel) {
        return new FeedbackListagemDto(
                registro.getId(),
                registro.getTipo(),
                registro.getNota(),
                registro.getMetadataJson(),
                registro.getCaminhoScreenshot(),
                screenshotDisponivel,
                registro.getUsuarioId(),
                registro.getUsuarioNome(),
                registro.getEnviadoEm(),
                registro.getRota(),
                registro.getStatus()
        );
    }
}
