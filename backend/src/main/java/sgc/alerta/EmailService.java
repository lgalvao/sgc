package sgc.alerta;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import sgc.comum.config.ConfigAplicacao;

import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

@Service
@Slf4j
public class EmailService {
    private static final Pattern PADRAO_EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PADRAO_PREFIXO_ASSUNTO = Pattern.compile("^(?:\\[SGC\\]|SGC\\s*[:\\-])\\s*", Pattern.CASE_INSENSITIVE);

    private final JavaMailSender enviadorEmail;
    private final ConfigAplicacao config;
    private final String modoEnvio;

    public EmailService(
            JavaMailSender enviadorEmail,
            ConfigAplicacao config,
            @Value("${sgc.notificacao-email.modo-envio:smtp}") String modoEnvio
    ) {
        this.enviadorEmail = enviadorEmail;
        this.config = config;
        this.modoEnvio = modoEnvio;
    }

    public void enviarEmail(String para, String assunto, String corpo) {
        processarEnvioEmail(para, assunto, corpo, false);
    }

    public void enviarEmailHtml(String para, String assunto, String corpoHtml) {
        processarEnvioEmail(para, assunto, corpoHtml, true);
    }

    private void processarEnvioEmail(String para, String assunto, String corpo, boolean html) {
        if (!isEmailValido(para)) {
            log.error("Endereço de e-mail inválido, envio cancelado: {}", para);
            return;
        }

        if (modoMockAtivo()) {
            registrarEmailMockado(para, assunto, corpo, html);
            return;
        }

        enviarEmailSmtp(para, assunto, corpo, html);
    }

    private void enviarEmailSmtp(String destinatario, String assunto, String corpo, boolean html) {
        MimeMessage mensagem = enviadorEmail.createMimeMessage();
        ConfigAplicacao.Email emailConfig = config.getEmail();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");
            helper.setFrom(new InternetAddress(emailConfig.getRemetente(), emailConfig.getRemetenteNome()));
            helper.setTo(destinatario);
            helper.setSubject(montarAssuntoCompleto(emailConfig.getAssuntoPrefixo(), assunto));
            helper.setText(corpo, html);
            enviadorEmail.send(mensagem);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new ErroEnvioEmail(destinatario, e);
        }
    }

    private boolean isEmailValido(String email) {
        return !email.isBlank() && PADRAO_EMAIL.matcher(email.trim()).matches();
    }

    private boolean modoMockAtivo() {
        return "mock".equalsIgnoreCase(modoEnvio);
    }

    private void registrarEmailMockado(String destinatario, String assunto, String corpo, boolean html) {
        log.debug(
                "E-mail mockado e descartado. destinatario={}, assunto={}, formato={}",
                destinatario,
                assunto,
                html ? "html" : "texto"
        );
        log.debug("Conteúdo do e-mail mockado: {}", corpo);
    }

    private String montarAssuntoCompleto(String prefixo, String assuntoBase) {
        String assuntoNormalizado = normalizarAssunto(assuntoBase);
        String prefixoEfetivo = prefixo.isBlank() ? "[SGC]" : prefixo.trim();
        if (assuntoNormalizado.isBlank()) {
            return prefixoEfetivo;
        }
        return "%s %s".formatted(prefixoEfetivo, assuntoNormalizado);
    }

    private String normalizarAssunto(String assunto) {
        return PADRAO_PREFIXO_ASSUNTO.matcher(assunto.trim()).replaceFirst("").trim();
    }
}
