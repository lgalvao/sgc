package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;

import java.util.List;

/**
 * Serviço de acesso a dados para Movimentações de Subprocesso.
 */
@Service
@RequiredArgsConstructor
public class MovimentacaoRepositoryService {
    private final MovimentacaoRepo movimentacaoRepo;

    @Transactional
    public Movimentacao salvar(Movimentacao movimentacao) {
        return movimentacaoRepo.save(movimentacao);
    }

    @Transactional(readOnly = true)
    public List<Movimentacao> buscarPorSubprocesso(Long subprocessoCodigo) {
        return movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocessoCodigo);
    }
}
