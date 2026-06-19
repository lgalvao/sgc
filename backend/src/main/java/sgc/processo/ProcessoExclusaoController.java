package sgc.processo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sgc.processo.service.ProcessoExclusaoCompletaService;

@RestController
@Profile("hom")
@RequestMapping("/api/processos")
@RequiredArgsConstructor
@Tag(name = "Processos", description = "Operacoes administrativas de processo em homologacao")
@PreAuthorize("isAuthenticated()")
public class ProcessoExclusaoController {
    private final ProcessoExclusaoCompletaService processoExclusaoCompletaService;

    @PostMapping("/{codigo}/excluir-completo")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Exclui um processo e todos os seus dependentes no ambiente de homologacao")
    public ResponseEntity<Void> excluirCompleto(@PathVariable Long codigo) {
        processoExclusaoCompletaService.excluirCompleto(codigo);
        return ResponseEntity.noContent().build();
    }
}
