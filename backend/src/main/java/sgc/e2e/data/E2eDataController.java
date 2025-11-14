package sgc.e2e.data;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/e2e/data")
@RequiredArgsConstructor
@Profile("e2e")
public class E2eDataController {

    private final E2eDataService e2eDataService;

    @PostMapping("/mapa")
    public ResponseEntity<Void> criarMapaVigente(@RequestBody CriarMapaVigenteReq requisicao) {
        e2eDataService.criarMapaVigenteParaUnidade(requisicao.getUnidadeId());
        return ResponseEntity.ok().build();
    }
}
