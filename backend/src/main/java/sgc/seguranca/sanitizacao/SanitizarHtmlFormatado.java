package sgc.seguranca.sanitizacao;

import com.fasterxml.jackson.annotation.*;
import tools.jackson.databind.annotation.*;

import java.lang.annotation.*;

/**
 * Anotação para marcar campos String que devem ser sanitizados durante a
 * deserialização JSON, preservando a formatação HTML básica permitida.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonDeserialize(using = DeserializadorHtmlFormatadoSanitizado.class)
public @interface SanitizarHtmlFormatado {
}
