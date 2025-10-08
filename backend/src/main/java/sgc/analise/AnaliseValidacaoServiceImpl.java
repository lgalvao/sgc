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
 * Implementação do serviço de análises de validação.
 */
@Service
@RequiredArgsConstructor
public class AnaliseValidacaoServiceImpl implements AnaliseValidacaoService {
    private final AnaliseValidacaoRepo analiseValidacaoRepo;
    private final SubprocessoRepo subprocessoRepo;

    @Override
    @Transactional(readOnly = true)
    public List<AnaliseValidacao> listarPorSubprocesso(Long subprocessoCodigo) {
        if (subprocessoRepo.findById(subprocessoCodigo).isEmpty()) {
            throw new ErroEntidadeNaoEncontrada("Subprocesso não encontrado: %d".formatted(subprocessoCodigo));
        }
        return analiseValidacaoRepo.findBySubprocesso_Codigo(subprocessoCodigo);
    }

    @Override
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

    @Override
    @Transactional
    public void removerPorSubprocesso(Long subprocessoCodigo) {
        analiseValidacaoRepo.deleteBySubprocesso_Codigo(subprocessoCodigo);
    }
}