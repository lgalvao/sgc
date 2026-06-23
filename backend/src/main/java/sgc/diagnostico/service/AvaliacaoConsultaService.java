package sgc.diagnostico.service;

import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.diagnostico.model.*;

import java.util.*;

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
