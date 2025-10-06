package sgc.notificacao;

public interface NotificationService {
    /**
     * Envia uma notificação por e-mail (mockável em testes).
     *
     * @param to      destinatário
     * @param subject assunto
     * @param body    conteúdo
     */
    void enviarEmail(String to, String subject, String body);
}