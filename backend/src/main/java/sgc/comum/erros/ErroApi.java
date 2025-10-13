package sgc.comum.erros;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErroApi {

    private int status;
    private String message;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private final LocalDateTime timestamp;
    private List<ErroSubApi> subErrors;
    @Setter
    private Map<String, ?> details;

    private ErroApi() {
        this.timestamp = LocalDateTime.now();
    }

    public ErroApi(HttpStatus status, String message) {
        this();
        this.status = status.value();
        this.message = message;
    }

    public ErroApi(HttpStatus status, String message, List<ErroSubApi> subErrors) {
        this(status, message);
        this.subErrors = subErrors;
    }

}