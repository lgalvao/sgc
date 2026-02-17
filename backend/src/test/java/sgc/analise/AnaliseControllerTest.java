package sgc.analise;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.analise.dto.AnaliseHistoricoDto;
import sgc.analise.dto.AnaliseValidacaoHistoricoDto;
import sgc.analise.dto.CriarAnaliseRequest;
import sgc.analise.model.Analise;
import sgc.comum.erros.RestExceptionHandler;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
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
    private static final String NOVA_ANALISE_DE_CADASTRO = "Nova análise de cadastro";
    private static final String ANALISE_INVALIDA = "Análise inválida";
    private static final String API_SUBPROCESSOS_1_ANALISES_VALIDACAO = "/api/subprocessos/1/analises-validacao";
    private static final String NOVA_ANALISE_DE_VALIDACAO = "Nova análise de validação";

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private AnaliseFacade analiseFacade;

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
            Analise analise1 = new Analise();
            analise1.setCodigo(1L);
            analise1.setObservacoes(OBSERVACAO_1);

            Analise analise2 = new Analise();
            analise2.setCodigo(2L);
            analise2.setObservacoes(OBSERVACAO_2);

            AnaliseHistoricoDto dto1 = AnaliseHistoricoDto.builder().observacoes(OBSERVACAO_1).build();
            AnaliseHistoricoDto dto2 = AnaliseHistoricoDto.builder().observacoes(OBSERVACAO_2).build();

            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(analiseFacade.listarHistoricoCadastro(1L))
                    .thenReturn(Arrays.asList(dto1, dto2));

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
            when(analiseFacade.listarHistoricoCadastro(1L))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get(API_SUBPROCESSOS_1_ANALISES_CADASTRO))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("Testes para criar análise de cadastro")
    class CriarAnaliseCadastro {

        @Test
        @DisplayName("Deve criar uma análise de cadastro e retornar 201 Created")
        @WithMockUser(roles = "GESTOR")
        void deveCriarAnaliseCadastro() throws Exception {
            CriarAnaliseRequest request = new CriarAnaliseRequest("123456789012", NOVA_ANALISE_DE_CADASTRO, "SIGLA", "MOTIVO");
            Analise analise = new Analise();
            AnaliseHistoricoDto dto = AnaliseHistoricoDto.builder().observacoes(NOVA_ANALISE_DE_CADASTRO).build();

            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(analiseFacade.criarAnalise(any(), any())).thenReturn(analise);
            when(analiseFacade.paraHistoricoDto(analise)).thenReturn(dto);

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
            CriarAnaliseRequest request = new CriarAnaliseRequest("123456789012", "", "SIGLA", "MOTIVO");
            Analise analise = new Analise();
            AnaliseHistoricoDto dto = AnaliseHistoricoDto.builder().observacoes("").build();

            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(analiseFacade.criarAnalise(any(), any())).thenReturn(analise);
            when(analiseFacade.paraHistoricoDto(analise)).thenReturn(dto);

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
            Analise analise = new Analise();
            AnaliseHistoricoDto dto = AnaliseHistoricoDto.builder().build();

            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(analiseFacade.criarAnalise(any(), any())).thenReturn(analise);
            when(analiseFacade.paraHistoricoDto(analise)).thenReturn(dto);

            mockMvc.perform(
                            post(API_SUBPROCESSOS_1_ANALISES_CADASTRO)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"tituloUsuario\":\"123456789012\", \"siglaUnidade\":\"SIGLA\"}"))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request para parâmetro inválido")
        @WithMockUser(roles = "GESTOR")
        void deveRetornarBadRequestParaParametroInvalido() throws Exception {
            CriarAnaliseRequest request = new CriarAnaliseRequest("123456789012", ANALISE_INVALIDA, "S", "M");

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
    }

    @Nested
    @DisplayName("Testes para listar análises de validação")
    class ListarAnalisesValidacao {

        @Test
        @DisplayName("Deve retornar lista de análises de validação com status 200 OK")
        @WithMockUser
        void deveRetornarListaDeAnalisesValidacao() throws Exception {
            AnaliseValidacaoHistoricoDto vDto = AnaliseValidacaoHistoricoDto.builder().observacoes(OBSERVACAO_1).build();

            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            // listarHistoricoValidacao já retorna DTOs, então não precisamos mapear no controller
            when(analiseFacade.listarHistoricoValidacao(1L))
                    .thenReturn(Collections.singletonList(vDto));

            mockMvc.perform(get(API_SUBPROCESSOS_1_ANALISES_VALIDACAO))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].observacoes").value(OBSERVACAO_1));
        }

        @Test
        @DisplayName("Deve retornar lista vazia de análises de validação com status 200 OK")
        @WithMockUser
        void deveRetornarListaVaziaDeAnalisesValidacao() throws Exception {
            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(analiseFacade.listarHistoricoValidacao(1L))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get(API_SUBPROCESSOS_1_ANALISES_VALIDACAO))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("Testes para criar análise de validação")
    class CriarAnaliseValidacao {

        @Test
        @DisplayName("Deve criar uma análise de validação e retornar 201 Created")
        @WithMockUser(roles = "ADMIN")
        void deveCriarAnaliseValidacao() throws Exception {
            CriarAnaliseRequest request = new CriarAnaliseRequest("123456789012", NOVA_ANALISE_DE_VALIDACAO, "S", "M");
            Analise analise = new Analise();
            AnaliseHistoricoDto dto = AnaliseHistoricoDto.builder().observacoes(NOVA_ANALISE_DE_VALIDACAO).build();

            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(analiseFacade.criarAnalise(any(), any())).thenReturn(analise);
            when(analiseFacade.paraHistoricoDto(analise)).thenReturn(dto);

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
