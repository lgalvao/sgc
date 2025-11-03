package sgc.notificacao;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.notificacao.dto.EmailDto;
import sgc.notificacao.modelo.Notificacao;
import sgc.notificacao.modelo.NotificacaoRepo;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

@Service
@Primary
@org.springframework.context.annotation.Profile("!e2e")
@RequiredArgsConstructor
@Slf4j
public class NotificacaoService {
    private final JavaMailSender enviadorDeEmail;
    private final NotificacaoRepo repositorioNotificacao;

    @Value("${aplicacao.email.remetente}")
    private String remetente;

    @Value("${aplicacao.email.remetente-nome}")
    private String nomeRemetente;

    @Value("${aplicacao.email.assunto-prefixo}")
    private String prefixoAssunto;

    private static final Pattern PADRAO_EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int MAX_TENTATIVAS = 3;
    private static final long ESPERA_ENTRE_TENTATIVAS_MS = 1000;

    /**
     * Envia um email de texto simples.
     * <p>
     * O processo de envio é assíncrono e inclui retentativas.
     *
     * @param para    O endereço de email do destinatário.
     * @param assunto O assunto do email.
     * @param corpo   O corpo do email em texto simples.
     */
    @Transactional
    public void enviarEmail(String para, String assunto, String corpo) {
        processarEnvioDeEmail(new EmailDto(para, assunto, corpo, false));
    }

    /**
     * Envia um email com conteúdo HTML.
     * <p>
     * O processo de envio é assíncrono e inclui retentativas.
     *
     * @param para      O endereço de email do destinatário.
     * @param assunto   O assunto do email.
     * @param corpoHtml O corpo do email em formato HTML.
     */
    @Transactional
    public void enviarEmailHtml(String para, String assunto, String corpoHtml) {
        processarEnvioDeEmail(new EmailDto(para, assunto, corpoHtml, true));
    }

    private void processarEnvioDeEmail(EmailDto emailDto) {
        if (!isEmailValido(emailDto.destinatario())) {
            log.error("Endereço de e-mail inválido, envio cancelado: {}", emailDto.destinatario());
            return;
        }

        try {
            Notificacao notificacao = criarEntidadeNotificacao(emailDto);
            repositorioNotificacao.save(notificacao);
            log.info("Notificação persistida no banco - Código: {}, Destinatário: {}",
                    notificacao.getCodigo(), emailDto.destinatario());

            enviarEmailAssincrono(emailDto)
                    .thenAccept(sucesso -> {
                        if (Boolean.TRUE.equals(sucesso)) {
                            log.info("E-mail para {} enviado com sucesso.", emailDto.destinatario());
                        } else {
                            log.error("Falha ao enviar e-mail para {} após {} tentativas.", emailDto.destinatario(), MAX_TENTATIVAS);
                        }
                    })
                    .exceptionally(ex -> {
                        log.error("Erro inesperado ao enviar e-mail para: {}", emailDto.destinatario(), ex);
                        return null;
                    });

        } catch (Exception e) {
            log.error("Erro ao processar notificação para {}: {}", emailDto.destinatario(), e.getMessage(), e);
        }
    }

    /**
     * Tenta enviar um email de forma assíncrona, com uma política de retentativas.
     * <p>
     * Este método é executado em uma thread separada. Ele tentará enviar o email
     * até {@code MAX_TENTATIVAS} vezes, com um tempo de espera crescente entre
     * as tentativas.
     *
     * @param emailDto O DTO contendo os detalhes do email a ser enviado.
     * @return Um {@link CompletableFuture} que será concluído com {@code true} se o
     * email for enviado com sucesso, ou {@code false} caso contrário.
     */
    @Async
    public CompletableFuture<Boolean> enviarEmailAssincrono(EmailDto emailDto) {
        Exception excecaoFinal = null;
        for (int tentativa = 1; tentativa <= MAX_TENTATIVAS; tentativa++) {
            try {
                log.debug("Tentativa {} de {} para enviar e-mail para: {}", tentativa, MAX_TENTATIVAS, emailDto.destinatario());
                enviarEmailSmtp(emailDto);
                log.info("E-mail enviado com sucesso na tentativa {} para: {}", tentativa, emailDto.destinatario());
                return CompletableFuture.completedFuture(true);
            } catch (Exception e) {
                excecaoFinal = e;
                log.warn("Falha na tentativa {} de {} ao enviar e-mail para {}: {}",
                        tentativa, MAX_TENTATIVAS, emailDto.destinatario(), e.getMessage());
                if (tentativa < MAX_TENTATIVAS) {
                    try {
                        Thread.sleep(ESPERA_ENTRE_TENTATIVAS_MS * tentativa);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Thread interrompida durante a espera para nova tentativa de envio.", ie);
                        break;
                    }
                }
            }
        }
        log.error("Não foi possível enviar o e-mail para {} após {} tentativas.", MAX_TENTATIVAS, emailDto.destinatario(), excecaoFinal);
        return CompletableFuture.completedFuture(false);
    }

    private void enviarEmailSmtp(EmailDto emailDto) throws UnsupportedEncodingException, MessagingException {
        MimeMessage mensagem = enviadorDeEmail.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");

        helper.setFrom(new InternetAddress(remetente, nomeRemetente));
        helper.setTo(emailDto.destinatario());
        String assuntoCompleto = "%s %s".formatted(prefixoAssunto, emailDto.assunto());
        helper.setSubject(assuntoCompleto);
        helper.setText(emailDto.corpo(), emailDto.html());

        enviadorDeEmail.send(mensagem);
        log.debug("E-mail enviado via SMTP para: {} - Assunto: {}", emailDto.destinatario(), assuntoCompleto);
    }

    private Notificacao criarEntidadeNotificacao(EmailDto emailDto) {
        Notificacao notificacao = new Notificacao();
        notificacao.setDataHora(LocalDateTime.now());
        String conteudo = String.format("Para: %s | Assunto: %s | Corpo: %s", emailDto.destinatario(), emailDto.assunto(), emailDto.corpo());
        if (conteudo.length() > 500) {
            conteudo = "%s...".formatted(conteudo.substring(0, 497));
        }
        notificacao.setConteudo(conteudo);
        return notificacao;
    }

    private boolean isEmailValido(String email) {
        return email != null && !email.trim().isEmpty() && PADRAO_EMAIL.matcher(email.trim()).matches();
    }
}