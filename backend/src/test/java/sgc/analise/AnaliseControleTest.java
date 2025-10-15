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
import sgc.analise.dto.CriarAnaliseRequestDto;
import sgc.analise.modelo.Analise;
import sgc.analise.modelo.TipoAnalise;
import sgc.comum.erros.ErroDominioNaoEncontrado;

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
    private AnaliseService analiseService;

    @Nested
    @DisplayName("Testes para listar análises de cadastro")
    class ListarAnalisesCadastro {

        @Test
        @DisplayName("Deve retornar lista de análises de cadastro com status 200 OK")
        void deveRetornarListaDeAnalisesCadastro() throws Exception {
            var analise1 = new Analise();
            analise1.setCodigo(1L);
            analise1.setObservacoes(OBSERVACAO_1);
            analise1.setDataHora(LocalDateTime.now());

            var analise2 = new Analise();
            analise2.setCodigo(2L);
            analise2.setObservacoes(OBSERVACAO_2);
            analise2.setDataHora(LocalDateTime.now());

            List<Analise> analises = Arrays.asList(analise1, analise2);

            when(analiseService.listarPorSubprocesso(1L, TipoAnalise.CADASTRO)).thenReturn(analises);

            mockMvc.perform(get(API_SUBPROCESSOS_1_ANALISES_CADASTRO))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0].codigo").value(1L))
                    .andExpect(jsonPath("$[0].observacoes").value(OBSERVACAO_1))
                    .andExpect(jsonPath("$[1].codigo").value(2L))
                    .andExpect(jsonPath("$[1].observacoes").value(OBSERVACAO_2));

            verify(analiseService, times(1)).listarPorSubprocesso(1L, TipoAnalise.CADASTRO);
        }

        @Test
        @DisplayName("Deve retornar lista vazia de análises de cadastro com status 200 OK")
        void deveRetornarListaVaziaDeAnalisesCadastro() throws Exception {
            when(analiseService.listarPorSubprocesso(1L, TipoAnalise.CADASTRO)).thenReturn(Collections.emptyList());

            mockMvc.perform(get(API_SUBPROCESSOS_1_ANALISES_CADASTRO))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isEmpty());

            verify(analiseService, times(1)).listarPorSubprocesso(1L, TipoAnalise.CADASTRO);
        }

        @Test
        @DisplayName(DEVE_RETORNAR_404_NOT_FOUND)
        void deveRetornarNotFoundParaSubprocessoInexistente() throws Exception {
            when(analiseService.listarPorSubprocesso(99L, TipoAnalise.CADASTRO)).thenThrow(new ErroDominioNaoEncontrado(SUBPROCESSO_NAO_ENCONTRADO));

            mockMvc.perform(get(API_SUBPROCESSOS_99_ANALISES_CADASTRO))
                    .andExpect(status().isNotFound());

            verify(analiseService, times(1)).listarPorSubprocesso(99L, TipoAnalise.CADASTRO);
        }

        @Test
        @DisplayName("Deve retornar 500 Internal Server Error para erro inesperado")
        void deveRetornarInternalServerErrorParaErroInesperado() throws Exception {
            when(analiseService.listarPorSubprocesso(99L, TipoAnalise.CADASTRO)).thenThrow(new RuntimeException(ERRO_INESPERADO));

            mockMvc.perform(get(API_SUBPROCESSOS_99_ANALISES_CADASTRO))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath(MESSAGE_JSON_PATH).value(OCORREU_UM_ERRO_INESPERADO));

            verify(analiseService, times(1)).listarPorSubprocesso(99L, TipoAnalise.CADASTRO);
        }
    }

    @Nested
    @DisplayName("Testes para criar análise de cadastro")
    class CriarAnaliseCadastro {

        @Test
        @DisplayName("Deve criar uma análise de cadastro e retornar 201 Created")
        void deveCriarAnaliseCadastro() throws Exception {
            var payload = Map.of(OBSERVACOES, NOVA_ANALISE_DE_CADASTRO);

            var analise = new Analise();
            analise.setCodigo(1L);
            analise.setObservacoes(NOVA_ANALISE_DE_CADASTRO);
            analise.setDataHora(LocalDateTime.now());

            when(analiseService.criarAnalise(eq(CriarAnaliseRequestDto.builder()
            .subprocessoCodigo(1L)
            .observacoes(NOVA_ANALISE_DE_CADASTRO)
            .tipo(TipoAnalise.CADASTRO)
            .acao(null)
            .unidadeSigla(null)
            .analistaUsuarioTitulo(null)
            .motivo(null)
            .build()))).thenReturn(analise);

            mockMvc.perform(post(API_SUBPROCESSOS_1_ANALISES_CADASTRO).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.codigo").value(1L))
                    .andExpect(jsonPath("$.observacoes").value(NOVA_ANALISE_DE_CADASTRO));

            verify(analiseService, times(1)).criarAnalise(eq(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(1L)
                .observacoes(NOVA_ANALISE_DE_CADASTRO)
                .tipo(TipoAnalise.CADASTRO)
                .acao(null)
                .unidadeSigla(null)
                .analistaUsuarioTitulo(null)
                .motivo(null)
                .build()));
        }

        @Test
        @DisplayName("Deve criar uma análise de cadastro com observações vazias e retornar 201 Created")
        void deveCriarAnaliseCadastroComObservacoesVazias() throws Exception {
            var payload = Map.of(OBSERVACOES, "");

            var analise = new Analise();
            analise.setCodigo(1L);
            analise.setObservacoes("");
            analise.setDataHora(LocalDateTime.now());

            when(analiseService.criarAnalise(eq(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(1L)
                .observacoes("")
                .tipo(TipoAnalise.CADASTRO)
                .acao(null)
                .unidadeSigla(null)
                .analistaUsuarioTitulo(null)
                .motivo(null)
                .build()))).thenReturn(analise);

            mockMvc.perform(post(API_SUBPROCESSOS_1_ANALISES_CADASTRO).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isCreated());

            verify(analiseService, times(1)).criarAnalise(eq(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(1L)
                .observacoes("")
                .tipo(TipoAnalise.CADASTRO)
                .acao(null)
                .unidadeSigla(null)
                .analistaUsuarioTitulo(null)
                .motivo(null)
                .build()));
        }

        @Test
        @DisplayName("Deve criar uma análise de cadastro sem payload e retornar 201 Created")
        void deveCriarAnaliseCadastroSemPayload() throws Exception {
            var analise = new Analise();
            analise.setCodigo(1L);

            when(analiseService.criarAnalise(eq(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(1L)
                .observacoes("")
                .tipo(TipoAnalise.CADASTRO)
                .acao(null)
                .unidadeSigla(null)
                .analistaUsuarioTitulo(null)
                .motivo(null)
                .build()))).thenReturn(analise);

            mockMvc.perform(post(API_SUBPROCESSOS_1_ANALISES_CADASTRO).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isCreated());

            verify(analiseService, times(1)).criarAnalise(eq(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(1L)
                .observacoes("")
                .tipo(TipoAnalise.CADASTRO)
                .acao(null)
                .unidadeSigla(null)
                .analistaUsuarioTitulo(null)
                .motivo(null)
                .build()));
        }

        @Test
        @DisplayName(DEVE_RETORNAR_404_NOT_FOUND)
        void deveRetornarNotFoundParaSubprocessoInexistenteNaCriacao() throws Exception {
            var payload = Map.of(OBSERVACOES, ANALISE_DE_CADASTRO);

            when(analiseService.criarAnalise(eq(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(99L)
                .observacoes(ANALISE_DE_CADASTRO)
                .tipo(TipoAnalise.CADASTRO)
                .acao(null)
                .unidadeSigla(null)
                .analistaUsuarioTitulo(null)
                .motivo(null)
                .build()))).thenThrow(new ErroDominioNaoEncontrado(SUBPROCESSO_NAO_ENCONTRADO));

            mockMvc.perform(post(API_SUBPROCESSOS_99_ANALISES_CADASTRO).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)));
            verify(analiseService, times(1)).criarAnalise(eq(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(99L)
                .observacoes(ANALISE_DE_CADASTRO)
                .tipo(TipoAnalise.CADASTRO)
                .acao(null)
                .unidadeSigla(null)
                .analistaUsuarioTitulo(null)
                .motivo(null)
                .build()));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request para parâmetro inválido")
        void deveRetornarBadRequestParaParametroInvalido() throws Exception {
            var payload = Map.of(OBSERVACOES, ANALISE_INVALIDA);

            when(analiseService.criarAnalise(eq(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(1L)
                .observacoes(ANALISE_INVALIDA)
                .tipo(TipoAnalise.CADASTRO)
                .acao(null)
                .unidadeSigla(null)
                .analistaUsuarioTitulo(null)
                .motivo(null)
                .build()))).thenThrow(new IllegalArgumentException("Parâmetro inválido"));

            mockMvc.perform(post(API_SUBPROCESSOS_1_ANALISES_CADASTRO).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath(MESSAGE_JSON_PATH).value("A requisição contém um argumento inválido ou malformado."));

            verify(analiseService, times(1)).criarAnalise(eq(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(1L)
                .observacoes(ANALISE_INVALIDA)
                .tipo(TipoAnalise.CADASTRO)
                .acao(null)
                .unidadeSigla(null)
                .analistaUsuarioTitulo(null)
                .motivo(null)
                .build()));
        }

        @Test
        @DisplayName("Deve retornar 500 Internal Server Error para erro inesperado na criação")
        void deveRetornarInternalServerErrorParaErroInesperadoNaCriacao() throws Exception {
            var payload = Map.of(OBSERVACOES, ANALISE_DE_CADASTRO);

            when(analiseService.criarAnalise(eq(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(99L)
                .observacoes(ANALISE_DE_CADASTRO)
                .tipo(TipoAnalise.CADASTRO)
                .acao(null)
                .unidadeSigla(null)
                .analistaUsuarioTitulo(null)
                .motivo(null)
                .build()))).thenThrow(new RuntimeException(ERRO_INESPERADO));

            mockMvc.perform(post(API_SUBPROCESSOS_99_ANALISES_CADASTRO).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath(MESSAGE_JSON_PATH).value(OCORREU_UM_ERRO_INESPERADO));

            verify(analiseService, times(1)).criarAnalise(eq(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(99L)
                .observacoes(ANALISE_DE_CADASTRO)
                .tipo(TipoAnalise.CADASTRO)
                .acao(null)
                .unidadeSigla(null)
                .analistaUsuarioTitulo(null)
                .motivo(null)
                .build()));
        }
    }

    @Nested
    @DisplayName("Testes para listar análises de validação")
    class ListarAnalisesValidacao {

        @Test
        @DisplayName("Deve retornar lista de análises de validação com status 200 OK")
        void deveRetornarListaDeAnalisesValidacao() throws Exception {
            var analise1 = new Analise();
            analise1.setCodigo(1L);
            analise1.setObservacoes(OBSERVACAO_1);
            analise1.setDataHora(LocalDateTime.now());

            var analise2 = new Analise();
            analise2.setCodigo(2L);
            analise2.setObservacoes(OBSERVACAO_2);
            analise2.setDataHora(LocalDateTime.now());

            List<Analise> analises = Arrays.asList(analise1, analise2);

            when(analiseService.listarPorSubprocesso(1L, TipoAnalise.VALIDACAO)).thenReturn(analises);

            mockMvc.perform(get(API_SUBPROCESSOS_1_ANALISES_VALIDACAO))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0].codigo").value(1L))
                    .andExpect(jsonPath("$[0].observacoes").value(OBSERVACAO_1))
                    .andExpect(jsonPath("$[1].codigo").value(2L))
                    .andExpect(jsonPath("$[1].observacoes").value(OBSERVACAO_2));

            verify(analiseService, times(1)).listarPorSubprocesso(1L, TipoAnalise.VALIDACAO);
        }

        @Test
        @DisplayName("Deve retornar lista vazia de análises de validação com status 200 OK")
        void deveRetornarListaVaziaDeAnalisesValidacao() throws Exception {
            when(analiseService.listarPorSubprocesso(1L, TipoAnalise.VALIDACAO)).thenReturn(Collections.emptyList());

            mockMvc.perform(get(API_SUBPROCESSOS_1_ANALISES_VALIDACAO))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isEmpty());

            verify(analiseService, times(1)).listarPorSubprocesso(1L, TipoAnalise.VALIDACAO);
        }

        @Test
        @DisplayName(DEVE_RETORNAR_404_NOT_FOUND)
        void deveRetornarNotFoundParaSubprocessoInexistenteValidacao() throws Exception {
            when(analiseService.listarPorSubprocesso(99L, TipoAnalise.VALIDACAO)).thenThrow(new ErroDominioNaoEncontrado(SUBPROCESSO_NAO_ENCONTRADO));

            mockMvc.perform(get(API_SUBPROCESSOS_99_ANALISES_VALIDACAO))
                    .andExpect(status().isNotFound());

            verify(analiseService, times(1)).listarPorSubprocesso(99L, TipoAnalise.VALIDACAO);
        }

        @Test
        @DisplayName("Deve retornar 500 Internal Server Error para erro inesperado")
        void deveRetornarInternalServerErrorParaErroValidacaoInesperado() throws Exception {
            when(analiseService.listarPorSubprocesso(99L, TipoAnalise.VALIDACAO)).thenThrow(new RuntimeException(ERRO_INESPERADO));

            mockMvc.perform(get(API_SUBPROCESSOS_99_ANALISES_VALIDACAO))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath(MESSAGE_JSON_PATH).value(OCORREU_UM_ERRO_INESPERADO));

            verify(analiseService, times(1)).listarPorSubprocesso(99L, TipoAnalise.VALIDACAO);
        }
    }

    @Nested
    @DisplayName("Testes para criar análise de validação")
    class CriarAnaliseValidacao {

        @Test
        @DisplayName("Deve criar uma análise de validação e retornar 201 Created")
        void deveCriarAnaliseValidacao() throws Exception {
            var payload = Map.of(OBSERVACOES, NOVA_ANALISE_DE_VALIDACAO);

            var analise = new Analise();
            analise.setCodigo(1L);
            analise.setObservacoes(NOVA_ANALISE_DE_VALIDACAO);
            analise.setDataHora(LocalDateTime.now());

            when(analiseService.criarAnalise(eq(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(1L)
                .observacoes(NOVA_ANALISE_DE_VALIDACAO)
                .tipo(TipoAnalise.VALIDACAO)
                .acao(null)
                .unidadeSigla(null)
                .analistaUsuarioTitulo(null)
                .motivo(null)
                .build()))).thenReturn(analise);

            mockMvc.perform(post(API_SUBPROCESSOS_1_ANALISES_VALIDACAO).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.codigo").value(1L))
                    .andExpect(jsonPath("$.observacoes").value(NOVA_ANALISE_DE_VALIDACAO));

            verify(analiseService, times(1)).criarAnalise(eq(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(1L)
                .observacoes(NOVA_ANALISE_DE_VALIDACAO)
                .tipo(TipoAnalise.VALIDACAO)
                .acao(null)
                .unidadeSigla(null)
                .analistaUsuarioTitulo(null)
                .motivo(null)
                .build()));
        }

        @Test
        @DisplayName("Deve criar uma análise de validação com observações vazias e retornar 201 Created")
        void deveCriarAnaliseValidacaoComObservacoesVazias() throws Exception {
            var payload = Map.of(OBSERVACOES, "");

            var analise = new Analise();
            analise.setCodigo(1L);

            when(analiseService.criarAnalise(eq(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(1L)
                .observacoes("")
                .tipo(TipoAnalise.VALIDACAO)
                .acao(null)
                .unidadeSigla(null)
                .analistaUsuarioTitulo(null)
                .motivo(null)
                .build()))).thenReturn(analise);

            mockMvc.perform(post(API_SUBPROCESSOS_1_ANALISES_VALIDACAO).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isCreated());

            verify(analiseService, times(1)).criarAnalise(eq(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(1L)
                .observacoes("")
                .tipo(TipoAnalise.VALIDACAO)
                .acao(null)
                .unidadeSigla(null)
                .analistaUsuarioTitulo(null)
                .motivo(null)
                .build()));
        }

        @Test
        @DisplayName(DEVE_RETORNAR_404_NOT_FOUND)
        void deveRetornarNotFoundParaSubprocessoInexistenteNaCriacaoValidacao() throws Exception {
            var payload = Map.of(OBSERVACOES, ANALISE_DE_VALIDACAO);

            when(analiseService.criarAnalise(eq(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(99L)
                .observacoes(ANALISE_DE_VALIDACAO)
                .tipo(TipoAnalise.VALIDACAO)
                .acao(null)
                .unidadeSigla(null)
                .analistaUsuarioTitulo(null)
                .motivo(null)
                .build()))).thenThrow(new ErroDominioNaoEncontrado(SUBPROCESSO_NAO_ENCONTRADO));

            mockMvc.perform(post(API_SUBPROCESSOS_99_ANALISES_VALIDACAO).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isNotFound());

            verify(analiseService, times(1)).criarAnalise(eq(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(99L)
                .observacoes(ANALISE_DE_VALIDACAO)
                .tipo(TipoAnalise.VALIDACAO)
                .acao(null)
                .unidadeSigla(null)
                .analistaUsuarioTitulo(null)
                .motivo(null)
                .build()));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request para parâmetro inválido")
        void deveRetornarBadRequestParaParametroInvalidoValidacao() throws Exception {
            var payload = Map.of(OBSERVACOES, ANALISE_INVALIDA);

            when(analiseService.criarAnalise(eq(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(1L)
                .observacoes(ANALISE_INVALIDA)
                .tipo(TipoAnalise.VALIDACAO)
                .acao(null)
                .unidadeSigla(null)
                .analistaUsuarioTitulo(null)
                .motivo(null)
                .build()))).thenThrow(new IllegalArgumentException("Parâmetro inválido"));

            mockMvc.perform(post(API_SUBPROCESSOS_1_ANALISES_VALIDACAO).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath(MESSAGE_JSON_PATH).value("A requisição contém um argumento inválido ou malformado."));

            verify(analiseService, times(1)).criarAnalise(eq(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(1L)
                .observacoes(ANALISE_INVALIDA)
                .tipo(TipoAnalise.VALIDACAO)
                .acao(null)
                .unidadeSigla(null)
                .analistaUsuarioTitulo(null)
                .motivo(null)
                .build()));
        }

        @Test
        @DisplayName("Deve retornar 500 Internal Server Error para erro inesperado na criação")
        void deveRetornarInternalServerErrorParaErroValidacaoInesperadoNaCriacao() throws Exception {
            var payload = Map.of(OBSERVACOES, ANALISE_DE_VALIDACAO);

            when(analiseService.criarAnalise(eq(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(99L)
                .observacoes(ANALISE_DE_VALIDACAO)
                .tipo(TipoAnalise.VALIDACAO)
                .acao(null)
                .unidadeSigla(null)
                .analistaUsuarioTitulo(null)
                .motivo(null)
                .build()))).thenThrow(new RuntimeException(ERRO_INESPERADO));

            mockMvc.perform(post(API_SUBPROCESSOS_99_ANALISES_VALIDACAO).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath(MESSAGE_JSON_PATH).value(OCORREU_UM_ERRO_INESPERADO));

            verify(analiseService, times(1)).criarAnalise(eq(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(99L)
                .observacoes(ANALISE_DE_VALIDACAO)
                .tipo(TipoAnalise.VALIDACAO)
                .acao(null)
                .unidadeSigla(null)
                .analistaUsuarioTitulo(null)
                .motivo(null)
                .build()));
        }
    }
}