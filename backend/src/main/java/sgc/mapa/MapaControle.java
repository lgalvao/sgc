package sgc.mapa;

import org.owasp.html.PolicyFactory;
import org.owasp.html.HtmlPolicyBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.MapaDto;
import sgc.mapa.dto.MapaMapper;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.sgrh.Usuario;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;

/**
 * Controlador REST para gerenciar Mapas usando DTOs.
 * Evita expor entidades JPA diretamente nas APIs.
 */
@RestController
@RequestMapping("/api/mapas")
@RequiredArgsConstructor
@Tag(name = "Mapas", description = "Endpoints para gerenciamento de mapas de competências")
public class MapaControle {
    private static final PolicyFactory HTML_SANITIZER_POLICY = new HtmlPolicyBuilder()
            .toFactory();

    private final MapaService mapaService;
    private final MapaCrudService mapaCrudService;
    private final MapaMapper mapaMapper;

    private MapaDto sanitizarEMapearMapaDto(MapaDto mapaDto) {
        var sanitizedObservacoesDisponibilizacao = HTML_SANITIZER_POLICY.sanitize(mapaDto.getObservacoesDisponibilizacao());
        var sanitizedSugestoes = HTML_SANITIZER_POLICY.sanitize(mapaDto.getSugestoes());

        return MapaDto.builder()
                .codigo(mapaDto.getCodigo())
                .dataHoraDisponibilizado(mapaDto.getDataHoraDisponibilizado())
                .observacoesDisponibilizacao(sanitizedObservacoesDisponibilizacao)
                .sugestoesApresentadas(mapaDto.getSugestoesApresentadas())
                .dataHoraHomologado(mapaDto.getDataHoraHomologado())
                .sugestoes(sanitizedSugestoes)
                .build();
    }

    @GetMapping
    @Operation(summary = "Lista todos os mapas")
    public List<MapaDto> listar() {
        return mapaCrudService.listar()
                .stream()
                .map(mapaMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtém um mapa pelo ID")
    public ResponseEntity<MapaDto> obterPorId(@PathVariable Long id) {
        var mapa = mapaCrudService.obterPorId(id);
        return ResponseEntity.ok(mapaMapper.toDTO(mapa));
    }

    @PostMapping
    @Operation(summary = "Cria um novo mapa")
    public ResponseEntity<MapaDto> criar(@Valid @RequestBody MapaDto mapaDto) {
        var sanitizedMapaDto = sanitizarEMapearMapaDto(mapaDto);

        var entidade = mapaMapper.toEntity(sanitizedMapaDto);
        var salvo = mapaCrudService.criar(entidade);
        URI uri = URI.create("/api/mapas/%d".formatted(salvo.getCodigo()));
        return ResponseEntity.created(uri).body(mapaMapper.toDTO(salvo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um mapa existente")
    public ResponseEntity<MapaDto> atualizar(@PathVariable Long id, @Valid @RequestBody MapaDto mapaDto) {
        var sanitizedMapaDto = sanitizarEMapearMapaDto(mapaDto);

        var entidade = mapaMapper.toEntity(sanitizedMapaDto);
        var atualizado = mapaCrudService.atualizar(id, entidade);
        return ResponseEntity.ok(mapaMapper.toDTO(atualizado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um mapa")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        mapaCrudService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * CDU-15 - Obter mapa completo com competências e atividades aninhadas.
     * GET /api/mapas/{id}/completo
     */
    @GetMapping("/{id}/completo")
    @Operation(summary = "Obtém um mapa completo com competências e atividades (CDU-15)")
    public ResponseEntity<MapaCompletoDto> obterCompleto(@PathVariable Long id) {
        MapaCompletoDto mapa = mapaService.obterMapaCompleto(id);
        return ResponseEntity.ok(mapa);
    }

    /**
     * CDU-15 - Salvar mapa completo (criar/editar competências + vínculos).
     * PUT /api/mapas/{id}/completo
     */
    @PutMapping("/{id}/completo")
    @Transactional
    @Operation(summary = "Salva um mapa completo com competências e atividades (CDU-15)")
    public ResponseEntity<MapaCompletoDto> salvarCompleto(
            @PathVariable Long id,
            @RequestBody @Valid SalvarMapaRequest request,
            @AuthenticationPrincipal Usuario usuario
    ) {
        MapaCompletoDto mapa = mapaService.salvarMapaCompleto(id, request, usuario.getTituloEleitoral());
        return ResponseEntity.ok(mapa);
    }
}