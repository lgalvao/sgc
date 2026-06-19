package sgc.comum.erros;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private @Nullable List<ErroSubApi> erros;

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

    public @Nullable List<ErroSubApi> getErros() {
        return erros != null ? new ArrayList<>(erros) : null;
    }
}
