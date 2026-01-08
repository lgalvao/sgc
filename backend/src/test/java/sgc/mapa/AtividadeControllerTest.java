package sgc.mapa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.comum.erros.RestExceptionHandler;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.dto.ResultadoOperacaoConhecimento;
import sgc.mapa.service.AtividadeFacade;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.service.ConhecimentoService;
import sgc.subprocesso.dto.AtividadeOperacaoResp;
import sgc.subprocesso.dto.AtividadeVisualizacaoDto;
import sgc.subprocesso.dto.SubprocessoSituacaoDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AtividadeController.class)
@Import({TestSecurityConfig.class, RestExceptionHandler.class})
@DisplayName("Testes do Controlador de Atividades")
class AtividadeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AtividadeService atividadeService;

    @MockitoBean
    private AtividadeFacade atividadeFacade;

    @MockitoBean
    private ConhecimentoService conhecimentoService;

    @Nested
    @DisplayName("Operações de Atividade")
    class OperacoesAtividade {
        @Test
        @DisplayName("Deve obter por ID")
        @WithMockUser
        void deveObterPorId() throws Exception {
            AtividadeDto dto = new AtividadeDto();
            dto.setCodigo(1L);
            Mockito.when(atividadeService.obterDto(1L)).thenReturn(dto);

            mockMvc.perform(get("/api/atividades/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.codigo").value(1));
        }


        @Test
        @DisplayName("Deve criar atividade")
        @WithMockUser(username = "123")
        void deveCriarAtividade() throws Exception {
            AtividadeVisualizacaoDto dto = new AtividadeVisualizacaoDto();
            dto.setCodigo(10L);

            AtividadeOperacaoResp response = AtividadeOperacaoResp.builder()
                    .atividade(dto)
                    .subprocesso(SubprocessoSituacaoDto.builder().build())
                    .build();

            Mockito.when(atividadeFacade.criarAtividade(any(), any())).thenReturn(response);

            mockMvc.perform(post("/api/atividades")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"mapaCodigo\": 1, \"descricao\": \"Teste\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"));

            Mockito.verify(atividadeFacade).criarAtividade(any(), any());
        }

        @Test
        @DisplayName("Deve retornar 401 se sem autenticação")
        void deveRetornar401SeSemAutenticacao() throws Exception {
            // Sem @WithMockUser, o contexto de segurança está vazio
            // O controller deve retornar 401
            mockMvc.perform(post("/api/atividades")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"mapaCodigo\": 1, \"descricao\": \"Teste\"}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Deve atualizar atividade")
        @WithMockUser
        void deveAtualizarAtividade() throws Exception {
            AtividadeOperacaoResp response = AtividadeOperacaoResp.builder().build();
            Mockito.when(atividadeFacade.atualizarAtividade(eq(1L), any())).thenReturn(response);

            mockMvc.perform(post("/api/atividades/1/atualizar")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"mapaCodigo\": 1, \"descricao\": \"Teste\"}"))
                    .andExpect(status().isOk());
            
            Mockito.verify(atividadeFacade).atualizarAtividade(eq(1L), any());
        }

        @Test
        @DisplayName("Deve excluir atividade")
        @WithMockUser
        void deveExcluirAtividade() throws Exception {
            AtividadeOperacaoResp response = AtividadeOperacaoResp.builder().build();
            Mockito.when(atividadeFacade.excluirAtividade(1L)).thenReturn(response);

            mockMvc.perform(post("/api/atividades/1/excluir")
                            .with(csrf()))
                    .andExpect(status().isOk());

            Mockito.verify(atividadeFacade).excluirAtividade(1L);
        }
    }

    @Nested
    @DisplayName("Operações de Conhecimento")
    class OperacoesConhecimento {
        @Test
        @DisplayName("Deve listar conhecimentos")
        @WithMockUser
        void deveListarConhecimentos() throws Exception {
            Mockito.when(conhecimentoService.listarPorAtividade(1L)).thenReturn(List.of());
            mockMvc.perform(get("/api/atividades/1/conhecimentos"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve criar conhecimento")
        @WithMockUser
        void deveCriarConhecimento() throws Exception {
            AtividadeOperacaoResp response = AtividadeOperacaoResp.builder().build();
            ResultadoOperacaoConhecimento resultado = new ResultadoOperacaoConhecimento(999L, response);
            Mockito.when(atividadeFacade.criarConhecimento(eq(1L), any())).thenReturn(resultado);

            mockMvc.perform(post("/api/atividades/1/conhecimentos")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"atividadeCodigo\": 1, \"descricao\": \"C1\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/999")));

            Mockito.verify(atividadeFacade).criarConhecimento(eq(1L), any());
        }

        @Test
        @DisplayName("Deve atualizar conhecimento")
        @WithMockUser
        void deveAtualizarConhecimento() throws Exception {
            AtividadeOperacaoResp response = AtividadeOperacaoResp.builder().build();
            Mockito.when(atividadeFacade.atualizarConhecimento(eq(1L), eq(2L), any())).thenReturn(response);

            mockMvc.perform(post("/api/atividades/1/conhecimentos/2/atualizar")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"atividadeCodigo\": 1, \"descricao\": \"C1 Update\"}"))
                    .andExpect(status().isOk());

            Mockito.verify(atividadeFacade).atualizarConhecimento(eq(1L), eq(2L), any());
        }

        @Test
        @DisplayName("Deve excluir conhecimento")
        @WithMockUser
        void deveExcluirConhecimento() throws Exception {
            AtividadeOperacaoResp response = AtividadeOperacaoResp.builder().build();
            Mockito.when(atividadeFacade.excluirConhecimento(1L, 2L)).thenReturn(response);

            mockMvc.perform(post("/api/atividades/1/conhecimentos/2/excluir")
                            .with(csrf()))
                    .andExpect(status().isOk());

            Mockito.verify(atividadeFacade).excluirConhecimento(1L, 2L);
        }
    }
}
