package sgc.processo.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.jspecify.annotations.*;
import org.springframework.security.core.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.organizacao.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.comum.erros.ErroValidacao;
import sgc.processo.model.*;
import sgc.subprocesso.service.*;

import java.util.*;
import java.util.stream.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessoValidacaoService {

    private final UnidadeService unidadeService;
    private final UsuarioFacade usuarioService;
    private final SubprocessoValidacaoService validacaoService;
    private final ProcessoRepo processoRepo;

    // ---- Validações de negócio ----

    /**
     * Valida se todas as unidades especificadas possuem mapa vigente.
     */
    public Optional<String> getMensagemErroUnidadesSemMapa(@Nullable List<Long> codigosUnidades) {
        if (codigosUnidades == null || codigosUnidades.isEmpty()) {
            return Optional.empty();
        }

        List<Long> unidadesSemMapa = codigosUnidades.stream()
                .filter(codigo -> !unidadeService.verificarMapaVigente(codigo))
                .toList();

        if (!unidadesSemMapa.isEmpty()) {
            List<String> siglasUnidadesSemMapa = unidadeService.buscarSiglasPorIds(unidadesSemMapa);
            return Optional.of(("As seguintes unidades não possuem mapa vigente e não podem participar"
                    + " de um processo de revisão: %s").formatted(String.join(", ", siglasUnidadesSemMapa)));
        }
        return Optional.empty();
    }

    /**
     * Valida se o processo pode ser finalizado.
     *
     * @throws ErroValidacao se o processo não estiver em situação válida para finalização
     */
    @Transactional(readOnly = true)
    public void validarFinalizacaoProcesso(Processo processo) {
        if (processo.getSituacao() != SituacaoProcesso.EM_ANDAMENTO) {
            throw new ErroValidacao("Apenas processos 'EM ANDAMENTO' podem ser finalizados.");
        }
        validarTodosSubprocessosHomologados(processo);
    }

    /**
     * Valida se todos os subprocessos de um processo estão homologados.
     *
     * @throws ErroValidacao se algum subprocesso não estiver homologado
     */
    public void validarTodosSubprocessosHomologados(Processo processo) {
        SubprocessoValidacaoService.ValidationResult resultado =
                validacaoService.validarSubprocessosParaFinalizacao(processo.getCodigo());

        if (!resultado.valido()) {
            log.warn("Validação de finalização falhou para processo {}: {}",
                    processo.getCodigo(), resultado.mensagem());
            throw new ErroValidacao(resultado.mensagem());
        }

        log.info("Todos os subprocessos do processo {} estão homologados", processo.getCodigo());
    }

    /**
     * Valida se as unidades participantes são elegíveis (não são INTERMEDIARIA).
     */
    public Optional<String> validarTiposUnidades(List<Unidade> unidades) {
        if (unidades.isEmpty()) {
            return Optional.empty();
        }

        List<String> unidadesInvalidas = unidades.stream()
                .filter(u -> u.getTipo() == TipoUnidade.INTERMEDIARIA)
                .map(Unidade::getSigla)
                .toList();

        if (!unidadesInvalidas.isEmpty()) {
            return Optional.of("Unidades do tipo INTERMEDIARIA não podem participar de processos: "
                    + String.join(", ", unidadesInvalidas));
        }

        return Optional.empty();
    }

    // ---- Controle de acesso ----

    @Transactional(readOnly = true)
    public boolean checarAcesso(@Nullable Authentication authentication, Long codProcesso) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            return false;
        }

        String username = authentication.getName();
        boolean isGestorOuChefe = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_GESTOR".equals(a.getAuthority()) || "ROLE_CHEFE".equals(a.getAuthority()));
        if (!isGestorOuChefe) return false;

        // Permitir visualização de processos FINALIZADOS para importação
        Optional<Processo> processoOpt = processoRepo.findById(codProcesso);
        if (processoOpt.isPresent() && processoOpt.get().getSituacao() == SituacaoProcesso.FINALIZADO) {
            return true;
        }

        List<PerfilDto> perfis = usuarioService.buscarPerfisUsuario(username);
        if (perfis.isEmpty()) return false;

        Set<Long> unidadesUsuario = perfis.stream()
                .map(PerfilDto::unidadeCodigo)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (unidadesUsuario.isEmpty()) return false;

        List<Unidade> todasUnidades = unidadeService.todasComHierarquia();
        Map<Long, List<Unidade>> mapaPorPai = buildMapaPorPai(todasUnidades);

        Set<Long> todasUnidadesAcesso = new HashSet<>();
        for (Long codUnidade : unidadesUsuario) {
            todasUnidadesAcesso.addAll(buscarDescendentesNoMapa(codUnidade, mapaPorPai));
        }

        return validacaoService.verificarAcessoUnidadeAoProcesso(codProcesso, new ArrayList<>(todasUnidadesAcesso));
    }

    @Transactional(readOnly = true)
    public List<Long> buscarCodigosDescendentes(Long codUnidade) {
        List<Unidade> todasUnidades = unidadeService.todasComHierarquia();
        Map<Long, List<Unidade>> mapaPorPai = buildMapaPorPai(todasUnidades);
        return new ArrayList<>(buscarDescendentesNoMapa(codUnidade, mapaPorPai));
    }

    private Map<Long, List<Unidade>> buildMapaPorPai(List<Unidade> todasUnidades) {
        Map<Long, List<Unidade>> mapaPorPai = new HashMap<>();
        for (Unidade u : todasUnidades) {
            Unidade unidadeSuperior = u.getUnidadeSuperior();
            if (unidadeSuperior != null) {
                mapaPorPai.computeIfAbsent(unidadeSuperior.getCodigo(), k -> new ArrayList<>()).add(u);
            }
        }
        return mapaPorPai;
    }

    private Set<Long> buscarDescendentesNoMapa(Long codUnidade, Map<Long, List<Unidade>> mapaPorPai) {
        Set<Long> resultado = new HashSet<>();
        Queue<Long> fila = new LinkedList<>();

        fila.add(codUnidade);
        resultado.add(codUnidade);

        while (!fila.isEmpty()) {
            Long atual = fila.poll();

            List<Unidade> filhos = mapaPorPai.get(atual);
            if (filhos != null) {
                for (Unidade filho : filhos) {
                    Long codigo = filho.getCodigo();
                    if (!resultado.contains(codigo)) {
                        resultado.add(codigo);
                        fila.add(codigo);
                    }
                }
            }
        }
        return resultado;
    }
}
