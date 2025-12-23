package sgc.unidade.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.internal.model.MapaRepo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.sgrh.api.UsuarioDto;
import sgc.sgrh.api.UnidadeDto;
import sgc.sgrh.api.SgrhMapper;
import sgc.sgrh.internal.model.Usuario;
import sgc.sgrh.internal.model.UsuarioRepo;
import sgc.unidade.api.AtribuicaoTemporariaDto;
import sgc.unidade.api.CriarAtribuicaoTemporariaReq;
import sgc.unidade.internal.model.AtribuicaoTemporaria;
import sgc.unidade.internal.model.AtribuicaoTemporariaRepo;
import sgc.unidade.internal.model.Unidade;
import sgc.unidade.internal.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UnidadeService {
    private final UnidadeRepo unidadeRepo;
    private final sgc.unidade.internal.model.UnidadeMapaRepo unidadeMapaRepo;
    private final MapaRepo mapaRepo;
    private final UsuarioRepo usuarioRepo;
    private final AtribuicaoTemporariaRepo atribuicaoTemporariaRepo;
    private final ProcessoRepo processoRepo;
    private final SgrhMapper sgrhMapper;

    public List<UnidadeDto> buscarTodasUnidades() {
        List<Unidade> todasUnidades = unidadeRepo.findAllWithHierarquia();
        return montarHierarquia(todasUnidades);
    }

    public List<AtribuicaoTemporariaDto> buscarTodasAtribuicoes() {
        return atribuicaoTemporariaRepo.findAll().stream()
                .map(sgrhMapper::toAtribuicaoTemporariaDto)
                .toList();
    }

    public List<UnidadeDto> buscarArvoreComElegibilidade(
            TipoProcesso tipoProcesso, Long codProcesso) {
        boolean requerMapaVigente =
                tipoProcesso == TipoProcesso.REVISAO || tipoProcesso == TipoProcesso.DIAGNOSTICO;

        List<Unidade> todasUnidades = unidadeRepo.findAllWithHierarquia();

        return montarHierarquiaComElegibilidade(todasUnidades, requerMapaVigente, codProcesso);
    }

    private List<UnidadeDto> montarHierarquiaComElegibilidade(
            List<Unidade> unidades, boolean requerMapaVigente, Long codProcessoIgnorar) {
        Map<Long, UnidadeDto> mapaUnidades = new HashMap<>();
        Map<Long, List<UnidadeDto>> mapaFilhas = new HashMap<>();
        List<UnidadeDto> raizes = new ArrayList<>();

        Set<Long> unidadesEmProcessoAtivo = getUnidadesEmProcessosAtivos(codProcessoIgnorar);

        // Otimização: Busca em lote os IDs das unidades que possuem mapa para evitar N+1
        Set<Long> unidadesComMapa = requerMapaVigente
                ? new HashSet<>(unidadeMapaRepo.findAllUnidadeCodigos())
                : Collections.emptySet();

        for (Unidade u : unidades) {
            // Elegibilidade simples (não recursiva):
            // 3. NÃO está em outro processo ativo
            boolean isElegivel =
                    u.getTipo() != sgc.unidade.internal.model.TipoUnidade.INTERMEDIARIA
                            && (!requerMapaVigente || unidadesComMapa.contains(u.getCodigo()))
                            && !unidadesEmProcessoAtivo.contains(u.getCodigo());

            UnidadeDto dto = sgrhMapper.toUnidadeDto(u, isElegivel);

            mapaUnidades.put(u.getCodigo(), dto);
            mapaFilhas.putIfAbsent(u.getCodigo(), new ArrayList<>());
        }

        for (Unidade u : unidades) {
            UnidadeDto dto = mapaUnidades.get(u.getCodigo());

            if (u.getUnidadeSuperior() == null) {
                raizes.add(dto);
            } else {
                Long codigoPai = u.getUnidadeSuperior().getCodigo();
                mapaFilhas.computeIfAbsent(codigoPai, k -> new ArrayList<>()).add(dto);
            }
        }

        List<UnidadeDto> resultado = new ArrayList<>();
        for (UnidadeDto raiz : raizes) {
            resultado.add(montarComSubunidades(raiz, mapaFilhas));
        }

        return resultado;
    }

    private Set<Long> getUnidadesEmProcessosAtivos(Long codProcessoIgnorar) {
        // Bolt Optimization: Use JPQL projection to fetch only IDs instead of hydrating full entities
        // and avoid N+1 queries from lazy loading participants
        return new HashSet<>(
                processoRepo.findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(
                        Arrays.asList(SituacaoProcesso.EM_ANDAMENTO, SituacaoProcesso.CRIADO),
                        codProcessoIgnorar));
    }

    public void criarAtribuicaoTemporaria(
            Long codUnidade, CriarAtribuicaoTemporariaReq request) {
        Unidade unidade =
                unidadeRepo
                        .findById(codUnidade)
                        .orElseThrow(
                                () ->
                                        new ErroEntidadeNaoEncontrada(
                                                "Unidade com codigo "
                                                        + codUnidade
                                                        + " não encontrada"));

        Usuario usuario =
                usuarioRepo
                        .findById(request.tituloEleitoralUsuario())
                        .orElseThrow(
                                () ->
                                        new ErroEntidadeNaoEncontrada(
                                                "Usuário com título eleitoral "
                                                        + request.tituloEleitoralUsuario()
                                                        + " não encontrado"));

        AtribuicaoTemporaria atribuicao = new AtribuicaoTemporaria();
        atribuicao.setUnidade(unidade);
        atribuicao.setUsuario(usuario);
        atribuicao.setDataInicio(LocalDateTime.now());
        atribuicao.setDataTermino(request.dataTermino().atTime(23, 59, 59));
        atribuicao.setJustificativa(request.justificativa());

        atribuicaoTemporariaRepo.save(atribuicao);
    }

    public boolean verificarMapaVigente(Long codigoUnidade) {
        return mapaRepo.findMapaVigenteByUnidade(codigoUnidade).isPresent();
    }

    public List<UsuarioDto> buscarUsuariosPorUnidade(Long codigoUnidade) {
        List<Usuario> usuarios = usuarioRepo.findByUnidadeLotacaoCodigo(codigoUnidade);

        return usuarios.stream()
                .map(sgrhMapper::toUsuarioDto)
                .toList();
    }

    private List<UnidadeDto> montarHierarquia(List<Unidade> unidades) {
        Map<Long, UnidadeDto> mapaUnidades = new HashMap<>();
        Map<Long, List<UnidadeDto>> mapaFilhas = new HashMap<>();
        List<UnidadeDto> raizes = new ArrayList<>();

        for (Unidade u : unidades) {
            UnidadeDto dto = sgrhMapper.toUnidadeDto(u);

            mapaUnidades.put(u.getCodigo(), dto);
            mapaFilhas.putIfAbsent(u.getCodigo(), new ArrayList<>());
        }

        for (Unidade u : unidades) {
            UnidadeDto dto = mapaUnidades.get(u.getCodigo());

            if (u.getUnidadeSuperior() == null) {
                raizes.add(dto);
            } else {
                Long codigoPai = u.getUnidadeSuperior().getCodigo();
                mapaFilhas.computeIfAbsent(codigoPai, k -> new ArrayList<>()).add(dto);
            }
        }

        List<UnidadeDto> resultado = new ArrayList<>();
        for (UnidadeDto raiz : raizes) {
            resultado.add(montarComSubunidades(raiz, mapaFilhas));
        }

        return resultado;
    }

    private UnidadeDto montarComSubunidades(
            UnidadeDto dto, Map<Long, List<UnidadeDto>> mapaFilhas) {
        List<UnidadeDto> filhas = mapaFilhas.get(dto.getCodigo());
        if (filhas == null || filhas.isEmpty()) {
            return dto;
        }

        List<UnidadeDto> subunidadesCompletas = new ArrayList<>();
        for (UnidadeDto filha : filhas) {
            subunidadesCompletas.add(montarComSubunidades(filha, mapaFilhas));
        }

        dto.setSubunidades(subunidadesCompletas);
        return dto;
    }

    public UnidadeDto buscarPorSigla(String sigla) {
        Unidade unidade =
                unidadeRepo
                        .findBySigla(sigla)
                        .orElseThrow(
                                () ->
                                        new ErroEntidadeNaoEncontrada(
                                                "Unidade com sigla " + sigla + " não encontrada"));

        return sgrhMapper.toUnidadeDto(unidade, false);
    }

    public UnidadeDto buscarPorCodigo(Long codigo) {
        Unidade unidade =
                unidadeRepo
                        .findById(codigo)
                        .orElseThrow(
                                () ->
                                        new ErroEntidadeNaoEncontrada(
                                                "Unidade com codigo "
                                                        + codigo
                                                        + " não encontrada"));

        return sgrhMapper.toUnidadeDto(unidade, false);
    }

    public UnidadeDto buscarArvore(Long codigo) {
        List<UnidadeDto> todas = buscarTodasUnidades();
        return buscarNaHierarquia(todas, codigo);
    }

    public List<String> buscarSiglasSubordinadas(String sigla) {
        List<UnidadeDto> todas = buscarTodasUnidades();
        UnidadeDto raiz = buscarNaHierarquiaPorSigla(todas, sigla);

        if (raiz == null) {
            throw new ErroEntidadeNaoEncontrada(
                    "Unidade com sigla " + sigla + " não encontrada na hierarquia");
        }

        List<String> resultado = new ArrayList<>();
        coletarSiglas(raiz, resultado);
        return resultado;
    }

    public String buscarSiglaSuperior(String sigla) {
        Unidade unidade =
                unidadeRepo
                        .findBySigla(sigla)
                        .orElseThrow(
                                () ->
                                        new ErroEntidadeNaoEncontrada(
                                                "Unidade com sigla " + sigla + " não encontrada"));

        if (unidade.getUnidadeSuperior() != null) {
            return unidade.getUnidadeSuperior().getSigla();
        }
        return null;
    }

    private UnidadeDto buscarNaHierarquia(List<UnidadeDto> lista, Long codigo) {
        for (UnidadeDto u : lista) {
            if (u.getCodigo().equals(codigo)) return u;
            if (u.getSubunidades() != null) {
                UnidadeDto found = buscarNaHierarquia(u.getSubunidades(), codigo);
                if (found != null) return found;
            }
        }
        return null;
    }

    private UnidadeDto buscarNaHierarquiaPorSigla(List<UnidadeDto> lista, String sigla) {
        for (UnidadeDto u : lista) {
            if (u.getSigla().equals(sigla)) return u;
            if (u.getSubunidades() != null) {
                UnidadeDto found = buscarNaHierarquiaPorSigla(u.getSubunidades(), sigla);
                if (found != null) return found;
            }
        }
        return null;
    }

    private void coletarSiglas(UnidadeDto unidade, List<String> resultado) {
        resultado.add(unidade.getSigla());
        if (unidade.getSubunidades() != null) {
            for (UnidadeDto filha : unidade.getSubunidades()) {
                coletarSiglas(filha, resultado);
            }
        }
    }
}
