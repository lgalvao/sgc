package sgc.sgrh;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
@Slf4j
@RequiredArgsConstructor
public class SgrhService {
    /**
     * Busca um usuário pelo seu título de eleitor.
     * <p>
     * <b>Implementação Mock:</b> Retorna sempre um usuário de exemplo com os dados
     * baseados no título fornecido.
     *
     * @param titulo O título de eleitor do usuário.
     * @return Um {@link Optional} contendo o {@link UsuarioDto} do usuário mockado.
     */
    public Optional<UsuarioDto> buscarUsuarioPorTitulo(String titulo) {
        log.warn("MOCK SGRH: Buscando usuário por título.");
        return Optional.of(new UsuarioDto(
                titulo,
                "Usuário Mock " + titulo,
                titulo + "@tre-pe.jus.br",
                "MAT" + titulo.substring(0, Math.min(6, titulo.length())),
                "Analista Judiciário"));
    }

    /**
     * Busca um usuário pelo seu endereço de email.
     * <p>
     * <b>Implementação Mock:</b> Gera um usuário de exemplo a partir do nome de
     * usuário extraído do email.
     *
     * @param email O email do usuário.
     * @return Um {@link Optional} contendo o {@link UsuarioDto} mockado.
     */
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
     * Retorna uma lista de todos os usuários ativos no sistema.
     * <p>
     * <b>Implementação Mock:</b> Retorna uma lista fixa de três usuários de exemplo.
     *
     * @return Uma {@link List} de {@link UsuarioDto}.
     */
    public List<UsuarioDto> buscarUsuariosAtivos() {
        log.warn("MOCK SGRH: Listando usuários ativos");
        return List.of(
                new UsuarioDto("123456789012", "João Silva", "joao.silva@tre-pe.jus.br", "MAT001",
                        "Analista Judiciário"),
                new UsuarioDto("987654321098", "Maria Santos", "maria.santos@tre-pe.jus.br", "MAT002",
                        "Técnico Judiciário"),
                new UsuarioDto("111222333444", "Pedro Oliveira", "pedro.oliveira@tre-pe.jus.br", "MAT003",
                        "Analista Judiciário"));
    }

    /**
     * Busca uma unidade organizacional pelo seu código.
     * <p>
     * <b>Implementação Mock:</b> Busca a unidade em um mapa de unidades de exemplo.
     *
     * @param codigo O código da unidade.
     * @return Um {@link Optional} contendo o {@link UnidadeDto} se a unidade for
     *         encontrada, ou vazio caso contrário.
     */
    public Optional<UnidadeDto> buscarUnidadePorCodigo(Long codigo) {
        log.warn("MOCK SGRH: Buscando unidade por código.");
        Map<Long, UnidadeDto> unidadesMock = criarUnidadesMock();
        return Optional.ofNullable(unidadesMock.get(codigo));
    }

    /**
     * Retorna uma lista de todas as unidades organizacionais ativas.
     * <p>
     * <b>Implementação Mock:</b> Retorna uma lista fixa de unidades de exemplo.
     *
     * @return Uma {@link List} de {@link UnidadeDto}.
     */
    public List<UnidadeDto> buscarUnidadesAtivas() {
        log.warn("MOCK SGRH: Listando unidades ativas");
        return new ArrayList<>(criarUnidadesMock().values());
    }

    /**
     * Busca as subunidades diretas de uma unidade pai.
     * <p>
     * <b>Implementação Mock:</b> Filtra a lista de unidades de exemplo para
     * encontrar aquelas cujo {@code codigoPai} corresponde ao ID fornecido.
     *
     * @param codigoPai O código da unidade pai.
     * @return Uma {@link List} de {@link UnidadeDto} representando as subunidades diretas.
     */
    public List<UnidadeDto> buscarSubunidades(Long codigoPai) {
        log.warn("MOCK SGRH: Buscando subunidades.");
        return criarUnidadesMock().values().stream()
                .filter(u -> codigoPai.equals(u.codigoPai()))
                .collect(Collectors.toList());
    }

    /**
     * Constrói a árvore hierárquica completa de unidades organizacionais.
     * <p>
     * <b>Implementação Mock:</b> Monta uma estrutura aninhada a partir da lista
     * de unidades de exemplo, identificando as unidades raiz e associando
     * recursivamente suas subunidades.
     *
     * @return Uma {@link List} de {@link UnidadeDto} representando as unidades
     *         raiz, com suas subunidades aninhadas.
     */
    public List<UnidadeDto> construirArvoreHierarquica() {
        log.warn("MOCK SGRH: Construindo árvore hierárquica de unidades");

        Map<Long, UnidadeDto> todasUnidades = criarUnidadesMock();
        Map<Long, List<UnidadeDto>> subunidadesPorPai = new HashMap<>();

        // Agrupar subunidades por pai
        for (UnidadeDto unidade : todasUnidades.values()) {
            if (unidade.codigoPai() != null) {
                subunidadesPorPai
                        .computeIfAbsent(unidade.codigoPai(), x -> new ArrayList<>())
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
                .toList();

        return new UnidadeDto(
                unidade.codigo(),
                unidade.nome(),
                unidade.sigla(),
                unidade.codigoPai(),
                unidade.tipo(),
                filhos.isEmpty() ? null : filhos);
    }

    /**
     * Busca os responsáveis (titular e substituto) por uma unidade específica.
     * <p>
     * <b>Implementação Mock:</b> Retorna um DTO com dados de exemplo fixos.
     *
     * @param unidadeCodigo O código da unidade.
     * @return Um {@link Optional} contendo o {@link ResponsavelDto} mockado.
     */
    public Optional<ResponsavelDto> buscarResponsavelUnidade(Long unidadeCodigo) {
        log.warn("MOCK SGRH: Buscando responsável da unidade.");

        return Optional.of(new ResponsavelDto(
                unidadeCodigo,
                "123456789012",
                "João Silva (Titular)",
                "987654321098",
                "Maria Santos (Substituto)"));
    }

    /**
     * Busca os responsáveis por uma lista de unidades em uma única chamada.
     * <p>
     * <b>Implementação Mock:</b> Gera dados de exemplo para cada código de unidade fornecido.
     *
     * @param unidadesCodigos A {@link List} de códigos das unidades.
     * @return Um {@link Map} onde a chave é o código da unidade e o valor é o
     *         {@link ResponsavelDto} correspondente.
     */
    public Map<Long, ResponsavelDto> buscarResponsaveisUnidades(List<Long> unidadesCodigos) {
        log.warn("MOCK SGRH: Buscando responsáveis de {} unidades em lote", unidadesCodigos.size());
        return unidadesCodigos.stream()
                .collect(Collectors.toMap(
                        codigo -> codigo,
                        codigo -> new ResponsavelDto(
                                codigo,
                                String.format("%012d", codigo), // titular
                                "Titular da Unidade " + codigo,
                                String.format("%012d", codigo + 1000), // substituto
                                "Substituto da Unidade " + codigo
                        )
                ));
    }

    /**
     * Busca múltiplos usuários por seus títulos de eleitor em uma única chamada.
     * <p>
     * <b>Implementação Mock:</b> Gera um usuário de exemplo para cada título fornecido.
     *
     * @param titulos A {@link List} de títulos de eleitor.
     * @return Um {@link Map} onde a chave é o título de eleitor e o valor é o
     *         {@link UsuarioDto} correspondente.
     */
    public Map<String, UsuarioDto> buscarUsuariosPorTitulos(List<String> titulos) {
        log.warn("MOCK SGRH: Buscando {} usuários por título em lote", titulos.size());
        return titulos.stream()
                .collect(Collectors.toMap(
                        titulo -> titulo,
                        titulo -> new UsuarioDto(
                                titulo,
                                "Usuário Mock " + titulo,
                                titulo + "@tre-pe.jus.br",
                                "MAT" + titulo.substring(0, Math.min(6, titulo.length())),
                                "Analista Judiciário"
                        ),
                        (u1, u2) -> u1 // Em caso de duplicatas
                ));
    }

    /**
     * Busca os códigos de todas as unidades onde um usuário é responsável (titular ou substituto).
     * <p>
     * <b>Implementação Mock:</b> Retorna uma lista fixa de códigos de unidade.
     *
     * @param titulo O título de eleitor do usuário.
     * @return Uma {@link List} de códigos de unidade.
     */
    public List<Long> buscarUnidadesOndeEhResponsavel(String titulo) {
        log.warn("MOCK SGRH: Buscando unidades onde o usuário é responsável.");
        // Mock: retorna algumas unidades
        return List.of(1L, 2L, 10L);
    }

    /**
     * Busca todos os perfis de um usuário, incluindo as unidades associadas a cada perfil.
     * <p>
     * <b>Implementação Mock:</b> Retorna uma lista fixa de perfis de exemplo.
     *
     * @param titulo O título de eleitor do usuário.
     * @return Uma {@link List} de {@link PerfilDto}.
     */
    public List<PerfilDto> buscarPerfisUsuario(String titulo) {
        log.warn("MOCK SGRH: Buscando perfis do usuário.");

        return List.of(
                new PerfilDto(titulo, 1L, "SEDOC - Secretaria de Documentação", "ADMIN"),
                new PerfilDto(titulo, 2L, "CGC - Coordenadoria de Gestão de Competências", "GESTOR"),
                new PerfilDto(titulo, 10L, "SETEC - Secretaria de Tecnologia", "SERVIDOR"));
    }

    /**
     * Verifica se um usuário possui um perfil específico em uma determinada unidade.
     * <p>
     * <b>Implementação Mock:</b> Retorna {@code true} apenas para a combinação
     * específica de perfil 'ADMIN' e unidade com código 1.
     *
     * @param titulo        O título de eleitor do usuário.
     * @param perfil        O nome do perfil a ser verificado (e.g., "ADMIN").
     * @param unidadeCodigo O código da unidade.
     * @return {@code true} se o usuário tiver o perfil na unidade, {@code false} caso contrário.
     */
    public boolean usuarioTemPerfil(String titulo, String perfil, Long unidadeCodigo) {
        log.warn("MOCK SGRH: Verificando se o usuário tem perfil na unidade.");

        // Mock: retorna true para alguns casos
        return "ADMIN".equals(perfil) && unidadeCodigo == 1L;
    }

    /**
     * Busca os códigos de todas as unidades onde um usuário possui um perfil específico.
     * <p>
     * <b>Implementação Mock:</b> Retorna listas fixas de códigos de unidade com
     * base no perfil solicitado.
     *
     * @param titulo O título de eleitor do usuário.
     * @param perfil O nome do perfil.
     * @return Uma {@link List} de códigos de unidade.
     */
    public List<Long> buscarUnidadesPorPerfil(String titulo, String perfil) {
        log.warn("MOCK SGRH: Buscando unidades onde o usuário tem perfil.");

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
        unidades.put(1L, new UnidadeDto(1L, "SEDOC - Secretaria de Documentação", "SEDOC", null, "ADMINISTRATIVA"));

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