package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.repo.ComumRepo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoStatusService {

    private final SubprocessoRepo subprocessoRepo;
    private final ComumRepo repo;

    @Transactional
    public void atualizarParaEmAndamento(Long mapaCodigo) {
        var subprocesso = repo.buscar(Subprocesso.class, "mapa.codigo", mapaCodigo);
        if (subprocesso.getSituacao() == NAO_INICIADO) {
            var tipoProcesso = subprocesso.getProcesso().getTipo();
            if (tipoProcesso == TipoProcesso.MAPEAMENTO) {
                log.info("Atualizando sit. SP{} para MAPEAMENTO_CADASTRO_EM_ANDAMENTO", subprocesso.getCodigo());
                subprocesso.setSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
                subprocessoRepo.save(subprocesso);
            } else if (tipoProcesso == TipoProcesso.REVISAO) {
                log.info("Atualizando sit. SP{} para REVISAO_CADASTRO_EM_ANDAMENTO", subprocesso.getCodigo());
                subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
                subprocessoRepo.save(subprocesso);
            }
        }
    }
}
