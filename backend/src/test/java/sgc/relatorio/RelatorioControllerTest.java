package sgc.relatorio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.security.test.context.support.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;
import sgc.comum.erros.*;
import sgc.seguranca.*;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RelatorioController.class)
@Import(RestExceptionHandler.class)
@Tag("integration")
@DisplayName("RelatorioController - Testes de Integração")
class RelatorioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SgcPermissionEvaluator permissionEvaluator;

    @MockitoBean
    private RelatorioFacade relatorioFacade;

    @Test
    @DisplayName("GET /api/relatorios/andamento/{codProcesso} - Deve retornar relatório de andamento")
    @WithMockUser(roles = "ADMIN")
    void deveObterRelatorioAndamento() throws Exception {
        RelatorioAndamentoDto dto = RelatorioAndamentoDto.builder()
                .siglaUnidade("U1")
                .nomeUnidade("Unidade 1")
                .situacaoAtual("EM_ANDAMENTO")
                .responsavel("Responsável")
                .titular("Responsável")
                .build();
        when(relatorioFacade.obterRelatorioAndamento(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/relatorios/andamento/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].siglaUnidade").value("U1"))
                .andExpect(jsonPath("$[0].nomeUnidade").value("Unidade 1"));
    }

    @Test
    @DisplayName("GET /api/relatorios/andamento/{codProcesso}/exportar - Deve gerar PDF")
    @WithMockUser(roles = "ADMIN")
    void deveGerarRelatorioAndamentoPdf() throws Exception {
        mockMvc.perform(get("/api/relatorios/andamento/1/exportar"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_andamento.pdf"));

        verify(relatorioFacade).gerarRelatorioAndamento(eq(1L), any());
    }

    @Test
    @DisplayName("GET /api/relatorios/mapas/{codProcesso}/exportar - Deve gerar PDF")
    @WithMockUser(roles = "ADMIN")
    void deveGerarRelatorioMapasPdf() throws Exception {
        mockMvc.perform(get("/api/relatorios/mapas/1/exportar").param("codUnidade", "2"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_mapas.pdf"));

        verify(relatorioFacade).gerarRelatorioMapas(eq(1L), eq(2L), any());
    }

}
