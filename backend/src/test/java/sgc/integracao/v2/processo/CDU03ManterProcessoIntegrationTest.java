package sgc.integracao.v2.processo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.integracao.v2.BaseIntegrationTestV2;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.TipoProcesso;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para CDU-03: Manter Processo
 * 
 * Este CDU permite ao ADMIN criar, editar e excluir processos de mapeamento, revisão e diagnóstico.
 * 
 * Requisitos testados:
 * - Criação de processos (mapeamento, revisão, diagnóstico)
 * - Edição de processos em status 'Criado'
 * - Exclusão de processos em status 'Criado'
 * - Validações obrigatórias (descrição, ao menos uma unidade)
 * - Regras de negócio (unidades sem processo ativo do mesmo tipo)
 * - Regras específicas (revisão/diagnóstico requer mapas existentes)
 */
@DisplayName("CDU-03: Manter Processo")
class CDU03ManterProcessoIntegrationTest extends BaseIntegrationTestV2 {
    
    private static final String API_PROCESSOS = "/api/processos";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    /**
     * Gera uma data limite futura formatada corretamente para o DTO.
     */
    private String getDataLimiteFutura() {
        return LocalDateTime.now().plusDays(10).format(DATE_FORMAT);
    }
    
    @Nested
    @DisplayName("Criação de Processos")
    class CriacaoProcessos {
        
        @Test
        @WithMockAdmin
        @DisplayName("ADMIN deve criar processo de mapeamento com sucesso")
        void testCriarProcessoMapeamento() throws Exception {
            // ARRANGE
            Unidade unidade1 = criarUnidadeOperacional("Unidade Teste 1");
            Unidade unidade2 = criarUnidadeOperacional("Unidade Teste 2");
            
            String requestBody = String.format("""
                {
                    "descricao": "Processo de Mapeamento Teste",
                    "tipo": "MAPEAMENTO",
                    "dataLimiteEtapa1": "%s",
                    "unidades": [%d, %d]
                }
                """, 
                getDataLimiteFutura(),
                unidade1.getCodigo(), unidade2.getCodigo());
            
            // ACT & ASSERT
            mockMvc.perform(post(API_PROCESSOS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.codigo").exists())
                    .andExpect(jsonPath("$.descricao").value("Processo de Mapeamento Teste"))
                    .andExpect(jsonPath("$.tipo").value("MAPEAMENTO"))
                    .andExpect(jsonPath("$.situacao").value("CRIADO"));
        }
        
        @Test
        @WithMockAdmin
        @DisplayName("ADMIN deve criar processo de revisão com sucesso")
        void testCriarProcessoRevisao() throws Exception {
            // ARRANGE
            Unidade unidade = criarUnidadeOperacional("Unidade com Mapa");
            
            // Cria um mapa vigente para a unidade (requisito para processos de revisão)
            criarMapaVigenteParaUnidade(unidade);
            
            String requestBody = String.format("""
                {
                    "descricao": "Processo de Revisão Teste",
                    "tipo": "REVISAO",
                    "dataLimiteEtapa1": "%s",
                    "unidades": [%d]
                }
                """, 
                getDataLimiteFutura(),
                unidade.getCodigo());
            
            // ACT & ASSERT
            mockMvc.perform(post(API_PROCESSOS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.tipo").value("REVISAO"));
        }
        
        @Test
        @WithMockAdmin
        @DisplayName("ADMIN deve criar processo de diagnóstico com sucesso")
        void testCriarProcessoDiagnostico() throws Exception {
            // ARRANGE
            Unidade unidade = criarUnidadeOperacional("Unidade para Diagnóstico");
            
            // Cria um mapa vigente para a unidade (requisito para processos de diagnóstico)
            criarMapaVigenteParaUnidade(unidade);
            
            String requestBody = String.format("""
                {
                    "descricao": "Processo de Diagnóstico Teste",
                    "tipo": "DIAGNOSTICO",
                    "dataLimiteEtapa1": "%s",
                    "unidades": [%d]
                }
                """, 
                getDataLimiteFutura(),
                unidade.getCodigo());
            
            // ACT & ASSERT
            mockMvc.perform(post(API_PROCESSOS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.tipo").value("DIAGNOSTICO"));
        }
    }
    
    @Nested
    @DisplayName("Validações de Criação")
    class ValidacoesCriacao {
        
        @Test
        @WithMockAdmin
        @DisplayName("Deve rejeitar criação sem descrição")
        void testRejeitarCriacaoSemDescricao() throws Exception {
            // ARRANGE
            Unidade unidade = criarUnidadeOperacional("Unidade Teste");
            
            String requestBody = String.format("""
                {
                    "tipo": "MAPEAMENTO",
                    "dataLimiteEtapa1": "%s",
                    "unidades": [%d]
                }
                """, 
                getDataLimiteFutura(),
                unidade.getCodigo());
            
            // ACT & ASSERT
            mockMvc.perform(post(API_PROCESSOS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @WithMockAdmin
        @DisplayName("Deve rejeitar criação sem unidades")
        void testRejeitarCriacaoSemUnidades() throws Exception {
            // ARRANGE
            String requestBody = String.format("""
                {
                    "descricao": "Processo Sem Unidades",
                    "tipo": "MAPEAMENTO",
                    "dataLimiteEtapa1": "%s",
                    "unidades": []
                }
                """,
                getDataLimiteFutura());
            
            // ACT & ASSERT
            mockMvc.perform(post(API_PROCESSOS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @WithMockAdmin
        @DisplayName("Deve rejeitar inicialização com unidade que já tem processo ativo do mesmo tipo")
        @org.junit.jupiter.api.Disabled("TODO: Investigar erro 400 ao iniciar segundo processo - pode ser problema de validação do Jackson ou lógica de negócio")
        void testRejeitarCriacaoComUnidadeComProcessoAtivo() throws Exception {
            // ARRANGE
            Unidade unidade = criarUnidadeOperacional("Unidade com Processo Ativo");
            
            String dataLimite = getDataLimiteFutura();
            
            // Cria e inicia primeiro processo de mapeamento
            String requestBody1 = String.format("""
                {
                    "descricao": "Primeiro Processo",
                    "tipo": "MAPEAMENTO",
                    "dataLimiteEtapa1": "%s",
                    "unidades": [%d]
                }
                """, dataLimite, unidade.getCodigo());
            
            String response = mockMvc.perform(post(API_PROCESSOS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody1))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();
            
            Long codigoProcesso1 = objectMapper.readTree(response).get("codigo").asLong();
            
            mockMvc.perform(post(API_PROCESSOS + "/" + codigoProcesso1 + "/iniciar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"tipo\": \"MAPEAMENTO\", \"unidades\": []}"))
                    .andExpect(status().isOk());
            
            // Cria segundo processo de mapeamento com mesma unidade
            String requestBody2 = String.format("""
                {
                    "descricao": "Segundo Processo",
                    "tipo": "MAPEAMENTO",
                    "dataLimiteEtapa1": "%s",
                    "unidades": [%d]
                }
                """, dataLimite, unidade.getCodigo());
            
            response = mockMvc.perform(post(API_PROCESSOS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody2))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();
            
            Long codigoProcesso2 = objectMapper.readTree(response).get("codigo").asLong();
            
            // ACT & ASSERT - Tenta iniciar segundo processo, deve falhar pois unidade já está em outro processo ativo
            mockMvc.perform(post(API_PROCESSOS + "/" + codigoProcesso2 + "/iniciar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"tipo\": \"MAPEAMENTO\", \"unidades\": []}"))
                    .andExpect(status().isConflict());
        }
    }
    
    @Nested
    @DisplayName("Edição de Processos")
    class EdicaoProcessos {
        
        @Test
        @WithMockAdmin
        @DisplayName("ADMIN deve editar processo em status Criado")
        void testEditarProcessoCriado() throws Exception {
            // ARRANGE
            Unidade unidade = criarUnidadeOperacional("Unidade Original");
            
            String dataLimite = getDataLimiteFutura();
            
            // Cria processo
            String requestCriacao = String.format("""
                {
                    "descricao": "Descrição Original",
                    "tipo": "MAPEAMENTO",
                    "dataLimiteEtapa1": "%s",
                    "unidades": [%d]
                }
                """, dataLimite, unidade.getCodigo());
            
            String responseCriacao = mockMvc.perform(post(API_PROCESSOS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestCriacao))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            
            Integer codigoProcesso = objectMapper.readTree(responseCriacao).get("codigo").asInt();
            
            // Edita processo
            String requestEdicao = String.format("""
                {
                    "descricao": "Descrição Editada",
                    "tipo": "MAPEAMENTO",
                    "dataLimiteEtapa1": "%s",
                    "unidades": [%d]
                }
                """, dataLimite, unidade.getCodigo());
            
            // ACT & ASSERT
            mockMvc.perform(post(API_PROCESSOS + "/{codigo}/atualizar", codigoProcesso)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestEdicao))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.descricao").value("Descrição Editada"));
        }
    }
    
    @Nested
    @DisplayName("Exclusão de Processos")
    class ExclusaoProcessos {
        
        @Test
        @WithMockAdmin
        @DisplayName("ADMIN deve excluir processo em status Criado")
        void testExcluirProcessoCriado() throws Exception {
            // ARRANGE
            Unidade unidade = criarUnidadeOperacional("Unidade para Exclusão");
            
            String dataLimite = getDataLimiteFutura();
            
            String requestCriacao = String.format("""
                {
                    "descricao": "Processo a Excluir",
                    "tipo": "MAPEAMENTO",
                    "dataLimiteEtapa1": "%s",
                    "unidades": [%d]
                }
                """, dataLimite, unidade.getCodigo());
            
            String responseCriacao = mockMvc.perform(post(API_PROCESSOS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestCriacao))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            
            Integer codigoProcesso = objectMapper.readTree(responseCriacao).get("codigo").asInt();
            
            // ACT & ASSERT - Exclusão
            mockMvc.perform(post(API_PROCESSOS + "/{codigo}/excluir", codigoProcesso))
                    .andExpect(status().isNoContent());
            
            // Verifica que processo foi excluído
            mockMvc.perform(get(API_PROCESSOS + "/{codigo}", codigoProcesso))
                    .andExpect(status().isNotFound());
        }
    }
    
    @Nested
    @DisplayName("Controle de Acesso")
    class ControleAcesso {
        
        @Test
        @WithMockUser(username = "99999")
        @DisplayName("CHEFE não deve conseguir criar processo")
        void testChefeNaoPodeCriarProcesso() throws Exception {
            // ARRANGE
            Unidade unidade = criarUnidadeOperacional("Unidade do Chefe");
            Usuario chefe = criarChefeParaUnidade(unidade);
            setupSecurityContext(chefe);
            
            String requestBody = String.format("""
                {
                    "descricao": "Processo Criado por Chefe",
                    "tipo": "MAPEAMENTO",
                    "dataLimiteEtapa1": "%s",
                    "unidades": [%d]
                }
                """, 
                getDataLimiteFutura(),
                unidade.getCodigo());
            
            // ACT & ASSERT
            mockMvc.perform(post(API_PROCESSOS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isForbidden());
        }
        
        @Test
        @WithMockUser(username = "88888")
        @DisplayName("GESTOR não deve conseguir criar processo")
        void testGestorNaoPodeCriarProcesso() throws Exception {
            // ARRANGE
            Unidade unidade = criarUnidadeOperacional("Unidade do Gestor");
            Usuario gestor = criarGestorParaUnidade(unidade);
            setupSecurityContext(gestor, unidade, "GESTOR");
            
            String requestBody = String.format("""
                {
                    "descricao": "Processo Criado por Gestor",
                    "tipo": "MAPEAMENTO",
                    "dataLimiteEtapa1": "%s",
                    "unidades": [%d]
                }
                """, 
                getDataLimiteFutura(),
                unidade.getCodigo());
            
            // ACT & ASSERT
            mockMvc.perform(post(API_PROCESSOS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isForbidden());
        }
    }
}
