package sgc.processo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Serviço exclusivo para consultas de verificação de Processos,
 * utilizado para evitar dependências circulares com outros módulos (ex: UnidadeService).
 */
@Service
@RequiredArgsConstructor
public class ProcessoConsultaService {

    private final ProcessoRepo processoRepo;

    @Transactional(readOnly = true)
    public Set<Long> buscarIdsUnidadesEmProcessosAtivos(Long codProcessoIgnorar) {
        return new HashSet<>(
                processoRepo.findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(
                        Arrays.asList(SituacaoProcesso.EM_ANDAMENTO, SituacaoProcesso.CRIADO),
                        codProcessoIgnorar));
    }
}
