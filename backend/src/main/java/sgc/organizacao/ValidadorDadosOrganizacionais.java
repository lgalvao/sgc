package sgc.organizacao;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.erros.*;
import sgc.organizacao.model.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static sgc.organizacao.model.Perfil.*;
import static sgc.organizacao.model.SituacaoUnidade.*;
import static sgc.organizacao.model.TipoUnidade.*;

/**
 * Valida invariantes organizacionais críticas na inicialização do sistema.
 *
 * <p>Estas invariantes dependem de views externas e precisam estar íntegras
 * para que login, permissões, notificações e fluxos operacionais funcionem corretamente.
 */
@Component
@ConditionalOnProperty(name = "sgc.validacao.startup", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class ValidadorDadosOrganizacionais implements ApplicationRunner {

    private static final Set<TipoUnidade> TIPOS_PARTICIPANTES = Set.of(OPERACIONAL, INTERMEDIARIA, INTEROPERACIONAL);

    private final UnidadeRepo unidadeRepo;
    private final UsuarioRepo usuarioRepo;
    private final ResponsabilidadeRepo responsabilidadeRepo;
    private final UsuarioPerfilRepo usuarioPerfilRepo;

    @Override
    @Transactional(readOnly = true)
    public void run(ApplicationArguments args) {
        log.info("Validando invariantes organizacionais de startup...");

        List<Unidade> unidadesParticipantes = carregarUnidadesParticipantes();
        Map<Long, Responsabilidade> responsabilidadesPorUnidade = carregarResponsabilidades(unidadesParticipantes);
        Map<String, Usuario> usuariosPorTitulo = carregarUsuarios(unidadesParticipantes, responsabilidadesPorUnidade);
        Map<Long, Set<PerfilUsuarioUnidade>> perfisPorUnidade = carregarPerfis(unidadesParticipantes);
        List<String> violacoes = new ArrayList<>();

        validarTitularidade(unidadesParticipantes, violacoes);
        validarResponsabilidade(unidadesParticipantes, responsabilidadesPorUnidade, violacoes);
        validarUsuariosReferenciados(unidadesParticipantes, responsabilidadesPorUnidade, usuariosPorTitulo, violacoes);
        validarUnidadesIntermediarias(unidadesParticipantes, responsabilidadesPorUnidade, perfisPorUnidade, violacoes);

        if (!violacoes.isEmpty()) {
            violacoes.forEach(violacao -> log.error("INVARIANTE ORGANIZACIONAL VIOLADA: {}", violacao));
            String termo = violacoes.size() == 1 ? "violacao encontrada" : "violacoes encontradas";
            throw new ErroConfiguracao("Dados organizacionais invalidos. %d %s. Verifique os logs."
                    .formatted(violacoes.size(), termo));
        }

        log.info("Invariantes organizacionais validadas com sucesso. {} unidades verificadas.", unidadesParticipantes.size());
    }

    private List<Unidade> carregarUnidadesParticipantes() {
        return unidadeRepo.findAllWithHierarquia().stream()
                .filter(unidade -> unidade.getSituacao() == ATIVA)
                .filter(unidade -> TIPOS_PARTICIPANTES.contains(unidade.getTipo()))
                .toList();
    }

    private Map<Long, Responsabilidade> carregarResponsabilidades(List<Unidade> unidades) {
        List<Long> codigos = unidades.stream()
                .map(Unidade::getCodigo)
                .toList();

        if (codigos.isEmpty()) {
            return Map.of();
        }

        return responsabilidadeRepo.findByUnidadeCodigoIn(codigos).stream()
                .collect(Collectors.toMap(
                        Responsabilidade::getUnidadeCodigo,
                        Function.identity(),
                        (responsabilidadeAtual, responsabilidadeDuplicada) -> responsabilidadeAtual
                ));
    }

    private Map<String, Usuario> carregarUsuarios(
            List<Unidade> unidades,
            Map<Long, Responsabilidade> responsabilidadesPorUnidade
    ) {
        List<String> titulos = Stream.concat(
                        unidades.stream().map(Unidade::getTituloTitular),
                        responsabilidadesPorUnidade.values().stream().map(Responsabilidade::getUsuarioTitulo)
                )
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(titulo -> !titulo.isBlank())
                .distinct()
                .toList();

        if (titulos.isEmpty()) {
            return Map.of();
        }

        return usuarioRepo.findAllById(titulos).stream()
                .collect(Collectors.toMap(Usuario::getTituloEleitoral, Function.identity()));
    }

    private Map<Long, Set<PerfilUsuarioUnidade>> carregarPerfis(List<Unidade> unidades) {
        List<Long> codigos = unidades.stream()
                .map(Unidade::getCodigo)
                .toList();

        if (codigos.isEmpty()) {
            return Map.of();
        }

        return usuarioPerfilRepo.findByUnidadeCodigoIn(codigos).stream()
                .collect(Collectors.groupingBy(
                        UsuarioPerfil::getUnidadeCodigo,
                        Collectors.mapping(
                                perfil -> new PerfilUsuarioUnidade(perfil.getUsuarioTitulo(), perfil.getPerfil()),
                                Collectors.toSet()
                        )
                ));
    }

    private void validarTitularidade(List<Unidade> unidades, List<String> violacoes) {
        for (Unidade unidade : unidades) {
            if (estaVazio(unidade.getTituloTitular())) {
                violacoes.add("Unidade %s (%s) nao possui titular cadastrado."
                        .formatted(unidade.getSigla(), unidade.getTipo()));
            }
        }
    }

    private void validarResponsabilidade(
            List<Unidade> unidades,
            Map<Long, Responsabilidade> responsabilidadesPorUnidade,
            List<String> violacoes
    ) {
        for (Unidade unidade : unidades) {
            Responsabilidade responsabilidade = responsabilidadesPorUnidade.get(unidade.getCodigo());
            if (responsabilidade == null || estaVazio(responsabilidade.getUsuarioTitulo())) {
                violacoes.add("Unidade %s (%s) nao possui responsavel efetivo."
                        .formatted(unidade.getSigla(), unidade.getTipo()));
            }
        }
    }

    private void validarUsuariosReferenciados(
            List<Unidade> unidades,
            Map<Long, Responsabilidade> responsabilidadesPorUnidade,
            Map<String, Usuario> usuariosPorTitulo,
            List<String> violacoes
    ) {
        for (Unidade unidade : unidades) {
            validarUsuarioExistente(
                    unidade.getTituloTitular(),
                    "Titular da unidade %s nao encontrado na view de usuarios."
                            .formatted(unidade.getSigla()),
                    usuariosPorTitulo,
                    violacoes
            );

            Responsabilidade responsabilidade = responsabilidadesPorUnidade.get(unidade.getCodigo());
            if (responsabilidade != null) {
                validarUsuarioExistente(
                        responsabilidade.getUsuarioTitulo(),
                        "Responsavel da unidade %s nao encontrado na view de usuarios."
                                .formatted(unidade.getSigla()),
                        usuariosPorTitulo,
                        violacoes
                );
            }
        }
    }

    private void validarUsuarioExistente(
            String titulo,
            String mensagemViolacao,
            Map<String, Usuario> usuariosPorTitulo,
            List<String> violacoes
    ) {
        if (estaVazio(titulo)) {
            return;
        }

        if (!usuariosPorTitulo.containsKey(titulo.trim())) {
            violacoes.add(mensagemViolacao);
        }
    }

    private void validarUnidadesIntermediarias(
            List<Unidade> unidades,
            Map<Long, Responsabilidade> responsabilidadesPorUnidade,
            Map<Long, Set<PerfilUsuarioUnidade>> perfisPorUnidade,
            List<String> violacoes
    ) {
        Set<Long> unidadesComFilhas = unidades.stream()
                .map(Unidade::getUnidadeSuperior)
                .filter(Objects::nonNull)
                .map(Unidade::getCodigo)
                .collect(Collectors.toSet());

        for (Unidade unidade : unidades) {
            if (unidade.getTipo() != INTERMEDIARIA) {
                continue;
            }

            if (!unidadesComFilhas.contains(unidade.getCodigo())) {
                violacoes.add("Unidade intermediaria %s nao possui filhas ativas participantes."
                        .formatted(unidade.getSigla()));
            }

            Responsabilidade responsabilidade = responsabilidadesPorUnidade.get(unidade.getCodigo());
            Set<PerfilUsuarioUnidade> perfis = perfisPorUnidade.getOrDefault(unidade.getCodigo(), Set.of());
            boolean possuiGestor = perfis.stream()
                    .anyMatch(perfil -> perfil.perfil() == GESTOR);

            if (!possuiGestor) {
                violacoes.add("Unidade intermediaria %s nao possui perfil GESTOR associado."
                        .formatted(unidade.getSigla()));
                continue;
            }

            if (responsabilidade != null && !estaVazio(responsabilidade.getUsuarioTitulo())) {
                boolean responsavelPossuiPerfilGestor = perfis.stream()
                        .anyMatch(perfil -> perfil.perfil() == GESTOR
                                && Objects.equals(perfil.usuarioTitulo(), responsabilidade.getUsuarioTitulo()));

                if (!responsavelPossuiPerfilGestor) {
                    violacoes.add("Responsavel da unidade intermediaria %s nao possui perfil GESTOR na view de perfis."
                            .formatted(unidade.getSigla()));
                }
            }
        }
    }

    private boolean estaVazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private record PerfilUsuarioUnidade(String usuarioTitulo, Perfil perfil) {
    }
}
