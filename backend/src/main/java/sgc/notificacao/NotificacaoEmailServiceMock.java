package sgc.notificacao;

import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import sgc.notificacao.dto.EmailDto;

/**
 * Mock do serviço de notificações Herda de NotificacaoEmailService para compatibilidade total.
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
        log.debug("[MOCK] E-mail NÃO enviado - Para: {}, Assunto: {}", para, assunto);
    }

    @Override
    public void enviarEmailHtml(String para, String assunto, String corpoHtml) {
        log.debug("[MOCK] E-mail HTML NÃO enviado - Para: {}, Assunto: {}", para, assunto);
    }

    @Override
    public CompletableFuture<Boolean> enviarEmailAssincrono(EmailDto emailDto) {
        log.debug("[MOCK] E-mail assíncrono NÃO enviado - Para: {}", emailDto.getDestinatario());
        return CompletableFuture.completedFuture(true);
    }
}
