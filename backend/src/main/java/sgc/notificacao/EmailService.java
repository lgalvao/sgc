package sgc.notificacao;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.config.ConfigAplicacao;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private static final Pattern PADRAO_EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final NotificacaoRepo repositorioNotificacao;
    private final JavaMailSender enviadorDeEmail;
    private final ConfigAplicacao config;

    /**
     * Envia um email de texto simples.
     */
    @Async
    @Transactional
    public void enviarEmail(String para, String assunto, String corpo) {
        processarEnvioDeEmail(para, assunto, corpo, false);
    }

    /**
     * Envia um email com HTML.
     */
    @Async
    @Transactional
    public void enviarEmailHtml(String para, String assunto, String corpoHtml) {
        processarEnvioDeEmail(para, assunto, corpoHtml, true);
    }

    private void processarEnvioDeEmail(String para, String assunto, String corpo, boolean html) {
        if (!isEmailValido(para)) {
            log.error("Endereço de e-mail inválido, envio cancelado: {}", para);
            return;
        }

        try {
            Notificacao notificacao = criarEntidadeNotificacao(para, assunto, corpo);
            repositorioNotificacao.save(notificacao);
            enviarEmailSmtp(para, assunto, corpo, html);
        } catch (MessagingException | UnsupportedEncodingException | RuntimeException e) {
            log.error("Erro ao processar notificação para {}: {}", para, e.getMessage(), e);
        }
    }

    private void enviarEmailSmtp(String destinatario, String assunto, String corpo, boolean html)
            throws UnsupportedEncodingException, MessagingException {
        MimeMessage mensagem = enviadorDeEmail.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");

        var emailConfig = config.getEmail();
        helper.setFrom(new InternetAddress(emailConfig.getRemetente(), emailConfig.getRemetenteNome()));
        helper.setTo(destinatario);
        String assuntoCompleto = "%s %s".formatted(emailConfig.getAssuntoPrefixo(), assunto);
        helper.setSubject(assuntoCompleto);
        helper.setText(corpo, html);

        enviadorDeEmail.send(mensagem);
    }

    private Notificacao criarEntidadeNotificacao(String para, String assunto, String corpo) {
        Notificacao notificacao = new Notificacao();
        notificacao.setDataHora(LocalDateTime.now());
        String conteudo = String.format("Para: %s | Assunto: %s | Corpo: %s", para, assunto, corpo);

        final int limite = 500;
        if (conteudo.length() > limite) {
            conteudo = "%s...".formatted(conteudo.substring(0, limite - 3));
        }
        notificacao.setConteudo(conteudo);
        return notificacao;
    }

    private boolean isEmailValido(String email) {
        return !email.isBlank() && PADRAO_EMAIL.matcher(email.trim()).matches();
    }
}
