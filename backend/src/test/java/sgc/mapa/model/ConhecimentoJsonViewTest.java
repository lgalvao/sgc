package sgc.mapa.model;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.json.*;
import tools.jackson.databind.*;

import static org.assertj.core.api.Assertions.*;

@JsonTest
@DisplayName("Testes de Serialização @JsonView - Conhecimento")
class ConhecimentoJsonViewTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve serializar apenas campos públicos do Conhecimento")
    void deveSerializarCamposPublicos() {
        Atividade atividade = Atividade.builder().codigo(1L).build();
        Conhecimento conhecimento = Conhecimento.builder()
                .codigo(100L)
                .descricao("Conhecimento Teste")
                .atividade(atividade)
                .build();

        String json = objectMapper
                .writerWithView(MapaViews.Publica.class)
                .writeValueAsString(conhecimento);

        assertThat(json).contains("\"codigo\":100");
        assertThat(json).contains("\"descricao\":\"Conhecimento Teste\"");
        assertThat(json).contains("\"atividadeCodigo\":1");

        // Relacionamentos devem ser ignorados
        assertThat(json).doesNotContain("\"atividade\"");
    }
}
