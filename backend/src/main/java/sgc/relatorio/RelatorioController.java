package sgc.relatorio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
@Tag(name = "Relatórios", description = "Endpoints para geração de relatórios em PDF")
@PreAuthorize("isAuthenticated()")
public class RelatorioController {
    private static final DateTimeFormatter FORMATADOR_DATA_ARQUIVO = DateTimeFormatter.ISO_LOCAL_DATE;
    private final RelatorioService relatorioService;

    @GetMapping("/andamento/{codProcesso}")
    @PreAuthorize("hasRole('ADMIN') or @processoService.checarAcesso(authentication, #codProcesso)")
    @Operation(summary = "Gera a visualização em JSON do andamento (CDU-35)")
    public ResponseEntity<List<RelatorioAndamentoDto>> obterRelatorioAndamento(@PathVariable Long codProcesso) {
        return ResponseEntity.ok(relatorioService.obterRelatorioAndamento(codProcesso));
    }

    @GetMapping("/andamento/{codProcesso}/exportar")
    @PreAuthorize("hasRole('ADMIN') or @processoService.checarAcesso(authentication, #codProcesso)")
    @Operation(summary = "Gera relatório de andamento do processo (CDU-35)")
    public void gerarRelatorioAndamentoPdf(@PathVariable Long codProcesso, HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=%s".formatted(nomeArquivoRelatorioAndamento()));
        relatorioService.gerarRelatorioAndamento(codProcesso, response.getOutputStream());
    }

    @GetMapping("/mapas/exportar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Gera relatório consolidado de mapas (CDU-36)")
    public void gerarRelatorioMapasPdf(@RequestParam(name = "codUnidade") List<Long> codigosUnidades,
                                       HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=%s".formatted(nomeArquivoRelatorioMapas()));
        relatorioService.gerarRelatorioMapas(codigosUnidades, response.getOutputStream());
    }

    @GetMapping("/mapas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Gera a visualização em JSON do relatório consolidado de mapas (CDU-36)")
    public ResponseEntity<List<RelatorioMapaDto>> obterRelatorioMapas(@RequestParam(name = "codUnidade") List<Long> codigosUnidades) {
        return ResponseEntity.ok(relatorioService.obterRelatorioMapas(codigosUnidades));
    }

    @GetMapping("/mapas/subprocessos/{codSubprocesso}")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    @Operation(summary = "Gera a visualização em JSON do mapa atual de um subprocesso")
    public ResponseEntity<RelatorioMapaDto> obterRelatorioMapaAtual(@PathVariable Long codSubprocesso) {
        return ResponseEntity.ok(relatorioService.obterRelatorioMapaAtual(codSubprocesso));
    }

    @GetMapping("/mapas/subprocessos/{codSubprocesso}/exportar")
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    @Operation(summary = "Gera relatório em PDF do mapa atual de um subprocesso")
    public void gerarRelatorioMapaAtualPdf(@PathVariable Long codSubprocesso, HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=%s".formatted(nomeArquivoRelatorioMapaAtual()));
        relatorioService.gerarRelatorioMapaAtual(codSubprocesso, response.getOutputStream());
    }

    @GetMapping("/mapas-vigentes/unidades/{codUnidade}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'CHEFE')")
    @Operation(summary = "Gera a visualização em JSON do mapa vigente de uma unidade")
    public ResponseEntity<RelatorioMapaDto> obterRelatorioMapaVigenteUnidade(@PathVariable Long codUnidade) {
        return ResponseEntity.ok(relatorioService.obterRelatorioMapaVigenteUnidade(codUnidade));
    }

    @GetMapping("/mapas-vigentes/unidades/{codUnidade}/exportar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'CHEFE')")
    @Operation(summary = "Gera relatório em PDF do mapa vigente de uma unidade")
    public void gerarRelatorioMapaVigenteUnidadePdf(@PathVariable Long codUnidade, HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=%s".formatted(nomeArquivoRelatorioMapaVigenteUnidade()));
        relatorioService.gerarRelatorioMapaVigenteUnidade(codUnidade, response.getOutputStream());
    }

    @GetMapping("/unidades-sem-mapas-vigentes/exportar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Gera relatório de unidades sem mapa vigente")
    public void gerarRelatorioUnidadesSemMapasVigentesPdf(HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=%s".formatted(nomeArquivoRelatorioUnidadesSemMapasVigentes()));
        relatorioService.gerarRelatorioUnidadesSemMapasVigentes(response.getOutputStream());
    }

    @GetMapping("/unidades-sem-mapas-vigentes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Gera a visualização em JSON das unidades sem mapa vigente")
    public ResponseEntity<List<RelatorioUnidadeSemMapaVigenteDto>> obterRelatorioUnidadesSemMapasVigentes() {
        return ResponseEntity.ok(relatorioService.obterRelatorioUnidadesSemMapasVigentes());
    }

    @GetMapping("/diagnostico/gaps/{codProcesso}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR') and @processoService.checarAcesso(authentication, #codProcesso)")
    @Operation(summary = "Gera a visualização em JSON do relatório de gaps de diagnóstico (CDU-53)")
    public ResponseEntity<List<RelatorioDiagnosticoGapDto>> obterRelatorioGapsDiagnostico(
            @PathVariable Long codProcesso,
            @RequestParam(name = "codUnidade") List<Long> codigosUnidades
    ) {
        return ResponseEntity.ok(relatorioService.obterRelatorioGapsDiagnostico(codProcesso, codigosUnidades));
    }

    @GetMapping("/diagnostico/gaps/{codProcesso}/exportar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR') and @processoService.checarAcesso(authentication, #codProcesso)")
    @Operation(summary = "Gera relatório de gaps de diagnóstico em PDF (CDU-53)")
    public void gerarRelatorioGapsDiagnosticoPdf(
            @PathVariable Long codProcesso,
            @RequestParam(name = "codUnidade") List<Long> codigosUnidades,
            HttpServletResponse response
    ) throws IOException {
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=%s".formatted(nomeArquivoRelatorioGapsDiagnostico()));
        relatorioService.gerarRelatorioGapsDiagnostico(codProcesso, codigosUnidades, response.getOutputStream());
    }

    @GetMapping("/diagnostico/situacao-capacitacao/{codProcesso}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR') and @processoService.checarAcesso(authentication, #codProcesso)")
    @Operation(summary = "Gera a visualização em JSON do relatório de situação de capacitação (CDU-54)")
    public ResponseEntity<List<RelatorioDiagnosticoSituacaoCapacitacaoDto>> obterRelatorioSituacaoCapacitacaoDiagnostico(
            @PathVariable Long codProcesso,
            @RequestParam(name = "codUnidade") List<Long> codigosUnidades
    ) {
        return ResponseEntity.ok(relatorioService.obterRelatorioSituacaoCapacitacaoDiagnostico(codProcesso, codigosUnidades));
    }

    @GetMapping("/diagnostico/situacao-capacitacao/{codProcesso}/exportar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR') and @processoService.checarAcesso(authentication, #codProcesso)")
    @Operation(summary = "Gera relatório de situação de capacitação em PDF (CDU-54)")
    public void gerarRelatorioSituacaoCapacitacaoDiagnosticoPdf(
            @PathVariable Long codProcesso,
            @RequestParam(name = "codUnidade") List<Long> codigosUnidades,
            HttpServletResponse response
    ) throws IOException {
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=%s".formatted(nomeArquivoRelatorioSituacaoCapacitacaoDiagnostico()));
        relatorioService.gerarRelatorioSituacaoCapacitacaoDiagnostico(codProcesso, codigosUnidades, response.getOutputStream());
    }

    private String nomeArquivoRelatorioAndamento() {
        return "sgc-rel-andamento-%s.pdf".formatted(LocalDate.now().format(FORMATADOR_DATA_ARQUIVO));
    }

    private String nomeArquivoRelatorioMapas() {
        return "sgc-rel-mapas-%s.pdf".formatted(LocalDate.now().format(FORMATADOR_DATA_ARQUIVO));
    }

    private String nomeArquivoRelatorioMapaAtual() {
        return "sgc-rel-mapa-atual-%s.pdf".formatted(LocalDate.now().format(FORMATADOR_DATA_ARQUIVO));
    }

    private String nomeArquivoRelatorioMapaVigenteUnidade() {
        return "sgc-rel-mapa-vigente-%s.pdf".formatted(LocalDate.now().format(FORMATADOR_DATA_ARQUIVO));
    }

    private String nomeArquivoRelatorioUnidadesSemMapasVigentes() {
        return "sgc-rel-unidades-sem-mapas-vigentes-%s.pdf".formatted(LocalDate.now().format(FORMATADOR_DATA_ARQUIVO));
    }

    private String nomeArquivoRelatorioGapsDiagnostico() {
        return "sgc-rel-gaps-diagnostico-%s.pdf".formatted(LocalDate.now().format(FORMATADOR_DATA_ARQUIVO));
    }

    private String nomeArquivoRelatorioSituacaoCapacitacaoDiagnostico() {
        return "sgc-rel-situacao-capacitacao-%s.pdf".formatted(LocalDate.now().format(FORMATADOR_DATA_ARQUIVO));
    }
}
