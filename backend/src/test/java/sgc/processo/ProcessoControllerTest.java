package sgc.processo;

import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;
import sgc.comum.erros.*;
import sgc.organizacao.model.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;
import sgc.processo.service.*;
import sgc.seguranca.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProcessoController.class)
@Import(RestExceptionHandler.class)
@Tag("integration")
@DisplayName("ProcessoController")
class ProcessoControllerTest {
    protected static final String API_PROCESSOS = "/api/processos";
    protected static final String API_PROCESSOS_1 = "/api/processos/1";
    protected static final String API_PROCESSOS_999 = "/api/processos/999";
    protected static final String CODIGO_JSON_PATH = "$.codigo";
    protected static final String DESCRICAO_JSON_PATH = "$.descricao";
    protected static final String PROCESSO_ATUALIZADO = "Processo atualizado";
    private static final String NOVO_PROCESSO = "Novo processo";

    @MockitoBean
    private ProcessoService processoService;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubprocessoService subprocessoService;

    @MockitoBean
    private SgcPermissionEvaluator permissionEvaluator;

    private ObjectMapper objectMapper;

    @Captor
    private ArgumentCaptor<CriarProcessoRequest> criarCaptor;

    @Captor
    private ArgumentCaptor<AtualizarProcessoRequest> atualizarCaptor;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    @Nested
    @DisplayName("Criação de Processo")
    class Criacao {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar 201 Created quando processo é válido")
        void deveRetornarCreatedQuandoProcessoValido() throws Exception {

            var req = new CriarProcessoRequest(
                            NOVO_PROCESSO,
                            TipoProcesso.MAPEAMENTO,
                            LocalDateTime.now().plusDays(30),
                            List.of(1L));

            var processo = Processo.builder()
                            .codigo(1L)
                            .dataCriacao(LocalDateTime.now())
                            .descricao(NOVO_PROCESSO)
                            .situacao(SituacaoProcesso.CRIADO)
                            .tipo(TipoProcesso.MAPEAMENTO)
                            .build();

            when(processoService.criar(any(CriarProcessoRequest.class))).thenReturn(processo);

            mockMvc.perform(post(API_PROCESSOS)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith(API_PROCESSOS_1)))
                    .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L))
                    .andExpect(jsonPath(DESCRICAO_JSON_PATH).value(NOVO_PROCESSO));

            verify(processoService).criar(criarCaptor.capture());
            CriarProcessoRequest capturado = criarCaptor.getValue();
            assertEquals(NOVO_PROCESSO, capturado.descricao());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar 400 Bad request quando processo é inválido (descrição vazia)")
        void deveRetornarBadRequestQuandoProcessoInvalido() throws Exception {

            var req = new CriarProcessoRequest("", TipoProcesso.MAPEAMENTO, LocalDateTime.now().plusDays(30), List.of(1L));

            mockMvc.perform(post(API_PROCESSOS)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Consulta de Processo")
    class Consulta {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar 200 OK quando processo existe")
        void deveRetornarOkQuandoProcessoExiste() throws Exception {
            var processo = Processo.builder()
                            .codigo(1L)
                            .dataCriacao(LocalDateTime.now())
                            .descricao("Processo teste")
                            .situacao(SituacaoProcesso.CRIADO)
                            .tipo(TipoProcesso.MAPEAMENTO)
                            .build();

            when(processoService.buscarOpt(1L)).thenReturn(Optional.of(processo));

            mockMvc.perform(get(API_PROCESSOS_1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L))
                    .andExpect(jsonPath(DESCRICAO_JSON_PATH).value("Processo teste"));

            verify(processoService).buscarOpt(1L);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar detalhes do processo com 200 OK")
        void deveRetornarOkAoObterDetalhesQuandoProcessoExiste() throws Exception {
            var dto = ProcessoDetalheDto.builder()
                            .codigo(1L)
                            .descricao("Processo detalhado")
                            .tipo(TipoProcesso.MAPEAMENTO.name())
                            .situacao(SituacaoProcesso.CRIADO)
                            .dataCriacao(LocalDateTime.now())
                            .build();

            when(processoService.obterDetalhesCompleto(eq(1L), any(Usuario.class), anyBoolean())).thenReturn(dto);

            mockMvc.perform(get("/api/processos/1/detalhes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L))
                    .andExpect(jsonPath("$.descricao").value("Processo detalhado"));

            verify(processoService).obterDetalhesCompleto(eq(1L), any(Usuario.class), anyBoolean());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar 403 Forbidden ao obter detalhes se acesso negado")
        void deveRetornarForbiddenAoObterDetalhesQuandoAcessoNegado() throws Exception {
            doThrow(new ErroAcessoNegado("Acesso negado")).when(processoService).obterDetalhesCompleto(eq(1L), any(Usuario.class), anyBoolean());
            mockMvc.perform(get("/api/processos/1/detalhes")).andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar 404 Not found quando processo não existe")
        void deveRetornarNotFoundQuandoProcessoNaoExiste() throws Exception {
            when(processoService.buscarOpt(999L)).thenReturn(Optional.empty());

            mockMvc.perform(get(API_PROCESSOS_999))
                    .andExpect(status().isNotFound());

            verify(processoService).buscarOpt(999L);
        }
    }

    @Nested
    @DisplayName("Atualização de Processo")
    class Atualizacao {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar 200 OK ao atualizar processo existente")
        void deveRetornarOkAoAtualizarQuandoProcessoExiste() throws Exception {
            AtualizarProcessoRequest req = new AtualizarProcessoRequest(
                    1L,
                    PROCESSO_ATUALIZADO,
                    TipoProcesso.REVISAO,
                    LocalDateTime.now().plusDays(45),
                    List.of(1L));

            Processo processo = Processo.builder()
                    .codigo(1L)
                    .dataCriacao(LocalDateTime.now())
                    .descricao(PROCESSO_ATUALIZADO)
                    .situacao(SituacaoProcesso.CRIADO)
                    .tipo(TipoProcesso.REVISAO)
                    .build();

            when(processoService.atualizar(eq(1L), any(AtualizarProcessoRequest.class))).thenReturn(processo);

            mockMvc.perform(post(API_PROCESSOS + "/1/atualizar")
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L))
                    .andExpect(jsonPath(DESCRICAO_JSON_PATH).value(PROCESSO_ATUALIZADO));

            verify(processoService).atualizar(eq(1L), atualizarCaptor.capture());
            AtualizarProcessoRequest capturado = atualizarCaptor.getValue();
            assertEquals(PROCESSO_ATUALIZADO, capturado.descricao());
        }
    }

    @Nested
    @DisplayName("Workflow e Operações")
    class Workflow {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar 200 OK ao iniciar mapeamento com sucesso")
        void deveRetornarOkAoIniciarMapeamentoQuandoValido() throws Exception {
            IniciarProcessoRequest req = new IniciarProcessoRequest(TipoProcesso.MAPEAMENTO, List.of(1L));
            Processo processo = Processo.builder().codigo(1L).descricao("Processo teste").build();

            when(processoService.buscarPorCodigoComParticipantes(1L)).thenReturn(processo);
            when(processoService.iniciar(eq(1L), anyList(), any())).thenReturn(List.of());

            mockMvc.perform(
                            post("/api/processos/1/iniciar")
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.codigo").value(1L))
                    .andExpect(jsonPath("$.descricao").value("Processo teste"));

            verify(processoService).iniciar(eq(1L), eq(List.of(1L)), any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar 200 OK ao finalizar processo com sucesso")
        void deveRetornarOkAoFinalizarQuandoValido() throws Exception {
            doNothing().when(processoService).finalizar(1L);
            mockMvc.perform(post("/api/processos/1/finalizar").with(csrf())).andExpect(status().isOk());
            verify(processoService).finalizar(1L);
        }
    }

    @Nested
    @DisplayName("Listagens")
    class Listagens {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar lista de processos finalizados")
        void deveRetornarListaDeProcessosFinalizados() throws Exception {
            when(processoService.listarFinalizados())
                    .thenReturn(List.of(Processo.builder().codigo(1L).build()));

            mockMvc.perform(get("/api/processos/finalizados"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].codigo").value(1L));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar lista de subprocessos")
        void deveRetornarListaDeSubprocessos() throws Exception {

            when(subprocessoService.listarEntidadesPorProcesso(1L))
                    .thenReturn(List.of(Subprocesso.builder().codigo(10L).build()));

            mockMvc.perform(get("/api/processos/1/subprocessos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].codigo").value(10L));
        }
    }

    @Nested
    @DisplayName("Novas listagens e Status")
    class NovasListagens {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("listarParaImportacao deve retornar lista de processos para importacao")
        void deveListarParaImportacao() throws Exception {
            when(processoService.listarParaImportacao())
                    .thenReturn(List.of(Processo.builder().codigo(1L).build()));

            mockMvc.perform(get("/api/processos/para-importacao"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].codigo").value(1L));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("listarUnidadesParaImportacao deve retornar lista de participantes quando finalizado")
        void deveListarUnidadesParaImportacaoQuandoFinalizado() throws Exception {
            Processo processo = Processo.builder()
                .codigo(1L)
                .situacao(SituacaoProcesso.FINALIZADO)
                .build();

            Unidade unidade = Unidade.builder().codigo(10L).sigla("U1").nome("Unidade 1").situacao(SituacaoUnidade.ATIVA).build();
            processo.adicionarParticipantes(Set.of(unidade));

            when(processoService.buscarPorCodigoComParticipantes(1L)).thenReturn(processo);

            Subprocesso sub = Subprocesso.builder()
                .codigo(100L)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO)
                .unidade(unidade)
                .dataLimiteEtapa1(LocalDateTime.now())
                .build();
            when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sub));

            mockMvc.perform(get("/api/processos/1/unidades-importacao"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].codUnidade").value(10L))
                    .andExpect(jsonPath("$[0].codSubprocesso").value(100L));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("obterStatusUnidades deve retornar unidades desabilitadas")
        void deveObterStatusUnidades() throws Exception {
            when(processoService.listarUnidadesBloqueadasPorTipo(TipoProcesso.MAPEAMENTO)).thenReturn(List.of(1L, 2L));

            mockMvc.perform(get("/api/processos/status-unidades")
                            .param("tipo", "MAPEAMENTO"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.unidadesDesabilitadas").isArray())
                    .andExpect(jsonPath("$.unidadesDesabilitadas[0]").value(1L));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("listarAtivos deve retornar processos em andamento")
        void deveListarAtivos() throws Exception {
            when(processoService.listarAtivos()).thenReturn(List.of(Processo.builder().codigo(1L).build()));

            mockMvc.perform(get("/api/processos/ativos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].codigo").value(1L));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("obterContextoCompleto deve retornar detalhes do processo")
        void deveObterContextoCompleto() throws Exception {
            ProcessoDetalheDto dto = ProcessoDetalheDto.builder().codigo(1L).build();
            when(processoService.obterDetalhesCompleto(eq(1L), any(), eq(true))).thenReturn(dto);

            mockMvc.perform(get("/api/processos/1/contexto-completo"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.codigo").value(1L));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("listarUnidadesBloqueadas deve retornar lista")
        void deveListarUnidadesBloqueadas() throws Exception {
            when(processoService.listarUnidadesBloqueadasPorTipo(TipoProcesso.REVISAO)).thenReturn(List.of(10L));

            mockMvc.perform(get("/api/processos/unidades-bloqueadas")
                            .param("tipo", "REVISAO"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0]").value(10L));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("listarSubprocessosElegiveis deve retornar lista")
        void deveListarSubprocessosElegiveis() throws Exception {
            SubprocessoElegivelDto dto = SubprocessoElegivelDto.builder().codigo(1L).build();
            when(processoService.listarSubprocessosElegiveis(1L)).thenReturn(List.of(dto));

            mockMvc.perform(get("/api/processos/1/subprocessos-elegiveis"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].codigo").value(1L));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("enviarLembrete deve chamar facade")
        void deveEnviarLembrete() throws Exception {
            EnviarLembreteRequest req = new EnviarLembreteRequest(10L);
            mockMvc.perform(post("/api/processos/1/enviar-lembrete")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk());

            verify(processoService).enviarLembrete(1L, 10L);
        }
    }

    @Nested
    @DisplayName("Cobertura extra")
    class CoberturaExtra {
        @Test
        @DisplayName("listarUnidadesParaImportacao deve lancar ErroValidacao quando nao finalizado")
        void deveLancarErroValidacaoQuandoListarUnidadesParaImportacaoNaoFinalizado() {
            Processo processo = Processo.builder()
                .codigo(1L)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .build();
            when(processoServiceMock.buscarPorCodigoComParticipantes(1L)).thenReturn(processo);

            ErroValidacao ex = assertThrows(ErroValidacao.class, () -> controller.listarUnidadesParaImportacao(1L));
            assertEquals(sgc.comum.SgcMensagens.PROCESSO_DEVE_ESTAR_FINALIZADO, ex.getMessage());
        }

        @Test
        @DisplayName("excluir chama servico e retorna 204")
        void excluir_RetornaNoContent() {
            Long codigo = 1L;
            ResponseEntity<Void> response = controller.excluir(codigo);
            verify(processoServiceMock).apagar(codigo);
            assertEquals(204, response.getStatusCode().value());
        }

        // Usar mocks manuais para testes isolados
        private ProcessoController controller;
        private ProcessoService processoServiceMock;

        @BeforeEach
        void setUp() {
            processoServiceMock = mock(ProcessoService.class);
            SubprocessoService subprocessoServiceMock = mock(SubprocessoService.class);
            controller = new ProcessoController(processoServiceMock, subprocessoServiceMock);
        }

        @Test
        @DisplayName("Deve lançar ErroValidacao com status UNPROCESSABLE_CONTENT quando iniciar processo retorna lista de erros")
        void deveLancarErroValidacaoQuandoIniciarProcessoRetornaErros() {
            IniciarProcessoRequest req = new IniciarProcessoRequest(TipoProcesso.MAPEAMENTO, List.of(1L));
            when(processoServiceMock.iniciar(anyLong(), anyList(), any()))
                    .thenReturn(List.of("erro"));

            ErroValidacao ex = assertThrows(ErroValidacao.class, () -> controller.iniciar(1L, req, null));
            assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, ex.getStatus());
            assertEquals("erro", ex.getMessage());
        }

        @Test
        @DisplayName("executarAcaoEmBloco chama facade e retorna 200")
        void executarAcaoEmBloco_Sucesso() {
            Long codigo = 1L;
            AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(List.of(10L), AcaoProcesso.ACEITAR, LocalDate.now());
            ResponseEntity<Void> response = controller.executarAcaoEmBloco(codigo, req);

            verify(processoServiceMock).executarAcaoEmBloco(codigo, req);
            assertEquals(200, response.getStatusCode().value());
        }
    }
}
