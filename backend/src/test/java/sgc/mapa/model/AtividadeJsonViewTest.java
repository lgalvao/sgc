package sgc.mapa.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@DisplayName("Testes de Serialização @JsonView - Atividade")
class AtividadeJsonViewTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve serializar campos públicos da Atividade incluindo conhecimentos")
    void deveSerializarCamposPublicos() throws Exception {
        Mapa mapa = Mapa.builder().codigo(10L).build();
        Atividade atividade = Atividade.builder()
                .codigo(1L)
                .descricao("Atividade Teste")
                .mapa(mapa)
                .conhecimentos(List.of(Conhecimento.builder().codigo(100L).descricao("K1").build()))
                .build();

        String json = objectMapper
                .writerWithView(MapaViews.Publica.class)
                .writeValueAsString(atividade);

        assertThat(json).contains("\"codigo\":1");
        assertThat(json).contains("\"descricao\":\"Atividade Teste\"");
        assertThat(json).contains("\"mapaCodigo\":10");
        
        // Agora conhecimentos são incluídos na Publica
        assertThat(json).contains("\"conhecimentos\"");
        assertThat(json).contains("\"descricao\":\"K1\"");

        // Relacionamentos ignorados
        assertThat(json).doesNotContain("\"competencias\"");
        assertThat(json).doesNotContain("\"mapa\":");
    }

    @Test
    @DisplayName("Deve ocultar conhecimentos na visão Minimal")
    void deveOcultarConhecimentosEmVisaoMinimal() throws Exception {
        Atividade atividade = Atividade.builder()
                .codigo(1L)
                .descricao("Atividade Teste")
                .conhecimentos(List.of(Conhecimento.builder().codigo(100L).descricao("K1").build()))
                .build();

        String json = objectMapper
                .writerWithView(MapaViews.Minimal.class)
                .writeValueAsString(atividade);

        assertThat(json).contains("\"codigo\":1");
        assertThat(json).doesNotContain("\"conhecimentos\"");
    }
}
