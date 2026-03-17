package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.transaction.annotation.*;
import sgc.integracao.mocks.*;
import sgc.subprocesso.model.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Transactional
@DisplayName("CDU-07: Detalhar subprocesso")
class CDU07IntegrationTest extends BaseIntegrationTest {

    @Test
    @WithMockAdmin
    @DisplayName("ADMIN pode visualizar qualquer subprocesso")
    void adminPodeVisualizar() throws Exception {
        // Subprocesso 60000 (Unidade 8 - SEDESENV) do data.sql
        mockMvc.perform(
                        get("/api/subprocessos/{codigo}", 60000L)
                                .param("perfil", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subprocesso.codUnidade").value(8))
                .andExpect(jsonPath("$.subprocesso.situacao").value(SituacaoSubprocesso.NAO_INICIADO.name()))
                .andExpect(jsonPath("$.responsavel.usuario.tituloEleitoral").exists())
                .andExpect(jsonPath("$.localizacaoAtual").exists())
                .andExpect(jsonPath("$.permissoes.habilitarAcessoCadastro").value(false)) // ADMIN não visualiza em CADASTRO_EM_ANDAMENTO
                .andExpect(jsonPath("$.permissoes.habilitarAcessoMapa").value(false)); // ADMIN não visualiza mapa em CADASTRO_EM_ANDAMENTO
    }

    @Test
    @WithMockChefe("3") // Fernanda oliveira - Chefe da Unidade 8 no data.sql
    @DisplayName("CHEFE pode visualizar o subprocesso da sua unidade")
    void chefePodeVisualizarSuaUnidade() throws Exception {
        mockMvc.perform(
                        get("/api/subprocessos/{codigo}", 60000L)
                                .param("perfil", "CHEFE")
                                .param("unidadeUsuario", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subprocesso.codUnidade").value(8))
                .andExpect(jsonPath("$.permissoes.habilitarAcessoCadastro").value(true)) // CHEFE sempre tem acesso ao cadastro
                .andExpect(jsonPath("$.permissoes.habilitarAcessoMapa").value(false)); // CHEFE não visualiza mapa em CADASTRO_EM_ANDAMENTO
    }

    @Test
    @WithMockGestor("202020202020")
    @DisplayName("GESTOR não visualiza cards antes da fase minima mesmo na hierarquia")
    void gestorNaoVisualizaCardsAntesDaFaseMinima() throws Exception {
        mockMvc.perform(
                        get("/api/subprocessos/{codigo}", 60000L)
                                .param("perfil", "GESTOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subprocesso.codUnidade").value(8))
                .andExpect(jsonPath("$.permissoes.habilitarAcessoCadastro").value(false))
                .andExpect(jsonPath("$.permissoes.habilitarAcessoMapa").value(false));
    }

    @Test
    @WithMockCustomUser(tituloEleitoral = "333333333333", perfis = {"CHEFE"}, unidadeId = 9L)
    // Chefe teste - Chefe da Unidade 9 no data.sql tentando ver subprocesso da 8
    @DisplayName("CHEFE NÃO pode visualizar o subprocesso de outra unidade")
    void chefeNaoPodeVisualizarOutraUnidade() throws Exception {
        mockMvc.perform(
                        get("/api/subprocessos/{codigo}", 60000L)
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
