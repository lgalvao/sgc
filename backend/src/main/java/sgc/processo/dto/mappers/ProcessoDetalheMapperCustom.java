package sgc.processo.dto.mappers;

import org.springframework.beans.factory.annotation.Autowired;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.model.Processo;
import sgc.subprocesso.model.Subprocesso;
import sgc.unidade.model.Unidade;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ProcessoDetalheMapperCustom implements ProcessoDetalheMapper {

    @Autowired
    private ProcessoDetalheMapper delegate;

    @Override
    public ProcessoDetalheDto toDetailDTO(Processo processo) {
        ProcessoDetalheDto dto = ProcessoDetalheDto.builder()
                .codigo(processo.getCodigo())
                .descricao(processo.getDescricao())
                .situacao(processo.getSituacao())
                .tipo(processo.getTipo().name())
                .dataCriacao(processo.getDataCriacao())
                .dataFinalizacao(processo.getDataFinalizacao())
                .dataLimite(processo.getDataLimite())
                .build();

        return dto;
    }

    protected void montarHierarquiaUnidades(ProcessoDetalheDto dto,
                                          Processo processo,
                                          List<Subprocesso> subprocessos) {
        Map<Long, ProcessoDetalheDto.UnidadeParticipanteDto> mapaUnidades = new HashMap<>();
        for (Unidade participante : processo.getParticipantes()) {
            mapaUnidades.put(participante.getCodigo(), delegate.unidadeToUnidadeParticipanteDTO(participante));
        }

        for (Subprocesso sp : subprocessos) {
            ProcessoDetalheDto.UnidadeParticipanteDto unidadeDto = mapaUnidades.get(sp.getUnidade().getCodigo());
        }

        // Monta a hierarquia
        for (ProcessoDetalheDto.UnidadeParticipanteDto unidadeDto : mapaUnidades.values()) {
            if (unidadeDto.getCodUnidadeSuperior() != null) {
                ProcessoDetalheDto.UnidadeParticipanteDto pai = mapaUnidades.get(unidadeDto.getCodUnidadeSuperior());
                if (pai != null) {
                    pai.getFilhos().add(unidadeDto);
                }
            }
        }

        // Adiciona apenas as unidades raiz ao DTO final
        for (ProcessoDetalheDto.UnidadeParticipanteDto unidadeDto : mapaUnidades.values()) {
            if (unidadeDto.getCodUnidadeSuperior() == null) {
                dto.getUnidades().add(unidadeDto);
            }
        }

        // Ordena as unidades e seus filhos
        Comparator<ProcessoDetalheDto.UnidadeParticipanteDto> comparator = Comparator.comparing(ProcessoDetalheDto.UnidadeParticipanteDto::getSigla);
        dto.getUnidades().sort(comparator);
        for (ProcessoDetalheDto.UnidadeParticipanteDto unidadeDto : mapaUnidades.values()) {
            unidadeDto.getFilhos().sort(comparator);
        }
    }
}
