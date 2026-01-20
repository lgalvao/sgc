package sgc.e2e.mock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import sgc.notificacao.NotificacaoEmailService;

/**
 * Mock do serviço de notificações. Herda de NotificacaoEmailService para compatibilidade total.
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
    public void enviarEmail(String para, String assunto, String corpo) {
    }

    @Override
    public void enviarEmailHtml(String para, String assunto, String corpoHtml) {
    }
}
