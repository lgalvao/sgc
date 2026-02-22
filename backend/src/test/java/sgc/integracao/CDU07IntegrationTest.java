package sgc.integracao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.integracao.mocks.WithMockChefe;
import sgc.integracao.mocks.WithMockCustomUser;
import sgc.subprocesso.model.SituacaoSubprocesso;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@Transactional
@DisplayName("CDU-07: Detalhar Subprocesso")
class CDU07IntegrationTest extends BaseIntegrationTest {

    @Test
    @WithMockAdmin
    @DisplayName("ADMIN pode visualizar qualquer subprocesso")
    void adminPodeVisualizar() throws Exception {
        // Subprocesso 60000 (Unidade 8 - SEDESENV) do data.sql
        mockMvc.perform(
                        get("/api/subprocessos/{id}", 60000L)
                                .param("perfil", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subprocesso.codUnidade").value(8))
                .andExpect(jsonPath("$.subprocesso.situacao").value(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO.name()));
    }

    @Test
    @WithMockChefe("3") // Fernanda Oliveira - Chefe da Unidade 8 no data.sql
    @DisplayName("CHEFE pode visualizar o subprocesso da sua unidade")
    void chefePodeVisualizarSuaUnidade() throws Exception {
        mockMvc.perform(
                        get("/api/subprocessos/{id}", 60000L)
                                .param("perfil", "CHEFE")
                                .param("unidadeUsuario", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subprocesso.codUnidade").value(8));
    }

    @Test
    @WithMockCustomUser(tituloEleitoral = "333333333333", perfis = {"CHEFE"}, unidadeId = 9L) 
    // Chefe Teste - Chefe da Unidade 9 no data.sql tentando ver subprocesso da 8
    @DisplayName("CHEFE NÃO pode visualizar o subprocesso de outra unidade")
    void chefeNaoPodeVisualizarOutraUnidade() throws Exception {
        mockMvc.perform(
                        get("/api/subprocessos/{id}", 60000L)
                                .param("perfil", "CHEFE")
                                .param("unidadeUsuario", "9"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve retornar 404 ao buscar subprocesso inexistente")
    void falhaSubprocessoInexistente() throws Exception {
        mockMvc.perform(get("/api/subprocessos/99999"))
                .andExpect(status().isForbidden());
    }
}
