package sgc.comum.model;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import sgc.comum.SgcMensagens;

import java.lang.annotation.*;

/**
 * Anotação customizada para validação de título eleitoral brasileiro.
 *
 * <p>Valida que o título eleitoral:
 * <ul>
 *   <li>Não é nulo</li>
 *   <li>Contém apenas dígitos numéricos</li>
 *   <li>Tem no máximo 12 caracteres</li>
 * </ul>
 *
 * <p>Uso:
 * <pre>
 * public record MinhaRequest(
 *     {@literal @}TituloEleitoral String tituloEleitoral,
 *     // outros campos...
 * ) {}
 * </pre>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotNull(message = SgcMensagens.TITULO_ELEITORAL_OBRIGATORIO)
@Size(max = 12, message = SgcMensagens.TITULO_ELEITORAL_MAX)
@Pattern(regexp = "^\\d+$", message = SgcMensagens.TITULO_ELEITORAL_APENAS_NUMEROS)
@Constraint(validatedBy = {})
public @interface TituloEleitoral {

    String message() default "Título eleitoral inválido.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
