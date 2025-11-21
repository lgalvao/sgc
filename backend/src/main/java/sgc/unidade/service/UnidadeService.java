package sgc.unidade.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sgc.mapa.model.MapaRepo;
import sgc.sgrh.dto.ServidorDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.unidade.dto.CriarAtribuicaoTemporariaRequest;
import sgc.unidade.model.AtribuicaoTemporaria;
import sgc.unidade.model.AtribuicaoTemporariaRepo;
import sgc.processo.model.TipoProcesso;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public List<UnidadeDto> buscarArvoreComElegibilidade(TipoProcesso tipoProcesso, Long codProcesso) {
        List<Unidade> todasUnidades = unidadeRepo.findAll();
        // Lógica de elegibilidade a ser implementada
        return montarHierarquia(todasUnidades).stream()
            .map(unidade -> {
                unidade.setElegivel(true);
                return unidade;
            })
            .collect(Collectors.toList());
    }

    public void criarAtribuicaoTemporaria(Long idUnidade, CriarAtribuicaoTemporariaRequest request) {
        Unidade unidade = unidadeRepo.findById(idUnidade)
            .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade com id " + idUnidade + " não encontrada"));

        Usuario usuario = usuarioRepo.findById(request.tituloEleitoralServidor())
            .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Usuário com título eleitoral " + request.tituloEleitoralServidor() + " não encontrado"));

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
    
    private UnidadeDto montarComSubunidades(UnidadeDto dto, Map<Long, List<UnidadeDto>> mapaFilhas) {
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
        Unidade unidade = unidadeRepo.findBySigla(sigla)
            .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade com sigla " + sigla + " não encontrada"));

        Long codigoPai = unidade.getUnidadeSuperior() != null ? unidade.getUnidadeSuperior().getCodigo() : null;

        return new UnidadeDto(
            unidade.getCodigo(),
            unidade.getNome(),
            unidade.getSigla(),
            codigoPai,
            unidade.getTipo().name(),
            new ArrayList<>(),
            false
        );
    }

    public UnidadeDto buscarPorId(Long id) {
        Unidade unidade = unidadeRepo.findById(id)
            .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade com id " + id + " não encontrada"));

        Long codigoPai = unidade.getUnidadeSuperior() != null ? unidade.getUnidadeSuperior().getCodigo() : null;

        return new UnidadeDto(
            unidade.getCodigo(),
            unidade.getNome(),
            unidade.getSigla(),
            codigoPai,
            unidade.getTipo().name(),
            new ArrayList<>(),
            false
        );
    }
}
