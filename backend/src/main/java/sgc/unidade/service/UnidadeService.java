package sgc.unidade.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.MapaRepo;
import sgc.processo.model.TipoProcesso;
import sgc.sgrh.dto.ServidorDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.unidade.dto.CriarAtribuicaoTemporariaRequest;
import sgc.unidade.model.AtribuicaoTemporaria;
import sgc.unidade.model.AtribuicaoTemporariaRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UnidadeService {
    private final UnidadeRepo unidadeRepo;
    private final MapaRepo mapaRepo;
    private final UsuarioRepo usuarioRepo;
    private final AtribuicaoTemporariaRepo atribuicaoTemporariaRepo;

    public List<UnidadeDto> buscarTodasUnidades() {
        List<Unidade> todasUnidades = unidadeRepo.findAll();
        return montarHierarquia(todasUnidades);
    }

    public List<UnidadeDto> buscarArvoreComElegibilidade(
            TipoProcesso tipoProcesso, Long codProcesso) {
        List<Unidade> todasUnidades = unidadeRepo.findAll();

        // For MAPEAMENTO, all units are elegível
        // For REVISAO and DIAGNOSTICO, only units with vigente maps are elegível
        boolean requerMapaVigente =
                tipoProcesso == TipoProcesso.REVISAO || tipoProcesso == TipoProcesso.DIAGNOSTICO;

        return montarHierarquiaComElegibilidade(todasUnidades, requerMapaVigente);
    }

    private List<UnidadeDto> montarHierarquiaComElegibilidade(
            List<Unidade> unidades, boolean requerMapaVigente) {
        Map<Long, UnidadeDto> mapaUnidades = new HashMap<>();
        Map<Long, List<UnidadeDto>> mapaFilhas = new HashMap<>();
        List<UnidadeDto> raizes = new ArrayList<>();

        for (Unidade u : unidades) {
            Long codigoPai =
                    u.getUnidadeSuperior() != null ? u.getUnidadeSuperior().getCodigo() : null;

            // Elegibilidade simples (não recursiva):
            // 1. NÃO é INTERMEDIARIA
            // 2. Tem mapa vigente (se requerido)
            // 3. NÃO está em outro processo ativo (TODO: implementar verificação)
            boolean isElegivel =
                    u.getTipo() != sgc.unidade.model.TipoUnidade.INTERMEDIARIA
                            && (!requerMapaVigente || u.getMapaVigente() != null);

            UnidadeDto dto =
                    new UnidadeDto(
                            u.getCodigo(),
                            u.getNome(),
                            u.getSigla(),
                            codigoPai,
                            u.getTipo().name(),
                            new ArrayList<>(),
                            isElegivel);
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

    public void criarAtribuicaoTemporaria(
            Long codUnidade, CriarAtribuicaoTemporariaRequest request) {
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
                        .findById(request.tituloEleitoralServidor())
                        .orElseThrow(
                                () ->
                                        new ErroEntidadeNaoEncontrada(
                                                "Usuário com título eleitoral "
                                                        + request.tituloEleitoralServidor()
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

    public List<ServidorDto> buscarServidoresPorUnidade(Long codigoUnidade) {
        List<Usuario> usuarios = usuarioRepo.findByUnidadeLotacaoCodigo(codigoUnidade);

        return usuarios.stream()
                .map(
                        u ->
                                new ServidorDto(
                                        u.getTituloEleitoral(),
                                        u.getNome(),
                                        u.getTituloEleitoral(),
                                        u.getEmail(),
                                        u.getUnidadeLotacao().getCodigo()))
                .toList();
    }

    private List<UnidadeDto> montarHierarquia(List<Unidade> unidades) {
        Map<Long, UnidadeDto> mapaUnidades = new HashMap<>();
        Map<Long, List<UnidadeDto>> mapaFilhas = new HashMap<>();
        List<UnidadeDto> raizes = new ArrayList<>();

        for (Unidade u : unidades) {
            Long codigoPai =
                    u.getUnidadeSuperior() != null ? u.getUnidadeSuperior().getCodigo() : null;

            UnidadeDto dto =
                    new UnidadeDto(
                            u.getCodigo(),
                            u.getNome(),
                            u.getSigla(),
                            codigoPai,
                            u.getTipo().name(),
                            new ArrayList<>(),
                            true // isElegivel: Set to true by default for now
                            );
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

        Long codigoPai =
                unidade.getUnidadeSuperior() != null
                        ? unidade.getUnidadeSuperior().getCodigo()
                        : null;

        return new UnidadeDto(
                unidade.getCodigo(),
                unidade.getNome(),
                unidade.getSigla(),
                codigoPai,
                unidade.getTipo().name(),
                new ArrayList<>(),
                false);
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

        Long codigoPai =
                unidade.getUnidadeSuperior() != null
                        ? unidade.getUnidadeSuperior().getCodigo()
                        : null;

        return new UnidadeDto(
                unidade.getCodigo(),
                unidade.getNome(),
                unidade.getSigla(),
                codigoPai,
                unidade.getTipo().name(),
                new ArrayList<>(),
                false);
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
