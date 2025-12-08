package sgc.notificacao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO para envio de e-mails. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailDto {
    /** Endereço de e-mail do destinatário. */
    private String destinatario;

    /** Assunto do e-mail. */
    private String assunto;

    /** Conteúdo do e-mail (texto ou HTML). */
    private String corpo;

    /** Se true, o corpo será tratado como HTML; se false, como texto simples. */
    private boolean html;

    /**
     * Construtor para e-mails de texto simples.
     *
     * @param destinatario Endereço de e-mail do destinatário
     * @param assunto Assunto do e-mail
     * @param corpo Conteúdo do e-mail (texto)
     */
    public EmailDto(String destinatario, String assunto, String corpo) {
        this(destinatario, assunto, corpo, false);
    }
}
