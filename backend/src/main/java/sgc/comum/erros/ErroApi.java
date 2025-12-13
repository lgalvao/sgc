package sgc.comum.erros;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
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
    private String traceId;
    private List<ErroSubApi> subErrors;

    @Setter
    private Map<String, ?> details;

    private ErroApi() {
        this.timestamp = LocalDateTime.now();
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
        this.subErrors = subErrors != null ? new ArrayList<>(subErrors) : null;
    }

    public List<ErroSubApi> getSubErrors() {
        return subErrors != null ? new ArrayList<>(subErrors) : null;
    }
}
