package sgc.sgrh;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.sgrh.dto.PerfilDto;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.dto.UsuarioDto;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Interface para serviço de integração com SGRH.
 * <p>
 * Responsável por consultar dados de usuários, unidades, responsabilidades
 * e perfis nas views do Oracle SGRH.
 */
@Service
@Transactional(readOnly = true)
@Cacheable("sgrh")
@Slf4j
@RequiredArgsConstructor
public class SgrhService {
    // ========== USUÁRIOS ==========
    /**
     * Busca usuário por título (CPF).
     *
     * @param titulo CPF/título do servidor
     * @return Optional com dados do usuário se encontrado
     */
    @Cacheable(value = "sgrh-usuarios", key = "#titulo")
    public Optional<UsuarioDto> buscarUsuarioPorTitulo(String titulo) {
        log.warn("MOCK SGRH: Buscando usuário por título: {}", titulo);
        return Optional.of(new UsuarioDto(
                titulo,
                "Usuário Mock " + titulo,
                titulo + "@tre-pe.jus.br",
                "MAT" + titulo.substring(0, Math.min(6, titulo.length())),
                "Analista Judiciário"));
    }

    /**
     * Busca usuário por email.
     *
     * @param email Email do servidor
     * @return Optional com dados do usuário se encontrado
     */
    @Cacheable(value = "sgrh-usuarios", key = "#email")
    public Optional<UsuarioDto> buscarUsuarioPorEmail(String email) {
        String titulo = email.split("@")[0];
        return Optional.of(new UsuarioDto(
                titulo,
                "Usuário " + titulo,
                email,
                "MAT" + titulo.hashCode(),
                "Analista Judiciário"));
    }

    /**
     * Lista todos os usuários ativos.
     *
     * @return Lista de usuários ativos
     */
    @Cacheable("sgrh-usuarios")
    public List<UsuarioDto> buscarUsuariosAtivos() {
        // TODO: Conectar ao banco SGRH real

        log.warn("MOCK SGRH: Listando usuários ativos");

        return List.of(
                new UsuarioDto("12345678901", "João Silva", "joao.silva@tre-pe.jus.br", "MAT001",
                        "Analista Judiciário"),
                new UsuarioDto("98765432109", "Maria Santos", "maria.santos@tre-pe.jus.br", "MAT002",
                        "Técnico Judiciário"),
                new UsuarioDto("11122233344", "Pedro Oliveira", "pedro.oliveira@tre-pe.jus.br", "MAT003",
                        "Analista Judiciário"));
    }

    // ========== UNIDADES ==========

    /**
     * Busca unidade por código.
     *
     * @param codigo Código da unidade
     * @return Optional com dados da unidade se encontrada
     */
    @Cacheable(value = "sgrh-unidades", key = "#codigo")
    public Optional<UnidadeDto> buscarUnidadePorCodigo(Long codigo) {
        log.warn("MOCK SGRH: Buscando unidade por código: {}", codigo);
        Map<Long, UnidadeDto> unidadesMock = criarUnidadesMock();
        return Optional.ofNullable(unidadesMock.get(codigo));
    }

    /**
     * Lista todas as unidades ativas.
     *
     * @return Lista de unidades ativas
     */
    @Cacheable("sgrh-unidades")
    public List<UnidadeDto> buscarUnidadesAtivas() {
        // TODO: Conectar ao banco SGRH real

        log.warn("MOCK SGRH: Listando unidades ativas");

        return new ArrayList<>(criarUnidadesMock().values());
    }

    /**
     * Busca subunidades de uma unidade pai.
     *
     * @param codigoPai Código da unidade pai
     * @return Lista de subunidades
     */
    @Cacheable(value = "sgrh-unidades", key = "'subunidades-' + #codigoPai")
    public List<UnidadeDto> buscarSubunidades(Long codigoPai) {
        log.warn("MOCK SGRH: Buscando subunidades de: {}", codigoPai);
        return criarUnidadesMock().values().stream()
                .filter(u -> codigoPai.equals(u.codigoPai()))
                .collect(Collectors.toList());
    }

    /**
     * Constrói árvore hierárquica completa de unidades.
     * Retorna as unidades raiz com suas subunidades aninhadas.
     *
     * @return Lista de unidades raiz com hierarquia completa
     */
    @Cacheable("sgrh-arvore-unidades")
    public List<UnidadeDto> construirArvoreHierarquica() {
        log.warn("MOCK SGRH: Construindo árvore hierárquica de unidades");

        Map<Long, UnidadeDto> todasUnidades = criarUnidadesMock();
        Map<Long, List<UnidadeDto>> subunidadesPorPai = new HashMap<>();

        // Agrupar subunidades por pai
        for (UnidadeDto unidade : todasUnidades.values()) {
            if (unidade.codigoPai() != null) {
                subunidadesPorPai
                        .computeIfAbsent(unidade.codigoPai(), _ -> new ArrayList<>())
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
                filhos.isEmpty() ? null : filhos);
    }

    // ========== RESPONSABILIDADES ==========

    /**
     * Busca responsável (titular e substituto) de uma unidade.
     *
     * @param unidadeCodigo Código da unidade
     * @return Optional com dados do responsável se encontrado
     */
    @Cacheable(value = "sgrh-responsabilidades", key = "#unidadeCodigo")
    public Optional<ResponsavelDto> buscarResponsavelUnidade(Long unidadeCodigo) {
        log.warn("MOCK SGRH: Buscando responsável da unidade: {}", unidadeCodigo);

        return Optional.of(new ResponsavelDto(
                unidadeCodigo,
                "12345678901",
                "João Silva (Titular)",
                "98765432109",
                "Maria Santos (Substituto)"));
    }

    /**
     * Busca unidades onde o servidor é responsável (titular ou substituto).
     *
     * @param titulo CPF/título do servidor
     * @return Lista de códigos de unidades onde é responsável
     */
    @Cacheable(value = "sgrh-responsabilidades", key = "'unidades-' + #titulo")
    public List<Long> buscarUnidadesOndeEhResponsavel(String titulo) {
        log.warn("MOCK SGRH: Buscando unidades onde {} é responsável", titulo);

        // Mock: retorna algumas unidades
        return List.of(1L, 2L, 10L);
    }

    // ========== PERFIS ==========

    /**
     * Busca todos os perfis de um usuário.
     *
     * @param titulo CPF/título do servidor
     * @return Lista de perfis com unidades associadas
     */
    @Cacheable(value = "sgrh-perfis", key = "#titulo")
    public List<PerfilDto> buscarPerfisUsuario(String titulo) {
        log.warn("MOCK SGRH: Buscando perfis do usuário: {}", titulo);

        return List.of(
                new PerfilDto(titulo, 1L, "SEDOC - Secretaria de Documentação", "ADMIN"),
                new PerfilDto(titulo, 2L, "CGC - Coordenadoria de Gestão de Competências", "GESTOR"),
                new PerfilDto(titulo, 10L, "SETEC - Secretaria de Tecnologia", "SERVIDOR"));
    }

    /**
     * Verifica se usuário tem perfil específico em uma unidade.
     *
     * @param titulo CPF/título do servidor
     * @param perfil Nome do perfil (ADMIN, GESTOR, CHEFE, SERVIDOR)
     * @param unidadeCodigo Código da unidade
     * @return true se usuário tem o perfil na unidade
     */
    public boolean usuarioTemPerfil(String titulo, String perfil, Long unidadeCodigo) {
        log.warn("MOCK SGRH: Verificando se {} tem perfil {} na unidade {}", titulo, perfil, unidadeCodigo);

        // Mock: retorna true para alguns casos
        return "ADMIN".equals(perfil) && unidadeCodigo == 1L;
    }

    /**
     * Busca unidades onde usuário tem perfil específico.
     *
     * @param titulo CPF/título do servidor
     * @param perfil Nome do perfil
     * @return Lista de códigos de unidades
     */
    @Cacheable(value = "sgrh-perfis", key = "#titulo + '-' + #perfil")
    public List<Long> buscarUnidadesPorPerfil(String titulo, String perfil) {
        log.warn("MOCK SGRH: Buscando unidades onde {} tem perfil {}", titulo, perfil);

        return switch (perfil) {
            case "ADMIN" -> List.of(1L);
            case "GESTOR" -> List.of(2L, 3L);
            case "CHEFE" -> List.of(10L, 11L);
            default -> List.of(1L, 2L, 10L);
        };
    }

    /**
     * Cria unidades MOCK para testes.
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