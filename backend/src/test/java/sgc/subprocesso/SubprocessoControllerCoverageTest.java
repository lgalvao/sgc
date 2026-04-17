package sgc.subprocesso;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.security.test.context.support.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.service.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubprocessoController.class)
@DisplayName("SubprocessoController - Cobertura de Testes")
class SubprocessoControllerCoverageTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubprocessoConsultaService consultaService;
    @MockitoBean
    private AnaliseHistoricoService analiseHistoricoService;
    @MockitoBean
    private SubprocessoService subprocessoService;
    @MockitoBean
    private SubprocessoTransicaoService transicaoService;
    @MockitoBean
    private sgc.organizacao.service.UnidadeService unidadeService;
    @MockitoBean
    private sgc.seguranca.SgcPermissionEvaluator permissionEvaluator;

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
}
