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
 * Serviço para gerenciar análises de cadastro (ANALISE_CADASTRO). *
 */
@Service
@RequiredArgsConstructor
public class AnaliseCadastroService {
    private final AnaliseCadastroRepo analiseCadastroRepo;
    private final SubprocessoRepo subprocessoRepo;

    /**
     * Recupera as análises vinculadas a um subprocesso.
     *
     * @param subprocessoCodigo id do subprocesso
     * @return lista de AnaliseCadastro
     */
    @Transactional(readOnly = true)
    public List<AnaliseCadastro> listarPorSubprocesso(Long subprocessoCodigo) {
        // valida existência do subprocesso antes de consultar
        if (subprocessoRepo.findById(subprocessoCodigo).isEmpty()) {
            throw new ErroEntidadeNaoEncontrada("Subprocesso não encontrado: %d".formatted(subprocessoCodigo));
        }
        return analiseCadastroRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocessoCodigo);
    }

    /**
     * Cria e persiste uma nova análise de cadastro para o subprocesso informado.
     *
     * @param subprocessoCodigo id do subprocesso
     * @param observacoes       texto com observações da análise
     * @return entidade AnaliseCadastro persistida
     */
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

    /**
     * Remove todas as análises de cadastro vinculadas ao subprocesso.
     *
     * @param subprocessoCodigo id do subprocesso
     */
    @Transactional
    public void removerPorSubprocesso(Long subprocessoCodigo) {
        analiseCadastroRepo.deleteBySubprocessoCodigo(subprocessoCodigo);
    }
}