package sgc.analise;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.analise.dto.CriarAnaliseApiReq;
import sgc.analise.dto.CriarAnaliseReq;
import sgc.analise.model.Analise;
import sgc.analise.model.TipoAnalise;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.RestExceptionHandler;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnaliseController.class)
@Import(RestExceptionHandler.class)
@Tag("unit")
@DisplayName("Testes do AnaliseController")
class AnaliseControllerTest {
    private static final String OBSERVACAO_1 = "Observação 1";
    private static final String OBSERVACAO_2 = "Observação 2";
    private static final String API_SUBPROCESSOS_1_ANALISES_CADASTRO = "/api/subprocessos/1/analises-cadastro";
    private static final String DEVE_RETORNAR_404_NOT_FOUND = "Deve retornar 404 Not Found quando subprocesso não encontrado";
    private static final String SUBPROCESSO_NAO_ENCONTRADO = "Subprocesso não encontrado";
    private static final String API_SUBPROCESSOS_99_ANALISES_CADASTRO = "/api/subprocessos/99/analises-cadastro";
    private static final String ERRO_INESPERADO = "Erro inesperado";
    private static final String MESSAGE_JSON_PATH = "$.message";
    private static final String OCORREU_UM_ERRO_INESPERADO = "Erro inesperado";
    private static final String NOVA_ANALISE_DE_CADASTRO = "Nova análise de cadastro";
    private static final String ANALISE_DE_CADASTRO = "Análise de cadastro";
    private static final String ANALISE_INVALIDA = "Análise inválida";
    private static final String API_SUBPROCESSOS_1_ANALISES_VALIDACAO = "/api/subprocessos/1/analises-validacao";
    private static final String API_SUBPROCESSOS_99_ANALISES_VALIDACAO = "/api/subprocessos/99/analises-validacao";
    private static final String NOVA_ANALISE_DE_VALIDACAO = "Nova análise de validação";
    private static final String ANALISE_DE_VALIDACAO = "Análise de validação";

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private AnaliseService analiseService;

    @MockitoBean
    private SubprocessoFacade subprocessoFacade;

    private Subprocesso subprocesso;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        subprocesso = new Subprocesso();
        subprocesso.setCodigo(1L);
    }

    @Nested
    @DisplayName("Testes para listar análises de cadastro")
    class ListarAnalisesCadastro {
        @Test
        @DisplayName("Deve retornar lista de análises de cadastro com status 200 OK")
        @WithMockUser
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

            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(analiseService.listarPorSubprocesso(1L, TipoAnalise.CADASTRO))
                    .thenReturn(analises);

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
        @WithMockUser
        void deveRetornarListaVaziaDeAnalisesCadastro() throws Exception {
            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(analiseService.listarPorSubprocesso(1L, TipoAnalise.CADASTRO))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get(API_SUBPROCESSOS_1_ANALISES_CADASTRO))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isEmpty());

            verify(analiseService, times(1)).listarPorSubprocesso(1L, TipoAnalise.CADASTRO);
        }

        @Test
        @DisplayName(DEVE_RETORNAR_404_NOT_FOUND)
        @WithMockUser
        void deveRetornarNotFoundParaSubprocessoInexistente() throws Exception {
            // Agora o Controller valida existência usando SubprocessoService
            when(subprocessoFacade.buscarSubprocesso(99L))
                    .thenThrow(new ErroEntidadeNaoEncontrada(SUBPROCESSO_NAO_ENCONTRADO));

            mockMvc.perform(get(API_SUBPROCESSOS_99_ANALISES_CADASTRO))
                    .andExpect(status().isNotFound());

            verify(subprocessoFacade, times(1)).buscarSubprocesso(99L);
        }

        @Test
        @DisplayName("Deve retornar 500 Internal Server Error para erro inesperado")
        @WithMockUser
        void deveRetornarInternalServerErrorParaErroInesperado() throws Exception {
            when(subprocessoFacade.buscarSubprocesso(99L)).thenReturn(subprocesso);
            when(analiseService.listarPorSubprocesso(99L, TipoAnalise.CADASTRO))
                    .thenThrow(new RuntimeException(ERRO_INESPERADO));

            mockMvc.perform(get(API_SUBPROCESSOS_99_ANALISES_CADASTRO))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath(MESSAGE_JSON_PATH).value(OCORREU_UM_ERRO_INESPERADO));
        }
    }

    @Nested
    @DisplayName("Testes para criar análise de cadastro")
    class CriarAnaliseCadastro {

        @Test
        @DisplayName("Deve criar uma análise de cadastro e retornar 201 Created")
        @WithMockUser
        void deveCriarAnaliseCadastro() throws Exception {
            var requestDto =
                    CriarAnaliseApiReq.builder().observacoes(NOVA_ANALISE_DE_CADASTRO).build();

            var analise = new Analise();
            analise.setCodigo(1L);
            analise.setObservacoes(NOVA_ANALISE_DE_CADASTRO);
            analise.setDataHora(LocalDateTime.now());

            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(analiseService.criarAnalise(any(Subprocesso.class), any(CriarAnaliseReq.class))).thenReturn(analise);

            mockMvc.perform(
                            post(API_SUBPROCESSOS_1_ANALISES_CADASTRO)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.codigo").value(1L))
                    .andExpect(jsonPath("$.observacoes").value(NOVA_ANALISE_DE_CADASTRO));

            verify(analiseService, times(1)).criarAnalise(any(Subprocesso.class), any(CriarAnaliseReq.class));
        }

        @Test
        @DisplayName(
                "Deve criar uma análise de cadastro com observações vazias e retornar 201 Created")
        @WithMockUser
        void deveCriarAnaliseCadastroComObservacoesVazias() throws Exception {
            var requestDto = CriarAnaliseApiReq.builder().observacoes("").build();

            var analise = new Analise();
            analise.setCodigo(1L);
            analise.setObservacoes("");
            analise.setDataHora(LocalDateTime.now());

            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(analiseService.criarAnalise(any(Subprocesso.class), any(CriarAnaliseReq.class))).thenReturn(analise);

            mockMvc.perform(
                            post(API_SUBPROCESSOS_1_ANALISES_CADASTRO)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated());

            verify(analiseService, times(1)).criarAnalise(any(Subprocesso.class), any(CriarAnaliseReq.class));
        }

        @Test
        @DisplayName("Deve criar uma análise de cadastro sem payload e retornar 201 Created")
        @WithMockUser
        void deveCriarAnaliseCadastroSemPayload() throws Exception {
            var analise = new Analise();
            analise.setCodigo(1L);

            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(analiseService.criarAnalise(any(Subprocesso.class), any(CriarAnaliseReq.class))).thenReturn(analise);

            mockMvc.perform(
                            post(API_SUBPROCESSOS_1_ANALISES_CADASTRO)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{}"))
                    .andExpect(status().isCreated());

            verify(analiseService, times(1)).criarAnalise(any(Subprocesso.class), any(CriarAnaliseReq.class));
        }

        @Test
        @DisplayName(DEVE_RETORNAR_404_NOT_FOUND)
        @WithMockUser
        void deveRetornarNotFoundParaSubprocessoInexistenteNaCriacao() throws Exception {
            var requestDto = CriarAnaliseApiReq.builder().observacoes(ANALISE_DE_CADASTRO).build();

            // Validação agora ocorre no SubprocessoService
            when(subprocessoFacade.buscarSubprocesso(99L))
                    .thenThrow(new ErroEntidadeNaoEncontrada(SUBPROCESSO_NAO_ENCONTRADO));

            mockMvc.perform(
                            post(API_SUBPROCESSOS_99_ANALISES_CADASTRO)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request para parâmetro inválido")
        @WithMockUser
        void deveRetornarBadRequestParaParametroInvalido() throws Exception {
            var requestDto = CriarAnaliseApiReq.builder().observacoes(ANALISE_INVALIDA).build();

            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(analiseService.criarAnalise(any(Subprocesso.class), any(CriarAnaliseReq.class)))
                    .thenThrow(new IllegalArgumentException("Parâmetro inválido"));

            mockMvc.perform(
                            post(API_SUBPROCESSOS_1_ANALISES_CADASTRO)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(
                            jsonPath(MESSAGE_JSON_PATH)
                                    .value(
                                            "A requisição contém um argumento inválido ou"
                                                    + " malformado."));
        }

        @Test
        @DisplayName("Deve retornar 500 Internal Server Error para erro inesperado na criação")
        @WithMockUser
        void deveRetornarInternalServerErrorParaErroInesperadoNaCriacao() throws Exception {
            var requestDto = CriarAnaliseApiReq.builder().observacoes(ANALISE_DE_CADASTRO).build();

            when(subprocessoFacade.buscarSubprocesso(99L)).thenReturn(subprocesso);
            when(analiseService.criarAnalise(any(Subprocesso.class), any(CriarAnaliseReq.class)))
                    .thenThrow(new RuntimeException(ERRO_INESPERADO));

            mockMvc.perform(
                            post(API_SUBPROCESSOS_99_ANALISES_CADASTRO)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath(MESSAGE_JSON_PATH).value(OCORREU_UM_ERRO_INESPERADO));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request para observação muito longa")
        @WithMockUser
        void deveRetornarBadRequestParaObservacaoMuitoLonga() throws Exception {
            var requestDto = CriarAnaliseApiReq.builder()
                    .observacoes("a".repeat(501))
                    .build();

            mockMvc.perform(
                            post(API_SUBPROCESSOS_1_ANALISES_CADASTRO)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath(MESSAGE_JSON_PATH).value("A requisição contém dados de entrada inválidos."));

            verify(analiseService, never()).criarAnalise(any(Subprocesso.class), any(CriarAnaliseReq.class));
        }
    }

    @Nested
    @DisplayName("Testes para listar análises de validação")
    class ListarAnalisesValidacao {

        @Test
        @DisplayName("Deve retornar lista de análises de validação com status 200 OK")
        @WithMockUser
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

            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(analiseService.listarPorSubprocesso(1L, TipoAnalise.VALIDACAO))
                    .thenReturn(analises);

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
        @WithMockUser
        void deveRetornarListaVaziaDeAnalisesValidacao() throws Exception {
            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(analiseService.listarPorSubprocesso(1L, TipoAnalise.VALIDACAO))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get(API_SUBPROCESSOS_1_ANALISES_VALIDACAO))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isEmpty());

            verify(analiseService, times(1)).listarPorSubprocesso(1L, TipoAnalise.VALIDACAO);
        }

        @Test
        @DisplayName(DEVE_RETORNAR_404_NOT_FOUND)
        @WithMockUser
        void deveRetornarNotFoundParaSubprocessoInexistenteValidacao() throws Exception {
            when(subprocessoFacade.buscarSubprocesso(99L))
                    .thenThrow(new ErroEntidadeNaoEncontrada(SUBPROCESSO_NAO_ENCONTRADO));

            mockMvc.perform(get(API_SUBPROCESSOS_99_ANALISES_VALIDACAO))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve retornar 500 Internal Server Error para erro inesperado")
        @WithMockUser
        void deveRetornarInternalServerErrorParaErroValidacaoInesperado() throws Exception {
            when(subprocessoFacade.buscarSubprocesso(99L)).thenReturn(subprocesso);
            when(analiseService.listarPorSubprocesso(99L, TipoAnalise.VALIDACAO))
                    .thenThrow(new RuntimeException(ERRO_INESPERADO));

            mockMvc.perform(get(API_SUBPROCESSOS_99_ANALISES_VALIDACAO))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath(MESSAGE_JSON_PATH).value(OCORREU_UM_ERRO_INESPERADO));
        }
    }

    @Nested
    @DisplayName("Testes para criar análise de validação")
    class CriarAnaliseValidacao {

        @Test
        @DisplayName("Deve criar uma análise de validação e retornar 201 Created")
        @WithMockUser
        void deveCriarAnaliseValidacao() throws Exception {
            var requestDto =
                    CriarAnaliseApiReq.builder().observacoes(NOVA_ANALISE_DE_VALIDACAO).build();

            var analise = new Analise();
            analise.setCodigo(1L);
            analise.setObservacoes(NOVA_ANALISE_DE_VALIDACAO);
            analise.setDataHora(LocalDateTime.now());

            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(analiseService.criarAnalise(any(Subprocesso.class), any(CriarAnaliseReq.class))).thenReturn(analise);

            mockMvc.perform(
                            post(API_SUBPROCESSOS_1_ANALISES_VALIDACAO)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.codigo").value(1L))
                    .andExpect(jsonPath("$.observacoes").value(NOVA_ANALISE_DE_VALIDACAO));

            verify(analiseService, times(1)).criarAnalise(any(Subprocesso.class), any(CriarAnaliseReq.class));
        }

        @Test
        @DisplayName(
                "Deve criar uma análise de validação com observações vazias e retornar 201 Created")
        @WithMockUser
        void deveCriarAnaliseValidacaoComObservacoesVazias() throws Exception {
            var requestDto = CriarAnaliseApiReq.builder().observacoes("").build();

            var analise = new Analise();
            analise.setCodigo(1L);

            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(analiseService.criarAnalise(any(Subprocesso.class), any(CriarAnaliseReq.class))).thenReturn(analise);

            mockMvc.perform(
                            post(API_SUBPROCESSOS_1_ANALISES_VALIDACAO)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated());

            verify(analiseService, times(1)).criarAnalise(any(Subprocesso.class), any(CriarAnaliseReq.class));
        }

        @Test
        @DisplayName(DEVE_RETORNAR_404_NOT_FOUND)
        @WithMockUser
        void deveRetornarNotFoundParaSubprocessoInexistenteNaCriacaoValidacao() throws Exception {
            var requestDto =
                    CriarAnaliseApiReq.builder().observacoes(ANALISE_DE_VALIDACAO).build();

            when(subprocessoFacade.buscarSubprocesso(99L))
                    .thenThrow(new ErroEntidadeNaoEncontrada(SUBPROCESSO_NAO_ENCONTRADO));

            mockMvc.perform(
                            post(API_SUBPROCESSOS_99_ANALISES_VALIDACAO)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request para parâmetro inválido")
        @WithMockUser
        void deveRetornarBadRequestParaParametroInvalidoValidacao() throws Exception {
            var requestDto = CriarAnaliseApiReq.builder().observacoes(ANALISE_INVALIDA).build();

            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(analiseService.criarAnalise(any(Subprocesso.class), any(CriarAnaliseReq.class)))
                    .thenThrow(new IllegalArgumentException("Parâmetro inválido"));

            mockMvc.perform(
                            post(API_SUBPROCESSOS_1_ANALISES_VALIDACAO)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(
                            jsonPath(MESSAGE_JSON_PATH)
                                    .value(
                                            "A requisição contém um argumento inválido ou"
                                                    + " malformado."));
        }

        @Test
        @DisplayName("Deve retornar 500 Internal Server Error para erro inesperado na criação")
        @WithMockUser
        void deveRetornarInternalServerErrorParaErroValidacaoInesperadoNaCriacao()
                throws Exception {
            var requestDto =
                    CriarAnaliseApiReq.builder().observacoes(ANALISE_DE_VALIDACAO).build();

            when(subprocessoFacade.buscarSubprocesso(99L)).thenReturn(subprocesso);
            when(analiseService.criarAnalise(any(Subprocesso.class), any(CriarAnaliseReq.class)))
                    .thenThrow(new RuntimeException(ERRO_INESPERADO));

            mockMvc.perform(
                            post(API_SUBPROCESSOS_99_ANALISES_VALIDACAO)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath(MESSAGE_JSON_PATH).value(OCORREU_UM_ERRO_INESPERADO));
        }
    }
}
