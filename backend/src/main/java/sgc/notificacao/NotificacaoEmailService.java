package sgc.notificacao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.notificacao.model.Notificacao;
import sgc.notificacao.model.NotificacaoRepo;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
@Primary
@RequiredArgsConstructor
@Slf4j
@Profile("!test & !e2e")
public class NotificacaoEmailService {
    private static final Pattern PADRAO_EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final NotificacaoRepo repositorioNotificacao;
    private final NotificacaoEmailAsyncExecutor emailExecutor;

    /**
     * Envia um email de texto simples.
     *
     * <p>
     * O processo de envio é assíncrono e inclui retentativas.
     *
     * @param para    O endereço de email do destinatário.
     * @param assunto O assunto do email.
     * @param corpo   O corpo do email em texto simples.
     */
    @Transactional
    public void enviarEmail(String para, String assunto, String corpo) {
        processarEnvioDeEmail(para, assunto, corpo, false);
    }

    /**
     * Envia um email com conteúdo HTML.
     *
     * <p>
     * O processo de envio é assíncrono e inclui retentativas.
     *
     * @param para      O endereço de email do destinatário.
     * @param assunto   O assunto do email.
     * @param corpoHtml O corpo do email em formato HTML.
     */
    @Transactional
    public void enviarEmailHtml(String para, String assunto, String corpoHtml) {
        processarEnvioDeEmail(para, assunto, corpoHtml, true);
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private void processarEnvioDeEmail(String para, String assunto, String corpo, boolean html) {
        if (!isEmailValido(para)) {
            log.error(
                    "Endereço de e-mail inválido, envio cancelado: {}", para);
            return;
        }

        try {
            Notificacao notificacao = criarEntidadeNotificacao(para, assunto, corpo);
            repositorioNotificacao.save(notificacao);
            log.info(
                    "Notificação persistida no banco - Código: {}, Destinatário: {}",
                    notificacao.getCodigo(),
                    para);

            emailExecutor.enviarEmailAssincrono(para, assunto, corpo, html)
                    .thenAccept(
                            sucesso -> {
                                if (Boolean.TRUE.equals(sucesso)) {
                                    log.info("E-mail para {} enviado.", para);
                                } else {
                                    log.error(
                                            "Falha ao enviar e-mail para {} após tentativas.",
                                            para);
                                }
                            })
                    .exceptionally(
                            ex -> {
                                log.error(
                                        "Erro inesperado ao enviar e-mail para: {}",
                                        para,
                                        ex);
                                return null;
                            });

        } catch (RuntimeException e) {
            log.error(
                    "Erro ao processar notificação para {}: {}",
                    para,
                    e.getMessage(),
                    e);
        }
    }

    private Notificacao criarEntidadeNotificacao(String para, String assunto, String corpo) {
        Notificacao notificacao = new Notificacao();
        notificacao.setDataHora(LocalDateTime.now());
        String conteudo = String.format(
                "Para: %s | Assunto: %s | Corpo: %s",
                para, assunto, corpo);
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
