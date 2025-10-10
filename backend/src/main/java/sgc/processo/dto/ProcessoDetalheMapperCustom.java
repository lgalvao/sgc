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
                if (unit.sigla() != null) {
                    unidadesBySigla.put(unit.sigla(), unit);
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
                    unit = new ProcessoDetalheDto.UnidadeParticipanteDTO(
                        unit.unidadeCodigo(),
                        unit.nome(),
                        unit.sigla(),
                        unit.unidadeSuperiorCodigo(),
                        sp.getSituacao(), // Novo valor para situacaoSubprocesso
                        sp.getDataLimiteEtapa1(), // Novo valor para dataLimite
                        unit.filhos()
                    );
                    unidadesBySigla.put(unit.sigla(), unit); // Atualizar no mapa
                } else {
                    // Cria uma nova unidade participante baseada no subprocesso
                    ProcessoDetalheDto.UnidadeParticipanteDTO unit = processoDetalheMapperInterface.subprocessoToUnidadeParticipanteDTO(sp);
                    unidades.add(unit);
                    if (unit.sigla() != null) {
                        unidadesBySigla.put(unit.sigla(), unit);
                    }
                }
            }
        }

        // Reconstroi a lista de unidades a partir do mapa para garantir que as atualizações sejam refletidas
        unidades = new ArrayList<>(unidadesBySigla.values());

        // Mapeia os subprocessos para resumo
        List<ProcessoResumoDto> resumoSubprocessos = new ArrayList<>();
        if (subprocessos != null) {
            for (Subprocesso sp : subprocessos) {
                ProcessoResumoDto resumoDto = processoDetalheMapperInterface.subprocessoToProcessoResumoDto(sp);
                resumoSubprocessos.add(resumoDto);
            }
        }

        return new ProcessoDetalheDto(
            dto.codigo(),
            dto.descricao(),
            dto.tipo(),
            dto.situacao(),
            dto.dataLimite(),
            dto.dataCriacao(),
            dto.dataFinalizacao(),
            unidades, // Lista de unidades atualizada
            resumoSubprocessos // Lista de subprocessos atualizada
        );
    }
}