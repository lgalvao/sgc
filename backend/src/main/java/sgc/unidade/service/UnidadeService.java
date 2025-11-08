package sgc.unidade.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sgc.mapa.model.MapaRepo;
import sgc.sgrh.dto.ServidorDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

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

    public List<UnidadeDto> buscarTodasUnidades() {
        List<Unidade> todasUnidades = unidadeRepo.findAll();
        return montarHierarquia(todasUnidades);
    }

    public boolean verificarMapaVigente(Long codigoUnidade) {
        return mapaRepo.findMapaVigenteByUnidade(codigoUnidade).isPresent();
    }

    public List<ServidorDto> buscarServidoresPorUnidade(Long codigoUnidade) {
        List<Usuario> usuarios = usuarioRepo.findByUnidadeCodigo(codigoUnidade);

        return usuarios.stream()
            .map(u -> new ServidorDto(
                u.getTituloEleitoral(),
                u.getNome(),
                u.getTituloEleitoral(),
                u.getEmail(),
                u.getUnidade().getCodigo()
            ))
            .toList();
    }

    private List<UnidadeDto> montarHierarquia(List<Unidade> unidades) {
        Map<Long, UnidadeDto> mapaUnidades = new HashMap<>();
        Map<Long, List<UnidadeDto>> mapaFilhas = new HashMap<>();
        List<UnidadeDto> raizes = new ArrayList<>();

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

    private UnidadeDto montarComSubunidades(UnidadeDto dto, Map<Long, List<UnidadeDto>> mapaFilhas) {
        List<UnidadeDto> filhas = mapaFilhas.get(dto.codigo());
        if (filhas == null || filhas.isEmpty()) {
            return dto;
        }

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

    public UnidadeDto buscarPorSigla(String sigla) {
        Unidade unidade = unidadeRepo.findBySigla(sigla)
            .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade com sigla " + sigla + " não encontrada"));

        Long codigoPai = unidade.getUnidadeSuperior() != null ? unidade.getUnidadeSuperior().getCodigo() : null;

        return new UnidadeDto(
            unidade.getCodigo(),
            unidade.getNome(),
            unidade.getSigla(),
            codigoPai,
            unidade.getTipo().name(),
            List.of()
        );
    }
}
