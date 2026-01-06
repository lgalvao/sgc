package sgc.notificacao;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import sgc.notificacao.dto.EmailDto;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.CompletableFuture;

/**
 * Executor responsável pelo envio assíncrono de e-mails.
 * Extraído de NotificacaoEmailService para resolver problemas de auto-invocação com @Async.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Profile("!test & !e2e & !secure-test")
public class NotificacaoEmailAsyncExecutor {
    private static final int MAX_TENTATIVAS = 3;
    private static final long ESPERA_ENTRE_TENTATIVAS_MS = 1000;

    private final JavaMailSender enviadorDeEmail;

    @Value("${aplicacao.email.remetente}")
    private String remetente;

    @Value("${aplicacao.email.remetente-nome}")
    private String nomeRemetente;

    @Value("${aplicacao.email.assunto-prefixo}")
    private String prefixoAssunto;

    /**
     * Tenta enviar um email de forma assíncrona, com uma política de retentativas.
     *
     * <p>Este método é executado em uma thread separada. Ele tentará enviar o email até {@code
     * MAX_TENTATIVAS} vezes, com um tempo de espera crescente entre as tentativas.
     *
     * @param emailDto O DTO contendo os detalhes do email a ser enviado.
     * @return Um {@link CompletableFuture} que será concluído com {@code true} se o email for
     * enviado, ou {@code false} caso contrário.
     */
    @Async
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public CompletableFuture<Boolean> enviarEmailAssincrono(EmailDto emailDto) {
        Exception excecaoFinal = null;
        for (int tentativa = 1; tentativa <= MAX_TENTATIVAS; tentativa++) {
            try {
                enviarEmailSmtp(emailDto);
                log.info("E-mail enviado para: {}", emailDto.getDestinatario());
                return CompletableFuture.completedFuture(true);
            } catch (MessagingException | UnsupportedEncodingException | RuntimeException e) {
                excecaoFinal = e;
                log.warn(
                        "Falha na tentativa {} de {} ao enviar e-mail para {}: {}",
                        tentativa,
                        MAX_TENTATIVAS,
                        emailDto.getDestinatario(),
                        e.getMessage());
                if (tentativa < MAX_TENTATIVAS) {
                    try {
                        Thread.sleep(ESPERA_ENTRE_TENTATIVAS_MS * tentativa);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error(
                                "Thread interrompida durante a espera para nova tentativa de"
                                        + " envio.",
                                ie);
                        break;
                    }
                }
            }
        }
        log.error(
                "Não foi possível enviar o e-mail para {} após {} tentativas.",
                MAX_TENTATIVAS,
                emailDto.getDestinatario(),
                excecaoFinal);
        return CompletableFuture.completedFuture(false);
    }

    private void enviarEmailSmtp(EmailDto emailDto)
            throws UnsupportedEncodingException, MessagingException {
        MimeMessage mensagem = enviadorDeEmail.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");

        helper.setFrom(new InternetAddress(remetente, nomeRemetente));
        helper.setTo(emailDto.getDestinatario());
        String assuntoCompleto = "%s %s".formatted(prefixoAssunto, emailDto.getAssunto());
        helper.setSubject(assuntoCompleto);
        helper.setText(emailDto.getCorpo(), emailDto.isHtml());

        enviadorDeEmail.send(mensagem);
    }
}
