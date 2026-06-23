package sgc.diagnostico.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.diagnostico.model.AvaliacaoServidorRepo;
import sgc.diagnostico.model.SituacaoAvaliacaoServidor;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvaliacaoConsultaService {
    private final AvaliacaoServidorRepo avaliacaoRepo;

    public boolean existeAvaliacaoPorSubprocessoEServidorESituacoes(
            Long subprocessoCodigo, String servidorTitulo, Set<SituacaoAvaliacaoServidor> situacoes) {
        return avaliacaoRepo.existePorSubprocessoEServidorESituacoes(subprocessoCodigo, servidorTitulo, situacoes);
    }

    public boolean existeAvaliacaoPorSubprocessoESituacao(
            Long subprocessoCodigo, SituacaoAvaliacaoServidor situacao) {
        return avaliacaoRepo.existePorSubprocessoESituacao(subprocessoCodigo, situacao);
    }
}
