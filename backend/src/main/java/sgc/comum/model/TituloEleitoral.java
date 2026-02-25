package sgc.comum.model;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

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
@NotNull(message = "O título eleitoral é obrigatório.")
@Size(max = 12, message = "O título eleitoral deve ter no máximo 12 caracteres.")
@Pattern(regexp = "^\\d+$", message = "O título eleitoral deve conter apenas números.")
@Constraint(validatedBy = {})
public @interface TituloEleitoral {
    
    String message() default "Título eleitoral inválido.";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
