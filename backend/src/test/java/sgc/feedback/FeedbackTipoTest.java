package sgc.feedback;

import org.junit.jupiter.api.*;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FeedbackTipo")
class FeedbackTipoTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("deve serializar para JSON em letras minúsculas")
    void deveSerializarParaJson() throws Exception {
        assertThat(mapper.writeValueAsString(FeedbackTipo.BUG)).isEqualTo("\"bug\"");
        assertThat(mapper.writeValueAsString(FeedbackTipo.SUGESTAO)).isEqualTo("\"sugestao\"");
        assertThat(mapper.writeValueAsString(FeedbackTipo.QUESTAO)).isEqualTo("\"questao\"");
        assertThat(mapper.writeValueAsString(FeedbackTipo.ELOGIO)).isEqualTo("\"elogio\"");
    }

    @Test
    @DisplayName("deve desserializar de JSON ignorando o caso")
    void deveDesserializarDeJson() throws Exception {
        assertThat(mapper.readValue("\"BUG\"", FeedbackTipo.class)).isEqualTo(FeedbackTipo.BUG);
        assertThat(mapper.readValue("\"bug\"", FeedbackTipo.class)).isEqualTo(FeedbackTipo.BUG);
        assertThat(mapper.readValue("\"sugestao\"", FeedbackTipo.class)).isEqualTo(FeedbackTipo.SUGESTAO);
        assertThat(mapper.readValue("\"SUGESTAO\"", FeedbackTipo.class)).isEqualTo(FeedbackTipo.SUGESTAO);
    }

    @Test
    @DisplayName("deve converter de string usando fromJson")
    void deveConverterUsandoFromJson() {
        assertThat(FeedbackTipo.fromJson("bug")).isEqualTo(FeedbackTipo.BUG);
        assertThat(FeedbackTipo.fromJson("BUG")).isEqualTo(FeedbackTipo.BUG);
        assertThat(FeedbackTipo.fromJson("Sugestao")).isEqualTo(FeedbackTipo.SUGESTAO);
    }
}
