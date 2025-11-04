package sgc.notificacao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import sgc.notificacao.dto.EmailDto;

import java.util.concurrent.CompletableFuture;

/**
 * Mock do serviço de notificações para testes E2E.
 * Herda de NotificacaoService para compatibilidade total.
 * Sobrescreve apenas os métodos públicos para evitar envio real de e-mails.
 */
@Service
@Primary
@Profile("e2e")
@Slf4j
public class NotificacaoServiceMock extends NotificacaoService {

    public NotificacaoServiceMock() {
        super(null, null);
        log.info(">>> NotificacaoServiceMock ATIVADO - E-mails serão mockados <<<");
    }

    @Override
    public void enviarEmail(String para, String assunto, String corpo) {
        log.debug("[MOCK E2E] E-mail NÃO enviado - Para: {}, Assunto: {}", para, assunto);
    }

    @Override
    public void enviarEmailHtml(String para, String assunto, String corpoHtml) {
        log.debug("[MOCK E2E] E-mail HTML NÃO enviado - Para: {}, Assunto: {}", para, assunto);
    }

    @Override
    public CompletableFuture<Boolean> enviarEmailAssincrono(EmailDto emailDto) {
        log.debug("[MOCK E2E] E-mail assíncrono NÃO enviado - Para: {}", emailDto.destinatario());
        return CompletableFuture.completedFuture(true);
    }
}
