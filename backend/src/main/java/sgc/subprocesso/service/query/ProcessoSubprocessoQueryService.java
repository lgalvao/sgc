package sgc.subprocesso.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.util.List;

import static sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO;
import static sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO;

/**
 * Serviço de consulta compartilhado para queries que envolvem Processo e Subprocesso.
 *
 * <p><b>Padrão:</b> Query Service Pattern (CQRS simplificado)
 * <ul>
 *   <li>Somente leitura (read-only)</li>
 *   <li>Sem lógica de negócio</li>
 *   <li>Queries otimizadas</li>
 *   <li>Independente de módulos de domínio</li>
 * </ul>
 *
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProcessoSubprocessoQueryService {
    private final SubprocessoRepo subprocessoRepo;

    /**
     * Verifica se todas as unidades especificadas participam do processo através de subprocessos.
     *
     * @param processoId     código do processo
     * @param unidadeCodigos códigos das unidades a verificar
     * @return true se pelo menos uma unidade participa do processo
     */
    public boolean verificarAcessoUnidadeAoProcesso(Long processoId, List<Long> unidadeCodigos) {
        if (unidadeCodigos == null || unidadeCodigos.isEmpty()) {
            return false;
        }
        return subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigoIn(processoId, unidadeCodigos);
    }

    /**
     * Valida se todos os subprocessos de um processo estão homologados.
     *
     * @param processoId código do processo
     * @return resultado da validação com detalhes
     */
    public ValidationResult validarSubprocessosParaFinalizacao(Long processoId) {
        long total = subprocessoRepo.countByProcessoCodigo(processoId);

        if (total == 0) return ValidationResult.ofInvalido("O processo não possui subprocessos para finalizar");

        long homologados = subprocessoRepo.countByProcessoCodigoAndSituacaoIn(
                processoId,
                List.of(MAPEAMENTO_MAPA_HOMOLOGADO, REVISAO_MAPA_HOMOLOGADO
                )
        );

        if (total != homologados) {
            return ValidationResult.ofInvalido(
                    ("Apenas %d de %d subprocessos foram homologados. " +
                            "Todos os subprocessos devem estar homologados para finalizar o processo.")
                            .formatted(homologados, total)
            );
        }

        return ValidationResult.ofValido();
    }

    /**
     * Lista subprocessos por processo e situação.
     *
     * @param processoId código do processo
     * @param situacao   situação dos subprocessos
     * @return lista de subprocessos
     */
    public List<Subprocesso> listarPorProcessoESituacao(Long processoId, SituacaoSubprocesso situacao) {
        return subprocessoRepo.findByProcessoCodigoAndSituacaoWithUnidade(processoId, situacao);
    }

    /**
     * Lista todos os subprocessos de um processo.
     *
     * @param processoId código do processo
     * @return lista de subprocessos com unidade e mapa carregados
     */
    public List<Subprocesso> listarEntidadesPorProcesso(Long processoId) {
        return subprocessoRepo.findByProcessoCodigoWithUnidade(processoId);
    }

    /**
     * Lista subprocessos por processo, unidade e situações.
     *
     * @param processoId código do processo
     * @param unidadeId  código da unidade
     * @param situacoes  situações dos subprocessos
     * @return lista de subprocessos
     */
    public List<Subprocesso> listarPorProcessoUnidadeESituacoes(
            Long processoId, Long unidadeId, List<SituacaoSubprocesso> situacoes) {
        return subprocessoRepo.findByProcessoCodigoAndUnidadeCodigoAndSituacaoInWithUnidade(
                processoId, unidadeId, situacoes);
    }

    /**
     * Resultado de validação imutável.
     *
     * @param valido   indica se a validação passou
     * @param mensagem mensagem de erro (presente apenas se inválido)
     */
    public record ValidationResult(boolean valido, String mensagem) {
        public static ValidationResult ofValido() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult ofInvalido(String mensagem) {
            return new ValidationResult(false, mensagem);
        }
    }
}
