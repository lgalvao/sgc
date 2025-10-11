package sgc.notificacao;

public interface NotificacaoServico {
    /**
     * Envia uma notificação por e-mail (mockável em testes).
     *
     * @param para    destinatário
     * @param assunto assunto
     * @param corpo   conteúdo
     */
    void enviarEmail(String para, String assunto, String corpo);

    /**
     * Envia uma notificação por e-mail com corpo em HTML.
     *
     * @param para      destinatário
     * @param assunto   assunto
     * @param corpoHtml conteúdo em HTML
     */
    void enviarEmailHtml(String para, String assunto, String corpoHtml);
}