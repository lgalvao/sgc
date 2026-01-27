package sgc.notificacao.dto;

import lombok.Builder;

/**
 * DTO para envio de e-mails.
 */
@Builder
public record EmailDto(
        /**
         * Endereço de e-mail do destinatário.
         */
        String destinatario,

        /**
         * Assunto do e-mail.
         */
        String assunto,

        /**
         * Conteúdo do e-mail (texto ou HTML).
         */
        String corpo,

        /**
         * Se true, o corpo será tratado como HTML; se false, como texto simples.
         */
        boolean html) {

    /**
     * Construtor para e-mails de texto simples.
     *
     * @param destinatario Endereço de e-mail do destinatário
     * @param assunto      Assunto do e-mail
     * @param corpo        Conteúdo do e-mail (texto)
     */
    public EmailDto(String destinatario, String assunto, String corpo) {
        this(destinatario, assunto, corpo, false);
    }
}
