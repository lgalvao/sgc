package sgc.subprocesso;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.comum.ComumDtos.JustificativaRequest;
import sgc.comum.ComumDtos.TextoRequest;
import sgc.comum.erros.RestExceptionHandler;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.dto.UnidadeDto;
import sgc.seguranca.SgcPermissionEvaluator;
import sgc.subprocesso.dto.CriarAnaliseRequest;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.model.Analise;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.TipoAcaoAnalise;
import sgc.subprocesso.model.TipoAnalise;
import sgc.subprocesso.service.SubprocessoService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubprocessoController.class)
@Import(RestExceptionHandler.class)
@DisplayName("SubprocessoController")
class SubprocessoControllerTest {

    @MockitoBean
    private SubprocessoService subprocessoService;

    @MockitoBean
    private OrganizacaoFacade organizacaoFacade;

    @MockitoBean
    private SgcPermissionEvaluator permissionEvaluator;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @Nested
    @DisplayName("CRUD Operations")
    class CrudTests {
        @Test
        @DisplayName("deve buscar por processo e unidade")
        @WithMockUser
        void buscarPorProcessoEUnidade() throws Exception {
            UnidadeDto unidade = UnidadeDto.builder().codigo(10L).build();
            when(organizacaoFacade.buscarPorSigla("U1")).thenReturn(unidade);
            when(subprocessoService.obterEntidadePorProcessoEUnidade(1L, 10L)).thenReturn(Subprocesso.builder().codigo(100L).build());

            mockMvc.perform(get("/api/subprocessos/buscar")
                            .param("codProcesso", "1")
                            .param("siglaUnidade", "U1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.codigo").value(100));
        }
    }

    @Nested
    @DisplayName("Cadastro Workflow")
    class CadastroWorkflowTests {
        @Test
        @DisplayName("deve disponibilizar cadastro")
        @WithMockUser(roles = "CHEFE")
        void deveDisponibilizarCadastro() throws Exception {
            when(subprocessoService.obterAtividadesSemConhecimento(1L))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(post("/api/subprocessos/1/cadastro/disponibilizar")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mensagem").value("Cadastro de atividades disponibilizado"));

            verify(subprocessoService).disponibilizarCadastro(eq(1L), any());
        }

        @Test
        @DisplayName("deve devolver cadastro")
        @WithMockUser(roles = "ADMIN")
        void deveDevolverCadastro() throws Exception {
            JustificativaRequest request = new JustificativaRequest("Ajustes");

            mockMvc.perform(post("/api/subprocessos/1/devolver-cadastro")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(subprocessoService).devolverCadastro(eq(1L), any(), anyString());
        }
    }

    @Nested
    @DisplayName("Mapa Workflow")
    class MapaWorkflowTests {
        @Test
        @DisplayName("deve obter mapa para visualização")
        @WithMockUser
        void deveObterMapaParaVisualizacao() throws Exception {
            mockMvc.perform(get("/api/subprocessos/1/mapa-visualizacao"))
                    .andExpect(status().isOk());
            verify(subprocessoService).mapaParaVisualizacao(1L);
        }

        @Test
        @DisplayName("deve disponibilizar mapa")
        @WithMockUser(roles = "ADMIN")
        void deveDisponibilizarMapa() throws Exception {
            DisponibilizarMapaRequest req = new DisponibilizarMapaRequest(java.time.LocalDate.now().plusDays(1), "Obs");
            mockMvc.perform(post("/api/subprocessos/1/disponibilizar-mapa")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk());

            verify(subprocessoService).disponibilizarMapa(eq(1L), any(), any());
        }
    }

    @Nested
    @DisplayName("Validação Workflow")
    class ValidacaoWorkflowTests {
        @Test
        @DisplayName("deve apresentar sugestões")
        @WithMockUser(roles = "CHEFE")
        void deveApresentarSugestoes() throws Exception {
            TextoRequest req = new TextoRequest("Sugestão");
            mockMvc.perform(post("/api/subprocessos/1/apresentar-sugestoes")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk());

            verify(subprocessoService).apresentarSugestoes(eq(1L), eq("Sugestão"), any());
        }
    }

    @Nested
    @DisplayName("Análises")
    class AnalisesTests {
        @Test
        @DisplayName("deve criar análise de cadastro")
        @WithMockUser(roles = "GESTOR")
        void deveCriarAnaliseCadastro() throws Exception {
            CriarAnaliseRequest request = CriarAnaliseRequest.builder()
                    .tituloUsuario("123456789012")
                    .observacoes("Obs")
                    .siglaUnidade("SIGLA")
                    .motivo("MOTIVO")
                    .acao(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                    .build();

            when(subprocessoService.buscarSubprocesso(1L)).thenReturn(new Subprocesso());
            when(subprocessoService.criarAnalise(any(), any())).thenReturn(new Analise());

            mockMvc.perform(post("/api/subprocessos/1/analises-cadastro")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(subprocessoService).criarAnalise(any(), any());
        }
    }
}
