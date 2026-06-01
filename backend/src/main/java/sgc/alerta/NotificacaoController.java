package sgc.alerta;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.*;
import org.springframework.web.bind.annotation.*;
import sgc.alerta.dto.*;

import java.util.*;

@RestController
@RequestMapping("/api/subprocessos/{codSubprocesso}/notificacoes-email")
@RequiredArgsConstructor
@Tag(name = "Notificações de e-mail", description = "Endpoints para acompanhamento do envio de e-mails")
@PreAuthorize("isAuthenticated()")
public class NotificacaoController {
    private final NotificacaoService notificacaoService;
    private final AlertaDtoMapper alertaDtoMapper;

    @GetMapping
    @PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', 'VISUALIZAR_SUBPROCESSO')")
    @Operation(summary = "Lista as notificações de e-mail geradas para um subprocesso")
    public ResponseEntity<List<NotificacaoDto>> listarPorSubprocesso(
            @PathVariable Long codSubprocesso,
            @RequestParam(defaultValue = "20") int limite) {
        List<NotificacaoDto> notificacoes = notificacaoService.listarPorSubprocesso(codSubprocesso, limite)
                .stream()
                .map(alertaDtoMapper::paraNotificacaoDto)
                .toList();

        return ResponseEntity.ok(notificacoes);
    }
}
