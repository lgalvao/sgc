package sgc.relatorio;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import jakarta.servlet.http.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.*;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.*;

@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
@Tag(name = "Relatórios", description = "Endpoints para geração de relatórios em PDF")
@PreAuthorize("isAuthenticated()")
public class RelatorioController {
    private final RelatorioFacade relatorioService;

    @GetMapping("/andamento/{codProcesso}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Gera a visualização em JSON do andamento (CDU-35)")
    public ResponseEntity<List<RelatorioAndamentoDto>> obterRelatorioAndamento(@PathVariable Long codProcesso) {
        return ResponseEntity.ok(relatorioService.obterRelatorioAndamento(codProcesso));
    }

    @GetMapping("/andamento/{codProcesso}/exportar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Gera relatório de andamento do processo (CDU-35)")
    public void gerarRelatorioAndamentoPdf(@PathVariable Long codProcesso, HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_andamento.pdf");
        relatorioService.gerarRelatorioAndamento(codProcesso, response.getOutputStream());
    }

    @GetMapping("/mapas/exportar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Gera relatório consolidado de mapas (CDU-36)")
    public void gerarRelatorioMapasPdf(@RequestParam(name = "codUnidade") List<Long> codigosUnidades,
                                       HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_mapas.pdf");
        relatorioService.gerarRelatorioMapas(codigosUnidades, response.getOutputStream());
    }

    @GetMapping("/mapas")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Gera a visualização em JSON do relatório consolidado de mapas (CDU-36)")
    public ResponseEntity<List<RelatorioMapaDto>> obterRelatorioMapas(@RequestParam(name = "codUnidade") List<Long> codigosUnidades) {
        return ResponseEntity.ok(relatorioService.obterRelatorioMapas(codigosUnidades));
    }
}
