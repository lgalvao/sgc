package sgc.notificacao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO para envio de e-mails.
 */
@Getter
@Builder
@AllArgsConstructor
public class EmailDto {

    /**
     * Endereço de e-mail do destinatário.
     */
    private final String destinatario;

    /**
     * Assunto do e-mail.
     */
    private final String assunto;

    /**
     * Conteúdo do e-mail (texto ou HTML).
     */
    private final String corpo;

    /**
     * Se true, o corpo será tratado como HTML; se false, como texto simples.
     */
    private final boolean html;

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
