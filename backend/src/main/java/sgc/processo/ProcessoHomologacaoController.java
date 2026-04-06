package sgc.processo;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import lombok.*;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.*;
import org.springframework.web.bind.annotation.*;
import sgc.processo.service.*;

@RestController
@Profile("hom")
@RequestMapping("/api/processos")
@RequiredArgsConstructor
@Tag(name = "Processos", description = "Operacoes administrativas de processo em homologacao")
@PreAuthorize("isAuthenticated()")
public class ProcessoHomologacaoController {
    private final ProcessoExclusaoCompletaService processoExclusaoCompletaService;

    @PostMapping("/{codigo}/excluir-completo")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Exclui um processo e todos os seus dependentes no ambiente de homologacao")
    public ResponseEntity<Void> excluirCompleto(@PathVariable Long codigo) {
        processoExclusaoCompletaService.excluirCompleto(codigo);
        return ResponseEntity.noContent().build();
    }
}
