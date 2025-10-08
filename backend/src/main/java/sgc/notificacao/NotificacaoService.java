package sgc.notificacao;

public interface NotificacaoService {
    /**
     * Envia uma notificação por e-mail (mockável em testes).
     *
     * @param para    destinatário
     * @param assunto assunto
     * @param corpo   conteúdo
     */
    void enviarEmail(String para, String assunto, String corpo);
}