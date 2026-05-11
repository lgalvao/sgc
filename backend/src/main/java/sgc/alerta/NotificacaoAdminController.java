package sgc.alerta;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.*;
import org.springframework.web.bind.annotation.*;
import sgc.alerta.dto.*;
import sgc.comum.config.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin/notificacoes")
@RequiredArgsConstructor
@Tag(name = "Notificações", description = "Acompanhamento administrativo de notificações")
@PreAuthorize("hasRole('ADMIN')")
public class NotificacaoAdminController {
    private final NotificacaoService notificacaoService;
    private final ConfigAplicacao configAplicacao;

    @GetMapping("/listar")
    @Operation(summary = "Lista as notificações individuais registradas")
    public ResponseEntity<List<NotificacaoDto>> listar(@RequestParam(defaultValue = "50") int limite) {
        List<NotificacaoDto> dtos = notificacaoService.listarTodasAdmin(limite)
                .stream()
                .map(NotificacaoDto::fromEntity)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{codigo}/reenviar")
    @Operation(summary = "Recoloca na fila uma notificação específica com falha definitiva")
    public ResponseEntity<NotificacaoReenvioDto> reenviar(@PathVariable Long codigo) {
        int reenfileiradas = notificacaoService.reenviarPorCodigo(codigo);
        return ResponseEntity.ok(new NotificacaoReenvioDto(codigo, reenfileiradas));
    }

    @GetMapping("/leitor-email-testes")
    @Operation(summary = "Retorna a URL do leitor de e-mails de testes, quando configurada")
    public ResponseEntity<UrlLeitorEmailTestesDto> buscarUrlLeitorEmailTestes() {
        return ResponseEntity.ok(new UrlLeitorEmailTestesDto(configAplicacao.getUrlLeitorEmailTestes()));
    }
}
