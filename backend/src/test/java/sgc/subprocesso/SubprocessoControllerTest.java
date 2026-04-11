package sgc.subprocesso;

import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;
import sgc.comum.ComumDtos.*;
import sgc.comum.erros.*;
import sgc.mapa.dto.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.seguranca.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubprocessoController.class)
@Import(RestExceptionHandler.class)
@DisplayName("SubprocessoController")
class SubprocessoControllerTest {
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @MockitoBean
    private SubprocessoService subprocessoService;
    @MockitoBean private SubprocessoConsultaService consultaService;
    @MockitoBean private AnaliseHistoricoService analiseHistoricoService;

    @MockitoBean
    private SubprocessoTransicaoService transicaoService;

    @MockitoBean
    private UnidadeService unidadeService;

    @MockitoBean
    private SgcPermissionEvaluator permissionEvaluator;

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Dado operação de consulta, quando buscar por processo e unidade, então retorna subprocesso")
    class CrudTests {
        @Test
        @DisplayName("deve buscar por processo e unidade")
        @WithMockUser
        void buscarPorProcessoEUnidade() throws Exception {
            Unidade unidade = new Unidade(); unidade.setCodigo(10L);
            when(unidadeService.buscarPorSigla("U1")).thenReturn(unidade);
            when(consultaService.obterEntidadePorProcessoEUnidade(1L, 10L)).thenReturn(Subprocesso.builder().codigo(100L).build());

            mockMvc.perform(get("/api/subprocessos/buscar")
                            .param("codProcesso", "1")
                            .param("siglaUnidade", "U1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.codigo").value(100));

            verify(unidadeService).buscarPorSigla("U1");
            verify(consultaService).obterEntidadePorProcessoEUnidade(1L, 10L);
        }

        @Test
        @DisplayName("deve retornar 500 quando sigla da unidade for inválida")
        @WithMockUser
        void buscarPorProcessoEUnidadeErro() throws Exception {
            when(unidadeService.buscarPorSigla("U1")).thenThrow(new RuntimeException("erro ao buscar unidade"));

            mockMvc.perform(get("/api/subprocessos/buscar")
                            .param("codProcesso", "1")
                            .param("siglaUnidade", "U1"))
                    .andExpect(status().isInternalServerError());

            verify(unidadeService).buscarPorSigla("U1");
            verify(consultaService, never()).obterEntidadePorProcessoEUnidade(anyLong(), anyLong());
        }
    }

    @Nested
    @DisplayName("Dado fluxo de cadastro, quando executar transições, então responde com sucesso")
    class CadastroWorkflowTests {
        @Test
        @DisplayName("deve disponibilizar cadastro")
        @WithMockUser(roles = "CHEFE")
        void deveDisponibilizarCadastro() throws Exception {
            mockMvc.perform(post("/api/subprocessos/1/cadastro/disponibilizar")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mensagem").value("Cadastro de atividades disponibilizado"));

            verify(transicaoService).disponibilizarCadastro(1L);
        }

        @Test
        @DisplayName("deve devolver erro de validação ao disponibilizar cadastro")
        @WithMockUser(roles = "CHEFE")
        void deveDisponibilizarCadastroComErroValidacao() throws Exception {
            doThrow(new ErroValidacao("Cadastro incompleto."))
                    .when(transicaoService).disponibilizarCadastro(1L);

            mockMvc.perform(post("/api/subprocessos/1/cadastro/disponibilizar")
                            .with(csrf()))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.message").value("Cadastro incompleto."));

            verify(transicaoService).disponibilizarCadastro(1L);
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

            verify(transicaoService).devolverCadastro(eq(1L), anyString());
        }

        @Test
        @DisplayName("deve rejeitar devolver cadastro sem justificativa")
        @WithMockUser(roles = "ADMIN")
        void deveRejeitarDevolverCadastroSemJustificativa() throws Exception {
            JustificativaRequest request = new JustificativaRequest(" ");

            mockMvc.perform(post("/api/subprocessos/1/devolver-cadastro")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details").exists());

            verify(transicaoService, never()).devolverCadastro(anyLong(), anyString());
        }

        @Test
        @DisplayName("deve aceitar cadastro com texto opcional")
        @WithMockUser(roles = "GESTOR")
        void deveAceitarCadastro() throws Exception {
            TextoOpcionalRequest request = new TextoOpcionalRequest("Parecer");

            mockMvc.perform(post("/api/subprocessos/1/aceitar-cadastro")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(transicaoService).aceitarCadastro(eq(1L), eq("Parecer"));
        }

        @Test
        @DisplayName("deve homologar cadastro com texto opcional")
        @WithMockUser(roles = "GESTOR")
        void deveHomologarCadastro() throws Exception {
            TextoOpcionalRequest request = new TextoOpcionalRequest("Homologado");

            mockMvc.perform(post("/api/subprocessos/1/homologar-cadastro")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(transicaoService).homologarCadastro(eq(1L), eq("Homologado"));
        }

        @Test
        @DisplayName("deve disponibilizar revisão de cadastro")
        @WithMockUser(roles = "CHEFE")
        void deveDisponibilizarRevisaoCadastro() throws Exception {
            mockMvc.perform(post("/api/subprocessos/1/disponibilizar-revisao")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mensagem").value("Revisão do cadastro disponibilizada"));

            verify(transicaoService).disponibilizarRevisao(1L);
        }

        @Test
        @DisplayName("deve iniciar revisão do cadastro")
        @WithMockUser(roles = "CHEFE")
        void deveIniciarRevisaoCadastro() throws Exception {
            mockMvc.perform(post("/api/subprocessos/1/iniciar-revisao-cadastro")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mensagem").value("Revisão do cadastro iniciada"));

            verify(transicaoService).iniciarRevisaoCadastro(1L);
        }

        @Test
        @DisplayName("deve devolver revisão de cadastro")
        @WithMockUser(roles = "GESTOR")
        void deveDevolverRevisaoCadastro() throws Exception {
            JustificativaRequest request = new JustificativaRequest("Ajustar revisão");

            mockMvc.perform(post("/api/subprocessos/1/devolver-revisao-cadastro")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(transicaoService).devolverRevisaoCadastro(eq(1L), eq("Ajustar revisão"));
        }

        @Test
        @DisplayName("deve aceitar revisão de cadastro")
        @WithMockUser(roles = "GESTOR")
        void deveAceitarRevisaoCadastro() throws Exception {
            TextoOpcionalRequest request = new TextoOpcionalRequest("Aceite revisão");

            mockMvc.perform(post("/api/subprocessos/1/aceitar-revisao-cadastro")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(transicaoService).aceitarRevisaoCadastro(eq(1L), eq("Aceite revisão"));
        }

        @Test
        @DisplayName("deve homologar revisão de cadastro")
        @WithMockUser(roles = "GESTOR")
        void deveHomologarRevisaoCadastro() throws Exception {
            TextoOpcionalRequest request = new TextoOpcionalRequest("Homologar revisão");

            mockMvc.perform(post("/api/subprocessos/1/homologar-revisao-cadastro")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(transicaoService).homologarRevisaoCadastro(eq(1L), eq("Homologar revisão"));
        }

        @Test
        @DisplayName("deve aceitar cadastro mesmo com texto nulo")
        @WithMockUser(roles = "GESTOR")
        void deveAceitarCadastroComTextoNulo() throws Exception {
            TextoOpcionalRequest request = new TextoOpcionalRequest(null);

            mockMvc.perform(post("/api/subprocessos/1/aceitar-cadastro")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(transicaoService).aceitarCadastro(eq(1L), eq(""));
        }
    }

    @Nested
    @DisplayName("Dado fluxo de mapa, quando consultar ou disponibilizar, então aplica regras de endpoint")
    class MapaWorkflowTests {
        @Test
        @DisplayName("deve obter mapa para visualização")
        @WithMockUser
        void deveObterMapaParaVisualizacao() throws Exception {
            mockMvc.perform(get("/api/subprocessos/1/mapa-visualizacao"))
                    .andExpect(status().isOk());
            verify(consultaService).mapaParaVisualizacao(1L);
        }

        @Test
        @DisplayName("deve retornar erro interno ao obter mapa para visualização")
        @WithMockUser
        void deveFalharAoObterMapaParaVisualizacao() throws Exception {
            when(consultaService.mapaParaVisualizacao(1L)).thenThrow(new RuntimeException("falha"));

            mockMvc.perform(get("/api/subprocessos/1/mapa-visualizacao"))
                    .andExpect(status().isInternalServerError());

            verify(consultaService).mapaParaVisualizacao(1L);
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

            verify(transicaoService).disponibilizarMapa(eq(1L), any());
        }

        @Test
        @DisplayName("deve rejeitar disponibilização de mapa com data inválida")
        @WithMockUser(roles = "ADMIN")
        void deveRejeitarDisponibilizarMapaComDataInvalida() throws Exception {
            DisponibilizarMapaRequest req = new DisponibilizarMapaRequest(java.time.LocalDate.now().minusDays(1), "Obs");
            mockMvc.perform(post("/api/subprocessos/1/disponibilizar-mapa")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());

            verify(transicaoService, never()).disponibilizarMapa(anyLong(), any());
        }

        @Test
        @DisplayName("deve obter sugestões consolidadas")
        @WithMockUser
        void deveObterSugestoes() throws Exception {
            when(consultaService.obterSugestoes(1L)).thenReturn(Map.of("total", 1, "itens", List.of("SUG1")));

            mockMvc.perform(get("/api/subprocessos/1/sugestoes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(1));

            verify(consultaService).obterSugestoes(1L);
        }

        @Test
        @DisplayName("deve obter histórico de validação")
        @WithMockUser
        void deveObterHistoricoValidacao() throws Exception {
            when(consultaService.listarHistoricoValidacao(1L)).thenReturn(List.of());

            mockMvc.perform(get("/api/subprocessos/1/historico-validacao"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());

            verify(consultaService).listarHistoricoValidacao(1L);
        }

        @Test
        @DisplayName("deve obter impactos de mapa")
        @WithMockUser
        void deveObterImpactosMapa() throws Exception {
            when(consultaService.verificarImpactos(1L)).thenReturn(ImpactoMapaResponse.builder().temImpactos(false).build());

            mockMvc.perform(get("/api/subprocessos/1/impactos-mapa"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.temImpactos").value(false));

            verify(consultaService).verificarImpactos(1L);
        }

        @Test
        @DisplayName("deve obter contexto de cadastro de atividades buscando processo e unidade")
        @WithMockUser
        void deveObterContextoCadastroAtividadesBusca() throws Exception {
            Unidade unidade = new Unidade();
            unidade.setCodigo(10L);
            when(unidadeService.buscarPorSigla("U10")).thenReturn(unidade);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(1L);
            when(consultaService.obterEntidadePorProcessoEUnidade(5L, 10L)).thenReturn(sp);

            when(consultaService.obterContextoCadastroAtividades(1L)).thenReturn(
                    new sgc.subprocesso.dto.ContextoCadastroAtividadesResponse(null, null, null, List.of())
            );

            mockMvc.perform(get("/api/subprocessos/contexto-cadastro-atividades/buscar")
                            .param("codProcesso", "5")
                            .param("siglaUnidade", "U10"))
                    .andExpect(status().isOk());

            verify(consultaService).obterContextoCadastroAtividades(1L);
        }
    }

    @Nested
    @DisplayName("Dado fluxo de validação, quando apresentar sugestões, então delega ao serviço")
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

            verify(transicaoService).apresentarSugestoes(eq(1L), eq("Sugestão"));
        }

        @Test
        @DisplayName("deve encaminhar sugestões mesmo quando texto estiver em branco")
        @WithMockUser(roles = "CHEFE")
        void deveEncaminharSugestoesSemTexto() throws Exception {
            TextoRequest req = new TextoRequest(" ");
            mockMvc.perform(post("/api/subprocessos/1/apresentar-sugestoes")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk());

            verify(transicaoService).apresentarSugestoes(eq(1L), eq(" "));
        }

        @Test
        @DisplayName("deve propagar erro de negócio ao apresentar sugestões")
        @WithMockUser(roles = "CHEFE")
        void devePropagarErroAoApresentarSugestoes() throws Exception {
            TextoRequest req = new TextoRequest("Sugestão");
            doThrow(new ErroValidacao("Sugestão inválida"))
                    .when(transicaoService).apresentarSugestoes(eq(1L), eq("Sugestão"));

            mockMvc.perform(post("/api/subprocessos/1/apresentar-sugestoes")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.message").value("Sugestão inválida"));

            verify(transicaoService).apresentarSugestoes(eq(1L), eq("Sugestão"));
        }
    }

    @Nested
    @DisplayName("Dado fluxo de análises, quando registrar parecer, então persiste histórico")
    class AnalisesTests {
        @Test
        @DisplayName("deve criar análise de cadastro")
        @WithMockUser(roles = "GESTOR")
        void deveCriarAnaliseCadastro() throws Exception {
            CriarAnaliseRequest request = CriarAnaliseRequest.builder()
                    .observacoes("Obs")
                    .motivo("MOTIVO")
                    .acao(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                    .build();

            when(consultaService.buscarSubprocesso(1L)).thenReturn(new Subprocesso());
            when(transicaoService.criarAnalise(any(), any(), any())).thenReturn(new Analise());

            mockMvc.perform(post("/api/subprocessos/1/analises-cadastro")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(transicaoService).criarAnalise(any(), any(), any());
        }

        @Test
        @DisplayName("deve criar análise de validação")
        @WithMockUser(roles = "GESTOR")
        void deveCriarAnaliseValidacao() throws Exception {
            CriarAnaliseRequest request = CriarAnaliseRequest.builder()
                    .observacoes("Obs")
                    .motivo("MOTIVO")
                    .acao(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                    .build();

            Analise analise = new Analise();
            when(consultaService.buscarSubprocesso(1L)).thenReturn(new Subprocesso());
            when(transicaoService.criarAnalise(any(), any(), eq(TipoAnalise.VALIDACAO))).thenReturn(analise);
            when(analiseHistoricoService.converter(analise)).thenReturn(new AnaliseHistoricoDto(
                    TipoAnalise.VALIDACAO,
                    TipoAcaoAnalise.ACEITE_MAPEAMENTO,
                    "123456789012",
                    "SIGLA",
                    "Unidade",
                    java.time.LocalDateTime.now(),
                    "MOTIVO",
                    "Obs"));

            mockMvc.perform(post("/api/subprocessos/1/analises-validacao")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.tipo").value("VALIDACAO"));

            verify(transicaoService).criarAnalise(any(), any(), eq(TipoAnalise.VALIDACAO));
        }

        @Test
        @DisplayName("deve propagar erro ao criar análise de cadastro")
        @WithMockUser(roles = "GESTOR")
        void devePropagarErroCriarAnaliseCadastro() throws Exception {
            CriarAnaliseRequest request = CriarAnaliseRequest.builder()
                    .observacoes("Obs")
                    .motivo("MOTIVO")
                    .acao(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                    .build();

            when(consultaService.buscarSubprocesso(1L)).thenReturn(new Subprocesso());
            when(transicaoService.criarAnalise(any(), any(), eq(TipoAnalise.CADASTRO)))
                    .thenThrow(new ErroValidacao("Parecer inválido"));

            mockMvc.perform(post("/api/subprocessos/1/analises-cadastro")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.message").value("Parecer inválido"));

            verify(transicaoService).criarAnalise(any(), any(), eq(TipoAnalise.CADASTRO));
        }

        @Test
        @DisplayName("deve rejeitar criação de análise com payload inválido")
        @WithMockUser(roles = "GESTOR")
        void deveRejeitarCriarAnaliseSemAcao() throws Exception {
            String jsonInvalido = """
                    {
                      "observacoes": "Obs",
                      "motivo": "MOTIVO"
                    }
                    """;

            mockMvc.perform(post("/api/subprocessos/1/analises-cadastro")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonInvalido))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details").exists());

            verify(transicaoService, never()).criarAnalise(any(), any(), any());
        }
    }
}
