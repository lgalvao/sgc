package sgc.comum.erros;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import org.jspecify.annotations.*;
import org.springframework.http.*;

import java.time.*;
import java.util.*;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErroApi {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private final LocalDateTime timestamp;
    private int status;
    private String message;
    private String code;
    @Setter
    private @Nullable String traceId;
    @Setter
    private @Nullable String stackTrace;
    private @Nullable List<ErroSubApi> subErrors;

    @Setter
    private @Nullable Map<String, ?> details;

    private ErroApi() {
        this.timestamp = LocalDateTime.now();
        this.message = "";
        this.code = "";
        this.details = new HashMap<>();
    }

    public ErroApi(HttpStatusCode status, String message) {
        this();
        this.status = status.value();
        this.message = message;
        this.code = "";
    }

    public ErroApi(HttpStatusCode status, String message, String code) {
        this(status, message);
        this.code = code;
    }

    public ErroApi(HttpStatusCode status, String message, String code, String traceId) {
        this(status, message, code);
        this.traceId = traceId;
    }

    public ErroApi(HttpStatusCode status, String message, List<ErroSubApi> subErrors) {
        this(status, message);
        this.subErrors = new ArrayList<>(subErrors);
    }

    public @Nullable List<ErroSubApi> getSubErrors() {
        return subErrors != null ? new ArrayList<>(subErrors) : null;
    }
}
