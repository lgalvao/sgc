package sgc.subprocesso;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.comum.erros.RestExceptionHandler;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.dto.UnidadeDto;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubprocessoCrudController.class)
@Import(RestExceptionHandler.class)
@DisplayName("SubprocessoCrudController - Busca Integração")
class SubprocessoBuscarIntegracaoTest {

    @MockitoBean
    private SubprocessoFacade subprocessoFacade;

    @MockitoBean
    private OrganizacaoFacade organizacaoFacade;

    @Autowired
    private MockMvc mockMvc;

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
        when(subprocessoFacade.obterEntidadePorProcessoEUnidade(codProcesso, 10L)).thenReturn(sp);

        // Act & Assert
        mockMvc.perform(get("/api/subprocessos/buscar")
                        .param("codProcesso", codProcesso.toString())
                        .param("siglaUnidade", siglaUnidade))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(100));
    }
}
