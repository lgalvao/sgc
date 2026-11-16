package sesel.sgc.processo;

import lombok.Data;
import sesel.sgc.mapa.MapaUnidadeSituacao;
import sesel.sgc.servidor.Servidor;
import sesel.sgc.unidade.Unidade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class ProcessoDto {
    private String id;
    private String nome;
    private LocalDate dataLimite;
    private ProcessoSituacao situacao;
    private long totalUnidades;
    private int percentualConclusao;
    private List<ProcessoUnidadeDto> processosUnidades;

    @Data
    public static class ProcessoUnidadeDto {
        private UUID id;
        private UUID unidadeId;
        private String unidadeNome;
        private String responsavelNome;
        private MapaUnidadeSituacao situacao;
        private LocalDateTime ultimaAtualizacao;

        public static ProcessoUnidadeDto fromEntity(sesel.sgc.processounidade.ProcessoUnidade entity) {
            if (entity == null)
                return null;
            ProcessoUnidadeDto dto = new ProcessoUnidadeDto();
            dto.setId(entity.getId());
            if (entity.getUnidade() != null) {
                dto.setUnidadeId(entity.getUnidade().getId());
                dto.setUnidadeNome(entity.getUnidade().getNome());
                dto.setResponsavelNome(
                        entity.getUnidade().getTitular() != null ? entity.getUnidade().getTitular().getNome() : "N/A");
            }
            dto.setSituacao(entity.getSituacao());
            dto.setUltimaAtualizacao(entity.getDataHoraInicioRegistro());
            return dto;
        }
    }

    public static ProcessoDto fromEntity(Processo processo, Long totalUnidades) {
        ProcessoDto dto = new ProcessoDto();
        dto.setId(String.valueOf(processo.getId()));
        dto.setNome(processo.getNome());
        dto.setDataLimite(processo.getDataLimite());
        dto.setSituacao(processo.getSituacao());
        dto.setTotalUnidades(totalUnidades);

        if (processo.getProcessosUnidades() != null) {
            dto.setProcessosUnidades(processo.getProcessosUnidades().stream().map(p -> {
                ProcessoUnidadeDto partDto = new ProcessoUnidadeDto();
                partDto.setId(p.getId());
                Unidade unidade = p.getUnidade();
                if (unidade != null) {
                    partDto.setUnidadeId(unidade.getId());
                    partDto.setUnidadeNome(unidade.getNome());
                    Servidor titular = unidade.getTitular();
                    partDto.setResponsavelNome(titular != null ? titular.getNome() : "N/A");
                } else {
                    partDto.setUnidadeId(new UUID(0, 0));
                    partDto.setUnidadeNome("N/A");
                    partDto.setResponsavelNome("N/A");
                }
                partDto.setSituacao(p.getSituacao());
                partDto.setUltimaAtualizacao(p.getDataHoraInicioRegistro());
                return partDto;
            }).collect(Collectors.toList()));
        }

        return dto;
    }
}
