package sgc.alerta;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sgc.alerta.dto.NotificacaoDto;

import java.util.List;

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
