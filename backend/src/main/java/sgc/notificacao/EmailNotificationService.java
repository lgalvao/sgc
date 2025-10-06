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

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * Implementação real do serviço de notificações usando Spring Mail.
 * Esta implementação substitui o MockNotificationService em produção.
 * <p>
 * Características:
 * - Envio real de e-mails via SMTP
 * - Persistência de notificações no banco de dados
 * - Execução assíncrona para não bloquear threads
 * - Retry automático em caso de falhas
 * - Validação de endereços de e-mail
 * - Logging detalhado de operações
 */
@Service
@Primary  // Esta implementação tem prioridade sobre MockNotificationService
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService implements NotificationService {

    private final JavaMailSender mailSender;
    private final NotificacaoRepository notificacaoRepository;

    @Value("${aplicacao.email.remetente}")
    private String remetente;

    @Value("${aplicacao.email.remetente-nome}")
    private String remetenteNome;

    @Value("${aplicacao.email.assunto-prefixo}")
    private String assuntoPrefixo;

    // Pattern para validação de e-mail
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final int MAX_TENTATIVAS = 3;
    private static final long DELAY_ENTRE_TENTATIVAS_MS = 1000;

    /**
     * Implementação da interface NotificationService.
     * Envia e-mail de texto simples.
     *
     * @param to      Destinatário
     * @param subject Assunto
     * @param body    Corpo do e-mail (texto simples)
     */
    @Override
    @Transactional
    public void enviarEmail(String to, String subject, String body) {
        enviarEmailInterno(new EmailDto(to, subject, body, false));
    }

    /**
     * Envia e-mail com conteúdo HTML.
     *
     * @param to       Destinatário
     * @param subject  Assunto
     * @param htmlBody Corpo do e-mail em HTML
     */
    @Transactional
    public void enviarEmailHtml(String to, String subject, String htmlBody) {
        enviarEmailInterno(new EmailDto(to, subject, htmlBody, true));
    }

    /**
     * Envia e-mail usando DTO.
     *
     * @param email DTO com informações do e-mail
     */
    @Transactional
    public void enviarEmailDto(EmailDto email) {
        enviarEmailInterno(email);
    }

    /**
     * Metodo interno que coordena a persistência e envio do e-mail.
     *
     * @param email DTO com informações do e-mail
     */
    @Transactional
    public void enviarEmailInterno(EmailDto email) {
        // Validar e-mail
        if (!validarEmail(email.destinatario())) {
            log.error("Endereço de e-mail inválido: {}", email.destinatario());
            return;
        }

        try {
            // 1. Persistir notificação no banco ANTES de tentar enviar
            Notificacao notificacao = criarNotificacao(email);
            notificacaoRepository.save(notificacao);
            log.info("Notificação persistida no banco - Código: {}, Destinatário: {}",
                    notificacao.getCodigo(), email.destinatario());

            // 2. Enviar e-mail de forma assíncrona
            enviarEmailAsync(email)
                    .thenAccept(sucesso -> {
                        if (sucesso) {
                            log.info("E-mail enviado com sucesso para: {}", email.destinatario());
                        } else {
                            log.error("Falha ao enviar e-mail para: {} após {} tentativas",
                                    email.destinatario(), MAX_TENTATIVAS);
                        }
                    })
                    .exceptionally(ex -> {
                        log.error("Erro ao enviar e-mail para: {}", email.destinatario(), ex);
                        return null;
                    });

        } catch (Exception e) {
            log.error("Erro ao processar notificação para: {}", email.destinatario(), e);
            // Não propaga exceção para não interromper fluxo do sistema
        }
    }

    /**
     * Envia e-mail de forma assíncrona com retry automático.
     *
     * @param email DTO com informações do e-mail
     * @return CompletableFuture com resultado do envio
     */
    @Async("emailTaskExecutor")
    public CompletableFuture<Boolean> enviarEmailAsync(EmailDto email) {
        int tentativa = 0;
        Exception ultimaExcecao = null;

        while (tentativa < MAX_TENTATIVAS) {
            tentativa++;

            try {
                log.debug("Tentativa {} de {} para enviar e-mail para: {}",
                        tentativa, MAX_TENTATIVAS, email.destinatario());

                enviarEmailSMTP(email);

                log.info("E-mail enviado com sucesso na tentativa {} para: {}",
                        tentativa, email.destinatario());
                return CompletableFuture.completedFuture(true);

            } catch (Exception e) {
                ultimaExcecao = e;
                log.warn("Falha na tentativa {} de {} ao enviar e-mail para {}: {}",
                        tentativa, MAX_TENTATIVAS, email.destinatario(), e.getMessage());

                if (tentativa < MAX_TENTATIVAS) {
                    try {
                        // Aguardar antes de tentar novamente (backoff linear)
                        Thread.sleep(DELAY_ENTRE_TENTATIVAS_MS * tentativa);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Thread interrompida durante retry", ie);
                        break;
                    }
                }
            }
        }

        log.error("Falha ao enviar e-mail após {} tentativas para: {}",
                MAX_TENTATIVAS, email.destinatario(), ultimaExcecao);
        return CompletableFuture.completedFuture(false);
    }

    /**
     * Envia e-mail via SMTP usando JavaMailSender.
     *
     * @param email DTO com informações do e-mail
     * @throws MessagingException Em caso de erro no envio
     */
    private void enviarEmailSMTP(EmailDto email) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // Configurar remetente
        helper.setFrom(new InternetAddress(remetente, remetenteNome));

        // Configurar destinatário
        helper.setTo(email.destinatario());

        // Configurar assunto com prefixo
        String assuntoCompleto = assuntoPrefixo + " " + email.assunto();
        helper.setSubject(assuntoCompleto);

        // Configurar corpo (HTML ou texto)
        helper.setText(email.corpo(), email.html());

        // Enviar
        mailSender.send(message);

        log.debug("E-mail enviado via SMTP para: {} - Assunto: {}",
                email.destinatario(), assuntoCompleto);
    }

    /**
     * Cria entidade Notificacao a partir do DTO de e-mail.
     *
     * @param email DTO com informações do e-mail
     * @return Entidade Notificacao preenchida
     */
    private Notificacao criarNotificacao(EmailDto email) {
        Notificacao notificacao = new Notificacao();
        notificacao.setDataHora(LocalDateTime.now());

        // Concatenar assunto e corpo para o conteúdo (limitado a 500 caracteres)
        String conteudo = String.format("Para: %s | Assunto: %s | Corpo: %s",
                email.destinatario(), email.assunto(), email.corpo());

        if (conteudo.length() > 500) {
            conteudo = conteudo.substring(0, 497) + "...";
        }

        notificacao.setConteudo(conteudo);

        // Subprocesso e unidades podem ser configurados posteriormente se necessário
        // Por enquanto, apenas persistimos as informações básicas do e-mail

        return notificacao;
    }

    /**
     * Valida formato de endereço de e-mail.
     *
     * @param email Endereço a validar
     * @return true se válido, false caso contrário
     */
    private boolean validarEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Metodo utilitário para teste/debug - não usar em produção.
     * Retorna configurações do serviço.
     *
     * @return String com configurações
     */
    public String getConfiguracao() {
        return String.format("Remetente: %s <%s>, Prefixo: %s",
                remetenteNome, remetente, assuntoPrefixo);
    }
}