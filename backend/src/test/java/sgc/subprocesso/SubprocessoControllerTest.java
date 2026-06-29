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
import sgc.mapa.model.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.seguranca.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.time.*;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({SubprocessoController.class, SubprocessoCadastroController.class})
@Import({RestExceptionHandler.class, SubprocessoDtoMapper.class, OrganizacaoDtoMapper.class})
@DisplayName("SubprocessoController")
class SubprocessoControllerTest {
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @MockitoBean
    private SubprocessoService subprocessoService;
    @MockitoBean
    private SubprocessoConsultaService consultaService;
    @MockitoBean
    private AnaliseHistoricoService analiseHistoricoService;

    @MockitoBean
    private SubprocessoTransicaoService transicaoService;

    @MockitoBean
    private CadastroFluxoService cadastroFluxoService;

    @MockitoBean
    private UnidadeService unidadeService;

    @MockitoBean
    private sgc.organizacao.UsuarioAplicacaoService usuarioService;

    @MockitoBean
    private SgcPermissionEvaluator permissionEvaluator;
    @MockitoBean
    private SubprocessoApresentacaoService subprocessoApresentacaoService;

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Dado operação de consulta, quando buscar por processo e unidade, então retorna subprocesso")
    class CrudTests {
        @Test
        @DisplayName("deve listar todos subprocessos")
        @WithMockUser(roles = "ADMIN")
        void deveListarTodos() throws Exception {
            Processo proc = new Processo();
            proc.setCodigo(100L);
            proc.setDescricao("Proc Teste");
            proc.setDataCriacao(java.time.LocalDateTime.now());
            proc.setTipo(TipoProcesso.MAPEAMENTO);

            Unidade un = new Unidade();
            un.setCodigo(200L);
            un.setSigla("U1");
            un.setNome("Unidade 1");
            un.setTipo(sgc.organizacao.model.TipoUnidade.OPERACIONAL);
            un.setTituloTitular("Diretor");

            Subprocesso sp = Subprocesso.builder()
                    .codigo(1L)
                    .processo(proc)
                    .unidade(un)
                    .build();
            sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
            when(consultaService.listarTodos()).thenReturn(List.of(sp));

            mockMvc.perform(get("/api/subprocessos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].codigo").value(1));

            verify(consultaService).listarTodos();
        }

        @Test
        @DisplayName("deve excluir subprocesso")
        @WithMockUser(roles = "ADMIN")
        void deveExcluirSubprocesso() throws Exception {
            mockMvc.perform(post("/api/subprocessos/1/excluir")
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(subprocessoService).excluir(1L);
        }

        @Test
        @DisplayName("deve obter status do subprocesso")
        @WithMockUser(roles = "GESTOR")
        void deveObterStatus() throws Exception {
            SubprocessoSituacaoDto dto = new SubprocessoSituacaoDto(1L, SituacaoSubprocesso.NAO_INICIADO.name());
            when(consultaService.obterStatus(1L)).thenReturn(dto);

            mockMvc.perform(get("/api/subprocessos/1/status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.situacao").value("NAO_INICIADO"));

            verify(consultaService).obterStatus(1L);
        }

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
        @DisplayName("deve buscar contexto edicao por processo e unidade")
        @WithMockUser
        void buscarContextoEdicaoPorProcessoEUnidade() throws Exception {
            SubprocessoResumoDto spResumo = SubprocessoResumoDto.builder()
                    .codigo(100L)
                    .build();
            SubprocessoDetalheResponse spResponse = SubprocessoDetalheResponse.builder()
                    .subprocesso(spResumo)
                    .build();
            ContextoEdicaoResponse response = ContextoEdicaoResponse.builder()
                    .detalhes(spResponse)
                    .build();
            when(subprocessoApresentacaoService.obterContextoEdicaoPorProcessoEUnidade(1L, "U1")).thenReturn(response);

            mockMvc.perform(get("/api/subprocessos/contexto-edicao/buscar")
                            .param("codProcesso", "1")
                            .param("siglaUnidade", "U1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.detalhes.subprocesso.codigo").value(100));

            verify(subprocessoApresentacaoService).obterContextoEdicaoPorProcessoEUnidade(1L, "U1");
        }

        @Test
        @DisplayName("deve negar contexto edicao por processo e unidade sem permissao")
        @WithMockUser
        void buscarContextoEdicaoPorProcessoEUnidadeSemPermissao() throws Exception {
            when(subprocessoApresentacaoService.obterContextoEdicaoPorProcessoEUnidade(1L, "U1"))
                    .thenThrow(new org.springframework.security.access.AccessDeniedException("Acesso negado ao subprocesso"));

            mockMvc.perform(get("/api/subprocessos/contexto-edicao/buscar")
                            .param("codProcesso", "1")
                            .param("siglaUnidade", "U1"))
                    .andExpect(status().isForbidden());

            verify(subprocessoApresentacaoService).obterContextoEdicaoPorProcessoEUnidade(1L, "U1");
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
        @DisplayName("deve validar cadastro")
        @WithMockUser(roles = "GESTOR")
        void deveValidarCadastro() throws Exception {
            ValidacaoCadastroDto dto = new ValidacaoCadastroDto(true, List.of());
            when(consultaService.validarCadastro(1L)).thenReturn(dto);

            mockMvc.perform(get("/api/subprocessos/1/validar-cadastro"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valido").value(true));

            verify(consultaService).validarCadastro(1L);
        }

        @Test
        @DisplayName("deve listar atividades para importacao")
        @WithMockUser(roles = "GESTOR")
        void deveListarAtividadesParaImportacao() throws Exception {
            AtividadeDto dto = new AtividadeDto(10L, "Atividade 1", List.of());
            when(consultaService.listarAtividadesParaImportacao(1L)).thenReturn(List.of(dto));

            mockMvc.perform(get("/api/subprocessos/1/atividades-importacao"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].codigo").value(10));

            verify(consultaService).listarAtividadesParaImportacao(1L);
        }

        @Test
        @DisplayName("deve disponibilizar cadastro")
        @WithMockUser(roles = "CHEFE")
        void deveDisponibilizarCadastro() throws Exception {
            mockMvc.perform(post("/api/subprocessos/1/cadastro/disponibilizar")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mensagem").value("Cadastro de atividades disponibilizado"));

            verify(cadastroFluxoService).disponibilizarCadastro(1L);
        }

        @Test
        @DisplayName("deve devolver erro de validação ao disponibilizar cadastro")
        @WithMockUser(roles = "CHEFE")
        void deveDisponibilizarCadastroComErroValidacao() throws Exception {
            doThrow(new ErroValidacao("Cadastro incompleto."))
                    .when(cadastroFluxoService).disponibilizarCadastro(1L);

            mockMvc.perform(post("/api/subprocessos/1/cadastro/disponibilizar")
                            .with(csrf()))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.message").value("Cadastro incompleto."));

            verify(cadastroFluxoService).disponibilizarCadastro(1L);
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

            verify(cadastroFluxoService).devolver(eq(1L), anyString());
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

            verify(cadastroFluxoService, never()).devolver(anyLong(), anyString());
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

            verify(cadastroFluxoService).aceitar(eq(1L), eq("Parecer"));
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

            verify(cadastroFluxoService).homologar(eq(1L), eq("Homologado"));
        }

        @Test
        @DisplayName("deve disponibilizar revisão de cadastro")
        @WithMockUser(roles = "CHEFE")
        void deveDisponibilizarRevisaoCadastro() throws Exception {
            mockMvc.perform(post("/api/subprocessos/1/disponibilizar-revisao")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mensagem").value("Revisão do cadastro disponibilizada"));

            verify(cadastroFluxoService).disponibilizarRevisao(1L);
        }

        @Test
        @DisplayName("deve iniciar revisão do cadastro")
        @WithMockUser(roles = "CHEFE")
        void deveIniciarRevisaoCadastro() throws Exception {
            mockMvc.perform(post("/api/subprocessos/1/iniciar-revisao-cadastro")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mensagem").value("Revisão do cadastro iniciada"));

            verify(cadastroFluxoService).iniciarRevisaoCadastro(1L);
        }

        @Test
        @DisplayName("deve cancelar início da revisão do cadastro")
        @WithMockUser(roles = "CHEFE")
        void deveCancelarInicioRevisaoCadastro() throws Exception {
            mockMvc.perform(post("/api/subprocessos/1/cancelar-inicio-revisao-cadastro")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mensagem").value("Início da revisão do cadastro cancelado"));

            verify(cadastroFluxoService).cancelarInicioRevisaoCadastro(1L);
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

            verify(cadastroFluxoService).devolver(eq(1L), eq("Ajustar revisão"));
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

            verify(cadastroFluxoService).aceitar(eq(1L), eq("Aceite revisão"));
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

            verify(cadastroFluxoService).homologar(eq(1L), eq("Homologar revisão"));
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

            verify(cadastroFluxoService).aceitar(eq(1L), eq(""));
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
        @DisplayName("deve obter mapa completo")
        @WithMockUser(roles = "GESTOR")
        void deveObterMapaCompleto() throws Exception {
            MapaCompletoDto dto = new MapaCompletoDto(1L, 100L, "Mapa", List.of(), List.of(), null);
            when(consultaService.mapaCompletoDtoPorSubprocesso(1L)).thenReturn(dto);

            mockMvc.perform(get("/api/subprocessos/1/mapa-completo"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.codigo").value(1));

            verify(consultaService).mapaCompletoDtoPorSubprocesso(1L);
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
            MapaCompletoDto dto = new MapaCompletoDto(1L, 100L, "Mapa", List.of(), List.of(), null);
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
            ProcessarEmBlocoRequest request = new ProcessarEmBlocoRequest(List.of(1L, 2L), null);

            mockMvc.perform(post("/api/subprocessos/disponibilizar-mapa-bloco")
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
            CriarCompetenciaRequest request = new CriarCompetenciaRequest("Descricao", List.of(10L));
            MapaCompletoDto dto = new MapaCompletoDto(1L, 100L, "Mapa", List.of(), List.of(), null);
            when(consultaService.mapaCompletoDtoPorSubprocesso(1L)).thenReturn(dto);

            mockMvc.perform(post("/api/subprocessos/1/competencia")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(subprocessoService).adicionarCompetencia(eq(1L), any(CriarCompetenciaRequest.class));
        }

        @Test
        @DisplayName("deve atualizar uma competência do mapa")
        @WithMockUser(roles = "GESTOR")
        void deveAtualizarCompetencia() throws Exception {
            AtualizarCompetenciaRequest request = new AtualizarCompetenciaRequest("Descricao", List.of());
            MapaCompletoDto dto = new MapaCompletoDto(1L, 100L, "Mapa", List.of(), List.of(), null);
            when(consultaService.mapaCompletoDtoPorSubprocesso(1L)).thenReturn(dto);

            mockMvc.perform(post("/api/subprocessos/1/competencia/2")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(subprocessoService).atualizarCompetencia(eq(1L), eq(2L), any(AtualizarCompetenciaRequest.class));
        }

        @Test
        @DisplayName("deve remover uma competência do mapa")
        @WithMockUser(roles = "GESTOR")
        void deveRemoverCompetencia() throws Exception {
            MapaCompletoDto dto = new MapaCompletoDto(1L, 100L, "Mapa", List.of(), List.of(), null);
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
            when(consultaService.obterSugestoes(1L)).thenReturn(new SugestoesDto("Sugestão teste"));

            mockMvc.perform(get("/api/subprocessos/1/sugestoes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sugestoes").value("Sugestão teste"));

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
            var response = new sgc.subprocesso.dto.ContextoCadastroAtividadesResponse(null, null, null, List.of(), "");
            when(subprocessoApresentacaoService.obterContextoCadastroAtividadesPorProcessoEUnidade(5L, "U10"))
                    .thenReturn(response);

            mockMvc.perform(get("/api/subprocessos/contexto-cadastro-atividades/buscar")
                            .param("codProcesso", "5")
                            .param("siglaUnidade", "U10"))
                    .andExpect(status().isOk());

            verify(subprocessoApresentacaoService).obterContextoCadastroAtividadesPorProcessoEUnidade(5L, "U10");
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
            ProcessarEmBlocoRequest request = new ProcessarEmBlocoRequest(List.of(1L, 2L), null);

            mockMvc.perform(post("/api/subprocessos/aceitar-validacao-bloco")
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
            ProcessarEmBlocoRequest request = new ProcessarEmBlocoRequest(List.of(1L, 2L), null);

            mockMvc.perform(post("/api/subprocessos/homologar-validacao-bloco")
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

            when(subprocessoApresentacaoService.criarAnalise(eq(1L), any(), eq(TipoAnalise.CADASTRO)))
                    .thenReturn(new AnaliseHistoricoDto(
                            TipoAnalise.CADASTRO.name(),
                            TipoAcaoAnalise.ACEITE_MAPEAMENTO.name(),
                            "Aceite",
                            "123456789012",
                            "Usuário Teste",
                            "SIGLA",
                            "Unidade",
                            java.time.LocalDateTime.now(),
                            "MOTIVO",
                            "Obs"));

            mockMvc.perform(post("/api/subprocessos/1/analises-cadastro")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(subprocessoApresentacaoService).criarAnalise(eq(1L), any(), eq(TipoAnalise.CADASTRO));
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

            when(subprocessoApresentacaoService.criarAnalise(eq(1L), any(), eq(TipoAnalise.VALIDACAO))).thenReturn(new AnaliseHistoricoDto(
                TipoAnalise.VALIDACAO.name(),
                TipoAcaoAnalise.ACEITE_MAPEAMENTO.name(),
                    "Aceite",
                    "123456789012",
                    "Usuário Teste",
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

            verify(subprocessoApresentacaoService).criarAnalise(eq(1L), any(), eq(TipoAnalise.VALIDACAO));
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

            when(subprocessoApresentacaoService.criarAnalise(eq(1L), any(), eq(TipoAnalise.CADASTRO)))
                    .thenThrow(new ErroValidacao("Parecer inválido"));

            mockMvc.perform(post("/api/subprocessos/1/analises-cadastro")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.message").value("Parecer inválido"));

            verify(subprocessoApresentacaoService).criarAnalise(eq(1L), any(), eq(TipoAnalise.CADASTRO));
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

    @Test
    @DisplayName("obterContextoCadastroAtividades - deve retornar contexto")
    @WithMockUser
    void obterContextoCadastroAtividades_Sucesso() throws Exception {
        Long cod = 1L;
        when(permissionEvaluator.hasPermission(any(), eq(cod), eq("Subprocesso"), any())).thenReturn(true);
        when(consultaService.obterContextoCadastroAtividades(cod)).thenReturn(mock(ContextoCadastroAtividadesResponse.class));

        mockMvc.perform(get("/api/subprocessos/" + cod + "/contexto-cadastro-atividades"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("disponibilizarCadastro - erro validacao")
    @WithMockUser
    void disponibilizarCadastroErro() throws Exception {
        doThrow(new ErroValidacao("Existem atividades sem conhecimentos associados."))
                .when(cadastroFluxoService).disponibilizarCadastro(1L);
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("DISPONIBILIZAR_CADASTRO"))).thenReturn(true);

        mockMvc.perform(post("/api/subprocessos/1/cadastro/disponibilizar").with(csrf()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Existem atividades sem conhecimentos associados."));

        verify(cadastroFluxoService).disponibilizarCadastro(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, cadastroFluxoService, unidadeService);
    }

    @Test
    @DisplayName("disponibilizarRevisao - erro validacao")
    @WithMockUser
    void disponibilizarRevisaoErro() throws Exception {
        doThrow(new ErroValidacao("Existem atividades sem conhecimentos associados."))
                .when(cadastroFluxoService).disponibilizarRevisao(1L);
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("DISPONIBILIZAR_REVISAO_CADASTRO"))).thenReturn(true);

        mockMvc.perform(post("/api/subprocessos/1/disponibilizar-revisao").with(csrf()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Existem atividades sem conhecimentos associados."));

        verify(cadastroFluxoService).disponibilizarRevisao(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, cadastroFluxoService, unidadeService);
    }

    @Test
    @DisplayName("obterMapaCompleto - erro generico")
    @WithMockUser
    void obterMapaCompletoErro() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("VISUALIZAR_SUBPROCESSO"))).thenReturn(true);
        when(consultaService.mapaCompletoDtoPorSubprocesso(1L)).thenThrow(new RuntimeException("erro"));

        mockMvc.perform(get("/api/subprocessos/1/mapa-completo"))
                .andExpect(status().isInternalServerError());

        verify(consultaService).mapaCompletoDtoPorSubprocesso(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("salvarMapaCompleto - deve persistir mapa completo")
    @WithMockUser
    void salvarMapaCompletoOk() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("EDITAR_MAPA"))).thenReturn(true);
        SalvarMapaRequest req = new SalvarMapaRequest(null, List.of());
        when(subprocessoService.salvarMapa(eq(1L), any())).thenReturn(new Mapa());

        mockMvc.perform(post("/api/subprocessos/1/mapa-completo")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(subprocessoService).salvarMapa(eq(1L), any());
        verify(consultaService).mapaCompletoDtoPorSubprocesso(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("disponibilizarMapaEmBloco - deve processar lista de subprocessos")
    @WithMockUser
    void disponibilizarMapaEmBlocoOk() throws Exception {
        ProcessarEmBlocoRequest req = new ProcessarEmBlocoRequest(List.of(1L), LocalDate.now());
        when(permissionEvaluator.hasPermission(any(), any(), eq("Subprocesso"), eq("DISPONIBILIZAR_MAPA"))).thenReturn(true);

        mockMvc.perform(post("/api/subprocessos/disponibilizar-mapa-bloco")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(transicaoService).disponibilizarMapaEmBloco(eq(List.of(1L)), any());
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("obterMapaParaAjuste - deve retornar payload de ajuste")
    @WithMockUser
    void obterMapaParaAjusteOk() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("AJUSTAR_MAPA"))).thenReturn(true);
        when(consultaService.obterMapaParaAjuste(1L)).thenReturn(MapaAjusteDto.builder().build());

        mockMvc.perform(get("/api/subprocessos/1/mapa-ajuste"))
                .andExpect(status().isOk());

        verify(consultaService).obterMapaParaAjuste(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("obterSugestoes - deve retornar sugestões consolidadas")
    @WithMockUser
    void obterSugestoesOk() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("VISUALIZAR_SUBPROCESSO"))).thenReturn(true);
        when(consultaService.obterSugestoes(1L)).thenReturn(new SugestoesDto("Sugestão consolidada"));

        mockMvc.perform(get("/api/subprocessos/1/sugestoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sugestoes").value("Sugestão consolidada"));

        verify(consultaService).obterSugestoes(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("criarAnaliseValidacao - deve registrar análise de validação")
    @WithMockUser(roles = {"GESTOR"})
    void criarAnaliseValidacaoOk() throws Exception {
        CriarAnaliseRequest req = new CriarAnaliseRequest("obs", "mot", sgc.subprocesso.model.TipoAcaoAnalise.ACEITE_MAPEAMENTO);
        AnaliseHistoricoDto resposta = new AnaliseHistoricoDto(
                TipoAnalise.VALIDACAO.name(),
                TipoAcaoAnalise.ACEITE_MAPEAMENTO.name(),
                "Aceite",
                "123456789012",
                "Usuário Teste",
                "SIGLA",
                "Unidade teste",
                LocalDateTime.now(),
                "mot",
                "obs");
        when(subprocessoApresentacaoService.criarAnalise(eq(1L), any(), eq(sgc.subprocesso.model.TipoAnalise.VALIDACAO))).thenReturn(resposta);

        mockMvc.perform(post("/api/subprocessos/1/analises-validacao")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("VALIDACAO"));

        verify(subprocessoApresentacaoService).criarAnalise(eq(1L), any(), eq(sgc.subprocesso.model.TipoAnalise.VALIDACAO));
    }

    @Test
    @DisplayName("criarAnaliseCadastro - deve registrar análise de cadastro")
    @WithMockUser(roles = {"GESTOR"})
    void criarAnaliseCadastroOk() throws Exception {
        CriarAnaliseRequest req = new CriarAnaliseRequest("obs", "mot", sgc.subprocesso.model.TipoAcaoAnalise.ACEITE_MAPEAMENTO);
        AnaliseHistoricoDto resposta = new AnaliseHistoricoDto(
                TipoAnalise.CADASTRO.name(),
                TipoAcaoAnalise.ACEITE_MAPEAMENTO.name(),
                "Aceite",
                "123456789012",
                "Usuário Teste",
                "SIGLA",
                "Unidade teste",
                LocalDateTime.now(),
                "mot",
                "obs");
        when(subprocessoApresentacaoService.criarAnalise(eq(1L), any(), eq(sgc.subprocesso.model.TipoAnalise.CADASTRO))).thenReturn(resposta);

        mockMvc.perform(post("/api/subprocessos/1/analises-cadastro")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("CADASTRO"));

        verify(subprocessoApresentacaoService).criarAnalise(eq(1L), any(), eq(sgc.subprocesso.model.TipoAnalise.CADASTRO));
    }

    @Test
    @DisplayName("obterMapaParaVisualizacao - deve retornar visão de leitura")
    @WithMockUser
    void obterMapaParaVisualizacaoOk() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("VISUALIZAR_SUBPROCESSO"))).thenReturn(true);
        when(consultaService.mapaParaVisualizacao(1L)).thenReturn(MapaVisualizacaoResponse.builder().build());

        mockMvc.perform(get("/api/subprocessos/1/mapa-visualizacao"))
                .andExpect(status().isOk());

        verify(consultaService).mapaParaVisualizacao(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("salvarAjustesMapa - deve aplicar ajustes recebidos")
    @WithMockUser
    void salvarAjustesMapaOk() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("EDITAR_MAPA"))).thenReturn(true);
        SalvarAjustesRequest req = new SalvarAjustesRequest(List.of(new CompetenciaAjusteDto(1L, "desc", List.of())));

        mockMvc.perform(post("/api/subprocessos/1/mapa-ajuste/atualizar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(subprocessoService).salvarAjustesMapa(eq(1L), any());
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("apresentarSugestoes - deve encaminhar justificativa ao fluxo")
    @WithMockUser
    void apresentarSugestoesOk() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("APRESENTAR_SUGESTOES"))).thenReturn(true);
        TextoRequest req = new TextoRequest("Sugestao");

        mockMvc.perform(post("/api/subprocessos/1/apresentar-sugestoes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(transicaoService).apresentarSugestoes(eq(1L), eq("Sugestao"));
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("obterHistoricoValidacao - deve retornar histórico de validação")
    @WithMockUser
    void obterHistoricoValidacaoOk() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("VISUALIZAR_SUBPROCESSO"))).thenReturn(true);
        when(consultaService.listarHistoricoValidacao(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/subprocessos/1/historico-validacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(consultaService).listarHistoricoValidacao(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("verificarImpactos - deve retornar impactos calculados")
    @WithMockUser
    void verificarImpactosOk() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("VERIFICAR_IMPACTOS"))).thenReturn(true);
        when(consultaService.verificarImpactos(1L)).thenReturn(ImpactoMapaResponse.builder().build());

        mockMvc.perform(get("/api/subprocessos/1/impactos-mapa"))
                .andExpect(status().isOk());

        verify(consultaService).verificarImpactos(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }
}
