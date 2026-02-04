package sgc.processo.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-04T16:19:25-0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.3.0.jar, environment: Java 25.0.1 (Amazon.com Inc.)"
)
@Component
public class ProcessoMapperImpl implements ProcessoMapper {

    @Override
    public ProcessoDto toDto(Processo processo) {
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

        ProcessoDto processoDtoResult = processoDto.build();

        mapAfterMapping( processo, processoDtoResult );

        return processoDtoResult;
    }

    @Override
    public Processo toEntity(ProcessoDto processoDTO) {
        if ( processoDTO == null ) {
            return null;
        }

        Processo.ProcessoBuilder<?, ?> processo = Processo.builder();

        processo.codigo( processoDTO.getCodigo() );
        processo.dataCriacao( processoDTO.getDataCriacao() );
        processo.dataFinalizacao( processoDTO.getDataFinalizacao() );
        processo.dataLimite( processoDTO.getDataLimite() );
        processo.descricao( processoDTO.getDescricao() );
        processo.situacao( processoDTO.getSituacao() );
        if ( processoDTO.getTipo() != null ) {
            processo.tipo( Enum.valueOf( TipoProcesso.class, processoDTO.getTipo() ) );
        }

        return processo.build();
    }
}
