package sgc.seguranca.sanitizacao;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
