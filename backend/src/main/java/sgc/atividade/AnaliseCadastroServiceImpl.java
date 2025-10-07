package sgc.atividade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.subprocesso.AnaliseCadastroRepository;
import sgc.subprocesso.Subprocesso;
import sgc.subprocesso.SubprocessoRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementação do serviço de análises de cadastro.
 *
 */
@Service
@RequiredArgsConstructor
public class AnaliseCadastroServiceImpl implements AnaliseCadastroService {
    private final AnaliseCadastroRepository analiseCadastroRepository;
    private final SubprocessoRepository subprocessoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AnaliseCadastro> listarPorSubprocesso(Long subprocessoCodigo) {
        // valida existência do subprocesso antes de consultar
        if (subprocessoRepository.findById(subprocessoCodigo).isEmpty()) {
            throw new ErroEntidadeNaoEncontrada("Subprocesso não encontrado: %d".formatted(subprocessoCodigo));
        }
        return analiseCadastroRepository.findBySubprocessoCodigo(subprocessoCodigo);
    }

    @Override
    @Transactional
    public AnaliseCadastro criarAnalise(Long subprocessoCodigo, String observacoes) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoCodigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado: %d".formatted(subprocessoCodigo)));

        AnaliseCadastro a = new AnaliseCadastro();
        a.setSubprocesso(sp);
        a.setDataHora(LocalDateTime.now());
        a.setObservacoes(observacoes);

        return analiseCadastroRepository.save(a);
    }

    @Override
    @Transactional
    public void removerPorSubprocesso(Long subprocessoCodigo) {
        analiseCadastroRepository.deleteBySubprocessoCodigo(subprocessoCodigo);
    }
}