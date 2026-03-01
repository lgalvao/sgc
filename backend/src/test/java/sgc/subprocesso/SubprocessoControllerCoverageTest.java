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

@WebMvcTest(SubprocessoController.class)
@Import(RestExceptionHandler.class)
@DisplayName("SubprocessoController - Cobertura")
class SubprocessoControllerCoverageTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    @MockitoBean
    private SubprocessoService subprocessoService;
    @MockitoBean
    private OrganizacaoFacade organizacaoFacade;
    @MockitoBean
    private SgcPermissionEvaluator permissionEvaluator;
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("listar - deve retornar lista e 200")
    @WithMockUser(roles = "ADMIN")
    void listar() throws Exception {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        when(subprocessoService.listarEntidades()).thenReturn(List.of(sp));

        mockMvc.perform(get("/api/subprocessos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value(1L));
    }

    @Test
    @DisplayName("obterStatus - deve retornar status e 200")
    @WithMockUser
    void obterStatus() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("VISUALIZAR_SUBPROCESSO"))).thenReturn(true);
        when(subprocessoService.obterStatus(1L)).thenReturn(SubprocessoSituacaoDto.builder().situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO).build());

        mockMvc.perform(get("/api/subprocessos/1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("MAPEAMENTO_MAPA_CRIADO"));
    }

    @Test
    @DisplayName("buscarPorProcessoEUnidade - deve retornar subprocesso")
    @WithMockUser
    void buscarPorProcessoEUnidade() throws Exception {
        sgc.organizacao.dto.UnidadeDto un = sgc.organizacao.dto.UnidadeDto.builder().codigo(2L).build();
        when(organizacaoFacade.buscarPorSigla("SIGLA")).thenReturn(un);
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        when(subprocessoService.obterEntidadePorProcessoEUnidade(1L, 2L)).thenReturn(sp);
        when(permissionEvaluator.hasPermission(any(), any(Subprocesso.class), eq("VISUALIZAR_SUBPROCESSO"))).thenReturn(true);

        mockMvc.perform(get("/api/subprocessos/buscar")
                        .param("codProcesso", "1")
                        .param("siglaUnidade", "SIGLA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(1L));
    }

    @Test
    @DisplayName("criar - deve chamar servico e retornar 201")
    @WithMockUser(roles = "ADMIN")
    void criar() throws Exception {
        CriarSubprocessoRequest req = new CriarSubprocessoRequest(1L, 10L, null, LocalDateTime.now(), LocalDateTime.now());
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);

        when(subprocessoService.criarEntidade(any())).thenReturn(sp);

        mockMvc.perform(post("/api/subprocessos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/subprocessos/100"));
    }

    @Test
    @DisplayName("atualizar - deve chamar servico e retornar 200")
    @WithMockUser(roles = "ADMIN")
    void atualizar() throws Exception {
        AtualizarSubprocessoRequest req = new AtualizarSubprocessoRequest(null, null, null, null, null, null);
        when(subprocessoService.atualizarEntidade(eq(1L), any())).thenReturn(new Subprocesso());

        mockMvc.perform(post("/api/subprocessos/1/atualizar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("excluir - deve chamar servico e retornar 204")
    @WithMockUser(roles = "ADMIN")
    void excluir() throws Exception {
        mockMvc.perform(post("/api/subprocessos/1/excluir")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(subprocessoService).excluir(1L);
    }

    @Test
    @DisplayName("alterarDataLimite - deve chamar servico e retornar 200")
    @WithMockUser(roles = "ADMIN")
    void alterarDataLimite() throws Exception {
        DataRequest req = new DataRequest(LocalDate.now());

        mockMvc.perform(post("/api/subprocessos/1/data-limite")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(subprocessoService).alterarDataLimite(eq(1L), any());
    }

    @Test
    @DisplayName("obterHistoricoCadastro - deve retornar lista e 200")
    @WithMockUser
    void obterHistoricoCadastro() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("VISUALIZAR_SUBPROCESSO"))).thenReturn(true);
        when(subprocessoService.listarHistoricoCadastro(1L)).thenReturn(List.of(
            new AnaliseHistoricoDto(sgc.subprocesso.model.TipoAnalise.CADASTRO, sgc.subprocesso.model.TipoAcaoAnalise.ACEITE_MAPEAMENTO, "obs", "nome", "justificativa", LocalDateTime.now(), "sigla", "unidade")
        ));

        mockMvc.perform(get("/api/subprocessos/1/historico-cadastro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("obterContextoEdicao - deve retornar contexto e 200")
    @WithMockUser
    void obterContextoEdicao() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("VISUALIZAR_SUBPROCESSO"))).thenReturn(true);
        when(subprocessoService.obterContextoEdicao(1L)).thenReturn(
            new ContextoEdicaoResponse(new sgc.organizacao.model.Unidade(), new Subprocesso(), null, null, List.of())
        );

        mockMvc.perform(get("/api/subprocessos/1/contexto-edicao"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("reabrirCadastro - deve chamar servico e retornar 200")
    @WithMockUser(roles = "ADMIN")
    void reabrirCadastro() throws Exception {
        JustificativaRequest req = new JustificativaRequest("Justificativa");

        mockMvc.perform(post("/api/subprocessos/1/reabrir-cadastro")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(subprocessoService).reabrirCadastro(eq(1L), eq("Justificativa"));
    }

    @Test
    @DisplayName("reabrirRevisaoCadastro - deve chamar servico e retornar 200")
    @WithMockUser(roles = "ADMIN")
    void reabrirRevisaoCadastro() throws Exception {
        JustificativaRequest req = new JustificativaRequest("Justificativa");

        mockMvc.perform(post("/api/subprocessos/1/reabrir-revisao-cadastro")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(subprocessoService).reabrirRevisaoCadastro(eq(1L), eq("Justificativa"));
    }

    @Test
    @DisplayName("validarCadastro - deve chamar servico e retornar 200")
    @WithMockUser
    void validarCadastro() throws Exception {
        when(subprocessoService.validarCadastro(1L)).thenReturn(new ValidacaoCadastroDto(true, List.of()));

        mockMvc.perform(get("/api/subprocessos/1/validar-cadastro"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("importarAtividades - deve chamar servico e retornar 200")
    @WithMockUser
    void importarAtividades() throws Exception {
        ImportarAtividadesRequest req = new ImportarAtividadesRequest(2L);

        mockMvc.perform(post("/api/subprocessos/1/importar-atividades")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(subprocessoService).importarAtividades(1L, 2L);
    }

    @Test
    @DisplayName("aceitarCadastroEmBloco - deve chamar servico e retornar 200")
    @WithMockUser
    void aceitarCadastroEmBloco() throws Exception {
        ProcessarEmBlocoRequest req = new ProcessarEmBlocoRequest("ACEITAR", List.of(1L, 2L), null);

        mockMvc.perform(post("/api/subprocessos/1/aceitar-cadastro-bloco")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(subprocessoService).aceitarCadastroEmBloco(anyList(), any());
    }

    @Test
    @DisplayName("salvarMapa - deve chamar servico e retornar 200")
    @WithMockUser
    void salvarMapa() throws Exception {
        SalvarMapaRequest req = new SalvarMapaRequest("Obs", List.of());
        when(subprocessoService.salvarMapa(eq(1L), any())).thenReturn(new Mapa());

        mockMvc.perform(post("/api/subprocessos/1/mapa")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("adicionarCompetencia - deve chamar servico e retornar 200")
    @WithMockUser
    void adicionarCompetencia() throws Exception {
        CompetenciaRequest req = new CompetenciaRequest("Comp", List.of(1L));
        when(subprocessoService.adicionarCompetencia(eq(1L), any())).thenReturn(new Mapa());

        mockMvc.perform(post("/api/subprocessos/1/competencia")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("atualizarCompetencia - deve chamar servico e retornar 200")
    @WithMockUser
    void atualizarCompetencia() throws Exception {
        CompetenciaRequest req = new CompetenciaRequest("Comp", List.of(1L));
        when(subprocessoService.atualizarCompetencia(eq(1L), eq(10L), any())).thenReturn(new Mapa());

        mockMvc.perform(post("/api/subprocessos/1/competencia/10")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("removerCompetencia - deve chamar servico e retornar 200")
    @WithMockUser
    void removerCompetencia() throws Exception {
        when(subprocessoService.removerCompetencia(1L, 10L)).thenReturn(new Mapa());

        mockMvc.perform(post("/api/subprocessos/1/competencia/10/remover")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("disponibilizarMapa - deve chamar servico e retornar 200")
    @WithMockUser
    void disponibilizarMapa() throws Exception {
        DisponibilizarMapaRequest req = new DisponibilizarMapaRequest(java.time.LocalDate.now().plusDays(10), "Obs");

        mockMvc.perform(post("/api/subprocessos/1/disponibilizar-mapa")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(subprocessoService).disponibilizarMapa(eq(1L), any(), any());
    }

    @Test
    @DisplayName("salvarMapaCompleto - deve chamar servico e retornar 200")
    @WithMockUser
    void salvarMapaCompleto() throws Exception {
        SalvarMapaRequest req = new SalvarMapaRequest("Obs", List.of());
        when(subprocessoService.salvarMapaSubprocesso(eq(1L), any())).thenReturn(new Mapa());

        mockMvc.perform(post("/api/subprocessos/1/mapa-completo")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("validarMapa - deve chamar servico e retornar 200")
    @WithMockUser
    void validarMapa() throws Exception {
        mockMvc.perform(post("/api/subprocessos/1/validar-mapa")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(subprocessoService).validarMapa(eq(1L), any());
    }

    @Test
    @DisplayName("devolverValidacao - deve chamar servico e retornar 200")
    @WithMockUser
    void devolverValidacao() throws Exception {
        JustificativaRequest req = new JustificativaRequest("Just");

        mockMvc.perform(post("/api/subprocessos/1/devolver-validacao")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(subprocessoService).devolverValidacao(eq(1L), any(), any());
    }

    @Test
    @DisplayName("aceitarValidacao - deve chamar servico e retornar 200")
    @WithMockUser
    void aceitarValidacao() throws Exception {
        mockMvc.perform(post("/api/subprocessos/1/aceitar-validacao")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(subprocessoService).aceitarValidacao(eq(1L), any());
    }

    @Test
    @DisplayName("homologarValidacao - deve chamar servico e retornar 200")
    @WithMockUser
    void homologarValidacao() throws Exception {
        mockMvc.perform(post("/api/subprocessos/1/homologar-validacao")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(subprocessoService).homologarValidacao(eq(1L), any());
    }

    @Test
    @DisplayName("submeterMapaAjustado - deve chamar servico e retornar 200")
    @WithMockUser
    void submeterMapaAjustado() throws Exception {
        SubmeterMapaAjustadoRequest req = new SubmeterMapaAjustadoRequest("Just", null, null);

        mockMvc.perform(post("/api/subprocessos/1/submeter-mapa-ajustado")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(subprocessoService).submeterMapaAjustado(eq(1L), any(), any());
    }

    @Test
    @DisplayName("aceitarValidacaoEmBloco - deve chamar servico e retornar 200")
    @WithMockUser
    void aceitarValidacaoEmBloco() throws Exception {
        ProcessarEmBlocoRequest req = new ProcessarEmBlocoRequest("ACEITAR", List.of(1L), null);

        mockMvc.perform(post("/api/subprocessos/1/aceitar-validacao-bloco")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(subprocessoService).aceitarValidacaoEmBloco(anyList(), any());
    }

    @Test
    @DisplayName("homologarValidacaoEmBloco - deve chamar servico e retornar 200")
    @WithMockUser
    void homologarValidacaoEmBloco() throws Exception {
        ProcessarEmBlocoRequest req = new ProcessarEmBlocoRequest("HOMOLOGAR", List.of(1L), null);

        mockMvc.perform(post("/api/subprocessos/1/homologar-validacao-bloco")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(subprocessoService).homologarValidacaoEmBloco(anyList(), any());
    }
}
