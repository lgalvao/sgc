package sgc.seguranca.sanitizacao;

import com.fasterxml.jackson.annotation.*;
import tools.jackson.databind.annotation.*;

import java.lang.annotation.*;

/**
 * Anotação para marcar campos String que devem ser sanitizados durante a
 * deserialização JSON.
 * Previne ataques XSS removendo HTML/JavaScript potencialmente malicioso.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonDeserialize(using = DeserializadorHtmlSanitizado.class)
public @interface SanitizarHtml {
}
