package sgc.relatorio;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;
import sgc.comum.erros.*;
import sgc.processo.service.*;
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

    @MockitoBean
    private ProcessoService processoService;

    @Test
    @DisplayName("GET /api/relatorios/andamento/{codProcesso} - Deve retornar relatório de andamento")
    @WithMockUser(roles = "ADMIN")
    void deveObterRelatorioAndamento() throws Exception {
        RelatorioAndamentoDto dto = RelatorioAndamentoDto.builder()
                .siglaUnidade("U1")
                .nomeUnidade("Unidade 1")
                .situacaoAtual("EM_ANDAMENTO")
                .dataLimiteEtapa1(java.time.LocalDateTime.of(2026, 4, 15, 10, 0))
                .dataLimiteEtapa2(java.time.LocalDateTime.of(2026, 4, 20, 10, 0))
                .dataFimEtapa1(java.time.LocalDateTime.of(2026, 4, 16, 11, 30))
                .dataFimEtapa2(java.time.LocalDateTime.of(2026, 4, 17, 14, 45))
                .responsavel("Responsável")
                .titular("Responsável")
                .build();
        when(relatorioFacade.obterRelatorioAndamento(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/relatorios/andamento/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].siglaUnidade").value("U1"))
                .andExpect(jsonPath("$[0].nomeUnidade").value("Unidade 1"))
                .andExpect(jsonPath("$[0].dataLimiteEtapa1").value("2026-04-15T10:00:00"))
                .andExpect(jsonPath("$[0].dataLimiteEtapa2").value("2026-04-20T10:00:00"))
                .andExpect(jsonPath("$[0].dataFimEtapa1").value("2026-04-16T11:30:00"))
                .andExpect(jsonPath("$[0].dataFimEtapa2").value("2026-04-17T14:45:00"));
    }

    @Test
    @DisplayName("GET /api/relatorios/andamento/{codProcesso} - Deve retornar relatório de andamento quando GESTOR com acesso")
    @WithMockUser(roles = "GESTOR")
    void deveObterRelatorioAndamentoComoGestorComAcesso() throws Exception {
        RelatorioAndamentoDto dto = RelatorioAndamentoDto.builder()
                .siglaUnidade("U1")
                .nomeUnidade("Unidade 1")
                .situacaoAtual("EM_ANDAMENTO")
                .responsavel("Responsável")
                .titular("Responsável")
                .build();
        when(processoService.checarAcesso(any(), eq(1L))).thenReturn(true);
        when(relatorioFacade.obterRelatorioAndamento(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/relatorios/andamento/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].siglaUnidade").value("U1"));
    }

    @Test
    @DisplayName("GET /api/relatorios/andamento/{codProcesso}/exportar - Deve gerar PDF")
    @WithMockUser(roles = "ADMIN")
    void deveGerarRelatorioAndamentoPdf() throws Exception {
        String dataAtual = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        mockMvc.perform(get("/api/relatorios/andamento/1/exportar"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sgc-rel-andamento-%s.pdf".formatted(dataAtual)));

        verify(relatorioFacade).gerarRelatorioAndamento(eq(1L), any());
    }

    @Test
    @DisplayName("GET /api/relatorios/andamento/{codProcesso}/exportar - Deve gerar PDF quando GESTOR com acesso")
    @WithMockUser(roles = "GESTOR")
    void deveGerarRelatorioAndamentoPdfComoGestorComAcesso() throws Exception {
        String dataAtual = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        when(processoService.checarAcesso(any(), eq(1L))).thenReturn(true);

        mockMvc.perform(get("/api/relatorios/andamento/1/exportar"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sgc-rel-andamento-%s.pdf".formatted(dataAtual)));

        verify(relatorioFacade).gerarRelatorioAndamento(eq(1L), any());
    }

    @Test
    @DisplayName("GET /api/relatorios/mapas/exportar - Deve gerar PDF")
    @WithMockUser(roles = "ADMIN")
    void deveGerarRelatorioMapasPdf() throws Exception {
        String dataAtual = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        mockMvc.perform(get("/api/relatorios/mapas/exportar").param("codUnidade", "2").param("codUnidade", "3"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sgc-rel-mapas-%s.pdf".formatted(dataAtual)));

        verify(relatorioFacade).gerarRelatorioMapas(eq(List.of(2L, 3L)), any());
    }

    @Test
    @DisplayName("GET /api/relatorios/mapas/exportar - Deve gerar PDF quando GESTOR")
    @WithMockUser(roles = "GESTOR")
    void deveGerarRelatorioMapasPdfComoGestor() throws Exception {
        String dataAtual = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        mockMvc.perform(get("/api/relatorios/mapas/exportar").param("codUnidade", "2"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sgc-rel-mapas-%s.pdf".formatted(dataAtual)));

        verify(relatorioFacade).gerarRelatorioMapas(eq(List.of(2L)), any());
    }

    @Test
    @DisplayName("GET /api/relatorios/mapas - Deve retornar relatório de mapas")
    @WithMockUser(roles = "ADMIN")
    void deveObterRelatorioMapas() throws Exception {
        RelatorioMapaDto dto = new RelatorioMapaDto(
                2L,
                "SEC",
                "Secretaria",
                1,
                List.of(new RelatorioMapaCompetenciaDto(
                        3L,
                        "Competência 1",
                        List.of(new RelatorioMapaAtividadeDto(
                                4L,
                                "Atividade 1",
                                List.of(new RelatorioMapaConhecimentoDto(5L, "Conhecimento 1"))
                        ))
                ))
        );
        when(relatorioFacade.obterRelatorioMapas(List.of(2L, 3L))).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/relatorios/mapas").param("codUnidade", "2").param("codUnidade", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigoUnidade").value(2))
                .andExpect(jsonPath("$[0].siglaUnidade").value("SEC"))
                .andExpect(jsonPath("$[0].nomeUnidade").value("Secretaria"))
                .andExpect(jsonPath("$[0].totalCompetencias").value(1))
                .andExpect(jsonPath("$[0].competencias[0].descricao").value("Competência 1"))
                .andExpect(jsonPath("$[0].competencias[0].atividades[0].descricao").value("Atividade 1"))
                .andExpect(jsonPath("$[0].competencias[0].atividades[0].conhecimentos[0].descricao").value("Conhecimento 1"));
    }

    @Test
    @DisplayName("GET /api/relatorios/mapas - Deve retornar relatório de mapas quando GESTOR")
    @WithMockUser(roles = "GESTOR")
    void deveObterRelatorioMapasComoGestor() throws Exception {
        RelatorioMapaDto dto = new RelatorioMapaDto(
                2L,
                "SEC",
                "Secretaria",
                1,
                List.of()
        );
        when(relatorioFacade.obterRelatorioMapas(List.of(2L))).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/relatorios/mapas").param("codUnidade", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigoUnidade").value(2))
                .andExpect(jsonPath("$[0].siglaUnidade").value("SEC"));
    }

    @Test
    @DisplayName("GET /api/relatorios/unidades-sem-mapas-vigentes - Deve retornar lista de códigos")
    @WithMockUser(roles = "ADMIN")
    void deveObterCodigosUnidadesSemMapasVigentes() throws Exception {
        when(relatorioFacade.obterCodigosUnidadesSemMapasVigentes()).thenReturn(List.of(1L, 2L, 3L));

        mockMvc.perform(get("/api/relatorios/unidades-sem-mapas-vigentes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(1L))
                .andExpect(jsonPath("$[1]").value(2L));

        verify(relatorioFacade).obterCodigosUnidadesSemMapasVigentes();
    }

    @Test
    @DisplayName("GET /api/relatorios/unidades-sem-mapas-vigentes/exportar - Deve gerar PDF")
    @WithMockUser(roles = "ADMIN")
    void deveGerarRelatorioUnidadesSemMapasVigentesPdf() throws Exception {
        String dataAtual = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        mockMvc.perform(get("/api/relatorios/unidades-sem-mapas-vigentes/exportar"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=sgc-rel-unidades-sem-mapas-vigentes-%s.pdf".formatted(dataAtual)
                ));

        verify(relatorioFacade).gerarRelatorioUnidadesSemMapasVigentes(any());
    }

}
