package sgc.subprocesso.service.query;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.util.List;

import static sgc.subprocesso.model.SituacaoSubprocesso.*;

/**
 * Serviço de consulta compartilhado para queries que envolvem Processo e Subprocesso.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ConsultasSubprocessoService {
    private final SubprocessoRepo subprocessoRepo;

    /**
     * Verifica se todas as unidades especificadas participam do processo através de subprocessos.
     *
     * @param processoId     código do processo
     * @param unidadeCodigos códigos das unidades a verificar
     * @return true se pelo menos uma unidade participa do processo
     */
    public boolean verificarAcessoUnidadeAoProcesso(Long processoId, List<Long> unidadeCodigos) {
        if (unidadeCodigos.isEmpty()) {
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

        long homologados = subprocessoRepo.countByProcessoCodigoAndSituacaoIn(processoId,
                List.of(
                        MAPEAMENTO_MAPA_HOMOLOGADO,
                        REVISAO_MAPA_HOMOLOGADO,
                        DIAGNOSTICO_CONCLUIDO
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
     * Lista subprocessos por processo e múltiplas situações.
     *
     * @param processoId código do processo
     * @param situacoes  lista de situações
     * @return lista de subprocessos
     */
    public List<Subprocesso> listarPorProcessoESituacoes(Long processoId, List<SituacaoSubprocesso> situacoes) {
        return subprocessoRepo.findByProcessoCodigoAndSituacaoInWithUnidade(processoId, situacoes);
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
     * Lista subprocessos por processo, múltiplas unidades e situações.
     *
     * @param processoId     código do processo
     * @param unidadeCodigos códigos das unidades
     * @param situacoes      situações dos subprocessos
     * @return lista de subprocessos
     */
    public List<Subprocesso> listarPorProcessoUnidadesESituacoes(
            Long processoId, List<Long> unidadeCodigos, List<SituacaoSubprocesso> situacoes) {
        return subprocessoRepo.findByProcessoCodigoAndUnidadeCodigoInAndSituacaoInWithUnidade(
                processoId, unidadeCodigos, situacoes);
    }

    /**
     * Resultado de validação imutável.
     *
     * @param valido   indica se a validação passou
     * @param mensagem mensagem de erro (presente apenas se inválido)
     */
    public record ValidationResult(boolean valido, @Nullable String mensagem) {
        public static ValidationResult ofValido() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult ofInvalido(String mensagem) {
            return new ValidationResult(false, mensagem);
        }
    }
}
