package sgc.processo;

import org.springframework.stereotype.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;

@Component
public class ProcessoDtoMapper {

    public ProcessoResumoDto paraResumo(Processo processo) {
        return ProcessoResumoDto.builder()
                .codigo(processo.getCodigo())
                .descricao(processo.getDescricao())
                .situacao(processo.getSituacao())
                .tipo(processo.getTipo().name())
                .dataLimite(processo.getDataLimite())
                .dataCriacao(processo.getDataCriacao())
                .dataFinalizacao(processo.getDataFinalizacao())
                .unidadesParticipantes(processo.getSiglasParticipantes())
                .build();
    }
}
