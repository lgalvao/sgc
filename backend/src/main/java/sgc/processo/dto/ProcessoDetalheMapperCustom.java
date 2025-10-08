package sgc.processo.dto;

import org.springframework.stereotype.Component;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.UnidadeProcesso;
import sgc.subprocesso.modelo.Subprocesso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementação customizada para mapeamento complexo de Processo para ProcessoDetalheDto.
 * Esta classe implementa a lógica de associação entre unidades de processo e subprocessos,
 * que não pode ser feita automaticamente pelo MapStruct.
 */
@Component
public class ProcessoDetalheMapperCustom {

    private final ProcessoDetalheMapperInterface processoDetalheMapperInterface;

    public ProcessoDetalheMapperCustom(ProcessoDetalheMapperInterface processoDetalheMapperInterface) {
        this.processoDetalheMapperInterface = processoDetalheMapperInterface;
    }

    /**
     * Converte um Processo com suas associações para ProcessoDetalheDto,
     * mapeando corretamente as unidades de processo e subprocessos.
     */
    public ProcessoDetalheDto toDetailDTO(Processo p,
                                         List<UnidadeProcesso> unidadesProcesso,
                                         List<Subprocesso> subprocessos) {
        if (p == null) return null;
        
        // Mapeia os dados básicos do processo usando MapStruct
        ProcessoDetalheDto dto = processoDetalheMapperInterface.toDetailDTO(p);

        Map<String, ProcessoDetalheDto.UnidadeParticipanteDTO> unidadesBySigla = new HashMap<>();
        List<ProcessoDetalheDto.UnidadeParticipanteDTO> unidades = new ArrayList<>();

        // Mapeia as unidades de processo
        if (unidadesProcesso != null) {
            for (UnidadeProcesso up : unidadesProcesso) {
                ProcessoDetalheDto.UnidadeParticipanteDTO unit = processoDetalheMapperInterface.unidadeProcessoToUnidadeParticipanteDTO(up);
                unidades.add(unit);
                if (unit.getSigla() != null) {
                    unidadesBySigla.put(unit.getSigla(), unit);
                }
            }
        }

        // Associa as informações dos subprocessos às unidades
        if (subprocessos != null) {
            for (Subprocesso sp : subprocessos) {
                String sigla = (sp.getUnidade() != null) ? sp.getUnidade().getSigla() : null;

                if (sigla != null && unidadesBySigla.containsKey(sigla)) {
                    // Atualiza a unidade existente com informações do subprocesso
                    ProcessoDetalheDto.UnidadeParticipanteDTO unit = unidadesBySigla.get(sigla);
                    unit.setSituacaoSubprocesso(sp.getSituacaoId());
                    unit.setDataLimite(sp.getDataLimiteEtapa1());
                } else {
                    // Cria uma nova unidade participante baseada no subprocesso
                    ProcessoDetalheDto.UnidadeParticipanteDTO unit = processoDetalheMapperInterface.subprocessoToUnidadeParticipanteDTO(sp);
                    unidades.add(unit);
                    if (unit.getSigla() != null) {
                        unidadesBySigla.put(unit.getSigla(), unit);
                    }
                }
            }
        }

        dto.setUnidades(unidades);

        // Mapeia os subprocessos para resumo
        List<ProcessoResumoDto> resumoSubprocessos = new ArrayList<>();
        if (subprocessos != null) {
            for (Subprocesso sp : subprocessos) {
                ProcessoResumoDto resumoDto = processoDetalheMapperInterface.subprocessoToProcessoResumoDto(sp);
                resumoSubprocessos.add(resumoDto);
            }
        }
        dto.setResumoSubprocessos(resumoSubprocessos);

        return dto;
    }
}