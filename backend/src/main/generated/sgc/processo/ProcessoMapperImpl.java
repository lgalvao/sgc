package sgc.processo;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.processo.dto.ProcessoDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-07T13:39:14-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25 (Amazon.com Inc.)"
)
@Component
public class ProcessoMapperImpl implements ProcessoMapper {

    @Override
    public ProcessoDTO toDTO(Processo processo) {
        if ( processo == null ) {
            return null;
        }

        ProcessoDTO processoDTO = new ProcessoDTO();

        processoDTO.setCodigo( processo.getCodigo() );
        processoDTO.setDataCriacao( processo.getDataCriacao() );
        processoDTO.setDataFinalizacao( processo.getDataFinalizacao() );
        processoDTO.setDataLimite( processo.getDataLimite() );
        processoDTO.setDescricao( processo.getDescricao() );
        processoDTO.setSituacao( processo.getSituacao() );
        processoDTO.setTipo( processo.getTipo() );

        return processoDTO;
    }

    @Override
    public Processo toEntity(ProcessoDTO processoDTO) {
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
