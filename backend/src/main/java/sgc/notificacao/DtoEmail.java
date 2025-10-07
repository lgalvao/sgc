package sgc.notificacao;

/**
 * DTO para envio de e-mails.
 * 
 * @param destinatario Endereço de e-mail do destinatário
 * @param assunto Assunto do e-mail
 * @param corpo Conteúdo do e-mail (texto ou HTML)
 * @param html Se true, o corpo será tratado como HTML; se false, como texto simples
 */
public record DtoEmail(
    String destinatario,
    String assunto,
    String corpo,
    boolean html
) {
    /**
     * Construtor para e-mails de texto simples.
     */
    public DtoEmail(String destinatario, String assunto, String corpo) {
        this(destinatario, assunto, corpo, false);
    }
}