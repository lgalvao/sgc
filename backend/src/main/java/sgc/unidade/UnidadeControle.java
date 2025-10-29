package sgc.unidade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sgc.unidade.modelo.Unidade;

import java.util.List;

@RestController
@RequestMapping("/api/unidades")
@RequiredArgsConstructor
@Tag(name = "Unidades", description = "Endpoints para gerenciamento de unidades")
public class UnidadeControle {

    private final UnidadeService unidadeService;

    @GetMapping
    @Operation(summary = "Lista todas as unidades")
    public ResponseEntity<List<Unidade>> listarTodasUnidades() {
        List<Unidade> unidades = unidadeService.listarTodas();
        return ResponseEntity.ok(unidades);
    }
}
