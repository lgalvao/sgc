package sgc.competencia.dto;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.competencia.modelo.Competencia;
import sgc.mapa.modelo.Mapa;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-08T14:30:14-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25 (Amazon.com Inc.)"
)
@Component
public class CompetenciaMapperImpl implements CompetenciaMapper {

    @Override
    public CompetenciaDto toDTO(Competencia competencia) {
        if ( competencia == null ) {
            return null;
        }

        CompetenciaDto competenciaDto = new CompetenciaDto();

        competenciaDto.setMapaCodigo( competenciaMapaCodigo( competencia ) );
        competenciaDto.setCodigo( competencia.getCodigo() );
        competenciaDto.setDescricao( competencia.getDescricao() );

        return competenciaDto;
    }

    @Override
    public Competencia toEntity(CompetenciaDto competenciaDTO) {
        if ( competenciaDTO == null ) {
            return null;
        }

        Competencia competencia = new Competencia();

        competencia.setMapa( map( competenciaDTO.getMapaCodigo() ) );
        competencia.setCodigo( competenciaDTO.getCodigo() );
        competencia.setDescricao( competenciaDTO.getDescricao() );

        return competencia;
    }

    private Long competenciaMapaCodigo(Competencia competencia) {
        Mapa mapa = competencia.getMapa();
        if ( mapa == null ) {
            return null;
        }
        return mapa.getCodigo();
    }
}
