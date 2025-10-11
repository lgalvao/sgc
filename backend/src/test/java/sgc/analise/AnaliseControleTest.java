package sgc.analise;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.analise.modelo.AnaliseCadastro;
import sgc.analise.modelo.AnaliseValidacao;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnaliseControle.class)
@DisplayName("Testes do AnaliseControle")
@WithMockUser
class AnaliseControleTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AnaliseCadastroService analiseCadastroService;

    @MockitoBean
    private AnaliseValidacaoService analiseValidacaoService;

    @Nested
    @DisplayName("Testes para listar análises de cadastro")
    class ListarAnalisesCadastro {

        @Test
        @DisplayName("Deve retornar lista de análises de cadastro com status 200 OK")
        void deveRetornarListaDeAnalisesCadastro() throws Exception {
            var analise1 = new AnaliseCadastro();
            analise1.setCodigo(1L);
            analise1.setObservacoes("Observação 1");
            analise1.setDataHora(LocalDateTime.now());

            var analise2 = new AnaliseCadastro();
            analise2.setCodigo(2L);
            analise2.setObservacoes("Observação 2");
            analise2.setDataHora(LocalDateTime.now());

            List<AnaliseCadastro> analises = Arrays.asList(analise1, analise2);

            when(analiseCadastroService.listarPorSubprocesso(1L)).thenReturn(analises);

            mockMvc.perform(get("/api/subprocessos/1/analises-cadastro"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0].codigo").value(1L))
                    .andExpect(jsonPath("$[0].observacoes").value("Observação 1"))
                    .andExpect(jsonPath("$[1].codigo").value(2L))
                    .andExpect(jsonPath("$[1].observacoes").value("Observação 2"));

            verify(analiseCadastroService, times(1)).listarPorSubprocesso(1L);
        }

        @Test
        @DisplayName("Deve retornar lista vazia de análises de cadastro com status 200 OK")
        void deveRetornarListaVaziaDeAnalisesCadastro() throws Exception {
            when(analiseCadastroService.listarPorSubprocesso(1L)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/subprocessos/1/analises-cadastro"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isEmpty());

            verify(analiseCadastroService, times(1)).listarPorSubprocesso(1L);
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found quando subprocesso não encontrado")
        void deveRetornarNotFoundParaSubprocessoInexistente() throws Exception {
            when(analiseCadastroService.listarPorSubprocesso(99L)).thenThrow(new ErroEntidadeNaoEncontrada("Subprocesso não encontrado"));

            mockMvc.perform(get("/api/subprocessos/99/analises-cadastro"))
                    .andExpect(status().isNotFound());

            verify(analiseCadastroService, times(1)).listarPorSubprocesso(99L);
        }

        @Test
        @DisplayName("Deve retornar 500 Internal Server Error para erro inesperado")
        void deveRetornarInternalServerErrorParaErroInesperado() throws Exception {
            when(analiseCadastroService.listarPorSubprocesso(99L)).thenThrow(new RuntimeException("Erro inesperado"));

            mockMvc.perform(get("/api/subprocessos/99/analises-cadastro"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Ocorreu um erro inesperado. Contate o suporte."));

            verify(analiseCadastroService, times(1)).listarPorSubprocesso(99L);
        }
    }

    @Nested
    @DisplayName("Testes para criar análise de cadastro")
    class CriarAnaliseCadastro {

        @Test
        @DisplayName("Deve criar uma análise de cadastro e retornar 201 Created")
        void deveCriarAnaliseCadastro() throws Exception {
            var payload = Map.of("observacoes", "Nova análise de cadastro");

            var analise = new AnaliseCadastro();
            analise.setCodigo(1L);
            analise.setObservacoes("Nova análise de cadastro");
            analise.setDataHora(LocalDateTime.now());

            when(analiseCadastroService.criarAnalise(1L, "Nova análise de cadastro")).thenReturn(analise);

            mockMvc.perform(post("/api/subprocessos/1/analises-cadastro").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.codigo").value(1L))
                    .andExpect(jsonPath("$.observacoes").value("Nova análise de cadastro"));

            verify(analiseCadastroService, times(1)).criarAnalise(1L, "Nova análise de cadastro");
        }

        @Test
        @DisplayName("Deve criar uma análise de cadastro com observações vazias e retornar 201 Created")
        void deveCriarAnaliseCadastroComObservacoesVazias() throws Exception {
            var payload = Map.of("observacoes", "");

            var analise = new AnaliseCadastro();
            analise.setCodigo(1L);

            when(analiseCadastroService.criarAnalise(1L, "")).thenReturn(analise);

            mockMvc.perform(post("/api/subprocessos/1/analises-cadastro").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isCreated());

            verify(analiseCadastroService, times(1)).criarAnalise(1L, "");
        }

        @Test
        @DisplayName("Deve criar uma análise de cadastro sem payload e retornar 201 Created")
        void deveCriarAnaliseCadastroSemPayload() throws Exception {
            var analise = new AnaliseCadastro();
            analise.setCodigo(1L);

            when(analiseCadastroService.criarAnalise(1L, "")).thenReturn(analise);

            mockMvc.perform(post("/api/subprocessos/1/analises-cadastro").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isCreated());

            verify(analiseCadastroService, times(1)).criarAnalise(1L, "");
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found quando subprocesso não encontrado")
        void deveRetornarNotFoundParaSubprocessoInexistenteNaCriacao() throws Exception {
            var payload = Map.of("observacoes", "Análise de cadastro");

            when(analiseCadastroService.criarAnalise(99L, "Análise de cadastro")).thenThrow(new ErroEntidadeNaoEncontrada("Subprocesso não encontrado"));

            mockMvc.perform(post("/api/subprocessos/99/analises-cadastro").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isNotFound());

            verify(analiseCadastroService, times(1)).criarAnalise(99L, "Análise de cadastro");
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request para parâmetro inválido")
        void deveRetornarBadRequestParaParametroInvalido() throws Exception {
            var payload = Map.of("observacoes", "Análise inválida");

            when(analiseCadastroService.criarAnalise(1L, "Análise inválida")).thenThrow(new IllegalArgumentException("Parâmetro inválido"));

            mockMvc.perform(post("/api/subprocessos/1/analises-cadastro").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Parâmetro inválido"));

            verify(analiseCadastroService, times(1)).criarAnalise(1L, "Análise inválida");
        }

        @Test
        @DisplayName("Deve retornar 500 Internal Server Error para erro inesperado na criação")
        void deveRetornarInternalServerErrorParaErroInesperadoNaCriacao() throws Exception {
            var payload = Map.of("observacoes", "Análise de cadastro");

            when(analiseCadastroService.criarAnalise(99L, "Análise de cadastro")).thenThrow(new RuntimeException("Erro inesperado"));

            mockMvc.perform(post("/api/subprocessos/99/analises-cadastro").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.message").value("Ocorreu um erro inesperado. Contate o suporte."));

            verify(analiseCadastroService, times(1)).criarAnalise(99L, "Análise de cadastro");
        }
    }

    @Nested
    @DisplayName("Testes para listar análises de validação")
    class ListarAnalisesValidacao {

        @Test
        @DisplayName("Deve retornar lista de análises de validação com status 200 OK")
        void deveRetornarListaDeAnalisesValidacao() throws Exception {
            var analise1 = new AnaliseValidacao();
            analise1.setCodigo(1L);
            analise1.setObservacoes("Observação 1");
            analise1.setDataHora(LocalDateTime.now());

            var analise2 = new AnaliseValidacao();
            analise2.setCodigo(2L);
            analise2.setObservacoes("Observação 2");
            analise2.setDataHora(LocalDateTime.now());

            List<AnaliseValidacao> analises = Arrays.asList(analise1, analise2);

            when(analiseValidacaoService.listarPorSubprocesso(1L)).thenReturn(analises);

            mockMvc.perform(get("/api/subprocessos/1/analises-validacao"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0].codigo").value(1L))
                    .andExpect(jsonPath("$[0].observacoes").value("Observação 1"))
                    .andExpect(jsonPath("$[1].codigo").value(2L))
                    .andExpect(jsonPath("$[1].observacoes").value("Observação 2"));

            verify(analiseValidacaoService, times(1)).listarPorSubprocesso(1L);
        }

        @Test
        @DisplayName("Deve retornar lista vazia de análises de validação com status 200 OK")
        void deveRetornarListaVaziaDeAnalisesValidacao() throws Exception {
            when(analiseValidacaoService.listarPorSubprocesso(1L)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/subprocessos/1/analises-validacao"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isEmpty());

            verify(analiseValidacaoService, times(1)).listarPorSubprocesso(1L);
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found quando subprocesso não encontrado")
        void deveRetornarNotFoundParaSubprocessoInexistenteValidacao() throws Exception {
            when(analiseValidacaoService.listarPorSubprocesso(99L)).thenThrow(new ErroEntidadeNaoEncontrada("Subprocesso não encontrado"));

            mockMvc.perform(get("/api/subprocessos/99/analises-validacao"))
                    .andExpect(status().isNotFound());

            verify(analiseValidacaoService, times(1)).listarPorSubprocesso(99L);
        }

        @Test
        @DisplayName("Deve retornar 500 Internal Server Error para erro inesperado")
        void deveRetornarInternalServerErrorParaErroValidacaoInesperado() throws Exception {
            when(analiseValidacaoService.listarPorSubprocesso(99L)).thenThrow(new RuntimeException("Erro inesperado"));

            mockMvc.perform(get("/api/subprocessos/99/analises-validacao"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Ocorreu um erro inesperado. Contate o suporte."));

            verify(analiseValidacaoService, times(1)).listarPorSubprocesso(99L);
        }
    }

    @Nested
    @DisplayName("Testes para criar análise de validação")
    class CriarAnaliseValidacao {

        @Test
        @DisplayName("Deve criar uma análise de validação e retornar 201 Created")
        void deveCriarAnaliseValidacao() throws Exception {
            var payload = Map.of("observacoes", "Nova análise de validação");

            var analise = new AnaliseValidacao();
            analise.setCodigo(1L);
            analise.setObservacoes("Nova análise de validação");
            analise.setDataHora(LocalDateTime.now());

            when(analiseValidacaoService.criarAnalise(1L, "Nova análise de validação")).thenReturn(analise);

            mockMvc.perform(post("/api/subprocessos/1/analises-validacao").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.codigo").value(1L))
                    .andExpect(jsonPath("$.observacoes").value("Nova análise de validação"));

            verify(analiseValidacaoService, times(1)).criarAnalise(1L, "Nova análise de validação");
        }

        @Test
        @DisplayName("Deve criar uma análise de validação com observações vazias e retornar 201 Created")
        void deveCriarAnaliseValidacaoComObservacoesVazias() throws Exception {
            var payload = Map.of("observacoes", "");

            var analise = new AnaliseValidacao();
            analise.setCodigo(1L);

            when(analiseValidacaoService.criarAnalise(1L, "")).thenReturn(analise);

            mockMvc.perform(post("/api/subprocessos/1/analises-validacao").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isCreated());

            verify(analiseValidacaoService, times(1)).criarAnalise(1L, "");
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found quando subprocesso não encontrado")
        void deveRetornarNotFoundParaSubprocessoInexistenteNaCriacaoValidacao() throws Exception {
            var payload = Map.of("observacoes", "Análise de validação");

            when(analiseValidacaoService.criarAnalise(99L, "Análise de validação")).thenThrow(new ErroEntidadeNaoEncontrada("Subprocesso não encontrado"));

            mockMvc.perform(post("/api/subprocessos/99/analises-validacao").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isNotFound());

            verify(analiseValidacaoService, times(1)).criarAnalise(99L, "Análise de validação");
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request para parâmetro inválido")
        void deveRetornarBadRequestParaParametroInvalidoValidacao() throws Exception {
            var payload = Map.of("observacoes", "Análise inválida");

            when(analiseValidacaoService.criarAnalise(1L, "Análise inválida")).thenThrow(new IllegalArgumentException("Parâmetro inválido"));

            mockMvc.perform(post("/api/subprocessos/1/analises-validacao").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Parâmetro inválido"));

            verify(analiseValidacaoService, times(1)).criarAnalise(1L, "Análise inválida");
        }

        @Test
        @DisplayName("Deve retornar 500 Internal Server Error para erro inesperado na criação")
        void deveRetornarInternalServerErrorParaErroValidacaoInesperadoNaCriacao() throws Exception {
            var payload = Map.of("observacoes", "Análise de validação");

            when(analiseValidacaoService.criarAnalise(99L, "Análise de validação")).thenThrow(new RuntimeException("Erro inesperado"));

            mockMvc.perform(post("/api/subprocessos/99/analises-validacao").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.message").value("Ocorreu um erro inesperado. Contate o suporte."));

            verify(analiseValidacaoService, times(1)).criarAnalise(99L, "Análise de validação");
        }
    }
}