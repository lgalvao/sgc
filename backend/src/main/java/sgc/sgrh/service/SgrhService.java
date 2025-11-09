package sgc.sgrh.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sgc.sgrh.dto.PerfilDto;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.dto.UsuarioDto;
import sgc.unidade.model.UnidadeRepo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SgrhService {
    final UnidadeRepo unidadeRepo;

    // Mapa estático para controle de mocks em testes E2E
    public static final Map<String, List<PerfilDto>> perfisMock = new ConcurrentHashMap<>();

    public Optional<UsuarioDto> buscarUsuarioPorTitulo(String titulo) {
        log.warn("MOCK SGRH: Buscando usuário por título.");

        return Optional.of(new UsuarioDto(titulo,
                "Usuário Mock %s".formatted(titulo),
                "%s@tre-pe.jus.br".formatted(titulo),
                "MAT%s".formatted(titulo.substring(0, Math.min(6, titulo.length()))),
                "Analista Judiciário"));
    }

    public List<PerfilDto> buscarPerfisUsuario(String titulo) {
        if (!perfisMock.isEmpty() && perfisMock.containsKey(titulo)) {
            log.info("Retornando perfis mockados para o usuário {}", titulo);
            return perfisMock.get(titulo);
        }
        log.warn("MOCK SGRH: Buscando perfis do usuário (padrão).");
        return List.of(
                new PerfilDto(titulo, 1L, "SEDOC", "ADMIN"),
                new PerfilDto(titulo, 2L, "SGP", "GESTOR")
        );
    }

    public Optional<UsuarioDto> buscarUsuarioPorEmail(String email) {
        String titulo = email.split("@")[0];
        return Optional.of(new UsuarioDto(titulo, "Usuário %s".formatted(titulo), email, "MAT%d".formatted(titulo.hashCode()), "Analista Judiciário"));
    }

    public List<UsuarioDto> buscarUsuariosAtivos() {
        log.warn("MOCK SGRH: Listando usuários ativos");
        return List.of(
                new UsuarioDto("123456789012", "João Silva", "joao.silva@tre-pe.jus.br", "MAT001", "Analista Judiciário"),
                new UsuarioDto("987654321098", "Maria Santos", "maria.santos@tre-pe.jus.br", "MAT002", "Técnico Judiciário"),
                new UsuarioDto("111222333444", "Pedro Oliveira", "pedro.oliveira@tre-pe.jus.br", "MAT003", "Analista Judiciário"));
    }

    public Optional<UnidadeDto> buscarUnidadePorCodigo(Long codigo) {
        log.warn("MOCK SGRH: Buscando unidade por código.");
        Map<Long, UnidadeDto> unidadesMock = criarUnidadesMock();
        return Optional.ofNullable(unidadesMock.get(codigo));
    }

    public List<UnidadeDto> buscarUnidadesAtivas() {
        log.warn("MOCK SGRH: Listando unidades ativas");
        return new ArrayList<>(criarUnidadesMock().values());
    }

    public List<UnidadeDto> buscarSubunidades(Long codigoPai) {
        log.warn("MOCK SGRH: Buscando subunidades.");
        return criarUnidadesMock().values().stream()
                .filter(u -> codigoPai.equals(u.getCodigoPai()))
                .collect(Collectors.toList());
    }

    public List<UnidadeDto> construirArvoreHierarquica() {
        log.warn("MOCK SGRH: Construindo árvore hierárquica de unidades");
        Map<Long, UnidadeDto> todasUnidades = criarUnidadesMock();
        Map<Long, List<UnidadeDto>> subunidadesPorPai = new HashMap<>();
        for (UnidadeDto unidade : todasUnidades.values()) {
            if (unidade.getCodigoPai() != null) {
                subunidadesPorPai.computeIfAbsent(unidade.getCodigoPai(), x -> new ArrayList<>()).add(unidade);
            }
        }
        return todasUnidades.values().stream()
                .filter(u -> u.getCodigoPai() == null)
                .map(u -> construirArvore(u, subunidadesPorPai))
                .collect(Collectors.toList());
    }

    private UnidadeDto construirArvore(UnidadeDto unidade, Map<Long, List<UnidadeDto>> subunidadesPorPai) {
        List<UnidadeDto> filhos = subunidadesPorPai.getOrDefault(unidade.getCodigo(), Collections.emptyList())
                .stream()
                .map(filho -> construirArvore(filho, subunidadesPorPai))
                .toList();
        return new UnidadeDto(unidade.getCodigo(), unidade.getNome(), unidade.getSigla(), unidade.getCodigoPai(), unidade.getTipo(), filhos.isEmpty() ? null : filhos);
    }

    public Optional<ResponsavelDto> buscarResponsavelUnidade(Long unidadeCodigo) {
        log.warn("MOCK SGRH: Buscando responsável da unidade.");
        return Optional.of(new ResponsavelDto(unidadeCodigo, "123456789012", "João Silva (Titular)", "987654321098", "Maria Santos (Substituto)"));
    }

    public Map<Long, ResponsavelDto> buscarResponsaveisUnidades(List<Long> unidadesCodigos) {
        log.warn("MOCK SGRH: Buscando responsáveis de {} unidades em lote", unidadesCodigos.size());
        return unidadesCodigos.stream().collect(Collectors.toMap(codigo -> codigo, codigo -> new ResponsavelDto(codigo, String.format("%012d", codigo), "Titular da Unidade " + codigo, String.format("%012d", codigo + 1000), "Substituto da Unidade " + codigo)));
    }

    public Map<String, UsuarioDto> buscarUsuariosPorTitulos(List<String> titulos) {
        log.warn("MOCK SGRH: Buscando {} usuários por título em lote", titulos.size());
        return titulos.stream().collect(Collectors.toMap(titulo -> titulo, titulo -> new UsuarioDto(
                titulo,
                "Usuário Mock %s".formatted(titulo),
                "%s@tre-pe.jus.br".formatted(titulo),
                "MAT%s".formatted(titulo.substring(0, Math.min(6, titulo.length()))),
                "Analista Judiciário"), (u1, u2) -> u1)
        );
    }

    public List<Long> buscarUnidadesOndeEhResponsavel(String titulo) {
        log.warn("MOCK SGRH: Buscando unidades onde o usuário é responsável.");
        return List.of(1L, 2L, 10L);
    }

    public boolean usuarioTemPerfil(String titulo, String perfil, Long unidadeCodigo) {
        log.warn("MOCK SGRH: Verificando se o usuário tem perfil na unidade.");
        return "ADMIN".equals(perfil) && unidadeCodigo == 1L;
    }

    public List<Long> buscarUnidadesPorPerfil(String titulo, String perfil) {
        log.warn("MOCK SGRH: Buscando unidades onde o usuário tem perfil.");
        return switch (perfil) {
            case "ADMIN" -> List.of(1L);
            case "GESTOR" -> List.of(2L, 3L);
            case "CHEFE" -> List.of(10L, 11L);
            default -> List.of(1L, 2L, 10L);
        };
    }

    private Map<Long, UnidadeDto> criarUnidadesMock() {
        Map<Long, UnidadeDto> unidadesDto = new HashMap<>();
        unidadesDto.put(2L, new UnidadeDto(2L, "Secretaria de Informática e Comunicações", "STIC", null, "INTEROPERACIONAL"));
        unidadesDto.put(3L, new UnidadeDto(3L, "Secretaria de Gestao de Pessoas", "SGP", 2L, "INTERMEDIARIA"));
        unidadesDto.put(6L, new UnidadeDto(6L, "Coordenadoria de Sistemas", "COSIS", 2L, "INTERMEDIARIA"));
        unidadesDto.put(7L, new UnidadeDto(7L, "Coordenadoria de Suporte e Infraestrutura", "COSINF", 2L, "INTERMEDIARIA"));
        unidadesDto.put(14L, new UnidadeDto(14L, "Coordenadoria Jurídica", "COJUR", 2L, "INTERMEDIARIA"));
        unidadesDto.put(4L, new UnidadeDto(4L, "Coordenadoria de Educação Especial", "COEDE", 3L, "INTERMEDIARIA"));
        unidadesDto.put(8L, new UnidadeDto(8L, "Seção de Desenvolvimento de Sistemas", "SEDESENV", 6L, "OPERACIONAL"));
        unidadesDto.put(9L, new UnidadeDto(9L, "Seção de Dados e Inteligência Artificial", "SEDIA", 6L, "OPERACIONAL"));
        unidadesDto.put(10L, new UnidadeDto(10L, "Seção de Sistemas Eleitorais", "SESEL", 6L, "OPERACIONAL"));
        unidadesDto.put(11L, new UnidadeDto(11L, "Seção de Infraestrutura", "SENIC", 7L, "OPERACIONAL"));
        unidadesDto.put(12L, new UnidadeDto(12L, "Seção Jurídica", "SEJUR", 14L, "OPERACIONAL"));
        unidadesDto.put(13L, new UnidadeDto(13L, "Seção de Processos", "SEPRO", 14L, "OPERACIONAL"));
        unidadesDto.put(15L, new UnidadeDto(15L, "Seção de Documentação", "SEDOC", 2L, "OPERACIONAL"));
        unidadesDto.put(5L, new UnidadeDto(5L, "Seção Magistrados e Requisitados", "SEMARE", 4L, "OPERACIONAL"));
        return unidadesDto;
    }
}