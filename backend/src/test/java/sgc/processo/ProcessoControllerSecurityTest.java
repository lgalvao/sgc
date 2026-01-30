package sgc.processo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.web.servlet.MockMvc;
import sgc.comum.erros.RestExceptionHandler;
import sgc.processo.service.ProcessoFacade;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProcessoController.class)
@Import(RestExceptionHandler.class)
@EnableMethodSecurity
@Tag("integration")
@DisplayName("ProcessoController Security")
class ProcessoControllerSecurityTest {

    @MockitoBean(name = "processoFacade")
    private ProcessoFacade processoFacade;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("Deve retornar 403 Forbidden ao listar subprocessos sem permissão")
    void deveRetornarForbiddenAoListarSubprocessosSemPermissao() throws Exception {
        // Arrange
        // Mock checarAcesso to return false (access denied)
        when(processoFacade.checarAcesso(any(), eq(1L))).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/processos/1/subprocessos"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}
