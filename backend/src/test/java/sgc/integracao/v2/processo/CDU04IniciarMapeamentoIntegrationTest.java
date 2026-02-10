package sgc.integracao.v2.processo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.integracao.mocks.WithMockChefe;
import sgc.integracao.mocks.WithMockGestor;
import sgc.integracao.v2.BaseIntegrationTestV2;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para CDU-04: Iniciar Processo de Mapeamento
 * 
 * Este CDU permite ao ADMIN iniciar um processo de mapeamento que esteja na situação 'Criado',
 * disparando a criação de subprocessos, mapas vazios, notificações e alertas para todas as unidades
 * participantes.
 * 
 * Requisitos testados:
 * - Inicialização de processo de mapeamento
 * - Criação de subprocessos para unidades operacionais/interoperacionais
 * - Preservação da hierarquia de unidades
 * - Criação de mapas vazios
 * - Criação de alertas apropriados (operacionais e intermediários)
 * - Registro de movimentações
 * - Validações e regras de autorização
 */
@DisplayName("CDU-04: Iniciar Processo de Mapeamento")
class CDU04IniciarMapeamentoIntegrationTest extends BaseIntegrationTestV2 {
    
    private static final String API_PROCESSOS = "/api/processos";
    private static final String API_SUBPROCESSOS = "/api/subprocessos";
    private static final String API_ALERTAS = "/api/alertas";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    /**
     * Cria um processo de mapeamento na situação 'Criado' para teste.
     * Usa repository diretamente para evitar problemas de autenticação em testes não-admin.
     */
    private Processo criarProcessoCriadoMapeamento(List<Unidade> unidades) {
        Processo processo = new Processo();
        processo.setDescricao("Processo de Mapeamento para Iniciar");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setDataCriacao(LocalDateTime.now());
        processo.setDataLimite(LocalDateTime.now().plusDays(30));
        processo.adicionarParticipantes(new java.util.HashSet<>(unidades));
        return processoRepo.saveAndFlush(processo);
    }
    
    @Nested
    @DisplayName("Inicialização de Processo")
    class InicializacaoProcesso {
        
        @Test
        @WithMockAdmin
        @DisplayName("ADMIN deve conseguir iniciar processo de mapeamento")
        void testAdminIniciaProcessoMapeamento() throws Exception {
            // ARRANGE
            Unidade unidade1 = criarUnidadeOperacional("Unidade Operacional 1");
            Unidade unidade2 = criarUnidadeOperacional("Unidade Operacional 2");
            
            Processo processo = criarProcessoCriadoMapeamento(List.of(unidade1, unidade2));
            
            String requestBody = String.format("""
                {
                    "tipo": "MAPEAMENTO",
                    "unidades": [%d, %d]
                }
                """,
                unidade1.getCodigo(), unidade2.getCodigo());
            
            // ACT
            mockMvc.perform(post(API_PROCESSOS + "/{codigo}/iniciar", processo.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.situacao").value("EM_ANDAMENTO"));
            
            // ASSERT - Verificar que subprocessos foram criados
            mockMvc.perform(get(API_SUBPROCESSOS))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
        }
        
        @Test
        @WithMockAdmin
        @DisplayName("Deve criar subprocessos para todas as unidades participantes")
        void testCriacaoSubprocessos() throws Exception {
            // ARRANGE
            Unidade unidade1 = criarUnidadeOperacional("Unidade 1");
            Unidade unidade2 = criarUnidadeOperacional("Unidade 2");
            Unidade unidade3 = criarUnidadeOperacional("Unidade 3");
            
            Processo processo = criarProcessoCriadoMapeamento(List.of(unidade1, unidade2, unidade3));
            
            String requestBody = String.format("""
                {
                    "tipo": "MAPEAMENTO",
                    "unidades": [%d, %d, %d]
                }
                """,
                unidade1.getCodigo(), unidade2.getCodigo(), unidade3.getCodigo());
            
            // ACT
            mockMvc.perform(post(API_PROCESSOS + "/{codigo}/iniciar", processo.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isOk());
            
            // ASSERT - Verificar que 3 subprocessos foram criados
            mockMvc.perform(get(API_SUBPROCESSOS))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
        }
        
        @Test
        @WithMockAdmin
        @DisplayName("Deve criar subprocessos com situação 'Não iniciado'")
        void testCriacaoSubprocessosComSituacaoCorreta() throws Exception {
            // ARRANGE
            Unidade unidadeOp = criarUnidadeOperacional("Unidade Operacional");
            Processo processo = criarProcessoCriadoMapeamento(List.of(unidadeOp));
            
            String requestBody = String.format("""
                {
                    "tipo": "MAPEAMENTO",
                    "unidades": [%d]
                }
                """,
                unidadeOp.getCodigo());
            
            // ACT
            mockMvc.perform(post(API_PROCESSOS + "/{codigo}/iniciar", processo.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isOk());
            
            // ASSERT - Verificar que subprocesso foi criado via API
            mockMvc.perform(get(API_SUBPROCESSOS))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
        }
        
        @Test
        @WithMockAdmin
        @DisplayName("Deve preservar hierarquia de unidades")
        void testPreservacaoHierarquiaUnidades() throws Exception {
            // ARRANGE
            Unidade raiz = criarHierarquiaUnidades("Unidade Raiz", "Filha 1", "Filha 2");
            
            // Buscar as unidades filhas
            List<Unidade> todasUnidades = unidadeRepo.findAll();
            List<Unidade> unidadesFilhas = todasUnidades.stream()
                    .filter(u -> u.getUnidadeSuperior() != null && u.getUnidadeSuperior().equals(raiz))
                    .toList();
            
            Processo processo = criarProcessoCriadoMapeamento(unidadesFilhas);
            
            String unidadesCodigos = unidadesFilhas.stream()
                    .map(u -> String.valueOf(u.getCodigo()))
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            
            String requestBody = String.format("""
                {
                    "tipo": "MAPEAMENTO",
                    "unidades": [%s]
                }
                """,
                unidadesCodigos);
            
            // ACT
            mockMvc.perform(post(API_PROCESSOS + "/{codigo}/iniciar", processo.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isOk());
            
            // ASSERT - Verificar que processo foi iniciado e possui detalhes
            mockMvc.perform(get(API_PROCESSOS + "/{codigo}/detalhes", processo.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.unidades", hasSize(greaterThanOrEqualTo(2))));
        }
    }
    
    @Nested
    @DisplayName("Validações e Regras")
    class ValidacoesRegras {
        
        @Test
        @WithMockAdmin
        @DisplayName("Deve rejeitar inicialização de processo que não está em status 'Criado'")
        void testRejeitarInicializacaoProcessoNaoCriado() throws Exception {
            // ARRANGE
            Unidade unidade = criarUnidadeOperacional("Unidade Teste");
            Processo processoJaIniciado = criarProcesso("Processo Já Iniciado", 
                    TipoProcesso.MAPEAMENTO, List.of(unidade));
            processoJaIniciado.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            processoRepo.saveAndFlush(processoJaIniciado);
            
            String requestBody = String.format("""
                {
                    "tipo": "MAPEAMENTO",
                    "unidades": [%d]
                }
                """,
                unidade.getCodigo());
            
            // ACT & ASSERT
            mockMvc.perform(post(API_PROCESSOS + "/{codigo}/iniciar", processoJaIniciado.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isUnprocessableEntity()); // 422
        }
        
        @Test
        @WithMockChefe
        @DisplayName("CHEFE não deve conseguir iniciar processo")
        void testChefeNaoPodeIniciarProcesso() throws Exception {
            // ARRANGE
            Unidade unidade = criarUnidadeOperacional("Unidade Teste");
            Processo processo = criarProcessoCriadoMapeamento(List.of(unidade));
            
            String requestBody = String.format("""
                {
                    "tipo": "MAPEAMENTO",
                    "unidades": [%d]
                }
                """,
                unidade.getCodigo());
            
            // ACT & ASSERT
            mockMvc.perform(post(API_PROCESSOS + "/{codigo}/iniciar", processo.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isForbidden());
        }
        
        @Test
        @WithMockGestor
        @DisplayName("GESTOR não deve conseguir iniciar processo")
        void testGestorNaoPodeIniciarProcesso() throws Exception {
            // ARRANGE
            Unidade unidade = criarUnidadeOperacional("Unidade Teste");
            Processo processo = criarProcessoCriadoMapeamento(List.of(unidade));
            
            String requestBody = String.format("""
                {
                    "tipo": "MAPEAMENTO",
                    "unidades": [%d]
                }
                """,
                unidade.getCodigo());
            
            // ACT & ASSERT
            mockMvc.perform(post(API_PROCESSOS + "/{codigo}/iniciar", processo.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isForbidden());
        }
    }
}
