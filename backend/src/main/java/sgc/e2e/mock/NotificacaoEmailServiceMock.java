package sgc.e2e.mock;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import sgc.notificacao.NotificacaoEmailService;

/**
 * Mock do serviço de notificações.
 * Sobrescreve apenas os métodos públicos para evitar envio real de e-mails.
 */
@Service
@Primary
@Profile({"test", "e2e"})
@Slf4j
public class NotificacaoEmailServiceMock extends NotificacaoEmailService {
    public NotificacaoEmailServiceMock() {
        super(null, null);
        log.info("NotificacaoEmailServiceMock ATIVADO - E-mails serão mockados");
    }

    @Override
    public void enviarEmail(@NonNull String para, @NonNull String assunto, @NonNull String corpo) {
    }

    @Override
    public void enviarEmailHtml(@NonNull String para, @NonNull String assunto, @NonNull String corpoHtml) {
    }
}
