package sgc.processo;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.RestExceptionHandler;
import sgc.processo.dto.*;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoFacade;
import tools.jackson.databind.ObjectMapper;

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
public class ProcessoControllerTest {
    private static final String NOVO_PROCESSO = "Novo Processo";
    private static final String API_PROCESSOS = "/api/processos";
    private static final String API_PROCESSOS_1 = "/api/processos/1";
    private static final String API_PROCESSOS_999 = "/api/processos/999";
    private static final String CODIGO_JSON_PATH = "$.codigo";
    private static final String DESCRICAO_JSON_PATH = "$.descricao";
    private static final String PROCESSO_ATUALIZADO = "Processo Atualizado";
    private static final String PROCESSO_NAO_ENCONTRADO = "Processo não encontrado";

    @MockitoBean
    private ProcessoFacade processoFacade;

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @org.mockito.Captor
    private org.mockito.ArgumentCaptor<CriarProcessoReq> criarCaptor;

    @org.mockito.Captor
    private org.mockito.ArgumentCaptor<AtualizarProcessoReq> atualizarCaptor;

    @BeforeEach
    void setUp() {
        org.mockito.MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("Criação de Processo")
    class Criacao {
        @Test
        @WithMockUser
        @DisplayName("Deve retornar 201 Created quando processo é válido")
        void deveRetornarCreatedQuandoProcessoValido() throws Exception {
            // Arrange
            var req =
                    new CriarProcessoReq(
                            NOVO_PROCESSO,
                            TipoProcesso.MAPEAMENTO,
                            LocalDateTime.now().plusDays(30),
                            List.of(1L));
            var dto =
                    ProcessoDto.builder()
                            .codigo(1L)
                            .dataCriacao(LocalDateTime.now())
                            .descricao(NOVO_PROCESSO)
                            .situacao(SituacaoProcesso.CRIADO)
                            .tipo(TipoProcesso.MAPEAMENTO.name())
                            .build();

            when(processoFacade.criar(any(CriarProcessoReq.class))).thenReturn(dto);

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
            CriarProcessoReq capturado = criarCaptor.getValue();
            assertEquals(NOVO_PROCESSO, capturado.getDescricao());
        }

        @Test
        @WithMockUser
        @DisplayName("Deve retornar 400 Bad Request quando processo é inválido (descrição vazia)")
        void deveRetornarBadRequestQuandoProcessoInvalido() throws Exception {
            // Arrange
            var req =
                    new CriarProcessoReq(
                            "", TipoProcesso.MAPEAMENTO, LocalDateTime.now().plusDays(30), List.of(1L));

            // Act & Assert
            mockMvc.perform(
                            post(API_PROCESSOS)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("Deve retornar 400 Bad Request quando data limite é nula")
        void deveRetornarBadRequestQuandoDataLimiteNull() throws Exception {
            // Arrange
            var req = new CriarProcessoReq(NOVO_PROCESSO, TipoProcesso.MAPEAMENTO, null, List.of(1L));

            // Act & Assert
            mockMvc.perform(
                            post(API_PROCESSOS)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("Deve retornar 400 Bad Request quando data limite está no passado")
        void deveRetornarBadRequestQuandoDataLimiteNoPassado() throws Exception {
            // Arrange
            var req =
                    new CriarProcessoReq(
                            NOVO_PROCESSO,
                            TipoProcesso.MAPEAMENTO,
                            LocalDateTime.now().minusDays(1),
                            List.of(1L));

            // Act & Assert
            mockMvc.perform(
                            post(API_PROCESSOS)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("Deve retornar 400 Bad Request quando lista de unidades é vazia")
        void deveRetornarBadRequestQuandoListaUnidadesVazia() throws Exception {
            // Arrange
            var req =
                    new CriarProcessoReq(
                            NOVO_PROCESSO,
                            TipoProcesso.MAPEAMENTO,
                            LocalDateTime.now().plusDays(30),
                            List.of());

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
        @WithMockUser
        @DisplayName("Deve retornar 200 OK quando processo existe")
        void deveRetornarOkQuandoProcessoExiste() throws Exception {
            // Arrange
            var dto =
                    ProcessoDto.builder()
                            .codigo(1L)
                            .dataCriacao(LocalDateTime.now())
                            .descricao("Processo Teste")
                            .situacao(SituacaoProcesso.CRIADO)
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
        @WithMockUser
        @DisplayName("Deve retornar 404 Not Found quando processo não existe")
        void deveRetornarNotFoundQuandoProcessoNaoExiste() throws Exception {
            // Arrange
            when(processoFacade.obterPorId(999L)).thenReturn(Optional.empty());

            // Act & Assert
            mockMvc.perform(get(API_PROCESSOS_999)).andExpect(status().isNotFound());

            verify(processoFacade).obterPorId(999L);
        }

        @Test
        @WithMockUser
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

            when(processoFacade.obterDetalhes(eq(1L))).thenReturn(dto);

            // Act & Assert
            mockMvc.perform(get("/api/processos/1/detalhes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L))
                    .andExpect(jsonPath("$.descricao").value("Processo Detalhado"));

            verify(processoFacade).obterDetalhes(eq(1L));
        }

        @Test
        @WithMockUser
        @DisplayName("Deve retornar 404 Not Found ao obter detalhes se processo não encontrado")
        void deveRetornarNotFoundAoObterDetalhesQuandoProcessoNaoEncontrado() throws Exception {
            // Arrange
            doThrow(new ErroEntidadeNaoEncontrada(PROCESSO_NAO_ENCONTRADO))
                    .when(processoFacade)
                    .obterDetalhes(eq(999L));

            // Act & Assert
            mockMvc.perform(get("/api/processos/999/detalhes")).andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser
        @DisplayName("Deve retornar 403 Forbidden ao obter detalhes se acesso negado")
        void deveRetornarForbiddenAoObterDetalhesQuandoAcessoNegado() throws Exception {
            // Arrange
            doThrow(new ErroAccessoNegado("Acesso negado")).when(processoFacade).obterDetalhes(eq(1L));

            // Act & Assert
            mockMvc.perform(get("/api/processos/1/detalhes")).andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Atualização de Processo")
    class Atualizacao {
        @Test
        @WithMockUser
        @DisplayName("Deve retornar 200 OK ao atualizar processo existente")
        void deveRetornarOkAoAtualizarQuandoProcessoExiste() throws Exception {
            // Arrange
            var req =
                    new AtualizarProcessoReq(
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
                            .situacao(SituacaoProcesso.CRIADO)
                            .tipo(TipoProcesso.REVISAO.name())
                            .build();

            when(processoFacade.atualizar(eq(1L), any(AtualizarProcessoReq.class))).thenReturn(dto);

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
            AtualizarProcessoReq capturado = atualizarCaptor.getValue();
            assertEquals(PROCESSO_ATUALIZADO, capturado.getDescricao());
        }

        @Test
        @WithMockUser
        @DisplayName("Deve retornar 404 Not Found ao atualizar se processo não encontrado")
        void deveRetornarNotFoundAoAtualizarQuandoProcessoNaoEncontrado() throws Exception {
            // Arrange
            var req =
                    new AtualizarProcessoReq(
                            999L,
                            "Teste",
                            TipoProcesso.MAPEAMENTO,
                            LocalDateTime.now().plusDays(30),
                            List.of(1L));

            doThrow(new ErroEntidadeNaoEncontrada(PROCESSO_NAO_ENCONTRADO))
                    .when(processoFacade)
                    .atualizar(eq(999L), any(AtualizarProcessoReq.class));

            // Act & Assert
            mockMvc.perform(
                            post(API_PROCESSOS + "/999/atualizar")
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser
        @DisplayName("Deve retornar 409 Conflict ao atualizar se estado inválido")
        void deveRetornarConflictAoAtualizarQuandoEstadoInvalido() throws Exception {
            // Arrange
            var req =
                    new AtualizarProcessoReq(
                            1L,
                            "Teste",
                            TipoProcesso.MAPEAMENTO,
                            LocalDateTime.now().plusDays(30),
                            List.of(1L));

            doThrow(new IllegalStateException())
                    .when(processoFacade)
                    .atualizar(eq(1L), any(AtualizarProcessoReq.class));

            // Act & Assert
            mockMvc.perform(
                            post(API_PROCESSOS + "/1/atualizar")
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("Exclusão de Processo")
    class Exclusao {
        @Test
        @WithMockUser
        @DisplayName("Deve retornar 204 No Content ao excluir processo existente")
        void deveRetornarNoContentAoExcluirQuandoProcessoExiste() throws Exception {
            // Act & Assert
            mockMvc.perform(post(API_PROCESSOS + "/1/excluir").with(csrf()))
                    .andExpect(status().isNoContent());

            verify(processoFacade).apagar(1L);
        }

        @Test
        @WithMockUser
        @DisplayName("Deve retornar 404 Not Found ao excluir se processo não encontrado")
        void deveRetornarNotFoundAoExcluirQuandoProcessoNaoEncontrado() throws Exception {
            // Arrange
            doThrow(new ErroEntidadeNaoEncontrada(PROCESSO_NAO_ENCONTRADO))
                    .when(processoFacade)
                    .apagar(999L);

            // Act & Assert
            mockMvc.perform(post(API_PROCESSOS + "/999/excluir").with(csrf()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser
        @DisplayName("Deve retornar 409 Conflict ao excluir se estado inválido")
        void deveRetornarConflictAoExcluirQuandoEstadoInvalido() throws Exception {
            // Arrange
            doThrow(new IllegalStateException()).when(processoFacade).apagar(eq(1L));

            // Act & Assert
            mockMvc.perform(post(API_PROCESSOS + "/1/excluir").with(csrf()))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("Workflow e Operações")
    class Workflow {
        @Test
        @WithMockUser
        @DisplayName("Deve retornar 200 OK ao iniciar mapeamento com sucesso")
        void deveRetornarOkAoIniciarMapeamentoQuandoValido() throws Exception {
            // Arrange
            var req = new IniciarProcessoReq(TipoProcesso.MAPEAMENTO, List.of(1L));
            var processo = ProcessoDto.builder().codigo(1L).descricao("Processo Teste").build();

            when(processoFacade.obterPorId(1L)).thenReturn(Optional.of(processo));
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

            verify(processoFacade).iniciarProcessoMapeamento(eq(1L), eq(List.of(1L)));
        }

        @Test
        @WithMockUser
        @DisplayName("Deve retornar 200 OK ao iniciar revisão com sucesso")
        void deveRetornarOkAoIniciarRevisaoQuandoValido() throws Exception {
            // Arrange
            var req = new IniciarProcessoReq(TipoProcesso.REVISAO, List.of(1L));
            var processo = ProcessoDto.builder().codigo(1L).descricao("Processo Teste").build();

            when(processoFacade.obterPorId(1L)).thenReturn(Optional.of(processo));
            when(processoFacade.iniciarProcessoRevisao(eq(1L), anyList())).thenReturn(List.of());

            // Act & Assert
            mockMvc.perform(
                            post(API_PROCESSOS_1 + "/iniciar")
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.codigo").value(1L))
                    .andExpect(jsonPath("$.descricao").value("Processo Teste"));

            verify(processoFacade).iniciarProcessoRevisao(eq(1L), eq(List.of(1L)));
        }

        @Test
        @WithMockUser
        @DisplayName("Deve retornar 400 Bad Request ao iniciar processo com requisição inválida")
        void deveRetornarBadRequestAoIniciarProcessoQuandoInvalido() throws Exception {
            // Arrange
            var req = new IniciarProcessoReq(null, List.of(1L));

            // Act & Assert
            mockMvc.perform(
                            post("/api/processos/999/iniciar")
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("Deve retornar 200 OK ao finalizar processo com sucesso")
        void deveRetornarOkAoFinalizarQuandoValido() throws Exception {
            // Arrange
            doNothing().when(processoFacade).finalizar(1L);

            // Act & Assert
            mockMvc.perform(post("/api/processos/1/finalizar").with(csrf())).andExpect(status().isOk());

            verify(processoFacade).finalizar(1L);
        }

        @Test
        @WithMockUser
        @DisplayName("Deve retornar 404 Not Found ao finalizar se processo não encontrado")
        void deveRetornarNotFoundAoFinalizarQuandoProcessoNaoEncontrado() throws Exception {
            // Arrange
            doThrow(new ErroEntidadeNaoEncontrada(PROCESSO_NAO_ENCONTRADO))
                    .when(processoFacade)
                    .finalizar(999L);

            // Act & Assert
            mockMvc.perform(post(API_PROCESSOS_999 + "/finalizar").with(csrf()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser
        @DisplayName("Deve retornar 409 Conflict ao finalizar se estado inválido")
        void deveRetornarConflictAoFinalizarQuandoEstadoInvalido() throws Exception {
            // Arrange
            doThrow(new IllegalStateException("Processo em estado inválido"))
                    .when(processoFacade)
                    .finalizar(1L);

            // Act & Assert
            mockMvc.perform(post("/api/processos/1/finalizar").with(csrf()))
                    .andExpect(status().isConflict())
                    .andExpect(
                            jsonPath("$.message")
                                    .value(
                                            "A operação não pode ser executada no estado atual do"
                                                    + " recurso."));
        }

        @Test
        @WithMockUser
        @DisplayName("Deve retornar 409 Conflict ao finalizar se validação falhar")
        void deveRetornarConflictAoFinalizarQuandoValidacaoFalha() throws Exception {
            // Arrange
            doThrow(new ErroProcesso("Subprocessos não homologados"))
                    .when(processoFacade)
                    .finalizar(1L);

            // Act & Assert
            mockMvc.perform(post("/api/processos/1/finalizar").with(csrf()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Subprocessos não homologados"));
        }
    }

    @Nested
    @DisplayName("Listagens")
    class Listagens {
        @Test
        @WithMockUser
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
        @WithMockUser
        @DisplayName("Deve retornar lista de processos ativos")
        void deveRetornarListaDeProcessosAtivos() throws Exception {
            // Arrange
            when(processoFacade.listarAtivos())
                    .thenReturn(List.of(ProcessoDto.builder().codigo(1L).build()));

            // Act & Assert
            mockMvc.perform(get("/api/processos/ativos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].codigo").value(1L));
        }

        @Test
        @WithMockUser
        @DisplayName("Deve retornar map de status de unidades")
        void deveRetornarMapDeStatusUnidades() throws Exception {
            // Arrange
            when(processoFacade.listarUnidadesBloqueadasPorTipo("MAPEAMENTO"))
                    .thenReturn(List.of(100L));

            // Act & Assert
            mockMvc.perform(get("/api/processos/status-unidades").param("tipo", "MAPEAMENTO"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.unidadesDesabilitadas").isArray())
                    .andExpect(jsonPath("$.unidadesDesabilitadas[0]").value(100L));
        }

        @Test
        @WithMockUser
        @DisplayName("Deve retornar lista de unidades bloqueadas")
        void deveRetornarListaDeUnidadesBloqueadas() throws Exception {
            // Arrange
            when(processoFacade.listarUnidadesBloqueadasPorTipo("MAPEAMENTO"))
                    .thenReturn(List.of(100L));

            // Act & Assert
            mockMvc.perform(get("/api/processos/unidades-bloqueadas").param("tipo", "MAPEAMENTO"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0]").value(100L));
        }

        @Test
        @WithMockUser
        @DisplayName("Deve retornar lista de subprocessos elegíveis")
        void deveRetornarListaDeSubprocessosElegiveis() throws Exception {
            // Arrange
            when(processoFacade.listarSubprocessosElegiveis(1L))
                    .thenReturn(
                            List.of(
                                    sgc.processo.dto.SubprocessoElegivelDto.builder()
                                            .codSubprocesso(10L)
                                            .build()));

            // Act & Assert
            mockMvc.perform(get("/api/processos/1/subprocessos-elegiveis"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].codSubprocesso").value(10L));
        }

        @Test
        @WithMockUser
        @DisplayName("Deve retornar lista de subprocessos")
        void deveRetornarListaDeSubprocessos() throws Exception {
            // Arrange
            when(processoFacade.listarTodosSubprocessos(1L))
                    .thenReturn(
                            List.of(sgc.subprocesso.dto.SubprocessoDto.builder().codigo(10L).build()));

            // Act & Assert
            mockMvc.perform(get("/api/processos/1/subprocessos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].codigo").value(10L));
        }
    }

    @Nested
    @DisplayName("Cobertura de Branches Adicionais")
    class CoberturaBranches {

        @Test
        @WithMockUser
        @DisplayName("Deve retornar 200 OK ao iniciar diagnóstico com sucesso")
        void deveRetornarOkAoIniciarDiagnosticoQuandoValido() throws Exception {
            // Arrange
            var req = new IniciarProcessoReq(TipoProcesso.DIAGNOSTICO, List.of(1L));
            var processo = ProcessoDto.builder().codigo(1L).descricao("Processo Diagnóstico").build();

            when(processoFacade.obterPorId(1L)).thenReturn(Optional.of(processo));
            when(processoFacade.iniciarProcessoDiagnostico(eq(1L), anyList())).thenReturn(List.of());

            // Act & Assert
            mockMvc.perform(
                            post("/api/processos/1/iniciar")
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.codigo").value(1L))
                    .andExpect(jsonPath("$.descricao").value("Processo Diagnóstico"));

            verify(processoFacade).iniciarProcessoDiagnostico(eq(1L), eq(List.of(1L)));
        }

        @Test
        @WithMockUser
        @DisplayName("Deve retornar 400 Bad Request quando iniciar processo retorna erros")
        void deveRetornarBadRequestQuandoIniciarRetornaErros() throws Exception {
            // Arrange
            var req = new IniciarProcessoReq(TipoProcesso.MAPEAMENTO, List.of(1L));

            when(processoFacade.iniciarProcessoMapeamento(eq(1L), anyList()))
                    .thenReturn(List.of("Erro 1", "Erro 2"));

            // Act & Assert
            mockMvc.perform(
                            post("/api/processos/1/iniciar")
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.erros").isArray())
                    .andExpect(jsonPath("$.erros[0]").value("Erro 1"))
                    .andExpect(jsonPath("$.erros[1]").value("Erro 2"));
        }

        @Test
        @WithMockUser
        @DisplayName("Deve retornar contexto completo com sucesso")
        void deveRetornarContextoCompletoComSucesso() throws Exception {
            // Arrange
            ProcessoDetalheDto detalhe = ProcessoDetalheDto.builder().codigo(1L).build();
            when(processoFacade.obterContextoCompleto(1L))
                    .thenReturn(ProcessoContextoDto.builder().processo(detalhe).build());

            // Act & Assert
            mockMvc.perform(get("/api/processos/1/contexto-completo"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.processo.codigo").value(1L));
        }
    }
}
