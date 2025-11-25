package sgc.sgrh.service;

import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sgc.sgrh.dto.PerfilDto;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.dto.UsuarioDto;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.unidade.model.UnidadeRepo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SgrhService {
    final UnidadeRepo unidadeRepo;
    final UsuarioRepo usuarioRepo;

    // Mapa estático para controle de mocks em testes E2E
    public static final Map<String, List<PerfilDto>> perfisMock = new ConcurrentHashMap<>();

    public Optional<UsuarioDto> buscarUsuarioPorTitulo(String titulo) {
        log.warn("MOCK SGRH: Buscando usuário por título.");

        return Optional.of(UsuarioDto.builder()
                .titulo(titulo)
                .nome("Usuário Mock %s".formatted(titulo))
                .email("%s@tre-pe.jus.br".formatted(titulo))
                .matricula("MAT%s".formatted(titulo.substring(0, Math.min(6, titulo.length()))))
                .cargo("Analista Judiciário")
                .build());
    }

    public Usuario buscarUsuarioPorLogin(String login) {
        return usuarioRepo.findById(login)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Usuário", login));
    }

    public Usuario buscarResponsavelVigente(String sigla) {
        log.warn("MOCK SGRH: Buscando responsável vigente para a sigla {}.", sigla);
        var unidade = unidadeRepo.findBySigla(sigla).orElse(null);
        return new Usuario("responsavel", "Responsável Vigente", "email", "ramal", unidade, Set.of(Perfil.CHEFE));
    }

    public List<PerfilDto> buscarPerfisUsuario(String titulo) {
        if (!perfisMock.isEmpty() && perfisMock.containsKey(titulo)) {
            log.info("Retornando perfis mockados para o usuário {}", titulo);
            return perfisMock.get(titulo);
        }
        log.warn("MOCK SGRH: Buscando perfis do usuário (padrão).");

        if ("1".equals(titulo)) { // Servidor Ana Paula Souza (SESEL)
            return List.of(PerfilDto.builder().usuarioTitulo(titulo).unidadeCodigo(10L).unidadeNome("SESEL").perfil("SERVIDOR").build());
        } else if ("6".equals(titulo)) { // Admin Ricardo Alves (STIC)
            return List.of(PerfilDto.builder().usuarioTitulo(titulo).unidadeCodigo(2L).unidadeNome("STIC").perfil("ADMIN").build());
        } else if ("777".equals(titulo)) { // Chefe STIC Teste (STIC)
            return List.of(PerfilDto.builder().usuarioTitulo(titulo).unidadeCodigo(2L).unidadeNome("STIC").perfil("CHEFE").build());
        } else if ("2".equals(titulo)) { // Chefe SGP
            return List.of(PerfilDto.builder().usuarioTitulo(titulo).unidadeCodigo(200L).unidadeNome("SGP").perfil("CHEFE").build());
        } else if ("3".equals(titulo)) { // Chefe SEDESENV
            return List.of(PerfilDto.builder().usuarioTitulo(titulo).unidadeCodigo(8L).unidadeNome("SEDESENV").perfil("CHEFE").build());
        } else if ("8".equals(titulo)) { // Gestor Paulo Horta (SEDESENV)
            return List.of(PerfilDto.builder().usuarioTitulo(titulo).unidadeCodigo(8L).unidadeNome("SEDESENV").perfil("GESTOR").build());
        } else if ("10".equals(titulo)) { // Chefe SEDIA (SEDIA)
            return List.of(PerfilDto.builder().usuarioTitulo(titulo).unidadeCodigo(9L).unidadeNome("SEDIA").perfil("CHEFE").build());
        } else if ("999999999999".equals(titulo)) { // Usuario Multi Perfil (STIC)
            return List.of(
                PerfilDto.builder().usuarioTitulo(titulo).unidadeCodigo(2L).unidadeNome("STIC").perfil("ADMIN").build(),
                PerfilDto.builder().usuarioTitulo(titulo).unidadeCodigo(2L).unidadeNome("STIC").perfil("GESTOR").build()
            );
        }

        // Default behavior for other users if not explicitly mocked
        return List.of(
            PerfilDto.builder().usuarioTitulo(titulo).unidadeCodigo(1L).unidadeNome("SEDOC").perfil("ADMIN").build(),
            PerfilDto.builder().usuarioTitulo(titulo).unidadeCodigo(2L).unidadeNome("SGP").perfil("GESTOR").build()
        );
    }

    public Optional<UsuarioDto> buscarUsuarioPorEmail(String email) {
        String titulo = email.split("@")[0];
        return Optional.of(UsuarioDto.builder()
                .titulo(titulo)
                .nome("Usuário %s".formatted(titulo))
                .email(email)
                .matricula("MAT%d".formatted(titulo.hashCode()))
                .cargo("Analista Judiciário")
                .build());
    }

    public List<UsuarioDto> buscarUsuariosAtivos() {
        log.warn("MOCK SGRH: Listando usuários ativos");
        return List.of(
                UsuarioDto.builder()
                        .titulo("123456789012")
                        .nome("João Silva")
                        .email("joao.silva@tre-pe.jus.br")
                        .matricula("MAT001")
                        .cargo("Analista Judiciário")
                        .build(),
                UsuarioDto.builder()
                        .titulo("987654321098")
                        .nome("Maria Santos")
                        .email("maria.santos@tre-pe.jus.br")
                        .matricula("MAT002")
                        .cargo("Técnico Judiciário")
                        .build(),
                UsuarioDto.builder()
                        .titulo("111222333444")
                        .nome("Pedro Oliveira")
                        .email("pedro.oliveira@tre-pe.jus.br")
                        .matricula("MAT003")
                        .cargo("Analista Judiciário")
                        .build());
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
        return UnidadeDto.builder()
                .codigo(unidade.getCodigo())
                .nome(unidade.getNome())
                .sigla(unidade.getSigla())
                .codigoPai(unidade.getCodigoPai())
                .tipo(unidade.getTipo())
                .subunidades(filhos.isEmpty() ? null : filhos)
                .isElegivel(false)
                .build();
    }

    public Optional<ResponsavelDto> buscarResponsavelUnidade(Long unidadeCodigo) {
        log.warn("MOCK SGRH: Buscando responsável da unidade.");
        return Optional.of(ResponsavelDto.builder()
                .unidadeCodigo(unidadeCodigo)
                .titularTitulo("123456789012")
                .titularNome("João Silva (Titular)")
                .substitutoTitulo("987654321098")
                .substitutoNome("Maria Santos (Substituto)")
                .build());
    }

    public Map<Long, ResponsavelDto> buscarResponsaveisUnidades(List<Long> unidadesCodigos) {
        log.warn("MOCK SGRH: Buscando responsáveis de {} unidades em lote", unidadesCodigos.size());
        return unidadesCodigos.stream().collect(Collectors.toMap(codigo -> codigo, codigo -> ResponsavelDto.builder()
                .unidadeCodigo(codigo)
                .titularTitulo(String.format("%012d", codigo))
                .titularNome("Titular da Unidade " + codigo)
                .substitutoTitulo(String.format("%012d", codigo + 1000))
                .substitutoNome("Substituto da Unidade " + codigo)
                .build()));
    }

    public Map<String, UsuarioDto> buscarUsuariosPorTitulos(List<String> titulos) {
        log.warn("MOCK SGRH: Buscando {} usuários por título em lote", titulos.size());
        return titulos.stream().collect(Collectors.toMap(titulo -> titulo, titulo -> UsuarioDto.builder()
                .titulo(titulo)
                .nome("Usuário Mock %s".formatted(titulo))
                .email("%s@tre-pe.jus.br".formatted(titulo))
                .matricula("MAT%s".formatted(titulo.substring(0, Math.min(6, titulo.length()))))
                .cargo("Analista Judiciário")
                .build(), (u1, u2) -> u1)
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
        unidadesDto.put(2L, UnidadeDto.builder().codigo(2L).nome("Secretaria de Informática e Comunicações").sigla("STIC").codigoPai(null).tipo("INTEROPERACIONAL").isElegivel(false).build());
        unidadesDto.put(3L, UnidadeDto.builder().codigo(3L).nome("Secretaria de Gestao de Pessoas").sigla("SGP").codigoPai(2L).tipo("INTERMEDIARIA").isElegivel(false).build());
        unidadesDto.put(6L, UnidadeDto.builder().codigo(6L).nome("Coordenadoria de Sistemas").sigla("COSIS").codigoPai(2L).tipo("INTERMEDIARIA").isElegivel(false).build());
        unidadesDto.put(7L, UnidadeDto.builder().codigo(7L).nome("Coordenadoria de Suporte e Infraestrutura").sigla("COSINF").codigoPai(2L).tipo("INTERMEDIARIA").isElegivel(false).build());
        unidadesDto.put(14L, UnidadeDto.builder().codigo(14L).nome("Coordenadoria Jurídica").sigla("COJUR").codigoPai(2L).tipo("INTERMEDIARIA").isElegivel(false).build());
        unidadesDto.put(4L, UnidadeDto.builder().codigo(4L).nome("Coordenadoria de Educação Especial").sigla("COEDE").codigoPai(3L).tipo("INTERMEDIARIA").isElegivel(false).build());
        unidadesDto.put(8L, UnidadeDto.builder().codigo(8L).nome("Seção de Desenvolvimento de Sistemas").sigla("SEDESENV").codigoPai(6L).tipo("OPERACIONAL").isElegivel(false).build());
        unidadesDto.put(9L, UnidadeDto.builder().codigo(9L).nome("Seção de Dados e Inteligência Artificial").sigla("SEDIA").codigoPai(6L).tipo("OPERACIONAL").isElegivel(false).build());
        unidadesDto.put(10L, UnidadeDto.builder().codigo(10L).nome("Seção de Sistemas Eleitorais").sigla("SESEL").codigoPai(6L).tipo("OPERACIONAL").isElegivel(false).build());
        unidadesDto.put(11L, UnidadeDto.builder().codigo(11L).nome("Seção de Infraestrutura").sigla("SENIC").codigoPai(7L).tipo("OPERACIONAL").isElegivel(false).build());
        unidadesDto.put(12L, UnidadeDto.builder().codigo(12L).nome("Seção Jurídica").sigla("SEJUR").codigoPai(14L).tipo("OPERACIONAL").isElegivel(false).build());
        unidadesDto.put(13L, UnidadeDto.builder().codigo(13L).nome("Seção de Processos").sigla("SEPRO").codigoPai(14L).tipo("OPERACIONAL").isElegivel(false).build());
        unidadesDto.put(15L, UnidadeDto.builder().codigo(15L).nome("Seção de Documentação").sigla("SEDOC").codigoPai(2L).tipo("OPERACIONAL").isElegivel(false).build());
        unidadesDto.put(5L, UnidadeDto.builder().codigo(5L).nome("Seção Magistrados e Requisitados").sigla("SEMARE").codigoPai(4L).tipo("OPERACIONAL").isElegivel(false).build());
        return unidadesDto;
    }
}
