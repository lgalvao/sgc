package sgc.analise;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.modelo.AnaliseCadastro;
import sgc.analise.modelo.AnaliseCadastroRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementação do serviço de análises de cadastro.
 *
 */
@Service
@RequiredArgsConstructor
public class AnaliseCadastroServiceImpl implements AnaliseCadastroService {
    private final AnaliseCadastroRepo analiseCadastroRepo;
    private final SubprocessoRepo subprocessoRepo;

    @Override
    @Transactional(readOnly = true)
    public List<AnaliseCadastro> listarPorSubprocesso(Long subprocessoCodigo) {
        // valida existência do subprocesso antes de consultar
        if (subprocessoRepo.findById(subprocessoCodigo).isEmpty()) {
            throw new ErroEntidadeNaoEncontrada("Subprocesso não encontrado: %d".formatted(subprocessoCodigo));
        }
        return analiseCadastroRepo.findBySubprocessoCodigo(subprocessoCodigo);
    }

    @Override
    @Transactional
    public AnaliseCadastro criarAnalise(Long subprocessoCodigo, String observacoes) {
        Subprocesso sp = subprocessoRepo.findById(subprocessoCodigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado: %d".formatted(subprocessoCodigo)));

        AnaliseCadastro a = new AnaliseCadastro();
        a.setSubprocesso(sp);
        a.setDataHora(LocalDateTime.now());
        a.setObservacoes(observacoes);

        return analiseCadastroRepo.save(a);
    }

    @Override
    @Transactional
    public void removerPorSubprocesso(Long subprocessoCodigo) {
        analiseCadastroRepo.deleteBySubprocessoCodigo(subprocessoCodigo);
    }
}