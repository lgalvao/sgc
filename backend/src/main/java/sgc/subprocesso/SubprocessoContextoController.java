package sgc.subprocesso;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sgc.subprocesso.dto.ContextoEdicaoDto;
import sgc.subprocesso.service.SubprocessoContextoService;

@RestController
@RequestMapping("/api/subprocessos")
@RequiredArgsConstructor
@Tag(name = "Subprocessos", description = "Endpoints para gerenciamento do workflow de subprocessos")
public class SubprocessoContextoController {

    private final SubprocessoContextoService subprocessoContextoService;

    /**
     * Obtém o contexto completo para a edição de um mapa de subprocesso.
     * Agrega dados de Subprocesso, Unidade, Mapa e Atividades para evitar múltiplas requisições.
     *
     * @param codigo O código do subprocesso.
     * @return O DTO com o contexto de edição.
     */
    @GetMapping("/{codigo}/contexto-edicao")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtém contexto completo para edição de mapa (BFF)")
    public ResponseEntity<ContextoEdicaoDto> obterContextoEdicao(@PathVariable Long codigo) {
        ContextoEdicaoDto contexto = subprocessoContextoService.obterContextoEdicao(codigo);
        return ResponseEntity.ok(contexto);
    }
}
