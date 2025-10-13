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
public class ApiError {

    private int status;
    private String message;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private final LocalDateTime timestamp;
    private List<ApiSubError> subErrors;
    @Setter
    private Map<String, ?> details;

    private ApiError() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiError(HttpStatus status, String message) {
        this();
        this.status = status.value();
        this.message = message;
    }

    public ApiError(HttpStatus status, String message, List<ApiSubError> subErrors) {
        this(status, message);
        this.subErrors = subErrors;
    }

}