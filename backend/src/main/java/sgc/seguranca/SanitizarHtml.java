package sgc.seguranca;

import tools.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark String fields that should be HTML-sanitized during JSON deserialization.
 * Prevents XSS attacks by removing potentially malicious HTML/JavaScript.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonDeserialize(using = HtmlSanitizingDeserializer.class)
public @interface SanitizarHtml {
}
