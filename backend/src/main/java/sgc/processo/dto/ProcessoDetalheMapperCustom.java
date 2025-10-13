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

        // Mapeia as unidades de processo
        if (unidadesProcesso != null) {
            for (UnidadeProcesso up : unidadesProcesso) {
                ProcessoDetalheDto.UnidadeParticipanteDTO unit = processoDetalheMapperInterface.unidadeProcessoToUnidadeParticipanteDTO(up);
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
                    ProcessoDetalheDto.UnidadeParticipanteDTO existingUnit = unidadesBySigla.get(sigla);
                    ProcessoDetalheDto.UnidadeParticipanteDTO updatedUnit = ProcessoDetalheDto.UnidadeParticipanteDTO.builder()
                        .unidadeCodigo(existingUnit.getUnidadeCodigo())
                        .nome(existingUnit.getNome())
                        .sigla(existingUnit.getSigla())
                        .unidadeSuperiorCodigo(existingUnit.getUnidadeSuperiorCodigo())
                        .situacaoSubprocesso(sp.getSituacao()) // Novo valor
                        .dataLimite(sp.getDataLimiteEtapa1())   // Novo valor
                        .filhos(existingUnit.getFilhos())
                        .build();
                    unidadesBySigla.put(sigla, updatedUnit); // Atualizar no mapa
                } else {
                    // Cria uma nova unidade participante baseada no subprocesso
                    ProcessoDetalheDto.UnidadeParticipanteDTO unit = processoDetalheMapperInterface.subprocessoToUnidadeParticipanteDTO(sp);
                    if (unit.getSigla() != null) {
                        unidadesBySigla.put(unit.getSigla(), unit);
                    }
                }
            }
        }

        // Constroi a lista final de unidades a partir do mapa para garantir que as atualizações sejam refletidas
        List<ProcessoDetalheDto.UnidadeParticipanteDTO> unidades = new ArrayList<>(unidadesBySigla.values());

        // Mapeia os subprocessos para resumo
        List<ProcessoResumoDto> resumoSubprocessos = new ArrayList<>();
        if (subprocessos != null) {
            for (Subprocesso sp : subprocessos) {
                ProcessoResumoDto resumoDto = processoDetalheMapperInterface.subprocessoToProcessoResumoDto(sp);
                resumoSubprocessos.add(resumoDto);
            }
        }

        return ProcessoDetalheDto.builder()
            .codigo(dto.getCodigo())
            .descricao(dto.getDescricao())
            .tipo(dto.getTipo())
            .situacao(dto.getSituacao())
            .dataLimite(dto.getDataLimite())
            .dataCriacao(dto.getDataCriacao())
            .dataFinalizacao(dto.getDataFinalizacao())
            .unidades(unidades)
            .resumoSubprocessos(resumoSubprocessos)
            .build();
    }
}