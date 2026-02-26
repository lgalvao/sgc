package sgc.alerta;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.mail.javamail.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import sgc.alerta.model.*;
import sgc.comum.config.*;

import java.io.*;
import java.time.*;
import java.util.regex.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private static final Pattern PADRAO_EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final NotificacaoRepo notificacaoRepo;
    private final JavaMailSender enviadorEmail;
    private final ConfigAplicacao config;

    /**
     * Envia um email de texto simples.
     */
    @Async
    @Transactional
    public void enviarEmail(String para, String assunto, String corpo) {
        processarEnvioEmail(para, assunto, corpo, false);
    }

    /**
     * Envia um email com HTML.
     */
    @Async
    @Transactional
    public void enviarEmailHtml(String para, String assunto, String corpoHtml) {
        processarEnvioEmail(para, assunto, corpoHtml, true);
    }

    private void processarEnvioEmail(String para, String assunto, String corpo, boolean html) {
        if (!isEmailValido(para)) {
            log.error("Endereço de e-mail inválido, envio cancelado: {}", para);
            return;
        }

        Notificacao notificacao = criarNotificacao(para, assunto, corpo);
        notificacaoRepo.save(notificacao);
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

    private Notificacao criarNotificacao(String para, String assunto, String corpo) {
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
