package sgc.processo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.comum.erros.ErroAcessoNegado;
import sgc.comum.erros.RestExceptionHandler;
import sgc.organizacao.model.Usuario;
import sgc.processo.dto.*;
import sgc.processo.model.AcaoProcesso;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoFacade;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProcessoController.class)
@Import(RestExceptionHandler.class)
@Tag("integration")
@DisplayName("ProcessoController")
class ProcessoControllerTest {
    private static final String NOVO_PROCESSO = "Novo Processo";
    protected static final String API_PROCESSOS = "/api/processos";
    protected static final String API_PROCESSOS_1 = "/api/processos/1";
    protected static final String API_PROCESSOS_999 = "/api/processos/999";
    protected static final String CODIGO_JSON_PATH = "$.codigo";
    protected static final String DESCRICAO_JSON_PATH = "$.descricao";
    protected static final String PROCESSO_ATUALIZADO = "Processo Atualizado";
    protected static final String PROCESSO_NAO_ENCONTRADO = "Processo não encontrado";
    
    @MockitoBean
    private ProcessoFacade processoFacade;

    @Autowired
    private MockMvc mockMvc;

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
            // Arrange
            var req =
                    new CriarProcessoRequest(
                            NOVO_PROCESSO,
                            TipoProcesso.MAPEAMENTO,
                            LocalDateTime.now().plusDays(30),
                            List.of(1L));
            var dto =
                    ProcessoDto.builder()
                            .codigo(1L)
                            .dataCriacao(LocalDateTime.now())
                            .descricao(NOVO_PROCESSO)
                            .situacao(SituacaoProcesso.CRIADO.name())
                            .tipo(TipoProcesso.MAPEAMENTO.name())
                            .build();

            when(processoFacade.criar(any(CriarProcessoRequest.class))).thenReturn(dto);

            // Act & Assert
            mockMvc.perform(
                            post(API_PROCESSOS)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", API_PROCESSOS_1))
                    .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L))
                    .andExpect(jsonPath(DESCRICAO_JSON_PATH).value(NOVO_PROCESSO));

            verify(processoFacade).criar(criarCaptor.capture());
            CriarProcessoRequest capturado = criarCaptor.getValue();
            assertEquals(NOVO_PROCESSO, capturado.descricao());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar 400 Bad Request quando processo é inválido (descrição vazia)")
        void deveRetornarBadRequestQuandoProcessoInvalido() throws Exception {
            // Arrange
            var req =
                    new CriarProcessoRequest(
                            "", TipoProcesso.MAPEAMENTO, LocalDateTime.now().plusDays(30), List.of(1L));

            // Act & Assert
            mockMvc.perform(
                            post(API_PROCESSOS)
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
            // Arrange
            var dto =
                    ProcessoDto.builder()
                            .codigo(1L)
                            .dataCriacao(LocalDateTime.now())
                            .descricao("Processo Teste")
                            .situacao(SituacaoProcesso.CRIADO.name())
                            .tipo(TipoProcesso.MAPEAMENTO.name())
                            .build();

            when(processoFacade.obterPorId(1L)).thenReturn(Optional.of(dto));

            // Act & Assert
            mockMvc.perform(get(API_PROCESSOS_1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L))
                    .andExpect(jsonPath(DESCRICAO_JSON_PATH).value("Processo Teste"));

            verify(processoFacade).obterPorId(1L);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar detalhes do processo com 200 OK")
        void deveRetornarOkAoObterDetalhesQuandoProcessoExiste() throws Exception {
            // Arrange
            var dto =
                    ProcessoDetalheDto.builder()
                            .codigo(1L)
                            .descricao("Processo Detalhado")
                            .tipo(TipoProcesso.MAPEAMENTO.name())
                            .situacao(SituacaoProcesso.CRIADO)
                            .dataCriacao(LocalDateTime.now())
                            .build();

            when(processoFacade.obterDetalhes(eq(1L), any(Usuario.class))).thenReturn(dto);

            // Act & Assert
            mockMvc.perform(get("/api/processos/1/detalhes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L))
                    .andExpect(jsonPath("$.descricao").value("Processo Detalhado"));

            verify(processoFacade).obterDetalhes(eq(1L), any(Usuario.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar 403 Forbidden ao obter detalhes se acesso negado")
        void deveRetornarForbiddenAoObterDetalhesQuandoAcessoNegado() throws Exception {
            // Arrange
            doThrow(new ErroAcessoNegado("Acesso negado")).when(processoFacade).obterDetalhes(eq(1L), any(Usuario.class));

            // Act & Assert
            mockMvc.perform(get("/api/processos/1/detalhes")).andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar 404 Not Found quando processo não existe")
        void deveRetornarNotFoundQuandoProcessoNaoExiste() throws Exception {
            // Arrange
            when(processoFacade.obterPorId(999L)).thenReturn(Optional.empty());

            // Act & Assert
            mockMvc.perform(get(API_PROCESSOS_999))
                    .andExpect(status().isNotFound());

            verify(processoFacade).obterPorId(999L);
        }
    }

    @Nested
    @DisplayName("Atualização de Processo")
    class Atualizacao {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar 200 OK ao atualizar processo existente")
        void deveRetornarOkAoAtualizarQuandoProcessoExiste() throws Exception {
            // Arrange
            var req =
                    new AtualizarProcessoRequest(
                            1L,
                            PROCESSO_ATUALIZADO,
                            TipoProcesso.REVISAO,
                            LocalDateTime.now().plusDays(45),
                            List.of(1L));
            var dto =
                    ProcessoDto.builder()
                            .codigo(1L)
                            .dataCriacao(LocalDateTime.now())
                            .descricao(PROCESSO_ATUALIZADO)
                            .situacao(SituacaoProcesso.CRIADO.name())
                            .tipo(TipoProcesso.REVISAO.name())
                            .build();

            when(processoFacade.atualizar(eq(1L), any(AtualizarProcessoRequest.class))).thenReturn(dto);

            // Act & Assert
            mockMvc.perform(
                            post(API_PROCESSOS + "/1/atualizar")
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L))
                    .andExpect(jsonPath(DESCRICAO_JSON_PATH).value(PROCESSO_ATUALIZADO));

            verify(processoFacade).atualizar(eq(1L), atualizarCaptor.capture());
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
            // Arrange
            var req = new IniciarProcessoRequest(TipoProcesso.MAPEAMENTO, List.of(1L));
            var processo = ProcessoDto.builder().codigo(1L).descricao("Processo Teste").build();

            when(processoFacade.obterDtoPorId(1L)).thenReturn(processo);
            when(processoFacade.iniciarProcessoMapeamento(eq(1L), anyList())).thenReturn(List.of());

            // Act & Assert
            mockMvc.perform(
                            post("/api/processos/1/iniciar")
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.codigo").value(1L))
                    .andExpect(jsonPath("$.descricao").value("Processo Teste"));

            verify(processoFacade).iniciarProcessoMapeamento(1L, List.of(1L));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar 200 OK ao finalizar processo com sucesso")
        void deveRetornarOkAoFinalizarQuandoValido() throws Exception {
            // Arrange
            doNothing().when(processoFacade).finalizar(1L);

            // Act & Assert
            mockMvc.perform(post("/api/processos/1/finalizar").with(csrf())).andExpect(status().isOk());

            verify(processoFacade).finalizar(1L);
        }
    }

    @Nested
    @DisplayName("Listagens")
    class Listagens {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar lista de processos finalizados")
        void deveRetornarListaDeProcessosFinalizados() throws Exception {
            // Arrange
            when(processoFacade.listarFinalizados())
                    .thenReturn(List.of(ProcessoDto.builder().codigo(1L).build()));

            // Act & Assert
            mockMvc.perform(get("/api/processos/finalizados"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].codigo").value(1L));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar lista de subprocessos")
        void deveRetornarListaDeSubprocessos() throws Exception {
            // Arrange
            when(processoFacade.listarEntidadesSubprocessos(1L))
                    .thenReturn(
                            List.of(Subprocesso.builder().codigo(10L).build()));

            // Act & Assert
            mockMvc.perform(get("/api/processos/1/subprocessos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].codigo").value(10L));
        }
    }

    @Nested
    @DisplayName("Novas Listagens e Status")
    class NovasListagens {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("obterStatusUnidades deve retornar unidades desabilitadas")
        void deveObterStatusUnidades() throws Exception {
            when(processoFacade.listarUnidadesBloqueadasPorTipo("MAPEAMENTO")).thenReturn(List.of(1L, 2L));

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
            when(processoFacade.listarAtivos()).thenReturn(List.of(ProcessoDto.builder().codigo(1L).build()));

            mockMvc.perform(get("/api/processos/ativos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].codigo").value(1L));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("obterContextoCompleto deve retornar detalhes do processo")
        void deveObterContextoCompleto() throws Exception {
            ProcessoDetalheDto dto = ProcessoDetalheDto.builder().codigo(1L).build();
            when(processoFacade.obterContextoCompleto(eq(1L), any())).thenReturn(dto);

            mockMvc.perform(get("/api/processos/1/contexto-completo"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.codigo").value(1L));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("listarUnidadesBloqueadas deve retornar lista")
        void deveListarUnidadesBloqueadas() throws Exception {
            when(processoFacade.listarUnidadesBloqueadasPorTipo("REVISAO")).thenReturn(List.of(10L));

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
            when(processoFacade.listarSubprocessosElegiveis(1L)).thenReturn(List.of(dto));

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

            verify(processoFacade).enviarLembrete(1L, 10L);
        }
    }

    @Nested
    @DisplayName("Cobertura Extra")
    class CoberturaExtra {
        
        // Usar mocks manuais para testes isolados
        private ProcessoController controller;
        private ProcessoFacade processoFacadeMock;

        @BeforeEach
        void setUp() {
            processoFacadeMock = mock(ProcessoFacade.class);
            controller = new ProcessoController(processoFacadeMock);
        }

        @Test
        @DisplayName("Deve retornar bad request quando iniciar processo retorna lista de erros")
        void deveRetornarBadRequestQuandoIniciarProcessoRetornaErros() {
            IniciarProcessoRequest req = new IniciarProcessoRequest(TipoProcesso.MAPEAMENTO, List.of(1L));
            when(processoFacadeMock.iniciarProcessoMapeamento(anyLong(), anyList()))
                .thenReturn(List.of("erro"));

            ResponseEntity<Object> response = controller.iniciar(1L, req);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("executarAcaoEmBloco chama facade e retorna 200")
        void executarAcaoEmBloco_Sucesso() {
            Long codigo = 1L;
            AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(List.of(10L), AcaoProcesso.ACEITAR, LocalDate.now());

            ResponseEntity<Void> response = controller.executarAcaoEmBloco(codigo, req);

            verify(processoFacadeMock).executarAcaoEmBloco(codigo, req);
            assertEquals(200, response.getStatusCode().value());
        }
    }
}
