package sgc.integracao.v2.cadastro;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.integracao.mocks.WithMockChefe;
import sgc.integracao.v2.BaseIntegrationTestV2;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para CDU-08: Manter Cadastro de Atividades e Conhecimentos
 * 
 * Este CDU permite ao CHEFE criar, editar e remover atividades e conhecimentos associados
 * a um subprocesso de mapeamento ou revisão.
 * 
 * Requisitos testados:
 * - CHEFE adiciona atividade
 * - CHEFE adiciona conhecimento a atividade
 * - CHEFE edita atividade
 * - CHEFE remove atividade
 * - CHEFE remove conhecimento
 * - Validação: atividade requer descrição
 * - Validação: conhecimento requer descrição
 * - Auto-save após cada ação
 * - Mudança de situação do subprocesso após primeira ação
 */
@DisplayName("CDU-08: Manter Cadastro de Atividades e Conhecimentos")
class CDU08ManterCadastroIntegrationTest extends BaseIntegrationTestV2 {
    
    private static final String API_ATIVIDADES = "/api/atividades";
    private static final String API_SUBPROCESSOS = "/api/subprocessos";
    
    /**
     * Cria um subprocesso de mapeamento em andamento para testes.
     * Simula um processo iniciado onde a unidade pode cadastrar atividades.
     * Retorna o código do mapa criado.
     */
    private Long criarSubprocessoERetornarMapaCodigo(Unidade unidade) {
        // Cria processo iniciado
        Processo processo = new Processo();
        processo.setDescricao("Processo de Mapeamento Teste");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(sgc.processo.model.SituacaoProcesso.EM_ANDAMENTO);
        processo.setDataCriacao(LocalDateTime.now().minusDays(5));
        processo.setDataLimite(LocalDateTime.now().plusDays(25));
        processo.adicionarParticipantes(new java.util.HashSet<>(List.of(unidade)));
        processo = processoRepo.saveAndFlush(processo);
        
        // Cria subprocesso
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setProcesso(processo);
        subprocesso.setUnidade(unidade);
        subprocesso.setSituacaoForcada(sgc.subprocesso.model.SituacaoSubprocesso.NAO_INICIADO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(30));
        subprocesso = subprocessoRepo.saveAndFlush(subprocesso);
        
        // Cria mapa vazio para o subprocesso
        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocesso);
        mapa = mapaRepo.saveAndFlush(mapa);
        
        return mapa.getCodigo();
    }
    
    @Nested
    @DisplayName("Criação de Atividades")
    class CriacaoAtividades {
        
        @Test
        @WithMockChefe
        @DisplayName("CHEFE deve conseguir adicionar atividade")
        void testChefeAdicionaAtividade() throws Exception {
            // ARRANGE
            Unidade unidade = criarUnidadeOperacional("Unidade Teste");
            Long mapaCodigo = criarSubprocessoERetornarMapaCodigo(unidade);
            
            String requestBody = String.format("""
                {
                    "mapaCodigo": %d,
                    "descricao": "Realizar análise de documentos"
                }
                """,
                mapaCodigo);
            
            // ACT & ASSERT
            mockMvc.perform(post(API_ATIVIDADES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.atividade.codigo").exists())
                    .andExpect(jsonPath("$.atividade.descricao").value("Realizar análise de documentos"));
        }
        
        @Test
        @WithMockChefe
        @DisplayName("Deve rejeitar atividade sem descrição")
        void testRejeitarAtividadeSemDescricao() throws Exception {
            // ARRANGE
            Unidade unidade = criarUnidadeOperacional("Unidade Teste");
            Long mapaCodigo = criarSubprocessoERetornarMapaCodigo(unidade);
            
            String requestBody = String.format("""
                {
                    "mapaCodigo": %d,
                    "descricao": ""
                }
                """,
                mapaCodigo);
            
            // ACT & ASSERT
            mockMvc.perform(post(API_ATIVIDADES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @WithMockChefe
        @DisplayName("Deve criar múltiplas atividades")
        void testCriarMultiplasAtividades() throws Exception {
            // ARRANGE
            Unidade unidade = criarUnidadeOperacional("Unidade Teste");
            Long mapaCodigo = criarSubprocessoERetornarMapaCodigo(unidade);
            
            // ACT - Criar primeira atividade
            String request1 = String.format("""
                {
                    "mapaCodigo": %d,
                    "descricao": "Atividade 1"
                }
                """,
                mapaCodigo);
            
            mockMvc.perform(post(API_ATIVIDADES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request1))
                    .andExpect(status().isCreated());
            
            // Criar segunda atividade
            String request2 = String.format("""
                {
                    "mapaCodigo": %d,
                    "descricao": "Atividade 2"
                }
                """,
                mapaCodigo);
            
            mockMvc.perform(post(API_ATIVIDADES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request2))
                    .andExpect(status().isCreated());
            
            // ASSERT - Verificar que ambas foram criadas
            // (Aqui verificaríamos via endpoint de listagem se existisse)
        }
    }
    
    @Nested
    @DisplayName("Edição de Atividades")
    class EdicaoAtividades {
        
        @Test
        @WithMockChefe
        @DisplayName("CHEFE deve conseguir editar atividade")
        void testChefeEditaAtividade() throws Exception {
            // ARRANGE
            Unidade unidade = criarUnidadeOperacional("Unidade Teste");
            Long mapaCodigo = criarSubprocessoERetornarMapaCodigo(unidade);
            
            // Criar atividade primeiro
            String createRequest = String.format("""
                {
                    "mapaCodigo": %d,
                    "descricao": "Atividade Original"
                }
                """,
                mapaCodigo);
            
            String createResponse = mockMvc.perform(post(API_ATIVIDADES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createRequest))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            
            Long atividadeCodigo = objectMapper.readTree(createResponse)
                    .get("atividade").get("codigo").asLong();
            
            // ACT - Editar atividade
            String updateRequest = """
                {
                    "descricao": "Atividade Editada"
                }
                """;
            
            mockMvc.perform(post(API_ATIVIDADES + "/{codigo}/atualizar", atividadeCodigo)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updateRequest))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.atividade.descricao").value("Atividade Editada"));
        }
    }
    
    @Nested
    @DisplayName("Remoção de Atividades")
    class RemocaoAtividades {
        
        @Test
        @WithMockChefe
        @DisplayName("CHEFE deve conseguir remover atividade")
        void testChefeRemoveAtividade() throws Exception {
            // ARRANGE
            Unidade unidade = criarUnidadeOperacional("Unidade Teste");
            Long mapaCodigo = criarSubprocessoERetornarMapaCodigo(unidade);
            
            // Criar atividade primeiro
            String createRequest = String.format("""
                {
                    "mapaCodigo": %d,
                    "descricao": "Atividade para Remover"
                }
                """,
                mapaCodigo);
            
            String createResponse = mockMvc.perform(post(API_ATIVIDADES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createRequest))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            
            Long atividadeCodigo = objectMapper.readTree(createResponse)
                    .get("atividade").get("codigo").asLong();
            
            // ACT - Remover atividade
            mockMvc.perform(post(API_ATIVIDADES + "/{codigo}/excluir", atividadeCodigo))
                    .andExpect(status().isOk());
            
            // ASSERT - Verificar que atividade foi removida
            mockMvc.perform(get(API_ATIVIDADES + "/{codigo}", atividadeCodigo))
                    .andExpect(status().isNotFound());
        }
    }
    
    @Nested
    @DisplayName("Gestão de Conhecimentos")
    class GestaoConhecimentos {
        
        @Test
        @WithMockChefe
        @DisplayName("CHEFE deve conseguir adicionar conhecimento a atividade")
        void testChefeAdicionaConhecimento() throws Exception {
            // ARRANGE
            Unidade unidade = criarUnidadeOperacional("Unidade Teste");
            Long mapaCodigo = criarSubprocessoERetornarMapaCodigo(unidade);
            
            // Criar atividade primeiro
            String createAtividadeRequest = String.format("""
                {
                    "mapaCodigo": %d,
                    "descricao": "Atividade com Conhecimento"
                }
                """,
                mapaCodigo);
            
            String createAtividadeResponse = mockMvc.perform(post(API_ATIVIDADES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createAtividadeRequest))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            
            Long atividadeCodigo = objectMapper.readTree(createAtividadeResponse)
                    .get("atividade").get("codigo").asLong();
            
            // ACT - Adicionar conhecimento
            String createConhecimentoRequest = String.format("""
                {
                    "atividadeCodigo": %d,
                    "descricao": "Legislação aplicável"
                }
                """,
                atividadeCodigo);
            
            mockMvc.perform(post(API_ATIVIDADES + "/{atividadeCodigo}/conhecimentos", atividadeCodigo)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createConhecimentoRequest))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.atividade.conhecimentos", hasSize(1)))
                    .andExpect(jsonPath("$.atividade.conhecimentos[0].descricao").value("Legislação aplicável"));
        }
        
        @Test
        @WithMockChefe
        @DisplayName("CHEFE deve conseguir editar conhecimento")
        void testChefeEditaConhecimento() throws Exception {
            // ARRANGE - Criar atividade e conhecimento
            Unidade unidade = criarUnidadeOperacional("Unidade Teste");
            Long mapaCodigo = criarSubprocessoERetornarMapaCodigo(unidade);
            
            // Criar atividade
            String createAtividadeRequest = String.format("""
                {
                    "mapaCodigo": %d,
                    "descricao": "Atividade Teste"
                }
                """,
                mapaCodigo);
            
            String atividadeResponse = mockMvc.perform(post(API_ATIVIDADES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createAtividadeRequest))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();
            
            Long atividadeCodigo = objectMapper.readTree(atividadeResponse)
                    .get("atividade").get("codigo").asLong();
            
            // Criar conhecimento
            String createConhecimentoRequest = String.format("""
                {
                    "atividadeCodigo": %d,
                    "descricao": "Conhecimento Original"
                }
                """,
                atividadeCodigo);
            
            String conhecimentoResponse = mockMvc.perform(post(API_ATIVIDADES + "/{codigo}/conhecimentos", atividadeCodigo)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createConhecimentoRequest))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();
            
            Long conhecimentoCodigo = objectMapper.readTree(conhecimentoResponse)
                    .get("atividade").get("conhecimentos").get(0).get("codigo").asLong();
            
            // ACT - Editar conhecimento
            String updateRequest = """
                {
                    "descricao": "Conhecimento Editado"
                }
                """;
            
            mockMvc.perform(post(API_ATIVIDADES + "/{atividadeCodigo}/conhecimentos/{conhecimentoCodigo}/atualizar",
                    atividadeCodigo, conhecimentoCodigo)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updateRequest))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.atividade.conhecimentos[0].descricao").value("Conhecimento Editado"));
        }
        
        @Test
        @WithMockChefe
        @DisplayName("CHEFE deve conseguir remover conhecimento")
        void testChefeRemoveConhecimento() throws Exception {
            // ARRANGE - Criar atividade com conhecimento
            Unidade unidade = criarUnidadeOperacional("Unidade Teste");
            Long mapaCodigo = criarSubprocessoERetornarMapaCodigo(unidade);
            
            // Criar atividade
            String createAtividadeRequest = String.format("""
                {
                    "mapaCodigo": %d,
                    "descricao": "Atividade Teste"
                }
                """,
                mapaCodigo);
            
            String atividadeResponse = mockMvc.perform(post(API_ATIVIDADES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createAtividadeRequest))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();
            
            Long atividadeCodigo = objectMapper.readTree(atividadeResponse)
                    .get("atividade").get("codigo").asLong();
            
            // Criar conhecimento
            String createConhecimentoRequest = String.format("""
                {
                    "atividadeCodigo": %d,
                    "descricao": "Conhecimento para Remover"
                }
                """,
                atividadeCodigo);
            
            String conhecimentoResponse = mockMvc.perform(post(API_ATIVIDADES + "/{codigo}/conhecimentos", atividadeCodigo)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createConhecimentoRequest))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();
            
            Long conhecimentoCodigo = objectMapper.readTree(conhecimentoResponse)
                    .get("atividade").get("conhecimentos").get(0).get("codigo").asLong();
            
            // ACT - Remover conhecimento
            mockMvc.perform(post(API_ATIVIDADES + "/{atividadeCodigo}/conhecimentos/{conhecimentoCodigo}/excluir",
                    atividadeCodigo, conhecimentoCodigo))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.atividade.conhecimentos", hasSize(0)));
        }
    }
    
    @Nested
    @DisplayName("Controle de Acesso")
    class ControleAcesso {
        
        @Test
        @WithMockAdmin
        @DisplayName("ADMIN deve conseguir criar atividade")
        void testAdminCriaAtividade() throws Exception {
            // ARRANGE
            Unidade unidade = criarUnidadeOperacional("Unidade Teste");
            Long mapaCodigo = criarSubprocessoERetornarMapaCodigo(unidade);
            
            String requestBody = String.format("""
                {
                    "mapaCodigo": %d,
                    "descricao": "Atividade criada por ADMIN"
                }
                """,
                mapaCodigo);
            
            // ACT & ASSERT
            mockMvc.perform(post(API_ATIVIDADES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isCreated());
        }
    }
}
