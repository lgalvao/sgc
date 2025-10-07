package sgc.competencia;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.mapa.Mapa;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-07T13:39:14-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25 (Amazon.com Inc.)"
)
@Component
public class CompetenciaMapperImpl implements CompetenciaMapper {

    @Override
    public CompetenciaDTO toDTO(Competencia competencia) {
        if ( competencia == null ) {
            return null;
        }

        CompetenciaDTO competenciaDTO = new CompetenciaDTO();

        competenciaDTO.setMapaCodigo( competenciaMapaCodigo( competencia ) );
        competenciaDTO.setCodigo( competencia.getCodigo() );
        competenciaDTO.setDescricao( competencia.getDescricao() );

        return competenciaDTO;
    }

    @Override
    public Competencia toEntity(CompetenciaDTO competenciaDTO) {
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
        if ( competencia == null ) {
            return null;
        }
        Mapa mapa = competencia.getMapa();
        if ( mapa == null ) {
            return null;
        }
        Long codigo = mapa.getCodigo();
        if ( codigo == null ) {
            return null;
        }
        return codigo;
    }
}
