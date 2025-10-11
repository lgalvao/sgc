package sgc.analise;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.modelo.AnaliseValidacao;
import sgc.analise.modelo.AnaliseValidacaoRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço para gerenciar análises de validação (ANALISE_VALIDACAO). *
 */
@Service
@RequiredArgsConstructor
public class AnaliseValidacaoService {
    private final AnaliseValidacaoRepo analiseValidacaoRepo;
    private final SubprocessoRepo subprocessoRepo;

    /**
     * Recupera as análises de validação vinculadas a um subprocesso.
     *
     * @param subprocessoCodigo id do subprocesso
     * @return lista de AnaliseValidacao
     */
    @Transactional(readOnly = true)
    public List<AnaliseValidacao> listarPorSubprocesso(Long subprocessoCodigo) {
        if (subprocessoRepo.findById(subprocessoCodigo).isEmpty()) {
            throw new ErroEntidadeNaoEncontrada("Subprocesso não encontrado: %d".formatted(subprocessoCodigo));
        }
        return analiseValidacaoRepo.findBySubprocesso_Codigo(subprocessoCodigo);
    }

    /**
     * Cria e persiste uma nova análise de validação para o subprocesso informado.
     *
     * @param subprocessoCodigo id do subprocesso
     * @param observacoes       texto com observações da análise
     * @return entidade AnaliseValidacao persistida
     */
    @Transactional
    public AnaliseValidacao criarAnalise(Long subprocessoCodigo, String observacoes) {
        Subprocesso sp = subprocessoRepo.findById(subprocessoCodigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado: %d".formatted(subprocessoCodigo)));

        AnaliseValidacao a = new AnaliseValidacao();
        a.setSubprocesso(sp);
        a.setDataHora(LocalDateTime.now());
        a.setObservacoes(observacoes);

        return analiseValidacaoRepo.save(a);
    }

    /**
     * Remove todas as análises de validação vinculadas ao subprocesso.
     *
     * @param subprocessoCodigo id do subprocesso
     */
    @Transactional
    public void removerPorSubprocesso(Long subprocessoCodigo) {
        analiseValidacaoRepo.deleteBySubprocesso_Codigo(subprocessoCodigo);
    }
}