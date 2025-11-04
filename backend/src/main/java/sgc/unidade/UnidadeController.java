package sgc.unidade;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sgc.mapa.modelo.MapaRepo;
import sgc.sgrh.dto.ServidorDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.modelo.Usuario;
import sgc.sgrh.modelo.UsuarioRepo;
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
public class UnidadeController {
    private final UnidadeRepo unidadeRepo;
    private final MapaRepo mapaRepo;
    private final UsuarioRepo usuarioRepo;

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
     * Verifica se a unidade possui mapa de competências vigente
     * Usado pelo frontend para determinar se deve exibir opções de revisão
     * 
     * @param codigoUnidade O código da unidade
     * @return Um objeto com o campo temMapaVigente (boolean)
     */
    @GetMapping("/{codigoUnidade}/mapa-vigente")
    public ResponseEntity<Map<String, Boolean>> verificarMapaVigente(@PathVariable Long codigoUnidade) {
        // Verifica se existe mapa vigente para a unidade usando o método do repositório
        boolean temMapaVigente = mapaRepo.findMapaVigenteByUnidade(codigoUnidade).isPresent();
        
        return ResponseEntity.ok(Map.of("temMapaVigente", temMapaVigente));
    }
    
    /**
     * Busca servidores (usuários) de uma unidade específica
     * Usado pelo frontend para validar se unidade tem servidores em processos de diagnóstico
     * 
     * @param codigoUnidade O código da unidade
     * @return Lista de servidores da unidade
     */
    @GetMapping("/{codigoUnidade}/servidores")
    public ResponseEntity<List<ServidorDto>> buscarServidoresPorUnidade(@PathVariable Long codigoUnidade) {
        List<Usuario> usuarios = usuarioRepo.findByUnidadeCodigo(codigoUnidade);
        
        List<ServidorDto> servidores = usuarios.stream()
            .map(u -> new ServidorDto(
                u.getTituloEleitoral(),
                u.getNome(),
                String.valueOf(u.getTituloEleitoral()),
                u.getEmail(),
                u.getUnidade().getCodigo()
            ))
            .toList();
        
        return ResponseEntity.ok(servidores);
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
