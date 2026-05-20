package sgc.feedback.dto;

import sgc.feedback.*;

import java.time.*;
import java.util.*;

public record FeedbackListagemDto(
        UUID codigo,
        FeedbackTipo tipo,
        String nota,
        String metadataJson,
        String caminhoScreenshot,
        boolean screenshotDisponivel,
        String usuarioCodigo,
        String usuarioNome,
        OffsetDateTime enviadoEm,
        String rota,
        FeedbackStatus status
) {
    public static FeedbackListagemDto from(FeedbackRegistro registro, boolean screenshotDisponivel) {
        return new FeedbackListagemDto(
                registro.getCodigo(),
                registro.getTipo(),
                registro.getNota(),
                registro.getMetadataJson(),
                registro.getCaminhoScreenshot(),
                screenshotDisponivel,
                registro.getUsuarioCodigo(),
                registro.getUsuarioNome(),
                registro.getEnviadoEm(),
                registro.getRota(),
                registro.getStatus()
        );
    }
}
