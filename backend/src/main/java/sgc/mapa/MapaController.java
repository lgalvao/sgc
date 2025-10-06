package sgc.mapa;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para gerenciar Mapas usando DTOs.
 * Evita expor entidades JPA diretamente nas APIs.
 */
@RestController
@RequestMapping("/api/mapas")
@RequiredArgsConstructor
public class MapaController {
    private final MapaRepository mapaRepository;
    private final MapaService mapaService;

    @GetMapping
    public List<MapaDTO> listarMapas() {
        return mapaRepository.findAll()
                .stream()
                .map(MapaMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MapaDTO> obterMapa(@PathVariable Long id) {
        Optional<Mapa> m = mapaRepository.findById(id);
        return m.map(MapaMapper::toDTO).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MapaDTO> criarMapa(@Valid @RequestBody MapaDTO mapaDto) {
        var entity = MapaMapper.toEntity(mapaDto);
        var salvo = mapaRepository.save(entity);
        URI uri = URI.create("/api/mapas/%d".formatted(salvo.getCodigo()));
        return ResponseEntity.created(uri).body(MapaMapper.toDTO(salvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MapaDTO> atualizarMapa(@PathVariable Long id, @Valid @RequestBody MapaDTO mapaDto) {
        return mapaRepository.findById(id)
                .map(existing -> {
                    existing.setDataHoraDisponibilizado(mapaDto.getDataHoraDisponibilizado());
                    existing.setObservacoesDisponibilizacao(mapaDto.getObservacoesDisponibilizacao());
                    existing.setSugestoesApresentadas(mapaDto.getSugestoesApresentadas());
                    existing.setDataHoraHomologado(mapaDto.getDataHoraHomologado());
                    var atualizado = mapaRepository.save(existing);
                    return ResponseEntity.ok(MapaMapper.toDTO(atualizado));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirMapa(@PathVariable Long id) {
        return mapaRepository.findById(id)
                .map(existing -> {
                    mapaRepository.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    /**
     * CDU-15 - Obter mapa completo com competências e atividades aninhadas.
     * GET /api/mapas/{id}/completo
     * <p>
     * Retorna o mapa com todas as suas competências e os vínculos
     * com atividades de forma agregada.
     *
     * @param id Código do mapa
     * @return Mapa completo com competências aninhadas
     */
    @GetMapping("/{id}/completo")
    public ResponseEntity<MapaCompletoDto> obterMapaCompleto(@PathVariable Long id) {
        try {
            MapaCompletoDto mapa = mapaService.obterMapaCompleto(id);
            return ResponseEntity.ok(mapa);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * CDU-15 - Salvar mapa completo (criar/editar competências + vínculos).
     * PUT /api/mapas/{id}/completo
     * <p>
     * Operação atômica que:
     * - Atualiza observações do mapa
     * - Remove competências excluídas
     * - Cria novas competências
     * - Atualiza competências existentes
     * - Atualiza vínculos com atividades
     *
     * @param id Código do mapa
     * @param request Request com dados do mapa completo
     * @return Mapa completo atualizado
     */
    @PutMapping("/{id}/completo")
    @Transactional
    public ResponseEntity<MapaCompletoDto> salvarMapaCompleto(
        @PathVariable Long id,
        @RequestBody @Valid SalvarMapaRequest request
    ) {
        try {
            // TODO: Extrair usuarioTitulo do token JWT quando autenticação estiver implementada
            String usuarioTitulo = "USUARIO_ATUAL";
            MapaCompletoDto mapa = mapaService.salvarMapaCompleto(id, request, usuarioTitulo);
            return ResponseEntity.ok(mapa);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}