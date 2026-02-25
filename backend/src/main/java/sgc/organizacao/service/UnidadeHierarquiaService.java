package sgc.organizacao.service;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.model.ComumRepo;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;

import java.util.*;
import java.util.function.Predicate;

/**
 * Serviço especializado para gerenciar a hierarquia de unidades organizacionais.
 */
@Service
@RequiredArgsConstructor
public class UnidadeHierarquiaService {
    private final UnidadeRepo unidadeRepo;
    private final ComumRepo repo;

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
    public List<UnidadeDto> buscarArvoreComElegibilidade(Predicate<Unidade> elegibilidadeChecker) {
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
        return buscarDescendentes(codigoUnidade, buscarMapaHierarquia());
    }

    /**
     * Busca todos os IDs de unidades descendentes usando um mapa pré-carregado.
     *
     * @param codigoUnidade código da unidade raiz
     * @param mapPaiFilhos mapa de hierarquia (Pai -> Lista de Filhos)
     * @return lista de códigos de descendentes
     */
    public List<Long> buscarDescendentes(Long codigoUnidade, Map<Long, List<Long>> mapPaiFilhos) {
        List<Long> descendentes = new ArrayList<>();
        coletarDescendentes(codigoUnidade, mapPaiFilhos, descendentes);
        return descendentes;
    }

    /**
     * Constrói o mapa de hierarquia (Pai -> Lista de Filhos) buscando todas as unidades.
     *
     * @return mapa onde a chave é o código da unidade pai e o valor é a lista de códigos das unidades filhas
     */
    public Map<Long, List<Long>> buscarMapaHierarquia() {
        List<Unidade> todas = unidadeRepo.findAllWithHierarquia();

        Map<Long, List<Long>> mapPaiFilhos = new HashMap<>();
        for (Unidade u : todas) {
            Optional.ofNullable(u.getUnidadeSuperior()).ifPresent(superior ->
                    mapPaiFilhos.computeIfAbsent(superior.getCodigo(), k -> new ArrayList<>()).add(u.getCodigo())
            );
        }
        return mapPaiFilhos;
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
        Optional<UnidadeDto> found = buscarNaHierarquia(todas, codigo);
        if (found.isPresent()) {
            return found.get();
        }
        Unidade u = repo.buscar(Unidade.class, codigo);
        return UnidadeDto.fromEntity(u);
    }

    /**
     * Busca todas as siglas de unidades subordinadas a uma unidade.
     *
     * @return lista de siglas de todas as subordinadas
     * @throws ErroEntidadeNaoEncontrada se a unidade não for encontrada
     */
    public List<String> buscarSiglasSubordinadas(String sigla) {
        List<UnidadeDto> todas = buscarArvoreHierarquica();
        Optional<UnidadeDto> found = buscarNaHierarquiaPorSigla(todas, sigla);
        
        if (found.isEmpty()) {
            repo.buscarPorSigla(Unidade.class, sigla);
            return List.of(); 
        }

        List<String> resultado = new ArrayList<>();
        coletarSiglas(found.get(), resultado);
        return resultado;
    }

    /**
     * Busca a sigla da unidade superior de uma unidade.
     *
     * @return Optional com a sigla da unidade superior, ou vazio se não houver
     * @throws ErroEntidadeNaoEncontrada se a unidade não for encontrada
     */
    public Optional<String> buscarSiglaSuperior(String sigla) {
        Unidade unidade = repo.buscarPorSigla(Unidade.class, sigla);

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
                .map(UnidadeDto::fromEntity)
                .toList();
    }

    private List<UnidadeDto> montarHierarquia(List<Unidade> unidades) {
        return montarHierarquia(unidades, null);
    }

    private List<UnidadeDto> montarHierarquia(List<Unidade> unidades, @Nullable Predicate<Unidade> elegibilidadeChecker) {
        Map<Long, UnidadeDto> mapaUnidades = new HashMap<>();
        Map<Long, List<UnidadeDto>> mapaFilhas = new HashMap<>();
        List<UnidadeDto> raizes = new ArrayList<>();

        for (Unidade u : unidades) {
            boolean isElegivel = elegibilidadeChecker == null || elegibilidadeChecker.test(u);
            UnidadeDto dto = UnidadeDto.fromEntity(u);
            dto.setElegivel(isElegivel);

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

    UnidadeDto montarComSubunidades(UnidadeDto dto, Map<Long, List<UnidadeDto>> mapaFilhas) {
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
