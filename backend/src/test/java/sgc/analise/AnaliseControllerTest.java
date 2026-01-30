package sgc.analise;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.analise.dto.AnaliseHistoricoDto;
import sgc.analise.dto.CriarAnaliseRequest;
import sgc.analise.mapper.AnaliseMapper;
import sgc.analise.model.Analise;
import sgc.analise.model.TipoAnalise;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.RestExceptionHandler;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Collections;
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

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AnaliseFacade analiseFacade;

    @MockitoBean
    private SubprocessoFacade subprocessoFacade;

    @MockitoBean
    private AnaliseMapper analiseMapper;

    private Subprocesso subprocesso;

    @BeforeEach
    void setup() {
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

            var analise2 = new Analise();
            analise2.setCodigo(2L);
            analise2.setObservacoes(OBSERVACAO_2);

            var dto1 = AnaliseHistoricoDto.builder().observacoes(OBSERVACAO_1).build();
            var dto2 = AnaliseHistoricoDto.builder().observacoes(OBSERVACAO_2).build();

            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(analiseFacade.listarPorSubprocesso(1L, TipoAnalise.CADASTRO))
                    .thenReturn(Arrays.asList(analise1, analise2));
            when(analiseMapper.toAnaliseHistoricoDto(analise1)).thenReturn(dto1);
            when(analiseMapper.toAnaliseHistoricoDto(analise2)).thenReturn(dto2);

            mockMvc.perform(get(API_SUBPROCESSOS_1_ANALISES_CADASTRO))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0].observacoes").value(OBSERVACAO_1))
                    .andExpect(jsonPath("$[1].observacoes").value(OBSERVACAO_2));
        }

        @Test
        @DisplayName("Deve retornar lista vazia de análises de cadastro com status 200 OK")
        @WithMockUser
        void deveRetornarListaVaziaDeAnalisesCadastro() throws Exception {
            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(analiseFacade.listarPorSubprocesso(1L, TipoAnalise.CADASTRO))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get(API_SUBPROCESSOS_1_ANALISES_CADASTRO))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName(DEVE_RETORNAR_404_NOT_FOUND)
        @WithMockUser
        void deveRetornarNotFoundParaSubprocessoInexistente() throws Exception {
            when(subprocessoFacade.buscarSubprocesso(99L))
                    .thenThrow(new ErroEntidadeNaoEncontrada(SUBPROCESSO_NAO_ENCONTRADO));

            mockMvc.perform(get(API_SUBPROCESSOS_99_ANALISES_CADASTRO))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve retornar 500 Internal Server Error para erro inesperado")
        @WithMockUser
        void deveRetornarInternalServerErrorParaErroInesperado() throws Exception {
            when(subprocessoFacade.buscarSubprocesso(99L)).thenReturn(subprocesso);
            when(analiseFacade.listarPorSubprocesso(99L, TipoAnalise.CADASTRO))
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
        @WithMockUser(roles = "GESTOR")
        void deveCriarAnaliseCadastro() throws Exception {
            var request = new CriarAnaliseRequest(NOVA_ANALISE_DE_CADASTRO, "SIGLA", "TITULO", "MOTIVO");
            var analise = new Analise();
            var dto = AnaliseHistoricoDto.builder().observacoes(NOVA_ANALISE_DE_CADASTRO).build();

            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(analiseFacade.criarAnalise(any(), any())).thenReturn(analise);
            when(analiseMapper.toAnaliseHistoricoDto(analise)).thenReturn(dto);

            mockMvc.perform(
                            post(API_SUBPROCESSOS_1_ANALISES_CADASTRO)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.observacoes").value(NOVA_ANALISE_DE_CADASTRO));
        }

        @Test
        @DisplayName("Deve criar uma análise de cadastro com observações vazias e retornar 201 Created")
        @WithMockUser(roles = "GESTOR")
        void deveCriarAnaliseCadastroComObservacoesVazias() throws Exception {
            var request = new CriarAnaliseRequest("", "SIGLA", "TITULO", "MOTIVO");
            var analise = new Analise();
            var dto = AnaliseHistoricoDto.builder().observacoes("").build();

            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(analiseFacade.criarAnalise(any(), any())).thenReturn(analise);
            when(analiseMapper.toAnaliseHistoricoDto(analise)).thenReturn(dto);

            mockMvc.perform(
                            post(API_SUBPROCESSOS_1_ANALISES_CADASTRO)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Deve criar uma análise de cadastro sem payload e retornar 201 Created")
        @WithMockUser(roles = "ADMIN")
        void deveCriarAnaliseCadastroSemPayload() throws Exception {
            // Need a valid request object for required fields if any, or just empty JSON if allowed
            var analise = new Analise();
            var dto = AnaliseHistoricoDto.builder().build();

            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(analiseFacade.criarAnalise(any(), any())).thenReturn(analise);
            when(analiseMapper.toAnaliseHistoricoDto(analise)).thenReturn(dto);

            mockMvc.perform(
                            post(API_SUBPROCESSOS_1_ANALISES_CADASTRO)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{}"))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName(DEVE_RETORNAR_404_NOT_FOUND)
        @WithMockUser(roles = "ADMIN")
        void deveRetornarNotFoundParaSubprocessoInexistenteNaCriacao() throws Exception {
            var request = new CriarAnaliseRequest(ANALISE_DE_CADASTRO, "S", "T", "M");

            when(subprocessoFacade.buscarSubprocesso(99L))
                    .thenThrow(new ErroEntidadeNaoEncontrada(SUBPROCESSO_NAO_ENCONTRADO));

            mockMvc.perform(
                            post(API_SUBPROCESSOS_99_ANALISES_CADASTRO)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request para parâmetro inválido")
        @WithMockUser(roles = "GESTOR")
        void deveRetornarBadRequestParaParametroInvalido() throws Exception {
            var request = new CriarAnaliseRequest(ANALISE_INVALIDA, "S", "T", "M");

            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(analiseFacade.criarAnalise(any(), any()))
                    .thenThrow(new IllegalArgumentException("Parâmetro inválido"));

            mockMvc.perform(
                            post(API_SUBPROCESSOS_1_ANALISES_CADASTRO)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar 500 Internal Server Error para erro inesperado na criação")
        @WithMockUser(roles = "ADMIN")
        void deveRetornarInternalServerErrorParaErroInesperadoNaCriacao() throws Exception {
            var request = new CriarAnaliseRequest(ANALISE_DE_CADASTRO, "S", "T", "M");

            when(subprocessoFacade.buscarSubprocesso(99L)).thenReturn(subprocesso);
            when(analiseFacade.criarAnalise(any(), any()))
                    .thenThrow(new RuntimeException(ERRO_INESPERADO));

            mockMvc.perform(
                            post(API_SUBPROCESSOS_99_ANALISES_CADASTRO)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("Testes para listar análises de validação")
    class ListarAnalisesValidacao {

        @Test
        @DisplayName("Deve retornar lista de análises de validação com status 200 OK")
        @WithMockUser
        void deveRetornarListaDeAnalisesValidacao() throws Exception {
            var analise = new Analise();
            var dto = AnaliseHistoricoDto.builder().observacoes(OBSERVACAO_1).build();

            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(analiseFacade.listarPorSubprocesso(1L, TipoAnalise.VALIDACAO))
                    .thenReturn(Collections.singletonList(analise));
            when(analiseMapper.toAnaliseHistoricoDto(analise)).thenReturn(dto);

            mockMvc.perform(get(API_SUBPROCESSOS_1_ANALISES_VALIDACAO))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].observacoes").value(OBSERVACAO_1));
        }

        @Test
        @DisplayName("Deve retornar lista vazia de análises de validação com status 200 OK")
        @WithMockUser
        void deveRetornarListaVaziaDeAnalisesValidacao() throws Exception {
            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(analiseFacade.listarPorSubprocesso(1L, TipoAnalise.VALIDACAO))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get(API_SUBPROCESSOS_1_ANALISES_VALIDACAO))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
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
    }

    @Nested
    @DisplayName("Testes para criar análise de validação")
    class CriarAnaliseValidacao {

        @Test
        @DisplayName("Deve criar uma análise de validação e retornar 201 Created")
        @WithMockUser(roles = "ADMIN")
        void deveCriarAnaliseValidacao() throws Exception {
            var request = new CriarAnaliseRequest(NOVA_ANALISE_DE_VALIDACAO, "S", "T", "M");
            var analise = new Analise();
            var dto = AnaliseHistoricoDto.builder().observacoes(NOVA_ANALISE_DE_VALIDACAO).build();

            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(analiseFacade.criarAnalise(any(), any())).thenReturn(analise);
            when(analiseMapper.toAnaliseHistoricoDto(analise)).thenReturn(dto);

            mockMvc.perform(
                            post(API_SUBPROCESSOS_1_ANALISES_VALIDACAO)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.observacoes").value(NOVA_ANALISE_DE_VALIDACAO));
        }
    }
}