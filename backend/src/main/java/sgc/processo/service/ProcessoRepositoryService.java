package sgc.processo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;

import java.util.List;
import java.util.Optional;

/**
 * Serviço de infraestrutura para acesso ao repositório de Processo.
 * Segue o padrão estabelecido no simplification-plan.md para remover acesso direto
 * de Facades aos Repositories.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessoRepositoryService {

    private final ProcessoRepo processoRepo;
    private static final String ENTIDADE_PROCESSO = "Processo";

    @Transactional(readOnly = true)
    public Optional<Processo> findById(Long id) {
        return processoRepo.findById(id);
    }

    @Transactional(readOnly = true)
    public Processo buscarPorId(Long id) {
        return processoRepo.findById(id)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(ENTIDADE_PROCESSO, id));
    }

    @Transactional
    public Processo salvar(Processo processo) {
        return processoRepo.save(processo);
    }

    @Transactional
    public Processo salvarEFlush(Processo processo) {
        return processoRepo.saveAndFlush(processo);
    }

    @Transactional
    public void excluir(Long id) {
        processoRepo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Processo> findBySituacao(SituacaoProcesso situacao) {
        return processoRepo.findBySituacao(situacao);
    }

    @Transactional(readOnly = true)
    public List<Processo> findBySituacaoOrderByDataFinalizacaoDesc(SituacaoProcesso situacao) {
        return processoRepo.findBySituacaoOrderByDataFinalizacaoDesc(situacao);
    }

    @Transactional(readOnly = true)
    public Page<Processo> findAll(Pageable pageable) {
        return processoRepo.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Processo> listarPorParticipantesIgnorandoSituacao(
            List<Long> codigos, SituacaoProcesso situacao, Pageable pageable) {
        return processoRepo.findDistinctByParticipantes_CodigoInAndSituacaoNot(codigos, situacao, pageable);
    }
}
