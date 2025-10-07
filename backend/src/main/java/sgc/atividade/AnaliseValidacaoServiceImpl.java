package sgc.atividade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.subprocesso.AnaliseValidacaoRepository;
import sgc.subprocesso.Subprocesso;
import sgc.subprocesso.SubprocessoRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementação do serviço de análises de validação.
 */
@Service
@RequiredArgsConstructor
public class AnaliseValidacaoServiceImpl implements AnaliseValidacaoService {
    private final AnaliseValidacaoRepository analiseValidacaoRepository;
    private final SubprocessoRepository subprocessoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AnaliseValidacao> listarPorSubprocesso(Long subprocessoCodigo) {
        if (subprocessoRepository.findById(subprocessoCodigo).isEmpty()) {
            throw new ErroEntidadeNaoEncontrada("Subprocesso não encontrado: " + subprocessoCodigo);
        }
        return analiseValidacaoRepository.findBySubprocesso_Codigo(subprocessoCodigo);
    }

    @Override
    @Transactional
    public AnaliseValidacao criarAnalise(Long subprocessoCodigo, String observacoes) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoCodigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado: " + subprocessoCodigo));

        AnaliseValidacao a = new AnaliseValidacao();
        a.setSubprocesso(sp);
        a.setDataHora(LocalDateTime.now());
        a.setObservacoes(observacoes);

        return analiseValidacaoRepository.save(a);
    }

    @Override
    @Transactional
    public void removerPorSubprocesso(Long subprocessoCodigo) {
        analiseValidacaoRepository.deleteBySubprocesso_Codigo(subprocessoCodigo);
    }
}