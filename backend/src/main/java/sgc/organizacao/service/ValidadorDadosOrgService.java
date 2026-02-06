package sgc.organizacao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroConfiguracao;
import sgc.organizacao.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static sgc.organizacao.model.TipoUnidade.*;

/**
 * Valida invariantes dos dados organizacionais na inicialização do sistema.
 *
 * <p>Os dados de unidades e usuários vêm de views Oracle (VW_UNIDADE, VW_USUARIO).
 * Se estes dados estiverem inconsistentes, o sistema não pode operar corretamente.
 * Este componente verifica as invariantes no startup e falha imediatamente
 * caso sejam violadas.
 *
 * <p>Invariantes validadas:
 * <ul>
 *   <li>Todas unidades ativas têm titular</li>
 *   <li>Todos titulares têm título eleitoral</li>
 *   <li>Todos titulares têm email cadastrado</li>
 *   <li>Todas unidades INTERMEDIARIAS têm pelo menos uma subordinada</li>
 * </ul>
 *
 * <p>Pode ser desabilitado com a propriedade {@code sgc.validacao.startup=false}.
 */
@Component
@ConditionalOnProperty(name = "sgc.validacao.startup", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class ValidadorDadosOrgService implements ApplicationRunner {

    private static final Set<TipoUnidade> TIPOS_QUE_PARTICIPAM = Set.of(OPERACIONAL, INTEROPERACIONAL, INTERMEDIARIA);

    private final UnidadeRepo unidadeRepo;
    private final UsuarioRepo usuarioRepo;
    private final ResponsabilidadeRepo responsabilidadeRepo;

    @Override
    @Transactional(readOnly = true)
    public void run(ApplicationArguments args) {
        List<String> violacoes = new ArrayList<>();
        List<Unidade> unidadesAtivas = carregarUnidadesAtivas();
        Map<String, Usuario> usuarios = carregarUsuariosTitulares(unidadesAtivas);

        validarTitularesUnidades(unidadesAtivas, violacoes);
        validarEmailsTitulares(unidadesAtivas, usuarios, violacoes);
        validarUnidadesIntermediarias(unidadesAtivas, violacoes);
        validarResponsaveisAtuais(unidadesAtivas, violacoes);

        if (!violacoes.isEmpty()) {
            violacoes.forEach(v -> log.error("INVARIANTE VIOLADA: {}", v));
            String termo = violacoes.size() == 1 ? "violação encontrada" : "violações encontradas";

            String detalhes = violacoes.stream()
                    .limit(3) // Mostra as 3 primeiras para não poluir
                    .collect(Collectors.joining("; "));

            if (violacoes.size() > 3) {
                detalhes += "; ... (+ " + (violacoes.size() - 3) + " outras)";
            }

            String msg = "Dados organizacionais inválidos. %d %s: [%s]".formatted(violacoes.size(), termo, detalhes);
            throw new ErroConfiguracao(msg);
        }

        log.info("Dados organizacionais validados com sucesso. {} unidades verificadas.", unidadesAtivas.size());
    }

    /**
     * Valida que todas as unidades participantes possuem um responsável atual definido.
     */
    private void validarResponsaveisAtuais(List<Unidade> unidades, List<String> violacoes) {
        List<Long> ids = unidades.stream().map(Unidade::getCodigo).toList();
        Map<Long, Responsabilidade> responsabilidades = responsabilidadeRepo.findByUnidadeCodigoIn(ids).stream()
                .collect(Collectors.toMap(Responsabilidade::getUnidadeCodigo, r -> r));

        for (Unidade u : unidades) {
            Responsabilidade r = responsabilidades.get(u.getCodigo());
            if (r == null || r.getUsuarioTitulo().isBlank()) {
                violacoes.add("Unidade %s (%s) não possui responsável atual definido (Titular, Substituto ou Atribuição)"
                        .formatted(u.getSigla(), u.getTipo()));
            }
        }
    }

    private List<Unidade> carregarUnidadesAtivas() {
        return unidadeRepo.findAllWithHierarquia().stream()
                .filter(u -> u.getSituacao() == SituacaoUnidade.ATIVA)
                .filter(u -> TIPOS_QUE_PARTICIPAM.contains(u.getTipo()))
                .toList();
    }

    private Map<String, Usuario> carregarUsuariosTitulares(List<Unidade> unidades) {
        List<String> titulos = unidades.stream()
                .map(Unidade::getTituloTitular)
                .filter(t -> !t.isBlank())
                .distinct()
                .toList();

        if (titulos.isEmpty()) {
            return Map.of();
        }

        return usuarioRepo.findAllById(titulos).stream()
                .collect(Collectors.toMap(Usuario::getTituloEleitoral, u -> u, (u1, u2) -> u1));
    }

    /**
     * Valida que toda unidade ativa tem titular com título eleitoral.
     */
    private void validarTitularesUnidades(List<Unidade> unidades, List<String> violacoes) {
        for (Unidade u : unidades) {
            String titulo = u.getTituloTitular();
            if (titulo.isBlank()) {
                violacoes.add("Unidade %s (%s) não possui titular cadastrado".formatted(u.getSigla(), u.getTipo()));
            }
        }
    }

    /**
     * Valida que todos os titulares de unidades têm email cadastrado.
     */
    private void validarEmailsTitulares(List<Unidade> unidades, Map<String, Usuario> usuarios, List<String> violacoes) {
        for (Unidade u : unidades) {
            String tituloTitular = u.getTituloTitular();
            if (!tituloTitular.isBlank()) {
                Usuario titular = usuarios.get(tituloTitular);
                if (titular == null) {
                    violacoes.add("Titular da unidade %s (título %s) não encontrado na base de usuários"
                            .formatted(u.getSigla(), tituloTitular));
                } else {
                    String email = titular.getEmail();
                    if (email.isBlank()) {
                        violacoes.add("Titular %s da unidade %s não possui email cadastrado"
                                .formatted(titular.getNome(), u.getSigla()));
                    }
                }
            }
        }
    }

    /**
     * Valida que todas as unidades INTERMEDIARIAS têm pelo menos uma subordinada.
     */
    private void validarUnidadesIntermediarias(List<Unidade> unidades, List<String> violacoes) {
        Set<Long> codigosComSuperior = unidades.stream()
                .filter(u -> u.getUnidadeSuperior() != null)
                .map(u -> u.getUnidadeSuperior().getCodigo())
                .collect(Collectors.toSet());

        for (Unidade u : unidades) {
            if (u.getTipo() == INTERMEDIARIA && !codigosComSuperior.contains(u.getCodigo())) {
                violacoes.add("Unidade intermediária %s não possui subordinadas".formatted(u.getSigla()));
            }
        }
    }
}
