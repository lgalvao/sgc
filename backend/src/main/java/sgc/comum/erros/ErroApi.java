package sgc.comum.erros;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private @Nullable List<ErroSubApi> subErrors;

    @Setter
    private Map<String, ?> details;

    private ErroApi() {
        this.timestamp = LocalDateTime.now();
        this.message = "";
    }

    public ErroApi(HttpStatusCode status, String message) {
        this();
        this.status = status.value();
        this.message = message;
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
