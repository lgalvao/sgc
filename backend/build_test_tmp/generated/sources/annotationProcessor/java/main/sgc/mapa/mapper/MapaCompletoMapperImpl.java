package sgc.mapa.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.model.Competencia;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-04T16:19:37-0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.3.0.jar, environment: Java 25.0.1 (Amazon.com Inc.)"
)
@Component
public class MapaCompletoMapperImpl implements MapaCompletoMapper {

    @Override
    public CompetenciaMapaDto toDto(Competencia competencia) {
        if ( competencia == null ) {
            return null;
        }

        CompetenciaMapaDto.CompetenciaMapaDtoBuilder competenciaMapaDto = CompetenciaMapaDto.builder();

        competenciaMapaDto.codigo( competencia.getCodigo() );
        competenciaMapaDto.descricao( competencia.getDescricao() );
        competenciaMapaDto.atividadesCodigos( mapAtividadesCodigos( competencia.getAtividades() ) );

        return competenciaMapaDto.build();
    }
}
