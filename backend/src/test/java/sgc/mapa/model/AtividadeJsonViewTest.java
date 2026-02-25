package sgc.mapa.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@DisplayName("Testes de Serialização @JsonView - Atividade")
class AtividadeJsonViewTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve serializar campos públicos da Atividade incluindo conhecimentos")
    void deveSerializarCamposPublicos() {
        Mapa mapa = Mapa.builder().codigo(10L).build();
        Atividade atividade = Atividade.builder()
                .codigo(1L)
                .descricao("Atividade Teste")
                .mapa(mapa)
                .build();
        
        Conhecimento conhecimento = Conhecimento.builder()
                .codigo(100L)
                .descricao("K1")
                .atividade(atividade)
                .build();
        
        atividade.setConhecimentos(Set.of(conhecimento));

        String json = objectMapper
                .writerWithView(MapaViews.Publica.class)
                .writeValueAsString(atividade);

        assertThat(json)
                .contains("\"codigo\":1")
                .contains("\"descricao\":\"Atividade Teste\"")
                .contains("\"mapaCodigo\":10")
                .contains("\"conhecimentos\"")
                .contains("\"descricao\":\"K1\"")
                .contains("\"competencias\"")
                .doesNotContain("\"mapa\":");
    }

    @Test
    @DisplayName("Deve ocultar conhecimentos na visão Minimal")
    void deveOcultarConhecimentosEmVisaoMinimal() {
        Atividade atividade = Atividade.builder()
                .codigo(1L)
                .descricao("Atividade Teste")
                .conhecimentos(Set.of(Conhecimento.builder().codigo(100L).descricao("K1").build()))
                .build();

        String json = objectMapper
                .writerWithView(MapaViews.Minimal.class)
                .writeValueAsString(atividade);

        assertThat(json)
                .contains("\"codigo\":1")
                .doesNotContain("\"conhecimentos\"");
    }
}
