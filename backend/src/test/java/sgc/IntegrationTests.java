package sgc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import sgc.service.notification.MockNotificationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integração básicos que validam fluxos essenciais das APIs usando H2 em memória.
 * - Cria Mapas, Competências, Atividades e Conhecimentos via endpoints e valida respostas.
 * <p>
 * Observação: os controllers foram refatorados para usar DTOs; os testes usam os endpoints públicos (JSON).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class IntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateMapaAndCompetenciaFlow() throws Exception {
        // Criar um Mapa mínimo
        String mapaJson = "{}";
        MvcResult mapaResult = mockMvc.perform(post("/api/mapas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapaJson))
                .andExpect(status().isCreated())
                .andReturn();

        String mapaBody = mapaResult.getResponse().getContentAsString();
        JsonNode mapaNode = objectMapper.readTree(mapaBody);
        Long mapaCodigo = mapaNode.has("codigo") && !mapaNode.get("codigo").isNull() ? mapaNode.get("codigo").asLong() : null;
        assertThat(mapaCodigo).isNotNull();

        // Criar Competencia referenciando o mapa criado
        String competenciaJson = objectMapper.writeValueAsString(
                objectMapper.createObjectNode()
                        .put("mapaCodigo", mapaCodigo)
                        .put("descricao", "Competencia de teste")
        );

        MvcResult compResult = mockMvc.perform(post("/api/competencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(competenciaJson))
                .andExpect(status().isCreated())
                .andReturn();

        String compBody = compResult.getResponse().getContentAsString();
        JsonNode compNode = objectMapper.readTree(compBody);
        assertThat(compNode.get("descricao").asText()).isEqualTo("Competencia de teste");
        assertThat(compNode.get("codigo").asLong()).isGreaterThan(0);

        // Verificar listagem inclui a competencia criada
        MvcResult listResult = mockMvc.perform(get("/api/competencias"))
                .andExpect(status().isOk())
                .andReturn();

        String listBody = listResult.getResponse().getContentAsString();
        JsonNode listNode = objectMapper.readTree(listBody);
        boolean encontrou = false;
        for (JsonNode n : listNode) {
            if (n.has("codigo") && n.get("codigo").asLong() == compNode.get("codigo").asLong()) {
                encontrou = true;
                break;
            }
        }
        assertThat(encontrou).isTrue();
    }

    @Test
    public void testCreateAtividadeAndConhecimentoFlow() throws Exception {
        // criar mapa
        MvcResult mapaResult = mockMvc.perform(post("/api/mapas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andReturn();

        Long mapaCodigo = objectMapper.readTree(mapaResult.getResponse().getContentAsString()).get("codigo").asLong();

        // criar atividade
        String atividadeJson = objectMapper.writeValueAsString(
                objectMapper.createObjectNode()
                        .put("mapaCodigo", mapaCodigo)
                        .put("descricao", "Atividade de teste")
        );

        MvcResult ativResult = mockMvc.perform(post("/api/atividades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(atividadeJson))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode ativNode = objectMapper.readTree(ativResult.getResponse().getContentAsString());
        Long atividadeCodigo = ativNode.get("codigo").asLong();

        // criar conhecimento referenciando atividade
        String conhecimentoJson = objectMapper.writeValueAsString(
                objectMapper.createObjectNode()
                        .put("atividadeCodigo", atividadeCodigo)
                        .put("descricao", "Conhecimento de teste")
        );

        MvcResult conResult = mockMvc.perform(post("/api/conhecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(conhecimentoJson))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode conNode = objectMapper.readTree(conResult.getResponse().getContentAsString());
        assertThat(conNode.get("descricao").asText()).isEqualTo("Conhecimento de teste");
        assertThat(conNode.get("codigo").asLong()).isGreaterThan(0);
    }

    @Autowired
    private MockNotificationService mockNotificationService;

    @Test
    public void testMockNotificationCapturesEmails() throws Exception {
        mockNotificationService.clear();
        mockNotificationService.sendEmail("test@example.com", "Assunto de teste", "Corpo do teste");
        assertThat(mockNotificationService.getSentMessages()).hasSize(1);
    }
}