package sgc.mapa;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sgc.mapa.dto.MapaDto;
import sgc.mapa.mapper.MapaMapper;
import sgc.mapa.service.MapaFacade;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import sgc.comum.erros.ErroEstadoImpossivel;

/**
 * Controlador REST para gerenciar Mapas usando DTOs. Evita expor entidades JPA diretamente nas
 * APIs.
 */
@RestController
@RequestMapping("/api/mapas")
@RequiredArgsConstructor
@Tag(name = "Mapas", description = "Endpoints para gerenciamento de mapas de competências")
public class MapaController {
    private final MapaFacade mapaFacade;
    private final MapaMapper mapaMapper;

    /**
     * Retorna uma lista com todos os mapas de competências.
     *
     * @return Uma {@link List} de {@link MapaDto}.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'CHEFE')")
    @Operation(summary = "Lista todos os mapas")
    public List<MapaDto> listar() {
        return mapaFacade.listar().stream()
                .map(mapaMapper::toDto)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Busca e retorna um mapa de competências específico pelo seu código.
     *
     * @param codigo O código do mapa a ser buscado.
     * @return Um {@link ResponseEntity} contendo o {@link MapaDto} correspondente.
     */
    @GetMapping("/{codigo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'CHEFE')")
    @Operation(summary = "Obtém um mapa pelo código")
    public ResponseEntity<MapaDto> obterPorId(@PathVariable Long codigo) {
        var mapa = mapaFacade.obterPorCodigo(codigo);
        var dto = mapaMapper.toDto(mapa);
        if (dto == null) {
             throw new ErroEstadoImpossivel("Falha ao converter mapa para DTO.");
        }
        return ResponseEntity.ok(dto);
    }

    /**
     * Cria um novo mapa de competências.
     *
     * @param mapaDto O DTO com os dados do mapa a ser criado.
     * @return Um {@link ResponseEntity} com status 201 Created, o URI do novo mapa e o {@link
     * MapaDto} criado no corpo da resposta.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cria um mapa")
    public ResponseEntity<MapaDto> criar(@Valid @RequestBody MapaDto mapaDto) {
        var entidade = mapaMapper.toEntity(mapaDto);
        if (entidade == null) {
            throw new ErroEstadoImpossivel("Falha ao converter DTO para entidade mapa.");
        }
        var salvo = mapaFacade.criar(entidade);
        URI uri = URI.create("/api/mapas/%d".formatted(salvo.getCodigo()));
        var salvoDto = mapaMapper.toDto(salvo);
        if (salvoDto == null) {
            throw new ErroEstadoImpossivel("Falha ao converter mapa salvo para DTO.");
        }
        return ResponseEntity.created(uri).body(salvoDto);
    }

    /**
     * Atualiza um mapa de competências existente.
     *
     * @param codMapa O código do mapa a ser atualizado.
     * @param mapaDto O DTO com os novos dados do mapa.
     * @return Um {@link ResponseEntity} com status 200 OK e o {@link MapaDto} atualizado.
     */
    @PostMapping("/{codMapa}/atualizar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza um mapa existente")
    public ResponseEntity<MapaDto> atualizar(@PathVariable Long codMapa, @Valid @RequestBody MapaDto mapaDto) {
        var entidade = mapaMapper.toEntity(mapaDto);
        if (entidade == null) {
            throw new ErroEstadoImpossivel("Falha ao converter DTO para entidade mapa.");
        }
        var atualizado = mapaFacade.atualizar(codMapa, entidade);
        var atualizadoDto = mapaMapper.toDto(atualizado);
        if (atualizadoDto == null) {
            throw new ErroEstadoImpossivel("Falha ao converter mapa atualizado para DTO.");
        }
        return ResponseEntity.ok(atualizadoDto);
    }

    /**
     * Exclui um mapa de competências.
     *
     * @param codMapa O código do mapa a ser excluído.
     * @return Um {@link ResponseEntity} com status 204 No Content.
     */
    @PostMapping("/{codMapa}/excluir")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Exclui um mapa")
    public ResponseEntity<Void> excluir(@PathVariable Long codMapa) {
        mapaFacade.excluir(codMapa);
        return ResponseEntity.noContent().build();
    }
}
