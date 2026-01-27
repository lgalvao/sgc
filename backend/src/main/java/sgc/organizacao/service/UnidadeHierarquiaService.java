package sgc.organizacao.service;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.mapper.UsuarioMapper;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;

import java.util.*;
import java.util.function.Function;

/**
 * Serviço especializado para gerenciar a hierarquia de unidades organizacionais.
 * 
 * <p>Responsabilidades:
 * <ul>
 *   <li>Montagem de árvore hierárquica de unidades</li>
 *   <li>Navegação na hierarquia (busca por código/sigla)</li>
 *   <li>Cálculo de descendentes e subordinadas</li>
 *   <li>Algoritmos recursivos de hierarquia</li>
 * </ul>
 * 
 * <p>Este serviço foi extraído de UnidadeFacade para respeitar o
 * Single Responsibility Principle (SRP).
 * 
 * @see UnidadeFacade
 */
@Service
@RequiredArgsConstructor
public class UnidadeHierarquiaService {
    private static final String MSG_NAO_ENCONTRADA = " não encontrada";

    private final UnidadeRepo unidadeRepo;
    private final UsuarioMapper usuarioMapper;

    /**
     * Busca a árvore hierárquica completa de unidades.
     * 
     * @return lista de unidades raiz com suas subunidades populadas
     */
    public List<UnidadeDto> buscarArvoreHierarquica() {
        List<Unidade> todasUnidades = unidadeRepo.findAllWithHierarquia();
        return montarHierarquia(todasUnidades);
    }

    /**
     * Busca a árvore hierárquica com filtro de elegibilidade.
     * 
     * @param elegibilidadeChecker função para verificar se unidade é elegível
     * @return lista de unidades raiz com suas subunidades populadas
     */
    public List<UnidadeDto> buscarArvoreComElegibilidade(Function<Unidade, Boolean> elegibilidadeChecker) {
        List<Unidade> todasUnidades = unidadeRepo.findAllWithHierarquia();
        return montarHierarquia(todasUnidades, elegibilidadeChecker);
    }

    /**
     * Busca todos os IDs de unidades descendentes de uma unidade.
     * 
     * @param codigoUnidade código da unidade raiz
     * @return lista de códigos de descendentes (filhos, netos, etc.)
     */
    public List<Long> buscarIdsDescendentes(Long codigoUnidade) {
        List<Unidade> todas = unidadeRepo.findAllWithHierarquia();

        Map<Long, List<Long>> mapPaiFilhos = new HashMap<>();
        for (Unidade u : todas) {
            Optional.ofNullable(u.getUnidadeSuperior()).ifPresent(superior ->
                    mapPaiFilhos.computeIfAbsent(superior.getCodigo(), k -> new ArrayList<>()).add(u.getCodigo())
            );
        }

        List<Long> descendentes = new ArrayList<>();
        coletarDescendentes(codigoUnidade, mapPaiFilhos, descendentes);
        return descendentes;
    }

    /**
     * Busca uma unidade específica na árvore hierárquica.
     * 
     * @param codigo código da unidade a buscar
     * @return DTO da unidade com sua subárvore
     * @throws ErroEntidadeNaoEncontrada se a unidade não for encontrada
     */
    public UnidadeDto buscarArvore(Long codigo) {
        List<UnidadeDto> todas = buscarArvoreHierarquica();
        return buscarNaHierarquia(todas, codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade", codigo));
    }

    /**
     * Busca todas as siglas de unidades subordinadas a uma unidade.
     * 
     * @param sigla sigla da unidade raiz
     * @return lista de siglas de todas as subordinadas
     * @throws ErroEntidadeNaoEncontrada se a unidade não for encontrada
     */
    public List<String> buscarSiglasSubordinadas(String sigla) {
        List<UnidadeDto> todas = buscarArvoreHierarquica();
        return buscarNaHierarquiaPorSigla(todas, sigla)
                .map(raiz -> {
                    List<String> resultado = new ArrayList<>();
                    coletarSiglas(raiz, resultado);
                    return resultado;
                })
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade", sigla));
    }

    /**
     * Busca a sigla da unidade superior de uma unidade.
     * 
     * @param sigla sigla da unidade
     * @return Optional com a sigla da unidade superior, ou vazio se não houver
     * @throws ErroEntidadeNaoEncontrada se a unidade não for encontrada
     */
    public Optional<String> buscarSiglaSuperior(String sigla) {
        Unidade unidade = unidadeRepo
                .findBySigla(sigla)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade com sigla %s%s".formatted(sigla, MSG_NAO_ENCONTRADA)));

        return Optional.ofNullable(unidade.getUnidadeSuperior()).map(Unidade::getSigla);
    }

    /**
     * Busca unidades subordinadas diretas de uma unidade.
     * 
     * @param codUnidade código da unidade pai
     * @return lista de DTOs das unidades filhas
     */
    public List<UnidadeDto> buscarSubordinadas(Long codUnidade) {
        return unidadeRepo.findByUnidadeSuperiorCodigo(codUnidade).stream()
                .map(u -> usuarioMapper.toUnidadeDto(u, true))
                .toList();
    }

    // ============ Métodos Privados (Algoritmos Recursivos) ============

    private List<UnidadeDto> montarHierarquia(List<Unidade> unidades) {
        return montarHierarquia(unidades, null);
    }

    private List<UnidadeDto> montarHierarquia(List<Unidade> unidades, @Nullable Function<Unidade, Boolean> elegibilidadeChecker) {
        Map<Long, UnidadeDto> mapaUnidades = new HashMap<>();
        Map<Long, List<UnidadeDto>> mapaFilhas = new HashMap<>();
        List<UnidadeDto> raizes = new ArrayList<>();

        for (Unidade u : unidades) {
            boolean isElegivel = elegibilidadeChecker != null ? elegibilidadeChecker.apply(u) : true;
            UnidadeDto dto = usuarioMapper.toUnidadeDto(u, isElegivel);

            mapaUnidades.put(u.getCodigo(), dto);
            mapaFilhas.putIfAbsent(u.getCodigo(), new ArrayList<>());
        }

        for (Unidade u : unidades) {
            UnidadeDto dto = mapaUnidades.get(u.getCodigo());

            Optional.ofNullable(u.getUnidadeSuperior()).ifPresentOrElse(
                    superior -> {
                        Long codigoPai = superior.getCodigo();
                        mapaFilhas.computeIfAbsent(codigoPai, k -> new ArrayList<>()).add(dto);
                    },
                    () -> raizes.add(dto)
            );
        }

        return raizes.stream().map(raiz -> montarComSubunidades(raiz, mapaFilhas)).toList();
    }

    private UnidadeDto montarComSubunidades(UnidadeDto dto, Map<Long, List<UnidadeDto>> mapaFilhas) {
        List<UnidadeDto> filhas = mapaFilhas.get(dto.getCodigo());
        if (filhas == null || filhas.isEmpty()) {
            return dto;
        }

        List<UnidadeDto> subunidadesCompletas = filhas.stream()
                .map(filha -> montarComSubunidades(filha, mapaFilhas))
                .toList();

        dto.setSubunidades(subunidadesCompletas);

        return dto;
    }

    private void coletarDescendentes(Long atual, Map<Long, List<Long>> map, List<Long> resultado) {
        List<Long> filhos = map.get(atual);
        if (filhos != null) {
            for (Long filho : filhos) {
                resultado.add(filho);
                coletarDescendentes(filho, map, resultado);
            }
        }
    }

    private Optional<UnidadeDto> buscarNaHierarquia(List<UnidadeDto> lista, Long codigo) {
        for (UnidadeDto u : lista) {
            if (u.getCodigo().equals(codigo)) {
                return Optional.of(u);
            }
            Optional<UnidadeDto> found = buscarNaHierarquia(u.getSubunidades(), codigo);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }

    private Optional<UnidadeDto> buscarNaHierarquiaPorSigla(List<UnidadeDto> lista, String sigla) {
        for (UnidadeDto u : lista) {
            if (u.getSigla().equals(sigla)) {
                return Optional.of(u);
            }
            Optional<UnidadeDto> found = buscarNaHierarquiaPorSigla(u.getSubunidades(), sigla);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }

    private void coletarSiglas(UnidadeDto unidade, List<String> resultado) {
        resultado.add(unidade.getSigla());
        unidade.getSubunidades().forEach(filha -> coletarSiglas(filha, resultado));
    }
}
