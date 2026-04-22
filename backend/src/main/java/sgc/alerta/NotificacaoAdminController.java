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
@RequestMapping("/api/admin/notificacoes")
@RequiredArgsConstructor
@Tag(name = "Notificações", description = "Acompanhamento administrativo de notificações")
@PreAuthorize("hasRole('ADMIN')")
public class NotificacaoAdminController {
    private final NotificacaoEmailService notificacaoEmailService;

    @GetMapping("/subprocessos-ativos")
    @Operation(summary = "Lista o resumo administrativo de notificações por subprocesso ativo")
    public ResponseEntity<List<NotificacaoSubprocessoResumoDto>> listarResumoSubprocessosAtivos() {
        return ResponseEntity.ok(notificacaoEmailService.listarResumoSubprocessosAtivos());
    }

    @PostMapping("/subprocessos/{codSubprocesso}/reenviar")
    @Operation(summary = "Recoloca na fila notificações com falha definitiva de um subprocesso")
    public ResponseEntity<ReenvioNotificacaoDto> reenviarFalhasDefinitivas(@PathVariable Long codSubprocesso) {
        int reenfileiradas = notificacaoEmailService.reenfileirarFalhasDefinitivasPorSubprocesso(codSubprocesso);
        return ResponseEntity.ok(new ReenvioNotificacaoDto(codSubprocesso, reenfileiradas));
    }
}
