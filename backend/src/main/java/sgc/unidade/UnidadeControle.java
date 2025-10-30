package sgc.unidade;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sgc.sgrh.dto.UnidadeDto;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller para operações relacionadas a unidades organizacionais
 */
@RestController
@RequestMapping("/api/unidades")
@RequiredArgsConstructor
public class UnidadeControle {

    private final UnidadeRepo unidadeRepo;

    /**
     * Busca todas as unidades em estrutura hierárquica
     * Endpoint usado pela tela de cadastro de processo para selecionar unidades participantes
     */
    @GetMapping
    public ResponseEntity<List<UnidadeDto>> buscarTodasUnidades() {
        List<Unidade> todasUnidades = unidadeRepo.findAll();
        List<UnidadeDto> hierarquia = montarHierarquia(todasUnidades);
        return ResponseEntity.ok(hierarquia);
    }

    /**
     * Monta hierarquia de unidades (raízes com filhas recursivamente)
     */
    private List<UnidadeDto> montarHierarquia(List<Unidade> unidades) {
        Map<Long, UnidadeDto> mapaUnidades = new HashMap<>();
        Map<Long, List<UnidadeDto>> mapaFilhas = new HashMap<>();
        List<UnidadeDto> raizes = new ArrayList<>();

        // Primeiro, criar DTOs para todas as unidades
        for (Unidade u : unidades) {
            Long codigoPai = u.getUnidadeSuperior() != null ? u.getUnidadeSuperior().getCodigo() : null;

            UnidadeDto dto = new UnidadeDto(
                u.getCodigo(),
                u.getNome(),
                u.getSigla(),
                codigoPai,
                u.getTipo().name()
            );
            mapaUnidades.put(u.getCodigo(), dto);

            // Preparar lista de filhas para cada unidade
            mapaFilhas.putIfAbsent(u.getCodigo(), new ArrayList<>());
        }

        // Depois, montar hierarquia
        for (Unidade u : unidades) {
            UnidadeDto dto = mapaUnidades.get(u.getCodigo());

            if (u.getUnidadeSuperior() == null) {
                // É raiz
                raizes.add(dto);
            } else {
                // Adicionar como filha da superior
                Long codigoPai = u.getUnidadeSuperior().getCodigo();
                mapaFilhas.computeIfAbsent(codigoPai, k -> new ArrayList<>()).add(dto);
            }
        }

        // Montar DTOs finais com subunidades
        List<UnidadeDto> resultado = new ArrayList<>();
        for (UnidadeDto raiz : raizes) {
            resultado.add(montarComSubunidades(raiz, mapaFilhas));
        }

        return resultado;
    }

    private UnidadeDto montarComSubunidades(UnidadeDto dto, Map<Long, List<UnidadeDto>> mapaFilhas) {
        List<UnidadeDto> filhas = mapaFilhas.get(dto.codigo());
        if (filhas == null || filhas.isEmpty()) {
            return dto;
        }

        // Recursivamente montar subunidades
        List<UnidadeDto> subunidadesCompletas = new ArrayList<>();
        for (UnidadeDto filha : filhas) {
            subunidadesCompletas.add(montarComSubunidades(filha, mapaFilhas));
        }

        return new UnidadeDto(
            dto.codigo(),
            dto.nome(),
            dto.sigla(),
            dto.codigoPai(),
            dto.tipo(),
            subunidadesCompletas
        );
    }
}
