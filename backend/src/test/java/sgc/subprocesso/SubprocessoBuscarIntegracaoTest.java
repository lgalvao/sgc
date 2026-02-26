package sgc.subprocesso;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.security.test.context.support.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;
import sgc.comum.erros.*;
import sgc.organizacao.*;
import sgc.organizacao.dto.*;
import sgc.seguranca.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubprocessoController.class)
@Import(RestExceptionHandler.class)
@DisplayName("SubprocessoController - Busca Integração")
@Tag("integration")
class SubprocessoBuscarIntegracaoTest {
    @MockitoBean
    private SubprocessoService subprocessoService;

    @MockitoBean
    private OrganizacaoFacade organizacaoFacade;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SgcPermissionEvaluator permissionEvaluator;

    @Test
    @DisplayName("buscarPorProcessoEUnidade - Sucesso com retorno de subprocesso")
    @WithMockUser
    void buscarPorProcessoEUnidade_Sucesso() throws Exception {
        // Arrange
        Long codProcesso = 201L;
        String siglaUnidade = "SESEL";
        UnidadeDto unidade = UnidadeDto.builder().codigo(10L).sigla(siglaUnidade).build();
        Subprocesso sp = Subprocesso.builder()
                .codigo(100L)
                .situacao(SituacaoSubprocesso.NAO_INICIADO)
                .build();

        when(organizacaoFacade.buscarPorSigla(siglaUnidade)).thenReturn(unidade);
        when(subprocessoService.obterEntidadePorProcessoEUnidade(codProcesso, 10L)).thenReturn(sp);

        // Act & Assert
        mockMvc.perform(get("/api/subprocessos/buscar")
                        .param("codProcesso", codProcesso.toString())
                        .param("siglaUnidade", siglaUnidade))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(100));
    }
}
