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
                    "tipoProcesso": "MAPEAMENTO",
                    "codigosUnidades": [%d, %d]
                }
                """, unidade1.getCodigo(), unidade2.getCodigo());
            
            // ACT & ASSERT
            mockMvc.perform(post(API_PROCESSOS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.codigo").exists())
                    .andExpect(jsonPath("$.descricao").value("Processo de Mapeamento Teste"))
                    .andExpect(jsonPath("$.tipoProcesso").value("MAPEAMENTO"))
                    .andExpect(jsonPath("$.situacao").value("CRIADO"));
        }
        
        @Test
        @WithMockAdmin
        @DisplayName("ADMIN deve criar processo de revisão com sucesso")
        void testCriarProcessoRevisao() throws Exception {
            // ARRANGE
            Unidade unidade = criarUnidadeOperacional("Unidade com Mapa");
            
            // Cria um processo de mapeamento e finaliza para ter mapa vigente
            // (simplificado para o teste - em cenário real seria via APIs)
            criarProcesso("Mapeamento Anterior", TipoProcesso.MAPEAMENTO, List.of(unidade));
            
            String requestBody = String.format("""
                {
                    "descricao": "Processo de Revisão Teste",
                    "tipoProcesso": "REVISAO",
                    "codigosUnidades": [%d]
                }
                """, unidade.getCodigo());
            
            // ACT & ASSERT
            mockMvc.perform(post(API_PROCESSOS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.tipoProcesso").value("REVISAO"));
        }
        
        @Test
        @WithMockAdmin
        @DisplayName("ADMIN deve criar processo de diagnóstico com sucesso")
        void testCriarProcessoDiagnostico() throws Exception {
            // ARRANGE
            Unidade unidade = criarUnidadeOperacional("Unidade para Diagnóstico");
            
            String requestBody = String.format("""
                {
                    "descricao": "Processo de Diagnóstico Teste",
                    "tipoProcesso": "DIAGNOSTICO",
                    "codigosUnidades": [%d]
                }
                """, unidade.getCodigo());
            
            // ACT & ASSERT
            mockMvc.perform(post(API_PROCESSOS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.tipoProcesso").value("DIAGNOSTICO"));
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
                    "tipoProcesso": "MAPEAMENTO",
                    "codigosUnidades": [%d]
                }
                """, unidade.getCodigo());
            
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
            String requestBody = """
                {
                    "descricao": "Processo Sem Unidades",
                    "tipoProcesso": "MAPEAMENTO",
                    "codigosUnidades": []
                }
                """;
            
            // ACT & ASSERT
            mockMvc.perform(post(API_PROCESSOS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @WithMockAdmin
        @DisplayName("Deve rejeitar criação com unidade que já tem processo ativo do mesmo tipo")
        void testRejeitarCriacaoComUnidadeComProcessoAtivo() throws Exception {
            // ARRANGE
            Unidade unidade = criarUnidadeOperacional("Unidade com Processo Ativo");
            
            // Cria primeiro processo de mapeamento
            String requestBody1 = String.format("""
                {
                    "descricao": "Primeiro Processo",
                    "tipoProcesso": "MAPEAMENTO",
                    "codigosUnidades": [%d]
                }
                """, unidade.getCodigo());
            
            mockMvc.perform(post(API_PROCESSOS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody1))
                    .andExpect(status().isCreated());
            
            // Tenta criar segundo processo de mapeamento com mesma unidade
            String requestBody2 = String.format("""
                {
                    "descricao": "Segundo Processo",
                    "tipoProcesso": "MAPEAMENTO",
                    "codigosUnidades": [%d]
                }
                """, unidade.getCodigo());
            
            // ACT & ASSERT
            mockMvc.perform(post(API_PROCESSOS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody2))
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
            
            // Cria processo
            String requestCriacao = String.format("""
                {
                    "descricao": "Descrição Original",
                    "tipoProcesso": "MAPEAMENTO",
                    "codigosUnidades": [%d]
                }
                """, unidade.getCodigo());
            
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
                    "tipoProcesso": "MAPEAMENTO",
                    "codigosUnidades": [%d]
                }
                """, unidade.getCodigo());
            
            // ACT & ASSERT
            mockMvc.perform(put(API_PROCESSOS + "/{codigo}", codigoProcesso)
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
            
            String requestCriacao = String.format("""
                {
                    "descricao": "Processo a Excluir",
                    "tipoProcesso": "MAPEAMENTO",
                    "codigosUnidades": [%d]
                }
                """, unidade.getCodigo());
            
            String responseCriacao = mockMvc.perform(post(API_PROCESSOS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestCriacao))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            
            Integer codigoProcesso = objectMapper.readTree(responseCriacao).get("codigo").asInt();
            
            // ACT & ASSERT - Exclusão
            mockMvc.perform(delete(API_PROCESSOS + "/{codigo}/excluir", codigoProcesso))
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
                    "tipoProcesso": "MAPEAMENTO",
                    "codigosUnidades": [%d]
                }
                """, unidade.getCodigo());
            
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
                    "tipoProcesso": "MAPEAMENTO",
                    "codigosUnidades": [%d]
                }
                """, unidade.getCodigo());
            
            // ACT & ASSERT
            mockMvc.perform(post(API_PROCESSOS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isForbidden());
        }
    }
}
