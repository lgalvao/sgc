package sgc.processo.dto;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.processo.modelo.Processo;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-08T14:30:14-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25 (Amazon.com Inc.)"
)
@Component
public class ProcessoMapperImpl implements ProcessoMapper {

    @Override
    public ProcessoDto toDTO(Processo processo) {
        if ( processo == null ) {
            return null;
        }

        ProcessoDto processoDto = new ProcessoDto();

        processoDto.setCodigo( processo.getCodigo() );
        processoDto.setDataCriacao( processo.getDataCriacao() );
        processoDto.setDataFinalizacao( processo.getDataFinalizacao() );
        processoDto.setDataLimite( processo.getDataLimite() );
        processoDto.setDescricao( processo.getDescricao() );
        processoDto.setSituacao( processo.getSituacao() );
        processoDto.setTipo( processo.getTipo() );

        return processoDto;
    }

    @Override
    public Processo toEntity(ProcessoDto processoDTO) {
        if ( processoDTO == null ) {
            return null;
        }

        Processo processo = new Processo();

        processo.setCodigo( processoDTO.getCodigo() );
        processo.setDataCriacao( processoDTO.getDataCriacao() );
        processo.setDataFinalizacao( processoDTO.getDataFinalizacao() );
        processo.setDataLimite( processoDTO.getDataLimite() );
        processo.setDescricao( processoDTO.getDescricao() );
        processo.setSituacao( processoDTO.getSituacao() );
        processo.setTipo( processoDTO.getTipo() );

        return processo;
    }
}
