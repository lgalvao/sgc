package sgc.unidade.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoConsultaService;
import sgc.usuario.mapper.UsuarioMapper;
import sgc.unidade.dto.UnidadeDto;
import sgc.usuario.dto.UsuarioDto;
import sgc.usuario.model.Usuario;
import sgc.unidade.dto.AtribuicaoTemporariaDto;
import sgc.unidade.dto.CriarAtribuicaoTemporariaReq;
import sgc.unidade.model.AtribuicaoTemporaria;
import sgc.unidade.model.AtribuicaoTemporariaRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UnidadeService {
    private final UnidadeRepo unidadeRepo;
    private final sgc.unidade.model.UnidadeMapaRepo unidadeMapaRepo;
    private final sgc.mapa.service.MapaService mapaService;
    private final sgc.usuario.UsuarioService usuarioService;
    private final AtribuicaoTemporariaRepo atribuicaoTemporariaRepo;
    private final ProcessoConsultaService processoConsultaService;
    private final UsuarioMapper usuarioMapper;

    public List<UnidadeDto> buscarArvoreHierarquica() {
        List<Unidade> todasUnidades = unidadeRepo.findAllWithHierarquia();
        return montarHierarquia(todasUnidades);
    }

    public List<UnidadeDto> buscarTodasUnidades() {
        return buscarArvoreHierarquica();
    }

    public List<AtribuicaoTemporariaDto> buscarTodasAtribuicoes() {
        return atribuicaoTemporariaRepo.findAll().stream()
                .map(usuarioMapper::toAtribuicaoTemporariaDto)
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
                    u.getTipo() != sgc.unidade.model.TipoUnidade.INTERMEDIARIA
                            && (!requerMapaVigente || unidadesComMapa.contains(u.getCodigo()))
                            && !unidadesEmProcessoAtivo.contains(u.getCodigo());

            UnidadeDto dto = usuarioMapper.toUnidadeDto(u, isElegivel);

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
        return processoConsultaService.buscarIdsUnidadesEmProcessosAtivos(codProcessoIgnorar);
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

        Usuario usuario = usuarioService.buscarEntidadePorId(request.tituloEleitoralUsuario());

        // Valida se o usuário pertence à unidade (simplificado, poderia ser mais rigoroso)
        // Se a unidade não for a mesma e nem superior/subordinada, talvez devesse bloquear?
        // O requisito CDU-28 diz apenas "ADMIN seleciona servidor" da lista de servidores da unidade.

        // Validação de datas
        if (request.dataTermino().isBefore(request.dataInicio())) {
            throw new sgc.comum.erros.ErroValidacao("A data de término deve ser posterior à data de início.");
        }

        AtribuicaoTemporaria atribuicao = new AtribuicaoTemporaria();
        atribuicao.setUnidade(unidade);
        atribuicao.setUsuarioTitulo(usuario.getTituloEleitoral());
        atribuicao.setUsuarioMatricula(usuario.getMatricula());
        atribuicao.setDataInicio(request.dataInicio() != null ? request.dataInicio().atStartOfDay() : LocalDateTime.now());
        atribuicao.setDataTermino(request.dataTermino().atTime(23, 59, 59));
        atribuicao.setJustificativa(request.justificativa());

        atribuicaoTemporariaRepo.save(atribuicao);
    }

    public boolean verificarMapaVigente(Long codigoUnidade) {
        return mapaService.buscarMapaVigentePorUnidade(codigoUnidade).isPresent();
    }

    public List<UsuarioDto> buscarUsuariosPorUnidade(Long codigoUnidade) {
        return usuarioService.buscarUsuariosPorUnidade(codigoUnidade);
    }

    private List<UnidadeDto> montarHierarquia(List<Unidade> unidades) {
        Map<Long, UnidadeDto> mapaUnidades = new HashMap<>();
        Map<Long, List<UnidadeDto>> mapaFilhas = new HashMap<>();
        List<UnidadeDto> raizes = new ArrayList<>();

        for (Unidade u : unidades) {
            UnidadeDto dto = usuarioMapper.toUnidadeDto(u);

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
        Unidade unidade = buscarEntidadePorSigla(sigla);
        return usuarioMapper.toUnidadeDto(unidade, false);
    }

    public Unidade buscarEntidadePorSigla(String sigla) {
        return unidadeRepo
                .findBySigla(sigla)
                .orElseThrow(
                        () ->
                                new ErroEntidadeNaoEncontrada(
                                        "Unidade com sigla " + sigla + " não encontrada"));
    }

    public UnidadeDto buscarPorCodigo(Long codigo) {
        Unidade unidade = buscarEntidadePorId(codigo);
        return usuarioMapper.toUnidadeDto(unidade, false);
    }

    public Unidade buscarEntidadePorId(Long codigo) {
        return unidadeRepo
                .findById(codigo)
                .orElseThrow(
                        () ->
                                new ErroEntidadeNaoEncontrada(
                                        "Unidade com codigo "
                                                + codigo
                                                + " não encontrada"));
    }

    public List<Unidade> listarSubordinadas(Long codigoPai) {
        return unidadeRepo.findByUnidadeSuperiorCodigo(codigoPai);
    }

    public List<Unidade> buscarEntidadesPorIds(List<Long> codigos) {
        return unidadeRepo.findAllById(codigos);
    }

    public List<Unidade> buscarTodasEntidadesComHierarquia() {
        return unidadeRepo.findAllWithHierarquia();
    }

    public List<String> buscarSiglasPorIds(List<Long> codigos) {
        return unidadeRepo.findSiglasByCodigos(codigos);
    }

    public boolean verificarExistenciaMapaVigente(Long codigoUnidade) {
        return unidadeMapaRepo.existsById(codigoUnidade);
    }

    @Transactional
    public void definirMapaVigente(Long codigoUnidade, sgc.mapa.model.Mapa mapa) {
        sgc.unidade.model.UnidadeMapa unidadeMapa = unidadeMapaRepo.findById(codigoUnidade)
                .orElse(new sgc.unidade.model.UnidadeMapa());
        unidadeMapa.setUnidadeCodigo(codigoUnidade);
        unidadeMapa.setMapaVigente(mapa);
        unidadeMapaRepo.save(unidadeMapa);
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
