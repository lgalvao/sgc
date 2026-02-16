package sgc.subprocesso;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.dto.UnidadeDto;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubprocessoCrudController.class)
@Tag("unit")
@DisplayName("SubprocessoCrudController")
class SubprocessoCrudControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubprocessoFacade subprocessoFacade;

    @MockBean
    private OrganizacaoFacade organizacaoFacade;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    @DisplayName("obterPermissoes - Sucesso")
    void obterPermissoes() throws Exception {
        when(subprocessoFacade.obterPermissoes(1L)).thenReturn(SubprocessoPermissoesDto.builder().build());

        mockMvc.perform(get("/api/subprocessos/1/permissoes"))
                .andExpect(status().isOk());

        verify(subprocessoFacade).obterPermissoes(1L);
    }

    @Test
    @WithMockUser
    @DisplayName("validarCadastro - Sucesso")
    void validarCadastro() throws Exception {
        when(subprocessoFacade.validarCadastro(1L)).thenReturn(ValidacaoCadastroDto.builder().valido(true).build());

        mockMvc.perform(get("/api/subprocessos/1/validar-cadastro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true));
    }

    @Test
    @WithMockUser
    @DisplayName("obterStatus - Sucesso")
    void obterStatus() throws Exception {
        when(subprocessoFacade.obterSituacao(1L)).thenReturn(SubprocessoSituacaoDto.builder().build());

        mockMvc.perform(get("/api/subprocessos/1/status"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("listar - Sucesso")
    void listar() throws Exception {
        when(subprocessoFacade.listar()).thenReturn(List.of(Subprocesso.builder().codigo(1L).build()));

        mockMvc.perform(get("/api/subprocessos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("obterPorCodigo - Sucesso")
    void obterPorCodigo() throws Exception {
        when(subprocessoFacade.obterDetalhes(1L)).thenReturn(SubprocessoDetalheResponse.builder().build());

        mockMvc.perform(get("/api/subprocessos/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("buscarPorProcessoEUnidade - Sucesso")
    void buscarPorProcessoEUnidade() throws Exception {
        UnidadeDto unidade = UnidadeDto.builder().codigo(10L).build();
        when(organizacaoFacade.buscarUnidadePorSigla("U1")).thenReturn(unidade);
        when(subprocessoFacade.obterEntidadePorProcessoEUnidade(1L, 10L)).thenReturn(Subprocesso.builder().codigo(100L).build());

        mockMvc.perform(get("/api/subprocessos/buscar")
                        .param("codProcesso", "1")
                        .param("siglaUnidade", "U1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(100));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("criar - Sucesso")
    void criar() throws Exception {
        CriarSubprocessoRequest req = CriarSubprocessoRequest.builder().codProcesso(1L).codUnidade(10L).build();
        when(subprocessoFacade.criar(any())).thenReturn(Subprocesso.builder().codigo(100L).build());

        mockMvc.perform(post("/api/subprocessos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("atualizar - Sucesso")
    void atualizar() throws Exception {
        AtualizarSubprocessoRequest req = AtualizarSubprocessoRequest.builder().build();
        when(subprocessoFacade.atualizar(eq(1L), any())).thenReturn(Subprocesso.builder().codigo(1L).build());

        mockMvc.perform(post("/api/subprocessos/1/atualizar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("excluir - Sucesso")
    void excluir() throws Exception {
        mockMvc.perform(post("/api/subprocessos/1/excluir").with(csrf()))
                .andExpect(status().isNoContent());

        verify(subprocessoFacade).excluir(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("alterarDataLimite - Sucesso")
    void alterarDataLimite() throws Exception {
        AlterarDataLimiteRequest req = new AlterarDataLimiteRequest(LocalDate.now());

        mockMvc.perform(post("/api/subprocessos/1/data-limite")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(subprocessoFacade).alterarDataLimite(eq(1L), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("reabrirCadastro - Sucesso")
    void reabrirCadastro() throws Exception {
        ReabrirProcessoRequest req = new ReabrirProcessoRequest("J");

        mockMvc.perform(post("/api/subprocessos/1/reabrir-cadastro")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(subprocessoFacade).reabrirCadastro(1L, "J");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("reabrirRevisaoCadastro - Sucesso")
    void reabrirRevisaoCadastro() throws Exception {
        ReabrirProcessoRequest req = new ReabrirProcessoRequest("J");

        mockMvc.perform(post("/api/subprocessos/1/reabrir-revisao-cadastro")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(subprocessoFacade).reabrirRevisaoCadastro(1L, "J");
    }
}
