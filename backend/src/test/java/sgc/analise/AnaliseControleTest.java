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
    private static final String OBSERVACAO_1 = "Observação 1";
    private static final String OBSERVACAO_2 = "Observação 2";
    private static final String API_SUBPROCESSOS_1_ANALISES_CADASTRO = "/api/subprocessos/1/analises-cadastro";
    private static final String DEVE_RETORNAR_404_NOT_FOUND = "Deve retornar 404 Not Found quando subprocesso não encontrado";
    private static final String SUBPROCESSO_NAO_ENCONTRADO = "Subprocesso não encontrado";
    private static final String API_SUBPROCESSOS_99_ANALISES_CADASTRO = "/api/subprocessos/99/analises-cadastro";
    private static final String ERRO_INESPERADO = "Erro inesperado";
    private static final String MESSAGE_JSON_PATH = "$.message";
    private static final String OCORREU_UM_ERRO_INESPERADO = "Ocorreu um erro inesperado. Contate o suporte.";
    private static final String OBSERVACOES = "observacoes";
    private static final String NOVA_ANALISE_DE_CADASTRO = "Nova análise de cadastro";
    private static final String ANALISE_DE_CADASTRO = "Análise de cadastro";
    private static final String ANALISE_INVALIDA = "Análise inválida";
    private static final String API_SUBPROCESSOS_1_ANALISES_VALIDACAO = "/api/subprocessos/1/analises-validacao";
    private static final String API_SUBPROCESSOS_99_ANALISES_VALIDACAO = "/api/subprocessos/99/analises-validacao";
    private static final String NOVA_ANALISE_DE_VALIDACAO = "Nova análise de validação";
    private static final String ANALISE_DE_VALIDACAO = "Análise de validação";
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
            analise1.setObservacoes(OBSERVACAO_1);
            analise1.setDataHora(LocalDateTime.now());

            var analise2 = new AnaliseCadastro();
            analise2.setCodigo(2L);
            analise2.setObservacoes(OBSERVACAO_2);
            analise2.setDataHora(LocalDateTime.now());

            List<AnaliseCadastro> analises = Arrays.asList(analise1, analise2);

            when(analiseCadastroService.listarPorSubprocesso(1L)).thenReturn(analises);

            mockMvc.perform(get(API_SUBPROCESSOS_1_ANALISES_CADASTRO))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0].codigo").value(1L))
                    .andExpect(jsonPath("$[0].observacoes").value(OBSERVACAO_1))
                    .andExpect(jsonPath("$[1].codigo").value(2L))
                    .andExpect(jsonPath("$[1].observacoes").value(OBSERVACAO_2));

            verify(analiseCadastroService, times(1)).listarPorSubprocesso(1L);
        }

        @Test
        @DisplayName("Deve retornar lista vazia de análises de cadastro com status 200 OK")
        void deveRetornarListaVaziaDeAnalisesCadastro() throws Exception {
            when(analiseCadastroService.listarPorSubprocesso(1L)).thenReturn(Collections.emptyList());

            mockMvc.perform(get(API_SUBPROCESSOS_1_ANALISES_CADASTRO))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isEmpty());

            verify(analiseCadastroService, times(1)).listarPorSubprocesso(1L);
        }

        @Test
        @DisplayName(DEVE_RETORNAR_404_NOT_FOUND)
        void deveRetornarNotFoundParaSubprocessoInexistente() throws Exception {
            when(analiseCadastroService.listarPorSubprocesso(99L)).thenThrow(new ErroEntidadeNaoEncontrada(SUBPROCESSO_NAO_ENCONTRADO));

            mockMvc.perform(get(API_SUBPROCESSOS_99_ANALISES_CADASTRO))
                    .andExpect(status().isNotFound());

            verify(analiseCadastroService, times(1)).listarPorSubprocesso(99L);
        }

        @Test
        @DisplayName("Deve retornar 500 Internal Server Error para erro inesperado")
        void deveRetornarInternalServerErrorParaErroInesperado() throws Exception {
            when(analiseCadastroService.listarPorSubprocesso(99L)).thenThrow(new RuntimeException(ERRO_INESPERADO));

            mockMvc.perform(get(API_SUBPROCESSOS_99_ANALISES_CADASTRO))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath(MESSAGE_JSON_PATH).value(OCORREU_UM_ERRO_INESPERADO));

            verify(analiseCadastroService, times(1)).listarPorSubprocesso(99L);
        }
    }

    @Nested
    @DisplayName("Testes para criar análise de cadastro")
    class CriarAnaliseCadastro {

        @Test
        @DisplayName("Deve criar uma análise de cadastro e retornar 201 Created")
        void deveCriarAnaliseCadastro() throws Exception {
            var payload = Map.of(OBSERVACOES, NOVA_ANALISE_DE_CADASTRO);

            var analise = new AnaliseCadastro();
            analise.setCodigo(1L);
            analise.setObservacoes(NOVA_ANALISE_DE_CADASTRO);
            analise.setDataHora(LocalDateTime.now());

            when(analiseCadastroService.criarAnalise(1L, NOVA_ANALISE_DE_CADASTRO)).thenReturn(analise);

            mockMvc.perform(post(API_SUBPROCESSOS_1_ANALISES_CADASTRO).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.codigo").value(1L))
                    .andExpect(jsonPath("$.observacoes").value(NOVA_ANALISE_DE_CADASTRO));

            verify(analiseCadastroService, times(1)).criarAnalise(1L, NOVA_ANALISE_DE_CADASTRO);
        }

        @Test
        @DisplayName("Deve criar uma análise de cadastro com observações vazias e retornar 201 Created")
        void deveCriarAnaliseCadastroComObservacoesVazias() throws Exception {
            var payload = Map.of(OBSERVACOES, "");

            var analise = new AnaliseCadastro();
            analise.setCodigo(1L);

            when(analiseCadastroService.criarAnalise(1L, "")).thenReturn(analise);

            mockMvc.perform(post(API_SUBPROCESSOS_1_ANALISES_CADASTRO).with(csrf())
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

            mockMvc.perform(post(API_SUBPROCESSOS_1_ANALISES_CADASTRO).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isCreated());

            verify(analiseCadastroService, times(1)).criarAnalise(1L, "");
        }

        @Test
        @DisplayName(DEVE_RETORNAR_404_NOT_FOUND)
        void deveRetornarNotFoundParaSubprocessoInexistenteNaCriacao() throws Exception {
            var payload = Map.of(OBSERVACOES, ANALISE_DE_CADASTRO);

            when(analiseCadastroService.criarAnalise(99L, ANALISE_DE_CADASTRO)).thenThrow(new ErroEntidadeNaoEncontrada(SUBPROCESSO_NAO_ENCONTRADO));

            mockMvc.perform(post(API_SUBPROCESSOS_99_ANALISES_CADASTRO).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isNotFound());

            verify(analiseCadastroService, times(1)).criarAnalise(99L, ANALISE_DE_CADASTRO);
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request para parâmetro inválido")
        void deveRetornarBadRequestParaParametroInvalido() throws Exception {
            var payload = Map.of(OBSERVACOES, ANALISE_INVALIDA);

            when(analiseCadastroService.criarAnalise(1L, ANALISE_INVALIDA)).thenThrow(new IllegalArgumentException("Parâmetro inválido"));

            mockMvc.perform(post(API_SUBPROCESSOS_1_ANALISES_CADASTRO).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath(MESSAGE_JSON_PATH).value("A requisição contém um argumento inválido ou malformado."));

            verify(analiseCadastroService, times(1)).criarAnalise(1L, ANALISE_INVALIDA);
        }

        @Test
        @DisplayName("Deve retornar 500 Internal Server Error para erro inesperado na criação")
        void deveRetornarInternalServerErrorParaErroInesperadoNaCriacao() throws Exception {
            var payload = Map.of(OBSERVACOES, ANALISE_DE_CADASTRO);

            when(analiseCadastroService.criarAnalise(99L, ANALISE_DE_CADASTRO)).thenThrow(new RuntimeException(ERRO_INESPERADO));

            mockMvc.perform(post(API_SUBPROCESSOS_99_ANALISES_CADASTRO).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath(MESSAGE_JSON_PATH).value(OCORREU_UM_ERRO_INESPERADO));

            verify(analiseCadastroService, times(1)).criarAnalise(99L, ANALISE_DE_CADASTRO);
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
            analise1.setObservacoes(OBSERVACAO_1);
            analise1.setDataHora(LocalDateTime.now());

            var analise2 = new AnaliseValidacao();
            analise2.setCodigo(2L);
            analise2.setObservacoes(OBSERVACAO_2);
            analise2.setDataHora(LocalDateTime.now());

            List<AnaliseValidacao> analises = Arrays.asList(analise1, analise2);

            when(analiseValidacaoService.listarPorSubprocesso(1L)).thenReturn(analises);

            mockMvc.perform(get(API_SUBPROCESSOS_1_ANALISES_VALIDACAO))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0].codigo").value(1L))
                    .andExpect(jsonPath("$[0].observacoes").value(OBSERVACAO_1))
                    .andExpect(jsonPath("$[1].codigo").value(2L))
                    .andExpect(jsonPath("$[1].observacoes").value(OBSERVACAO_2));

            verify(analiseValidacaoService, times(1)).listarPorSubprocesso(1L);
        }

        @Test
        @DisplayName("Deve retornar lista vazia de análises de validação com status 200 OK")
        void deveRetornarListaVaziaDeAnalisesValidacao() throws Exception {
            when(analiseValidacaoService.listarPorSubprocesso(1L)).thenReturn(Collections.emptyList());

            mockMvc.perform(get(API_SUBPROCESSOS_1_ANALISES_VALIDACAO))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isEmpty());

            verify(analiseValidacaoService, times(1)).listarPorSubprocesso(1L);
        }

        @Test
        @DisplayName(DEVE_RETORNAR_404_NOT_FOUND)
        void deveRetornarNotFoundParaSubprocessoInexistenteValidacao() throws Exception {
            when(analiseValidacaoService.listarPorSubprocesso(99L)).thenThrow(new ErroEntidadeNaoEncontrada(SUBPROCESSO_NAO_ENCONTRADO));

            mockMvc.perform(get(API_SUBPROCESSOS_99_ANALISES_VALIDACAO))
                    .andExpect(status().isNotFound());

            verify(analiseValidacaoService, times(1)).listarPorSubprocesso(99L);
        }

        @Test
        @DisplayName("Deve retornar 500 Internal Server Error para erro inesperado")
        void deveRetornarInternalServerErrorParaErroValidacaoInesperado() throws Exception {
            when(analiseValidacaoService.listarPorSubprocesso(99L)).thenThrow(new RuntimeException(ERRO_INESPERADO));

            mockMvc.perform(get(API_SUBPROCESSOS_99_ANALISES_VALIDACAO))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath(MESSAGE_JSON_PATH).value(OCORREU_UM_ERRO_INESPERADO));

            verify(analiseValidacaoService, times(1)).listarPorSubprocesso(99L);
        }
    }

    @Nested
    @DisplayName("Testes para criar análise de validação")
    class CriarAnaliseValidacao {

        @Test
        @DisplayName("Deve criar uma análise de validação e retornar 201 Created")
        void deveCriarAnaliseValidacao() throws Exception {
            var payload = Map.of(OBSERVACOES, NOVA_ANALISE_DE_VALIDACAO);

            var analise = new AnaliseValidacao();
            analise.setCodigo(1L);
            analise.setObservacoes(NOVA_ANALISE_DE_VALIDACAO);
            analise.setDataHora(LocalDateTime.now());

            when(analiseValidacaoService.criarAnalise(1L, NOVA_ANALISE_DE_VALIDACAO)).thenReturn(analise);

            mockMvc.perform(post(API_SUBPROCESSOS_1_ANALISES_VALIDACAO).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.codigo").value(1L))
                    .andExpect(jsonPath("$.observacoes").value(NOVA_ANALISE_DE_VALIDACAO));

            verify(analiseValidacaoService, times(1)).criarAnalise(1L, NOVA_ANALISE_DE_VALIDACAO);
        }

        @Test
        @DisplayName("Deve criar uma análise de validação com observações vazias e retornar 201 Created")
        void deveCriarAnaliseValidacaoComObservacoesVazias() throws Exception {
            var payload = Map.of(OBSERVACOES, "");

            var analise = new AnaliseValidacao();
            analise.setCodigo(1L);

            when(analiseValidacaoService.criarAnalise(1L, "")).thenReturn(analise);

            mockMvc.perform(post(API_SUBPROCESSOS_1_ANALISES_VALIDACAO).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isCreated());

            verify(analiseValidacaoService, times(1)).criarAnalise(1L, "");
        }

        @Test
        @DisplayName(DEVE_RETORNAR_404_NOT_FOUND)
        void deveRetornarNotFoundParaSubprocessoInexistenteNaCriacaoValidacao() throws Exception {
            var payload = Map.of(OBSERVACOES, ANALISE_DE_VALIDACAO);

            when(analiseValidacaoService.criarAnalise(99L, ANALISE_DE_VALIDACAO)).thenThrow(new ErroEntidadeNaoEncontrada(SUBPROCESSO_NAO_ENCONTRADO));

            mockMvc.perform(post(API_SUBPROCESSOS_99_ANALISES_VALIDACAO).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isNotFound());

            verify(analiseValidacaoService, times(1)).criarAnalise(99L, ANALISE_DE_VALIDACAO);
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request para parâmetro inválido")
        void deveRetornarBadRequestParaParametroInvalidoValidacao() throws Exception {
            var payload = Map.of(OBSERVACOES, ANALISE_INVALIDA);

            when(analiseValidacaoService.criarAnalise(1L, ANALISE_INVALIDA)).thenThrow(new IllegalArgumentException("Parâmetro inválido"));

            mockMvc.perform(post(API_SUBPROCESSOS_1_ANALISES_VALIDACAO).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath(MESSAGE_JSON_PATH).value("A requisição contém um argumento inválido ou malformado."));

            verify(analiseValidacaoService, times(1)).criarAnalise(1L, ANALISE_INVALIDA);
        }

        @Test
        @DisplayName("Deve retornar 500 Internal Server Error para erro inesperado na criação")
        void deveRetornarInternalServerErrorParaErroValidacaoInesperadoNaCriacao() throws Exception {
            var payload = Map.of(OBSERVACOES, ANALISE_DE_VALIDACAO);

            when(analiseValidacaoService.criarAnalise(99L, ANALISE_DE_VALIDACAO)).thenThrow(new RuntimeException(ERRO_INESPERADO));

            mockMvc.perform(post(API_SUBPROCESSOS_99_ANALISES_VALIDACAO).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath(MESSAGE_JSON_PATH).value(OCORREU_UM_ERRO_INESPERADO));

            verify(analiseValidacaoService, times(1)).criarAnalise(99L, ANALISE_DE_VALIDACAO);
        }
    }
}