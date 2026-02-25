package sgc.mapa;

import org.hamcrest.Matchers;
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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.RestExceptionHandler;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.dto.ResultadoOperacaoConhecimento;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.seguranca.SgcPermissionEvaluator;
import sgc.subprocesso.dto.AtividadeOperacaoResponse;
import sgc.subprocesso.dto.SubprocessoSituacaoDto;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AtividadeController.class)
@Import({ TestSecurityConfig.class, RestExceptionHandler.class })
@Tag("integration")
@DisplayName("Testes do Controlador de Atividades")
class AtividadeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SgcPermissionEvaluator permissionEvaluator;

    @MockitoBean
    private AtividadeFacade atividadeFacade;

    @Nested
    @DisplayName("Operações de Atividade")
    class OperacoesAtividade {
        @Test
        @DisplayName("Deve obter por ID")
        void deveObterPorId() throws Exception {
            Atividade response = Atividade.builder()
                    .codigo(1L)
                    .descricao("Atividade Teste")
                    .mapa(Mapa.builder().codigo(10L).build())
                    .conhecimentos(new LinkedHashSet<>())
                    .competencias(new HashSet<>())
                    .build();
            Mockito.when(atividadeFacade.obterAtividadePorId(1L)).thenReturn(response);

            mockMvc.perform(get("/api/atividades/1").with(user("123")))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.codigo").value(1));
        }

        @Test
        @DisplayName("Deve criar atividade")
        void deveCriarAtividade() throws Exception {
            AtividadeDto dto = AtividadeDto.builder()
                    .codigo(10L)
                    .build();

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
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
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
                    .andExpect(header().string("Location", Matchers.containsString("/999")));

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

    @Nested
    @DisplayName("Casos de Erro - Pattern 2")
    class CasosDeErro {
        @Test
        @DisplayName("Deve retornar NotFound ao obter atividade inexistente")
        void deveRetornarNotFoundAoObterAtividadeInexistente() throws Exception {
            // Pattern 2: Testing error branch
            Mockito.when(atividadeFacade.obterAtividadePorId(999L))
                    .thenThrow(new ErroEntidadeNaoEncontrada("Atividade", 999L));

            mockMvc.perform(get("/api/atividades/999")
                    .with(user("123")))
                    .andExpect(status().isNotFound());

            Mockito.verify(atividadeFacade).obterAtividadePorId(999L);
        }

        @Test
        @DisplayName("Deve retornar NotFound ao excluir atividade inexistente")
        void deveRetornarNotFoundAoExcluirAtividadeInexistente() throws Exception {
            // Pattern 2: Testing error branch
            Mockito.when(atividadeFacade.excluirAtividade(999L))
                    .thenThrow(new ErroEntidadeNaoEncontrada("Atividade", 999L));

            mockMvc.perform(post("/api/atividades/999/excluir")
                    .with(user("123").roles("CHEFE"))
                    .with(csrf()))
                    .andExpect(status().isNotFound());

            Mockito.verify(atividadeFacade).excluirAtividade(999L);
        }

        @Test
        @DisplayName("Deve retornar NotFound ao atualizar atividade inexistente")
        void deveRetornarNotFoundAoAtualizarAtividadeInexistente() throws Exception {
            // Pattern 2: Testing error branch
            Mockito.when(atividadeFacade.atualizarAtividade(eq(999L), any()))
                    .thenThrow(new ErroEntidadeNaoEncontrada("Atividade", 999L));

            mockMvc.perform(post("/api/atividades/999/atualizar")
                    .with(user("123").roles("CHEFE"))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"mapaCodigo\": 1, \"descricao\": \"Teste\"}"))
                    .andExpect(status().isNotFound());

            Mockito.verify(atividadeFacade).atualizarAtividade(eq(999L), any());
        }

        @Test
        @DisplayName("Deve retornar NotFound ao excluir conhecimento de atividade inexistente")
        void deveRetornarNotFoundAoExcluirConhecimentoDeAtividadeInexistente() throws Exception {
            // Pattern 2: Testing error branch
            Mockito.when(atividadeFacade.excluirConhecimento(999L, 2L))
                    .thenThrow(new ErroEntidadeNaoEncontrada("Atividade", 999L));

            mockMvc.perform(post("/api/atividades/999/conhecimentos/2/excluir")
                    .with(user("123").roles("CHEFE"))
                    .with(csrf()))
                    .andExpect(status().isNotFound());

            Mockito.verify(atividadeFacade).excluirConhecimento(999L, 2L);
        }
    }
}
