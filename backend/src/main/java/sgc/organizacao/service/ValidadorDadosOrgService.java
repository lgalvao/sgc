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
 *   <li>Toda unidade ativa tem titular</li>
 *   <li>Todo titular tem título eleitoral</li>
 *   <li>Todo titular tem email cadastrado</li>
 *   <li>Toda unidade INTERMEDIARIA tem pelo menos uma subordinada</li>
 * </ul>
 *
 * <p>Pode ser desabilitado com a propriedade {@code sgc.validacao.startup=false}.
 */
@Component
@ConditionalOnProperty(name = "sgc.validacao.startup", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class ValidadorDadosOrganizacionais implements ApplicationRunner {

    private static final Set<TipoUnidade> TIPOS_QUE_PARTICIPAM = Set.of(OPERACIONAL, INTEROPERACIONAL, INTERMEDIARIA);

    private final UnidadeRepo unidadeRepo;
    private final UsuarioRepo usuarioRepo;

    @Override
    @Transactional(readOnly = true)
    public void run(ApplicationArguments args) {
        log.info("Validando integridade dos dados organizacionais...");

        List<String> violacoes = new ArrayList<>();
        List<Unidade> unidadesAtivas = carregarUnidadesAtivas();
        Map<String, Usuario> usuarios = carregarUsuariosTitulares(unidadesAtivas);

        validarTitularesUnidades(unidadesAtivas, violacoes);
        validarEmailsTitulares(unidadesAtivas, usuarios, violacoes);
        validarUnidadesIntermediarias(unidadesAtivas, violacoes);

        if (!violacoes.isEmpty()) {
            violacoes.forEach(v -> log.error("INVARIANTE VIOLADA: {}", v));
            String termo = violacoes.size() == 1 ? "violação encontrada" : "violações encontradas";
            throw new ErroConfiguracao("Dados organizacionais inválidos. %d %s. Verifique os logs."
                    .formatted(violacoes.size(), termo));
        }

        log.info("Dados organizacionais validados com sucesso. {} unidades verificadas.", unidadesAtivas.size());
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
                .filter(t -> t != null && !t.isBlank())
                .distinct()
                .toList();

        if (titulos.isEmpty()) {
            return Map.of();
        }

        return usuarioRepo.findAllById(titulos).stream()
                .collect(Collectors.toMap(Usuario::getTituloEleitoral, u -> u));
    }

    /**
     * Valida que toda unidade ativa tem titular com título eleitoral.
     */
    private void validarTitularesUnidades(List<Unidade> unidades, List<String> violacoes) {
        for (Unidade u : unidades) {
            if (u.getTituloTitular() == null || u.getTituloTitular().isBlank()) {
                violacoes.add("Unidade %s (%s) não possui titular cadastrado".formatted(u.getSigla(), u.getTipo()));
            }
        }
    }

    /**
     * Valida que todo titular de unidade tem email cadastrado.
     */
    private void validarEmailsTitulares(List<Unidade> unidades, Map<String, Usuario> usuarios, List<String> violacoes) {
        for (Unidade u : unidades) {
            String tituloTitular = u.getTituloTitular();
            if (tituloTitular == null || tituloTitular.isBlank()) {
                continue; // Já validado em validarTitularesUnidades
            }

            Usuario titular = usuarios.get(tituloTitular);
            if (titular == null) {
                violacoes.add("Titular da unidade %s (título %s) não encontrado na base de usuários"
                        .formatted(u.getSigla(), tituloTitular));
                continue;
            }

            if (titular.getEmail() == null || titular.getEmail().isBlank()) {
                violacoes.add("Titular %s da unidade %s não possui email cadastrado"
                        .formatted(titular.getNome(), u.getSigla()));
            }
        }
    }

    /**
     * Valida que toda unidade INTERMEDIARIA tem pelo menos uma subordinada.
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
