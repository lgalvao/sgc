package sgc.processo.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.organizacao.model.Unidade;
import sgc.processo.dto.ProcessoDetalheDto;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-04T16:19:25-0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.3.0.jar, environment: Java 25.0.1 (Amazon.com Inc.)"
)
@Component
public class ProcessoDetalheMapperImpl implements ProcessoDetalheMapper {

    @Override
    public ProcessoDetalheDto.UnidadeParticipanteDto toUnidadeParticipanteDto(Unidade unidade) {
        if ( unidade == null ) {
            return null;
        }

        ProcessoDetalheDto.UnidadeParticipanteDto.UnidadeParticipanteDtoBuilder unidadeParticipanteDto = ProcessoDetalheDto.UnidadeParticipanteDto.builder();

        unidadeParticipanteDto.codUnidade( unidade.getCodigo() );
        unidadeParticipanteDto.codUnidadeSuperior( unidadeUnidadeSuperiorCodigo( unidade ) );
        unidadeParticipanteDto.nome( unidade.getNome() );
        unidadeParticipanteDto.sigla( unidade.getSigla() );

        unidadeParticipanteDto.filhos( new java.util.ArrayList<>() );

        return unidadeParticipanteDto.build();
    }

    private Long unidadeUnidadeSuperiorCodigo(Unidade unidade) {
        Unidade unidadeSuperior = unidade.getUnidadeSuperior();
        if ( unidadeSuperior == null ) {
            return null;
        }
        return unidadeSuperior.getCodigo();
    }
}
