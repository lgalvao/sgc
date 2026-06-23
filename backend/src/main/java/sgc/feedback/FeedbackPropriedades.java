package sgc.feedback;

import org.jspecify.annotations.*;
import org.springframework.boot.context.properties.*;

/**
 * Propriedades de configuração do módulo de feedback.
 */
@ConfigurationProperties(prefix = "sgc.feedback")
public record FeedbackPropriedades(
        @Nullable String screenshotDir,
        long maxScreenshotSizeBytes
) {
    public FeedbackPropriedades {
        if (maxScreenshotSizeBytes <= 0) {
            maxScreenshotSizeBytes = 5_242_880L;
        }
    }
}
