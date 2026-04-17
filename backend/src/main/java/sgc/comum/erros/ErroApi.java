package sgc.comum.erros;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import org.jspecify.annotations.*;
import org.springframework.http.*;

import java.time.*;
import java.util.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErroApi {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();

    private int status;
    private String message;

    @Builder.Default
    private String code = "";

    @Setter
    private @Nullable String traceId;

    private @Nullable List<ErroSubApi> subErrors;

    @Setter
    @Builder.Default
    private @Nullable Map<String, ?> details = new HashMap<>();

    public ErroApi(HttpStatusCode status, String message) {
        this.timestamp = LocalDateTime.now();
        this.status = status.value();
        this.message = message;
        this.code = "";
        this.details = new HashMap<>();
    }

    public ErroApi(HttpStatusCode status, String message, String code) {
        this(status, message);
        this.code = code;
    }

    public @Nullable List<ErroSubApi> getSubErrors() {
        return subErrors != null ? new ArrayList<>(subErrors) : null;
    }
}
