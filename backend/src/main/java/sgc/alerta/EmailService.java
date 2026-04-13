package sgc.alerta;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import sgc.comum.config.*;

import java.io.*;
import java.util.regex.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private static final Pattern PADRAO_EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final JavaMailSender enviadorEmail;
    private final ConfigAplicacao config;

    @Transactional
    public void enviarEmail(String para, String assunto, String corpo) {
        processarEnvioEmail(para, assunto, corpo, false);
    }

    @Transactional
    public void enviarEmailHtml(String para, String assunto, String corpoHtml) {
        processarEnvioEmail(para, assunto, corpoHtml, true);
    }

    private void processarEnvioEmail(String para, String assunto, String corpo, boolean html) {
        if (!isEmailValido(para)) {
            log.error("Endereço de e-mail inválido, envio cancelado: {}", para);
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
            String assuntoCompleto = "%s %s".formatted(emailConfig.getAssuntoPrefixo(), assunto);
            helper.setSubject(assuntoCompleto);
            helper.setText(corpo, html);
            enviadorEmail.send(mensagem);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isEmailValido(String email) {
        return !email.isBlank() && PADRAO_EMAIL.matcher(email.trim()).matches();
    }
}
