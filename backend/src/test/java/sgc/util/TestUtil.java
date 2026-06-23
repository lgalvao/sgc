package sgc.util;

import org.springframework.stereotype.*;
import tools.jackson.core.*;
import tools.jackson.databind.*;

@Component
public class TestUtil {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }
}
