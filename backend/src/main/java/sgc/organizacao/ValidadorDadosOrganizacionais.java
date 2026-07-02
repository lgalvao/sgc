package sgc.organizacao;

import lombok.*;
import lombok.extern.slf4j.*;
import org.jspecify.annotations.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.config.CacheConfig;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static java.util.stream.Collectors.*;
import static sgc.organizacao.model.Perfil.*;
import static sgc.organizacao.model.SituacaoUnidade.*;
import static sgc.organizacao.model.TipoUnidade.*;

/**
 * Diagnostica invariantes organizacionais críticas sob demanda.
 *
 * <p>Estas invariantes dependem de views externas e precisam estar íntegras
 * para que login, permissões, notificações e fluxos operacionais funcionem corretamente.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ValidadorDadosOrganizacionais {
    private static final Set<TipoUnidade> TIPOS_PARTICIPANTES = Set.of(OPERACIONAL, INTERMEDIARIA, INTEROPERACIONAL);
    private final UsuarioRepo usuarioRepo;
    private final CacheViewsOrganizacaoService cacheViewsOrganizacaoService;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Cacheable(cacheNames = CacheConfig.CACHE_DIAGNOSTICO_ORGANIZACIONAL, sync = true)
    @Transactional(readOnly = true)
    public DiagnosticoOrganizacionalDto diagnosticar() {
        Map<String, List<String>> violacoesPorTipo = new LinkedHashMap<>();

        var unidadesParticipantes = carregarUnidadesParticipantes();
        var responsabilidadesPorUnidade = carregarResponsabilidades(unidadesParticipantes);
        var resultadoUsuarios = carregarUsuarios(unidadesParticipantes, responsabilidadesPorUnidade);
        var usuariosPorTitulo = resultadoUsuarios.usuariosPorTitulo();
        var usuariosConsultaPorUnidade = carregarUsuariosConsultaPorUnidade(unidadesParticipantes);
        var resultadoPerfis = carregarPerfis(unidadesParticipantes);
        var perfisPorUnidade = resultadoPerfis.perfisPorUnidade();
        var unidadesSemResponsavel = obterUnidadesSemResponsavel(unidadesParticipantes, responsabilidadesPorUnidade);

        validarDuplicidadeUsuarios(resultadoUsuarios.titulosDuplicados(), violacoesPorTipo);
        validarIntegridadePerfis(resultadoPerfis.perfisInvalidos(), unidadesSemResponsavel, violacoesPorTipo);
        validarResponsabilidade(unidadesParticipantes, responsabilidadesPorUnidade, violacoesPorTipo);
        validarUsuariosReferenciados(unidadesParticipantes, responsabilidadesPorUnidade, usuariosPorTitulo, violacoesPorTipo);
        validarUsuariosSemEmail(unidadesParticipantes, usuariosConsultaPorUnidade, violacoesPorTipo);
        validarUnidadesIntermediarias(unidadesParticipantes, responsabilidadesPorUnidade, perfisPorUnidade, violacoesPorTipo);

        if (violacoesPorTipo.isEmpty()) {
            log.info("Diagnostico organizacional concluido sem violacoes. {} unidades verificadas.", unidadesParticipantes.size());
            return DiagnosticoOrganizacionalDto.semViolacoes();
        }

        violacoesPorTipo.forEach((tipo, detalhes) ->
                log.warn("DIAGN. ORGANIZACIONAL [{}]: {} ocorrência(s). {}",
                        tipo,
                        detalhes.size(),
                        String.join("; ", detalhes))
        );

        int quantidadeOcorrencias = violacoesPorTipo.values().stream().mapToInt(List::size).sum();
        var grupos = violacoesPorTipo.entrySet().stream()
                .map(entry -> new GrupoViolacaoOrganizacionalDto(
                        entry.getKey(),
                        entry.getValue().size(),
                        List.copyOf(entry.getValue())
                ))
                .toList();

        String resumo = construirResumo(violacoesPorTipo);
        return new DiagnosticoOrganizacionalDto(
                true,
                resumo,
                violacoesPorTipo.size(),
                quantidadeOcorrencias,
                grupos
        );
    }

    private String construirResumo(int quantidadeTiposViolacao, int quantidadeOcorrencias) {
        return "Foram encontradas inconsistências nos dados organizacionais.";
    }

    private String construirResumo(Map<String, List<String>> violacoesPorTipo) {
        List<String> unidadesSemResponsavel = violacoesPorTipo
                .getOrDefault("Unidade sem responsável", List.of())
                .stream()
                .<String>mapMulti((detalhe, consumer) -> {
                    String sigla = extrairSigla(detalhe);
                    if (sigla != null) {
                        consumer.accept(sigla);
                    }
                })
                .distinct()
                .toList();

        if (!unidadesSemResponsavel.isEmpty() && violacoesPorTipo.size() == 1) {
            return "Foram encontradas inconsistências nos dados organizacionais.";
        }

        return construirResumo(violacoesPorTipo.size(), violacoesPorTipo.values().stream()
                .mapToInt(List::size)
                .sum());
    }

    @Nullable
    String extrairSigla(String detalhe) {
        String prefixo = "sigla=";
        int inicio = detalhe.indexOf(prefixo);
        if (inicio < 0) return null;

        int fim = detalhe.indexOf(",", inicio);
        if (fim < 0) fim = detalhe.length();

        String sigla = detalhe.substring(inicio + prefixo.length(), fim).trim();
        return sigla.isBlank() ? null : sigla;
    }

    private List<UnidadeHierarquiaLeitura> carregarUnidadesParticipantes() {
        return cacheViewsOrganizacaoService.listarTodasUnidades().stream()
                .filter(unidade -> unidade.situacao() == ATIVA)
                .filter(unidade -> TIPOS_PARTICIPANTES.contains(unidade.tipo()))
                .toList();
    }

    private Map<Long, ResponsabilidadeLeitura> carregarResponsabilidades(List<UnidadeHierarquiaLeitura> unidades) {
        Set<Long> codigos = unidades.stream()
                .map(UnidadeHierarquiaLeitura::codigo)
                .collect(toSet());

        if (codigos.isEmpty()) return Map.of();

        return cacheViewsOrganizacaoService.listarTodasResponsabilidades().stream()
                .filter(responsabilidade -> codigos.contains(responsabilidade.unidadeCodigo()))
                .collect(toMap(
                        ResponsabilidadeLeitura::unidadeCodigo,
                        Function.identity(),
                        (responsabilidadeAtual, responsabilidadeDuplicada) -> responsabilidadeAtual
                ));
    }

    private ResultadoCargaUsuarios carregarUsuarios(
            List<UnidadeHierarquiaLeitura> unidades,
            Map<Long, ResponsabilidadeLeitura> respPorUnidade
    ) {
        List<String> titulos = Stream.concat(
                        unidades.stream().map(UnidadeHierarquiaLeitura::tituloTitular),
                        respPorUnidade.values().stream().map(ResponsabilidadeLeitura::usuarioTitulo)
                )
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(titulo -> !titulo.isBlank())
                .distinct()
                .toList();

        if (titulos.isEmpty()) return new ResultadoCargaUsuarios(Map.of(), Set.of());

        Set<String> titulosDuplicados = diagnosticarTitulosDuplicados(titulos);
        List<Usuario> usuarios = usuarioRepo.findAllById(titulos);
        Map<String, Usuario> usuariosPorTitulo = usuarios.stream().collect(toMap(
                Usuario::getTituloEleitoral,
                Function.identity(),
                (usuarioAtual, usuarioDuplicado) -> usuarioAtual
        ));

        return new ResultadoCargaUsuarios(usuariosPorTitulo, titulosDuplicados);
    }

    private ResultadoCargaPerfis carregarPerfis(List<UnidadeHierarquiaLeitura> unidades) {
        List<Long> codigos = unidades.stream()
                .map(UnidadeHierarquiaLeitura::codigo)
                .toList();

        if (codigos.isEmpty()) return new ResultadoCargaPerfis(Map.of(), List.of());

        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList("""
                SELECT usuario_titulo, perfil, unidade_codigo
                FROM sgc.vw_usuario_perfil_unidade
                WHERE unidade_codigo IN (:codigos)
                   OR unidade_codigo IS NULL
                """, Map.of("codigos", codigos));

        List<Map<String, Object>> linhas = new ArrayList<>(result);

        List<PerfilInvalido> perfisInvalidos = new ArrayList<>();
        Map<Long, Set<PerfilUsuarioUnidade>> perfisPorUnidade = new LinkedHashMap<>();
        Map<ChavePerfilUsuarioUnidade, Integer> contagemPorChave = new LinkedHashMap<>();

        ContextoCargaPerfis contexto = new ContextoCargaPerfis(perfisInvalidos, perfisPorUnidade, contagemPorChave);
        linhas.forEach(linha -> processarLinhaPerfil(linha, contexto));

        contagemPorChave.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .forEach(entry -> perfisInvalidos.add(new PerfilInvalido(
                        "VW_USUARIO_PERFIL_UNIDADE com chave duplicada",
                        "usuario_titulo=%s, perfil=%s, unidade_codigo=%s, quantidade=%s".formatted(
                                entry.getKey().usuarioTitulo(),
                                entry.getKey().perfil(),
                                entry.getKey().unidadeCodigo(),
                                entry.getValue()
                        ),
                        entry.getKey().unidadeCodigo()
                )));

        return new ResultadoCargaPerfis(perfisPorUnidade, perfisInvalidos);
    }

    private Map<Long, List<UsuarioConsultaLeitura>> carregarUsuariosConsultaPorUnidade(List<UnidadeHierarquiaLeitura> unidades) {
        Set<Long> codigos = unidades.stream()
                .map(UnidadeHierarquiaLeitura::codigo)
                .collect(toSet());

        if (codigos.isEmpty()) return Map.of();

        return cacheViewsOrganizacaoService.listarTodosUsuarios().stream()
                .filter(usuario -> usuario.unidadeCodigo() != null && codigos.contains(usuario.unidadeCodigo()))
                .collect(groupingBy(
                        UsuarioConsultaLeitura::unidadeCodigo,
                        LinkedHashMap::new,
                        toList()
                ));
    }

    private Set<String> diagnosticarTitulosDuplicados(List<String> titulos) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList("""
                SELECT titulo, COUNT(*) AS quantidade
                FROM sgc.VW_USUARIO
                WHERE titulo IN (:titulos)
                GROUP BY titulo
                HAVING COUNT(*) > 1
                """, Map.of("titulos", titulos));
        List<Map<String, Object>> linhas = new ArrayList<>(result);

        Set<String> titulosDuplicados = new TreeSet<>();
        for (Map<String, Object> linha : linhas) {
            String titulo = lerString(linha, "titulo");
            if (!vazio(titulo)) {
                titulosDuplicados.add(titulo);
            }
        }
        return titulosDuplicados;
    }

    @Nullable
    private String lerString(Map<String, Object> linha, String coluna) {
        Object valor = linha.get(coluna);
        if (valor == null) {
            valor = linha.get(coluna.toUpperCase(Locale.ROOT));
        }
        return valor != null ? valor.toString() : null;
    }

    @Nullable
    private Long lerLong(Map<String, Object> linha) {
        Object valor = linha.get("unidade_codigo");
        if (valor == null) {
            valor = linha.get("unidade_codigo".toUpperCase(Locale.ROOT));
        }

        if (valor == null) {
            return null;
        }

        if (valor instanceof Number numero) {
            return numero.longValue();
        }

        return Long.valueOf(valor.toString());
    }

    private String valorOuNulo(@Nullable Object valor) {
        return valor != null ? valor.toString() : "null";
    }

    private void validarResponsabilidade(
            List<UnidadeHierarquiaLeitura> unidades,
            Map<Long, ResponsabilidadeLeitura> respPorUnidade,
            Map<String, List<String>> violacoesPorTipo
    ) {
        unidades.forEach(unidade -> {
            ResponsabilidadeLeitura resp = respPorUnidade.get(unidade.codigo());
            if (resp == null || vazio(resp.usuarioTitulo())) {
                adicionarViolacao(violacoesPorTipo,
                        "Unidade sem responsável",
                        "sigla=%s, tipo=%s".formatted(unidade.sigla(), unidade.tipo()));
            }
        });
    }

    private Set<Long> obterUnidadesSemResponsavel(
            List<UnidadeHierarquiaLeitura> unidades,
            Map<Long, ResponsabilidadeLeitura> responsabilidadesPorUnidade
    ) {
        return unidades.stream()
                .map(UnidadeHierarquiaLeitura::codigo)
                .filter(codigo -> {
                    ResponsabilidadeLeitura resp = responsabilidadesPorUnidade.get(codigo);
                    return resp == null || vazio(resp.usuarioTitulo());
                })
                .collect(toSet());
    }

    private void validarUsuariosReferenciados(
            List<UnidadeHierarquiaLeitura> unidades,
            Map<Long, ResponsabilidadeLeitura> responsabilidadesPorUnidade,
            Map<String, Usuario> usuariosPorTitulo,
            Map<String, List<String>> violacoesPorTipo
    ) {
        unidades.forEach(unidade -> {
            String tituloTitular = unidade.tituloTitular();
            validarUsuarioExistente(
                    tituloTitular,
                    "Titular referenciado ausente na VW_USUARIO",
                    "sigla=%s, titulo_titular=%s".formatted(unidade.sigla(), valorOuNulo(tituloTitular)),
                    usuariosPorTitulo,
                    violacoesPorTipo
            );

            ResponsabilidadeLeitura responsabilidade = responsabilidadesPorUnidade.get(unidade.codigo());
            if (responsabilidade != null) {
                validarUsuarioExistente(
                        responsabilidade.usuarioTitulo(),
                        "Responsavel referenciado ausente na VW_USUARIO",
                        "sigla=%s, usuario_titulo=%s".formatted(unidade.sigla(), responsabilidade.usuarioTitulo()),
                        usuariosPorTitulo,
                        violacoesPorTipo
                );
            }
        });
    }

    private void validarUsuarioExistente(
            @Nullable String titulo,
            String tipoViolacao,
            String detalheViolacao,
            Map<String, Usuario> usuariosPorTitulo,
            Map<String, List<String>> violacoesPorTipo
    ) {
        if (vazio(titulo)) return;

        if (!usuariosPorTitulo.containsKey(titulo.trim())) {
            adicionarViolacao(violacoesPorTipo, tipoViolacao, detalheViolacao);
        }
    }

    private void validarDuplicidadeUsuarios(Set<String> titulosDuplicados, Map<String, List<String>> violacoesPorTipo) {
        for (String titulo : titulosDuplicados) {
            adicionarViolacao(violacoesPorTipo, "VW_USUARIO com titulo duplicado", "titulo=%s".formatted(titulo));
        }
    }

    private void validarUsuariosSemEmail(
            List<UnidadeHierarquiaLeitura> unidades,
            Map<Long, List<UsuarioConsultaLeitura>> usuariosConsultaPorUnidade,
            Map<String, List<String>> violacoesPorTipo
    ) {
        unidades.forEach(unidade -> {
            List<UsuarioConsultaLeitura> usuarios = usuariosConsultaPorUnidade.getOrDefault(unidade.codigo(), List.of());
            usuarios.stream().filter(usuario -> vazio(usuario.email())).forEach(usuario -> adicionarViolacao(
                    violacoesPorTipo,
                    "Usuario sem e-mail na VW_USUARIO",
                    "sigla=%s, titulo=%s, nome=%s".formatted(
                            unidade.sigla(),
                            usuario.tituloEleitoral(),
                            usuario.nome()
                    )
            ));
        });
    }

    private void validarIntegridadePerfis(
            List<PerfilInvalido> perfisInvalidos,
            Set<Long> unidadesSemResponsavel,
            Map<String, List<String>> violacoesPorTipo
    ) {
        perfisInvalidos.forEach(perfilInvalido -> {
            String tipo = perfilInvalido.tipo();
            boolean derivadaDeUnidadeSemResponsavel = tipo.equals("VW_USUARIO_PERFIL_UNIDADE com usuario_titulo nulo")
                    && perfilInvalido.unidadeCodigo() != null
                    && unidadesSemResponsavel.contains(perfilInvalido.unidadeCodigo());

            if (!derivadaDeUnidadeSemResponsavel) {
                adicionarViolacao(violacoesPorTipo, tipo, perfilInvalido.detalhe());
            }
        });
    }

    private void validarUnidadesIntermediarias(
            List<UnidadeHierarquiaLeitura> unidades,
            Map<Long, ResponsabilidadeLeitura> respsPorUnidade,
            Map<Long, Set<PerfilUsuarioUnidade>> perfisPorUnidade,
            Map<String, List<String>> violacoesPorTipo
    ) {
        Set<Long> unidadesComFilhas = unidades.stream()
                .<Long>mapMulti((unidade, consumer) -> {
                    Long codigoSuperior = unidade.unidadeSuperiorCodigo();
                    if (codigoSuperior != null) consumer.accept(codigoSuperior);
                })
                .collect(toSet());

        ContextoValidacaoIntermediaria contexto = new ContextoValidacaoIntermediaria(
                unidadesComFilhas, respsPorUnidade, perfisPorUnidade, violacoesPorTipo);

        for (UnidadeHierarquiaLeitura unidade : unidades) {
            if (unidade.tipo() == INTERMEDIARIA) validarUnidadeIntermediaria(unidade, contexto);
        }
    }

    private void adicionarViolacao(Map<String, List<String>> violacoesPorTipo, String tipo, String detalhe) {
        violacoesPorTipo.computeIfAbsent(tipo, chave -> new ArrayList<>()).add(detalhe);
    }

    private void processarLinhaPerfil(Map<String, Object> linha, ContextoCargaPerfis contexto) {
        String titulo = lerString(linha, "usuario_titulo");
        String perfilBruto = lerString(linha, "perfil");
        Long unidadeCodigo = lerLong(linha);

        if (vazio(titulo)) {
            contexto.perfisInvalidos().add(new PerfilInvalido(
                    "VW_USUARIO_PERFIL_UNIDADE com usuario_titulo nulo",
                    "perfil=%s, unidade_codigo=%s".formatted(valorOuNulo(perfilBruto), valorOuNulo(unidadeCodigo)),
                    unidadeCodigo
            ));
        } else if (vazio(perfilBruto)) {
            contexto.perfisInvalidos().add(new PerfilInvalido(
                    "VW_USUARIO_PERFIL_UNIDADE com perfil nulo",
                    "usuario_titulo=%s, unidade_codigo=%s".formatted(titulo, valorOuNulo(unidadeCodigo)),
                    unidadeCodigo
            ));
        } else if (unidadeCodigo == null) {
            contexto.perfisInvalidos().add(new PerfilInvalido(
                    "VW_USUARIO_PERFIL_UNIDADE com unidade_codigo nulo",
                    "usuario_titulo=%s, perfil=%s".formatted(titulo, perfilBruto),
                    null
            ));
        } else {
            DadosLinhaPerfil dados = new DadosLinhaPerfil(titulo, perfilBruto, unidadeCodigo);
            tentarMapearPerfil(dados, contexto);
        }
    }

    private void tentarMapearPerfil(DadosLinhaPerfil dados, ContextoCargaPerfis contexto) {
        try {
            Perfil perfil = Perfil.valueOf(dados.perfilBruto());
            ChavePerfilUsuarioUnidade chave = new ChavePerfilUsuarioUnidade(dados.titulo(), perfil, dados.codigo());
            contexto.contagemPorChave().merge(chave, 1, Integer::sum);
            contexto.perfisPorUnidade().computeIfAbsent(dados.codigo(), c -> new LinkedHashSet<>())
                    .add(new PerfilUsuarioUnidade(dados.titulo(), perfil));
        } catch (IllegalArgumentException e) {
            contexto.perfisInvalidos().add(new PerfilInvalido(
                    "VW_USUARIO_PERFIL_UNIDADE com perfil invalido",
                    "usuario_titulo=%s, perfil=%s, unidade_codigo=%s".formatted(dados.titulo(), dados.perfilBruto(), dados.codigo()),
                    dados.codigo()
            ));
        }
    }

    private void validarUnidadeIntermediaria(UnidadeHierarquiaLeitura unidade, ContextoValidacaoIntermediaria contexto) {
        if (!contexto.unidadesComFilhas().contains(unidade.codigo())) {
            adicionarViolacao(contexto.violacoesPorTipo(),
                    "Unidade intermediaria sem filhas ativas participantes",
                    "sigla=%s".formatted(unidade.sigla()));
        }

        ResponsabilidadeLeitura responsabilidade = contexto.responsabilidadesPorUnidade().get(unidade.codigo());
        Set<PerfilUsuarioUnidade> perfis = contexto.perfisPorUnidade().getOrDefault(unidade.codigo(), Set.of());
        boolean possuiGestor = perfis.stream()
                .anyMatch(perfil -> perfil.perfil() == GESTOR);

        if (!possuiGestor) {
            adicionarViolacao(contexto.violacoesPorTipo(),
                    "Unidade intermediaria sem perfil GESTOR",
                    "sigla=%s".formatted(unidade.sigla()));
        } else if (responsabilidade != null && !vazio(responsabilidade.usuarioTitulo())) {
            validarGestorResponsavel(unidade, responsabilidade, contexto);
        }
    }

    private void validarGestorResponsavel(UnidadeHierarquiaLeitura u, ResponsabilidadeLeitura r, ContextoValidacaoIntermediaria c) {
        Set<PerfilUsuarioUnidade> perfis = c.perfisPorUnidade().getOrDefault(u.codigo(), Set.of());
        boolean responsavelPossuiPerfilGestor = perfis.stream()
                .anyMatch(perfil -> perfil.perfil() == GESTOR
                        && Objects.equals(perfil.usuarioTitulo(), r.usuarioTitulo()));

        if (!responsavelPossuiPerfilGestor) {
            adicionarViolacao(c.violacoesPorTipo(),
                    "Responsavel de unidade intermediaria sem perfil GESTOR correspondente",
                    "sigla=%s, usuario_titulo=%s".formatted(u.sigla(), r.usuarioTitulo()));
        }
    }

    private boolean vazio(@Nullable String valor) {
        return valor == null || valor.isBlank();
    }

    private record DadosLinhaPerfil(String titulo, String perfilBruto, Long codigo) {
    }

    private record ContextoCargaPerfis(
            List<PerfilInvalido> perfisInvalidos,
            Map<Long, Set<PerfilUsuarioUnidade>> perfisPorUnidade,
            Map<ChavePerfilUsuarioUnidade, Integer> contagemPorChave
    ) {
    }

    private record ContextoValidacaoIntermediaria(
            Set<Long> unidadesComFilhas,
            Map<Long, ResponsabilidadeLeitura> responsabilidadesPorUnidade,
            Map<Long, Set<PerfilUsuarioUnidade>> perfisPorUnidade,
            Map<String, List<String>> violacoesPorTipo
    ) {
    }

    private record ResultadoCargaUsuarios(Map<String, Usuario> usuariosPorTitulo, Set<String> titulosDuplicados) {
    }

    private record ResultadoCargaPerfis(
            Map<Long, Set<PerfilUsuarioUnidade>> perfisPorUnidade,
            List<PerfilInvalido> perfisInvalidos
    ) {
    }

    private record PerfilInvalido(String tipo, String detalhe, @Nullable Long unidadeCodigo) {
    }

    private record ChavePerfilUsuarioUnidade(String usuarioTitulo, Perfil perfil, Long unidadeCodigo) {
    }

    private record PerfilUsuarioUnidade(String usuarioTitulo, Perfil perfil) {
    }
}
