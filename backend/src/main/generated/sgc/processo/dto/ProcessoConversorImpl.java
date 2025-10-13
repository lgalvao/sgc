package sgc.processo.dto;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.TipoProcesso;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-13T10:53:07-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.8 (Amazon.com Inc.)"
)
@Component
public class ProcessoConversorImpl implements ProcessoConversor {

    @Override
    public ProcessoDto toDTO(Processo processo) {
        if ( processo == null ) {
            return null;
        }

        ProcessoDto.ProcessoDtoBuilder processoDto = ProcessoDto.builder();

        processoDto.codigo( processo.getCodigo() );
        processoDto.dataCriacao( processo.getDataCriacao() );
        processoDto.dataFinalizacao( processo.getDataFinalizacao() );
        processoDto.dataLimite( processo.getDataLimite() );
        processoDto.descricao( processo.getDescricao() );
        processoDto.situacao( processo.getSituacao() );
        if ( processo.getTipo() != null ) {
            processoDto.tipo( processo.getTipo().name() );
        }

        return processoDto.build();
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
        if ( processoDTO.getTipo() != null ) {
            processo.setTipo( Enum.valueOf( TipoProcesso.class, processoDTO.getTipo() ) );
        }

        return processo;
    }
}
