package sgc.relatorio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sgc.relatorio.dto.RelatorioAndamentoDto;
import sgc.relatorio.service.RelatorioService;

import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Relatórios", description = "Endpoints para extração de relatórios (CDU-35, CDU-36)")
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/andamento/{processoId}")
    @Operation(summary = "Gera o relatório de andamento de subprocessos em tela (CDU-35)")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<RelatorioAndamentoDto>> obterRelatorioAndamento(@PathVariable Long processoId) {
        return ResponseEntity.ok(relatorioService.obterRelatorioAndamento(processoId));
    }

    @GetMapping("/andamento/{processoId}/exportar")
    @Operation(summary = "Exporta o relatório de andamento em PDF (CDU-35)")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<byte[]> exportarRelatorioAndamento(@PathVariable Long processoId) {
        byte[] pdf = relatorioService.exportarRelatorioAndamentoPdf(processoId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "relatorio-andamento-" + processoId + ".pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
    
    @GetMapping("/mapas/{processoId}/exportar")
    @Operation(summary = "Exporta os mapas de um processo em PDF (CDU-36)")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<byte[]> exportarRelatorioMapas(
            @PathVariable Long processoId,
            @RequestParam(required = false) Long unidadeId) {
        
        byte[] pdf = relatorioService.exportarRelatorioMapasPdf(processoId, unidadeId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "relatorio-mapas-" + processoId + ".pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
}
