package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.http.*;
import org.springframework.transaction.annotation.*;
import sgc.integracao.mocks.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Transactional
@DisplayName("CDU-37: Gerar relatório de unidades sem mapas vigentes")
class CDU37IntegrationTest extends BaseIntegrationTest {
    private static final String API_REL_UNIDADES_SEM_MAPAS = "/api/relatorios/unidades-sem-mapas-vigentes/exportar";
    private static final String API_UNIDADES_SEM_MAPA_VIGENTE = "/api/unidades/sem-mapa-vigente";

    @Test
    @DisplayName("Deve gerar relatório de unidades sem mapas vigentes em PDF quando ADMIN")
    @WithMockAdmin
    void gerarRelatorioUnidadesSemMapasVigentes_comoAdmin_sucesso() throws Exception {
        mockMvc.perform(get(API_REL_UNIDADES_SEM_MAPAS).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/pdf"))
                .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION));
    }

    @Test
    @DisplayName("Deve negar geração do relatório de unidades sem mapas vigentes quando GESTOR")
    @WithMockGestor
    void gerarRelatorioUnidadesSemMapasVigentes_comoGestor_proibido() throws Exception {
        mockMvc.perform(get(API_REL_UNIDADES_SEM_MAPAS).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve buscar códigos das unidades sem mapa vigente com sucesso quando ADMIN")
    @WithMockAdmin
    void buscarCodigosUnidadesSemMapaVigente_comoAdmin_sucesso() throws Exception {
        mockMvc.perform(get(API_UNIDADES_SEM_MAPA_VIGENTE).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Deve negar busca de códigos de unidades sem mapa vigente quando GESTOR")
    @WithMockGestor
    void buscarCodigosUnidadesSemMapaVigente_comoGestor_proibido() throws Exception {
        mockMvc.perform(get(API_UNIDADES_SEM_MAPA_VIGENTE).with(csrf()))
                .andExpect(status().isForbidden());
    }
}
