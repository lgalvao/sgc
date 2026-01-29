package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.repo.RepositorioComum;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.util.List;
import java.util.Optional;

/**
 * Serviço de infraestrutura para acesso a dados de Subprocesso.
 * Encapsula chamadas ao SubprocessoRepo e RepositorioComum.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubprocessoRepositoryService {

    private final SubprocessoRepo subprocessoRepo;
    private final RepositorioComum repo;

    public Subprocesso buscar(Long codigo) {
        return repo.buscar(Subprocesso.class, codigo);
    }

    public Optional<Subprocesso> findById(Long codigo) {
        return subprocessoRepo.findById(codigo);
    }

    public Subprocesso obterPorId(Long codigo) {
        return findById(codigo).orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado: " + codigo));
    }

    public Optional<Subprocesso> findByMapaCodigo(Long mapaCodigo) {
        return subprocessoRepo.findByMapaCodigo(mapaCodigo);
    }

    public Optional<Subprocesso> findByProcessoCodigoAndUnidadeCodigo(Long codProcesso, Long codUnidade) {
        return subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(codProcesso, codUnidade);
    }

    public List<Subprocesso> findByProcessoCodigoWithUnidade(Long codProcesso) {
        return subprocessoRepo.findByProcessoCodigoWithUnidade(codProcesso);
    }

    public List<Subprocesso> findByProcessoCodigoAndSituacaoWithUnidade(Long codProcesso, SituacaoSubprocesso situacao) {
        return subprocessoRepo.findByProcessoCodigoAndSituacaoWithUnidade(codProcesso, situacao);
    }

    public List<Subprocesso> findByProcessoCodigoAndUnidadeCodigoAndSituacaoInWithUnidade(Long codProcesso, Long codUnidade, List<SituacaoSubprocesso> situacoes) {
        return subprocessoRepo.findByProcessoCodigoAndUnidadeCodigoAndSituacaoInWithUnidade(codProcesso, codUnidade, situacoes);
    }

    public List<Subprocesso> findAllComFetch() {
        return subprocessoRepo.findAllComFetch();
    }

    public boolean existsByProcessoCodigoAndUnidadeCodigoIn(Long codProcesso, List<Long> unidadesCodigos) {
        return subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigoIn(codProcesso, unidadesCodigos);
    }

    public List<Subprocesso> findBySituacao(SituacaoSubprocesso situacao) {
        return subprocessoRepo.findBySituacao(situacao);
    }

    @Transactional
    public Subprocesso save(Subprocesso subprocesso) {
        return subprocessoRepo.save(subprocesso);
    }

    @Transactional
    public void deleteById(Long codigo) {
        subprocessoRepo.deleteById(codigo);
    }
}
