package sgc.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class TestUtil {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Long extrairCodigoDaResposta(String jsonResponse) {
        try {
            var jsonNode = objectMapper.readTree(jsonResponse);
            return jsonNode.get("codigo").asLong();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao extrair código da resposta: " + e.getMessage(), e);
        }
    }
}