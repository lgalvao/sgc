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

    @MockitoBean private SubprocessoService subprocessoService;
    @MockitoBean private OrganizacaoFacade organizacaoFacade;
    @MockitoBean private SgcPermissionEvaluator permissionEvaluator;

    @Autowired private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

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
