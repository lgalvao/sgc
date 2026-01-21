package sgc.mapa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.comum.erros.RestExceptionHandler;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.mapa.dto.AtividadeResponse;
import sgc.mapa.dto.ResultadoOperacaoConhecimento;
import sgc.mapa.service.AtividadeFacade;
import sgc.subprocesso.dto.AtividadeOperacaoResponse;
import sgc.subprocesso.dto.AtividadeVisualizacaoDto;
import sgc.subprocesso.dto.SubprocessoSituacaoDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AtividadeController.class)
@Import({TestSecurityConfig.class, RestExceptionHandler.class})
@Tag("integration")
@DisplayName("Testes do Controlador de Atividades")
class AtividadeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AtividadeFacade atividadeFacade;

    @Nested
    @DisplayName("Operações de Atividade")
    class OperacoesAtividade {
        @Test
        @DisplayName("Deve obter por ID")
        void deveObterPorId() throws Exception {
            AtividadeResponse response = AtividadeResponse.builder()
                    .codigo(1L)
                    .build();
            Mockito.when(atividadeFacade.obterAtividadePorId(1L)).thenReturn(response);

            mockMvc.perform(get("/api/atividades/1").with(user("123")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.codigo").value(1));
        }

        @Test
        @DisplayName("Deve criar atividade")
        void deveCriarAtividade() throws Exception {
            AtividadeVisualizacaoDto dto = new AtividadeVisualizacaoDto();
            dto.setCodigo(10L);

            AtividadeOperacaoResponse response = AtividadeOperacaoResponse.builder()
                    .atividade(dto)
                    .subprocesso(SubprocessoSituacaoDto.builder().build())
                    .build();

            Mockito.when(atividadeFacade.criarAtividade(any())).thenReturn(response);

            mockMvc.perform(post("/api/atividades")
                            .with(user("123").roles("CHEFE"))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"mapaCodigo\": 1, \"descricao\": \"Teste\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"));

            Mockito.verify(atividadeFacade).criarAtividade(any());
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
        @DisplayName("Deve retornar 403 se usuário não tem role adequada")
        void deveRetornar403SeUsuarioSemRole() throws Exception {
            mockMvc.perform(post("/api/atividades")
                            .with(user("123").roles("SERVIDOR"))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"mapaCodigo\": 1, \"descricao\": \"Teste\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Deve atualizar atividade")
        void deveAtualizarAtividade() throws Exception {
            AtividadeOperacaoResponse response = AtividadeOperacaoResponse.builder().build();
            Mockito.when(atividadeFacade.atualizarAtividade(eq(1L), any())).thenReturn(response);

            mockMvc.perform(post("/api/atividades/1/atualizar")
                            .with(user("123").roles("CHEFE"))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"mapaCodigo\": 1, \"descricao\": \"Teste\"}"))
                    .andExpect(status().isOk());
            
            Mockito.verify(atividadeFacade).atualizarAtividade(eq(1L), any());
        }

        @Test
        @DisplayName("Deve excluir atividade")
        void deveExcluirAtividade() throws Exception {
            AtividadeOperacaoResponse response = AtividadeOperacaoResponse.builder().build();
            Mockito.when(atividadeFacade.excluirAtividade(1L)).thenReturn(response);

            mockMvc.perform(post("/api/atividades/1/excluir")
                            .with(user("123").roles("CHEFE"))
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
        void deveListarConhecimentos() throws Exception {
            Mockito.when(atividadeFacade.listarConhecimentosPorAtividade(1L)).thenReturn(List.of());
            mockMvc.perform(get("/api/atividades/1/conhecimentos").with(user("123")))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve criar conhecimento")
        void deveCriarConhecimento() throws Exception {
            AtividadeOperacaoResponse response = AtividadeOperacaoResponse.builder().build();
            ResultadoOperacaoConhecimento resultado = new ResultadoOperacaoConhecimento(999L, response);
            Mockito.when(atividadeFacade.criarConhecimento(eq(1L), any())).thenReturn(resultado);

            mockMvc.perform(post("/api/atividades/1/conhecimentos")
                            .with(user("123").roles("CHEFE"))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"atividadeCodigo\": 1, \"descricao\": \"C1\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/999")));

            Mockito.verify(atividadeFacade).criarConhecimento(eq(1L), any());
        }

        @Test
        @DisplayName("Deve atualizar conhecimento")
        void deveAtualizarConhecimento() throws Exception {
            AtividadeOperacaoResponse response = AtividadeOperacaoResponse.builder().build();
            Mockito.when(atividadeFacade.atualizarConhecimento(eq(1L), eq(2L), any())).thenReturn(response);

            mockMvc.perform(post("/api/atividades/1/conhecimentos/2/atualizar")
                            .with(user("123").roles("CHEFE"))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"atividadeCodigo\": 1, \"descricao\": \"C1 Update\"}"))
                    .andExpect(status().isOk());

            Mockito.verify(atividadeFacade).atualizarConhecimento(eq(1L), eq(2L), any());
        }

        @Test
        @DisplayName("Deve excluir conhecimento")
        void deveExcluirConhecimento() throws Exception {
            AtividadeOperacaoResponse response = AtividadeOperacaoResponse.builder().build();
            Mockito.when(atividadeFacade.excluirConhecimento(1L, 2L)).thenReturn(response);

            mockMvc.perform(post("/api/atividades/1/conhecimentos/2/excluir")
                            .with(user("123").roles("CHEFE"))
                            .with(csrf()))
                    .andExpect(status().isOk());

            Mockito.verify(atividadeFacade).excluirConhecimento(1L, 2L);
        }
    }
}
