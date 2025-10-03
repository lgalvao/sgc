package sgc.service.notification;

public interface NotificationService {
    /**
     * Envia uma notificação por e-mail (mockável em testes).
     *
     * @param to      destinatário
     * @param subject assunto
     * @param body    conteúdo
     */
    void sendEmail(String to, String subject, String body);
}