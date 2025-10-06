package sgc.sgrh.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.sgrh.dto.PerfilDto;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.dto.UsuarioDto;
import sgc.sgrh.repository.VwResponsabilidadeRepository;
import sgc.sgrh.repository.VwUnidadeRepository;
import sgc.sgrh.repository.VwUsuarioPerfilUnidadeRepository;
import sgc.sgrh.repository.VwUsuarioRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementação do serviço de integração com SGRH.
 * <p>
 * IMPORTANTE: Esta implementação usa dados MOCK enquanto não há conexão
 * real com o banco Oracle SGRH. Os Metodos estão preparados para serem
 * substituídos por consultas reais aos repositories quando a conexão
 * estiver disponível.
 */
@Service
@Transactional(readOnly = true)
@Cacheable("sgrh")
@Slf4j
@RequiredArgsConstructor
public class SgrhServiceImpl implements SgrhService {
    
    // Repositories prontos para uso quando banco SGRH estiver disponível
    private final VwUsuarioRepository usuarioRepository;
    private final VwUnidadeRepository unidadeRepository;
    private final VwResponsabilidadeRepository responsabilidadeRepository;
    private final VwUsuarioPerfilUnidadeRepository perfilUnidadeRepository;
    
    // ========== USUÁRIOS ==========
    
    @Override
    @Cacheable(value = "sgrh-usuarios", key = "#titulo")
    public Optional<UsuarioDto> buscarUsuarioPorTitulo(String titulo) {
        // TODO: Conectar ao banco SGRH real
        // return usuarioRepository.findByTitulo(titulo)
        //     .map(this::converterParaUsuarioDto);
        
        log.warn("MOCK SGRH: Buscando usuário por título: {}", titulo);
        
        // Dados MOCK para testes
        return Optional.of(new UsuarioDto(
            titulo,
            "Usuário Mock " + titulo,
            titulo + "@tre-pe.jus.br",
            "MAT" + titulo.substring(0, Math.min(6, titulo.length())),
            "Analista Judiciário"
        ));
    }
    
    @Override
    @Cacheable(value = "sgrh-usuarios", key = "#email")
    public Optional<UsuarioDto> buscarUsuarioPorEmail(String email) {
        // TODO: Conectar ao banco SGRH real
        // return usuarioRepository.findByEmail(email)
        //     .map(this::converterParaUsuarioDto);
        
        log.warn("MOCK SGRH: Buscando usuário por email: {}", email);
        
        String titulo = email.split("@")[0];
        return Optional.of(new UsuarioDto(
            titulo,
            "Usuário " + titulo,
            email,
            "MAT" + titulo.hashCode(),
            "Analista Judiciário"
        ));
    }
    
    @Override
    @Cacheable("sgrh-usuarios")
    public List<UsuarioDto> buscarUsuariosAtivos() {
        // TODO: Conectar ao banco SGRH real
        // return usuarioRepository.findByAtivoTrue().stream()
        //     .map(this::converterParaUsuarioDto)
        //     .collect(Collectors.toList());
        
        log.warn("MOCK SGRH: Listando usuários ativos");
        
        return List.of(
            new UsuarioDto("12345678901", "João Silva", "joao.silva@tre-pe.jus.br", "MAT001", "Analista Judiciário"),
            new UsuarioDto("98765432109", "Maria Santos", "maria.santos@tre-pe.jus.br", "MAT002", "Técnico Judiciário"),
            new UsuarioDto("11122233344", "Pedro Oliveira", "pedro.oliveira@tre-pe.jus.br", "MAT003", "Analista Judiciário")
        );
    }
    
    // ========== UNIDADES ==========
    
    @Override
    @Cacheable(value = "sgrh-unidades", key = "#codigo")
    public Optional<UnidadeDto> buscarUnidadePorCodigo(Long codigo) {
        // TODO: Conectar ao banco SGRH real
        // return unidadeRepository.findByCodigo(codigo)
        //     .map(this::converterParaUnidadeDto);
        
        log.warn("MOCK SGRH: Buscando unidade por código: {}", codigo);
        
        // Dados MOCK
        Map<Long, UnidadeDto> unidadesMock = criarUnidadesMock();
        return Optional.ofNullable(unidadesMock.get(codigo));
    }
    
    @Override
    @Cacheable("sgrh-unidades")
    public List<UnidadeDto> buscarUnidadesAtivas() {
        // TODO: Conectar ao banco SGRH real
        // return unidadeRepository.findByAtivaTrue().stream()
        //     .map(this::converterParaUnidadeDto)
        //     .collect(Collectors.toList());
        
        log.warn("MOCK SGRH: Listando unidades ativas");
        
        return new ArrayList<>(criarUnidadesMock().values());
    }
    
    @Override
    @Cacheable(value = "sgrh-unidades", key = "'subunidades-' + #codigoPai")
    public List<UnidadeDto> buscarSubunidades(Long codigoPai) {
        // TODO: Conectar ao banco SGRH real
        // return unidadeRepository.findByCodigoPaiAndAtivaTrue(codigoPai).stream()
        //     .map(this::converterParaUnidadeDto)
        //     .collect(Collectors.toList());
        
        log.warn("MOCK SGRH: Buscando subunidades de: {}", codigoPai);
        
        return criarUnidadesMock().values().stream()
            .filter(u -> codigoPai.equals(u.codigoPai()))
            .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable("sgrh-arvore-unidades")
    public List<UnidadeDto> construirArvoreHierarquica() {
        // TODO: Conectar ao banco SGRH real e montar árvore
        
        log.warn("MOCK SGRH: Construindo árvore hierárquica de unidades");
        
        Map<Long, UnidadeDto> todasUnidades = criarUnidadesMock();
        Map<Long, List<UnidadeDto>> subunidadesPorPai = new HashMap<>();
        
        // Agrupar subunidades por pai
        for (UnidadeDto unidade : todasUnidades.values()) {
            if (unidade.codigoPai() != null) {
                subunidadesPorPai
                    .computeIfAbsent(unidade.codigoPai(), k -> new ArrayList<>())
                    .add(unidade);
            }
        }
        
        // Construir árvore recursivamente

        return todasUnidades.values().stream()
            .filter(u -> u.codigoPai() == null)
            .map(u -> construirArvore(u, subunidadesPorPai))
            .collect(Collectors.toList());
    }
    
    private UnidadeDto construirArvore(UnidadeDto unidade, Map<Long, List<UnidadeDto>> subunidadesPorPai) {
        List<UnidadeDto> filhos = subunidadesPorPai.getOrDefault(unidade.codigo(), Collections.emptyList())
            .stream()
            .map(filho -> construirArvore(filho, subunidadesPorPai))
            .collect(Collectors.toList());
        
        return new UnidadeDto(
            unidade.codigo(),
            unidade.nome(),
            unidade.sigla(),
            unidade.codigoPai(),
            unidade.tipo(),
            filhos.isEmpty() ? null : filhos
        );
    }
    
    // ========== RESPONSABILIDADES ==========
    
    @Override
    @Cacheable(value = "sgrh-responsabilidades", key = "#unidadeCodigo")
    public Optional<ResponsavelDto> buscarResponsavelUnidade(Long unidadeCodigo) {
        // TODO: Conectar ao banco SGRH real
        // return responsabilidadeRepository.findByUnidadeCodigoAndAtivaTrue(unidadeCodigo)
        //     .flatMap(resp -> {
        //         Optional<VwUsuario> titular = usuarioRepository.findByTitulo(resp.getTitularTitulo());
        //         Optional<VwUsuario> substituto = usuarioRepository.findByTitulo(resp.getSubstitutoTitulo());
        //         // Montar ResponsavelDto
        //     });
        
        log.warn("MOCK SGRH: Buscando responsável da unidade: {}", unidadeCodigo);
        
        return Optional.of(new ResponsavelDto(
            unidadeCodigo,
            "12345678901",
            "João Silva (Titular)",
            "98765432109",
            "Maria Santos (Substituto)"
        ));
    }
    
    @Override
    @Cacheable(value = "sgrh-responsabilidades", key = "'unidades-' + #titulo")
    public List<Long> buscarUnidadesOndeEhResponsavel(String titulo) {
        // TODO: Conectar ao banco SGRH real
        // List<VwResponsabilidade> responsabilidades = 
        //     responsabilidadeRepository.findByTitularOuSubstituto(titulo);
        // return responsabilidades.stream()
        //     .map(VwResponsabilidade::getUnidadeCodigo)
        //     .collect(Collectors.toList());
        
        log.warn("MOCK SGRH: Buscando unidades onde {} é responsável", titulo);
        
        // Mock: retorna algumas unidades
        return List.of(1L, 2L, 10L);
    }
    
    // ========== PERFIS ==========
    
    @Override
    @Cacheable(value = "sgrh-perfis", key = "#titulo")
    public List<PerfilDto> buscarPerfisUsuario(String titulo) {
        // TODO: Conectar ao banco SGRH real
        // List<VwUsuarioPerfilUnidade> perfis = 
        //     perfilUnidadeRepository.findByIdUsuarioTituloAndAtivoTrue(titulo);
        // return perfis.stream()
        //     .map(p -> {
        //         Optional<VwUnidade> unidade = unidadeRepository.findByCodigo(p.getId().getUnidadeCodigo());
        //         return new PerfilDto(
        //             titulo,
        //             p.getId().getUnidadeCodigo(),
        //             unidade.map(VwUnidade::getNome).orElse("Unidade não encontrada"),
        //             p.getPerfil()
        //         );
        //     })
        //     .collect(Collectors.toList());
        
        log.warn("MOCK SGRH: Buscando perfis do usuário: {}", titulo);
        
        return List.of(
            new PerfilDto(titulo, 1L, "SEDOC - Secretaria de Documentação", "ADMIN"),
            new PerfilDto(titulo, 2L, "CGC - Coordenadoria de Gestão de Competências", "GESTOR"),
            new PerfilDto(titulo, 10L, "SETEC - Secretaria de Tecnologia", "SERVIDOR")
        );
    }
    
    @Override
    public boolean usuarioTemPerfil(String titulo, String perfil, Long unidadeCodigo) {
        // TODO: Conectar ao banco SGRH real
        // return perfilUnidadeRepository.existsByUsuarioUnidadeAndPerfil(
        //     titulo, unidadeCodigo, perfil
        // );
        
        log.warn("MOCK SGRH: Verificando se {} tem perfil {} na unidade {}", titulo, perfil, unidadeCodigo);
        
        // Mock: retorna true para alguns casos
        return "ADMIN".equals(perfil) && unidadeCodigo == 1L;
    }
    
    @Override
    @Cacheable(value = "sgrh-perfis", key = "#titulo + '-' + #perfil")
    public List<Long> buscarUnidadesPorPerfil(String titulo, String perfil) {
        // TODO: Conectar ao banco SGRH real
        // return perfilUnidadeRepository.findByIdUsuarioTituloAndPerfilAndAtivoTrue(titulo, perfil)
        //     .stream()
        //     .map(p -> p.getId().getUnidadeCodigo())
        //     .collect(Collectors.toList());
        
        log.warn("MOCK SGRH: Buscando unidades onde {} tem perfil {}", titulo, perfil);
        
        return switch (perfil) {
            case "ADMIN" -> List.of(1L);
            case "GESTOR" -> List.of(2L, 3L);
            case "CHEFE" -> List.of(10L, 11L);
            default -> List.of(1L, 2L, 10L);
        };
    }
    
    // ========== MetodoS AUXILIARES ==========
    
    /**
     * Cria unidades MOCK para testes.
     * TODO: Remover quando conectar ao banco SGRH real.
     */
    private Map<Long, UnidadeDto> criarUnidadesMock() {
        Map<Long, UnidadeDto> unidades = new HashMap<>();
        
        // Unidade raiz
        unidades.put(1L, new UnidadeDto(1L, "SEDOC - Secretaria de Documentação", "SEDOC", null, "INTERMEDIARIA"));
        
        // Nível 1
        unidades.put(2L, new UnidadeDto(2L, "CGC - Coordenadoria de Gestão de Competências", "CGC", 1L, "INTERMEDIARIA"));
        unidades.put(3L, new UnidadeDto(3L, "COP - Coordenadoria Operacional", "COP", 1L, "INTERMEDIARIA"));
        
        // Nível 2
        unidades.put(10L, new UnidadeDto(10L, "SETEC - Secretaria de Tecnologia", "SETEC", 2L, "OPERACIONAL"));
        unidades.put(11L, new UnidadeDto(11L, "SEPES - Secretaria de Pessoal", "SEPES", 2L, "OPERACIONAL"));
        unidades.put(12L, new UnidadeDto(12L, "SEADM - Secretaria Administrativa", "SEADM", 3L, "OPERACIONAL"));
        
        return unidades;
    }
}