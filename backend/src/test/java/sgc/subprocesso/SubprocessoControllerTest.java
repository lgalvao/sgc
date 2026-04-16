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
            when(unidadeService.buscarCodigoPorSigla("U1")).thenReturn(10L);
            when(consultaService.obterEntidadePorProcessoEUnidade(1L, 10L)).thenReturn(Subprocesso.builder().codigo(100L).build());

            mockMvc.perform(get("/api/subprocessos/buscar")
                            .param("codProcesso", "1")
                            .param("siglaUnidade", "U1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.codigo").value(100));

            verify(unidadeService).buscarCodigoPorSigla("U1");
            verify(consultaService).obterEntidadePorProcessoEUnidade(1L, 10L);
        }

        @Test
        @DisplayName("deve retornar 500 quando sigla da unidade for inválida")
        @WithMockUser
        void buscarPorProcessoEUnidadeErro() throws Exception {
            when(unidadeService.buscarCodigoPorSigla("U1")).thenThrow(new RuntimeException("erro ao buscar unidade"));

            mockMvc.perform(get("/api/subprocessos/buscar")
                            .param("codProcesso", "1")
                            .param("siglaUnidade", "U1"))
                    .andExpect(status().isInternalServerError());

            verify(unidadeService).buscarCodigoPorSigla("U1");
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
        @DisplayName("deve cancelar início da revisão do cadastro")
        @WithMockUser(roles = "CHEFE")
        void deveCancelarInicioRevisaoCadastro() throws Exception {
            mockMvc.perform(post("/api/subprocessos/1/cancelar-inicio-revisao-cadastro")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mensagem").value("Início da revisão do cadastro cancelado"));

            verify(transicaoService).cancelarInicioRevisaoCadastro(1L);
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
        @DisplayName("deve salvar mapa completo em batch")
        @WithMockUser(roles = "GESTOR")
        void deveSalvarMapaCompleto() throws Exception {
            SalvarMapaRequest request = SalvarMapaRequest.builder().competencias(List.of()).build();
            MapaCompletoDto dto = new MapaCompletoDto(1L, 100L, "Mapa", List.of(), null);
            when(consultaService.mapaCompletoDtoPorSubprocesso(1L)).thenReturn(dto);

            mockMvc.perform(post("/api/subprocessos/1/mapa-completo")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(subprocessoService).salvarMapa(eq(1L), any(SalvarMapaRequest.class));
            verify(consultaService).mapaCompletoDtoPorSubprocesso(1L);
        }

        @Test
        @DisplayName("deve disponibilizar mapas em bloco")
        @WithMockUser(roles = "GESTOR")
        void deveDisponibilizarMapaEmBloco() throws Exception {
            ProcessarEmBlocoRequest request = new ProcessarEmBlocoRequest("DISPONIBILIZAR", List.of(1L, 2L), null);

            mockMvc.perform(post("/api/subprocessos/1/disponibilizar-mapa-bloco")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(transicaoService).disponibilizarMapaEmBloco(eq(List.of(1L, 2L)), any());
        }

        @Test
        @DisplayName("deve obter mapa preparado para ajuste")
        @WithMockUser(roles = "GESTOR")
        void deveObterMapaParaAjuste() throws Exception {
            MapaAjusteDto dto = MapaAjusteDto.builder().codMapa(1L).unidadeNome("MAPA").competencias(List.of()).justificativaDevolucao("Obs").build();
            when(consultaService.obterMapaParaAjuste(1L)).thenReturn(dto);

            mockMvc.perform(get("/api/subprocessos/1/mapa-ajuste"))
                    .andExpect(status().isOk());

            verify(consultaService).obterMapaParaAjuste(1L);
        }

        @Test
        @DisplayName("deve salvar os ajustes feitos no mapa")
        @WithMockUser(roles = "GESTOR")
        void deveSalvarAjustesMapa() throws Exception {
            SalvarAjustesRequest request = new SalvarAjustesRequest(List.of(CompetenciaAjusteDto.builder().codCompetencia(1L).nome("Comp").atividades(List.of()).build()));

            mockMvc.perform(post("/api/subprocessos/1/mapa-ajuste/atualizar")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(subprocessoService).salvarAjustesMapa(eq(1L), anyList());
        }

        @Test
        @DisplayName("deve adicionar uma competência ao mapa")
        @WithMockUser(roles = "GESTOR")
        void deveAdicionarCompetencia() throws Exception {
            CompetenciaRequest request = new CompetenciaRequest("Descricao", List.of());
            MapaCompletoDto dto = new MapaCompletoDto(1L, 100L, "Mapa", List.of(), null);
            when(consultaService.mapaCompletoDtoPorSubprocesso(1L)).thenReturn(dto);

            mockMvc.perform(post("/api/subprocessos/1/competencia")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(subprocessoService).adicionarCompetencia(eq(1L), any(CompetenciaRequest.class));
        }

        @Test
        @DisplayName("deve atualizar uma competência do mapa")
        @WithMockUser(roles = "GESTOR")
        void deveAtualizarCompetencia() throws Exception {
            CompetenciaRequest request = new CompetenciaRequest("Descricao", List.of());
            MapaCompletoDto dto = new MapaCompletoDto(1L, 100L, "Mapa", List.of(), null);
            when(consultaService.mapaCompletoDtoPorSubprocesso(1L)).thenReturn(dto);

            mockMvc.perform(post("/api/subprocessos/1/competencia/2")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(subprocessoService).atualizarCompetencia(eq(1L), eq(2L), any(CompetenciaRequest.class));
        }

        @Test
        @DisplayName("deve remover uma competência do mapa")
        @WithMockUser(roles = "GESTOR")
        void deveRemoverCompetencia() throws Exception {
            MapaCompletoDto dto = new MapaCompletoDto(1L, 100L, "Mapa", List.of(), null);
            when(consultaService.mapaCompletoDtoPorSubprocesso(1L)).thenReturn(dto);

            mockMvc.perform(post("/api/subprocessos/1/competencia/2/remover")
                            .with(csrf()))
                    .andExpect(status().isOk());

            verify(subprocessoService).removerCompetencia(1L, 2L);
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
            when(unidadeService.buscarCodigoPorSigla("U10")).thenReturn(10L);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(1L);
            when(consultaService.obterEntidadePorProcessoEUnidade(5L, 10L)).thenReturn(sp);

            when(consultaService.obterContextoCadastroAtividades(1L)).thenReturn(
                    new sgc.subprocesso.dto.ContextoCadastroAtividadesResponse(null, null, null, List.of(), "")
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
        @DisplayName("deve validar o mapa de competências da unidade")
        @WithMockUser(roles = "CHEFE")
        void deveValidarMapa() throws Exception {
            mockMvc.perform(post("/api/subprocessos/1/validar-mapa")
                            .with(csrf()))
                    .andExpect(status().isOk());

            verify(transicaoService).validarMapa(1L);
        }

        @Test
        @DisplayName("deve devolver validação (pelo gestor/chefe)")
        @WithMockUser(roles = "GESTOR")
        void deveDevolverValidacao() throws Exception {
            JustificativaRequest request = new JustificativaRequest("Precisa ajustar algo");

            mockMvc.perform(post("/api/subprocessos/1/devolver-validacao")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(transicaoService).devolverValidacao(eq(1L), eq("Precisa ajustar algo"));
        }

        @Test
        @DisplayName("deve aceitar validação (pelo gestor)")
        @WithMockUser(roles = "GESTOR")
        void deveAceitarValidacao() throws Exception {
            TextoOpcionalRequest request = new TextoOpcionalRequest("Tudo certo");

            mockMvc.perform(post("/api/subprocessos/1/aceitar-validacao")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(transicaoService).aceitarValidacao(eq(1L), eq("Tudo certo"));
        }

        @Test
        @DisplayName("deve aceitar validação sem observações")
        @WithMockUser(roles = "GESTOR")
        void deveAceitarValidacaoSemObservacoes() throws Exception {
            TextoOpcionalRequest request = new TextoOpcionalRequest(null);

            mockMvc.perform(post("/api/subprocessos/1/aceitar-validacao")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(transicaoService).aceitarValidacao(eq(1L), isNull());
        }

        @Test
        @DisplayName("deve homologar validação")
        @WithMockUser(roles = "ADMIN")
        void deveHomologarValidacao() throws Exception {
            TextoOpcionalRequest request = new TextoOpcionalRequest("Homologado");

            mockMvc.perform(post("/api/subprocessos/1/homologar-validacao")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(transicaoService).homologarValidacao(eq(1L), eq("Homologado"));
        }

        @Test
        @DisplayName("deve homologar validação sem observações")
        @WithMockUser(roles = "ADMIN")
        void deveHomologarValidacaoSemObservacoes() throws Exception {
            TextoOpcionalRequest request = new TextoOpcionalRequest(null);

            mockMvc.perform(post("/api/subprocessos/1/homologar-validacao")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(transicaoService).homologarValidacao(eq(1L), isNull());
        }

        @Test
        @DisplayName("deve homologar validação com request nulo")
        @WithMockUser(roles = "ADMIN")
        void deveHomologarValidacaoComRequestNulo() throws Exception {
            mockMvc.perform(post("/api/subprocessos/1/homologar-validacao")
                            .with(csrf()))
                    .andExpect(status().isOk());

            verify(transicaoService).homologarValidacao(eq(1L), isNull());
        }

        @Test
        @DisplayName("deve submeter o mapa após ajustes")
        @WithMockUser(roles = "CHEFE")
        void deveSubmeterMapaAjustado() throws Exception {
            SubmeterMapaAjustadoRequest request = new SubmeterMapaAjustadoRequest("Ajustes feitos", null, List.of());

            mockMvc.perform(post("/api/subprocessos/1/submeter-mapa-ajustado")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(transicaoService).submeterMapaAjustado(eq(1L), any(SubmeterMapaAjustadoRequest.class));
        }

        @Test
        @DisplayName("deve aceitar validação em bloco")
        @WithMockUser(roles = "GESTOR")
        void deveAceitarValidacaoEmBloco() throws Exception {
            ProcessarEmBlocoRequest request = new ProcessarEmBlocoRequest("ACEITAR", List.of(1L, 2L), null);

            mockMvc.perform(post("/api/subprocessos/1/aceitar-validacao-bloco")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(transicaoService).aceitarValidacaoEmBloco(List.of(1L, 2L));
        }

        @Test
        @DisplayName("deve homologar validação em bloco")
        @WithMockUser(roles = "ADMIN")
        void deveHomologarValidacaoEmBloco() throws Exception {
            ProcessarEmBlocoRequest request = new ProcessarEmBlocoRequest("HOMOLOGAR", List.of(1L, 2L), null);

            mockMvc.perform(post("/api/subprocessos/1/homologar-validacao-bloco")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(transicaoService).homologarValidacaoEmBloco(List.of(1L, 2L));
        }

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
