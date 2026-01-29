package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;

import java.util.List;

/**
 * Serviço de infraestrutura para acesso a dados de Movimentacao.
 * Encapsula chamadas ao MovimentacaoRepo.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovimentacaoRepositoryService {

    private final MovimentacaoRepo movimentacaoRepo;

    public List<Movimentacao> findBySubprocessoCodigoOrderByDataHoraDesc(Long subprocessoCodigo) {
        return movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocessoCodigo);
    }

    @Transactional
    public Movimentacao save(Movimentacao movimentacao) {
        return movimentacaoRepo.save(movimentacao);
    }
}
